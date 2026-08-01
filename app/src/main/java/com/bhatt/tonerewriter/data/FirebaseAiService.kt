package com.bhatt.tonerewriter.data

import com.bhatt.tonerewriter.domain.PromptFactory
import com.bhatt.tonerewriter.domain.RewriteRequest
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.FinishReason
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.InvalidAPIKeyException
import com.google.firebase.ai.type.PromptBlockedException
import com.google.firebase.ai.type.QuotaExceededException
import com.google.firebase.ai.type.RequestTimeoutException
import com.google.firebase.ai.type.ResponseStoppedException
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.ServerException
import com.google.firebase.ai.type.HarmBlockThreshold
import com.google.firebase.ai.type.HarmCategory
import com.google.firebase.ai.type.SafetySetting
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import org.json.JSONObject
import java.io.IOException

/**
 * Talks to Gemini through Firebase AI Logic.
 *
 * No API key is compiled into the app: the SDK authenticates through the Firebase project
 * declared by google-services.json, and App Check attests that the caller is really this app.
 * [GenerativeBackend.googleAI] selects the Gemini Developer API, which has the free tier;
 * swapping it for [GenerativeBackend.vertexAI] is the only change needed to move to Vertex AI.
 */
class FirebaseAiService(
    /** A provider, not a value, so a Remote Config activate is picked up without a restart. */
    private val modelName: () -> String = ModelConfig::modelName
) : ToneRewriteService {

    override suspend fun rewrite(request: RewriteRequest): Result<String> = try {
        val response = modelFor(request).generateContent(PromptFactory.userContent(request))
        val raw = response.text?.trim()

        if (raw.isNullOrEmpty()) {
            Result.failure(RewriteError.EmptyResponse)
        } else {
            val rewrite = extractRewrite(raw)
            if (rewrite.isEmpty()) Result.failure(RewriteError.EmptyResponse)
            else Result.success(rewrite)
        }
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (error: Throwable) {
        android.util.Log.e("FirebaseAiService", "rewrite failed", error)
        Result.failure(error.toRewriteError())
    }

    /**
     * A [GenerativeModel] is a cheap config holder, so a fresh one per request is fine and
     * keeps the tone-specific system instruction next to the call that uses it.
     */
    private fun modelFor(request: RewriteRequest): GenerativeModel =
        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = modelName(),
            generationConfig = generationConfig {
                temperature = 0.7f
                // Gemini 3.x always thinks, and thinking tokens count against this ceiling, so
                // it has to cover the reasoning as well as the sentence we actually want back.
                // Too low and generation stops on MAX_TOKENS having emitted no text at all.
                //
                // Thinking is deliberately not configured: the 3.x series controls it with
                // thinking_level, and ThinkingConfig in firebase-ai 17.1.0 only exposes the 2.5
                // series' thinking_budget. Sending a budget here is the wrong parameter family.
                maxOutputTokens = 2048
                // Constrained decoding: the model must answer {"rewrite": "..."} and cannot
                // prepend "Sure, here's your rewrite:".
                responseMimeType = "application/json"
                responseSchema = REWRITE_SCHEMA
            },
            // Already as permissive as the API allows. Anything still blocked is caught by
            // Gemini's non-configurable core filters, which no client setting can turn off.
            safetySettings = listOf(
                SafetySetting(HarmCategory.HARASSMENT, HarmBlockThreshold.NONE),
                SafetySetting(HarmCategory.HATE_SPEECH, HarmBlockThreshold.NONE),
                SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, HarmBlockThreshold.NONE),
                SafetySetting(HarmCategory.DANGEROUS_CONTENT, HarmBlockThreshold.NONE),
                SafetySetting(HarmCategory.CIVIC_INTEGRITY, HarmBlockThreshold.NONE)
            ),
            systemInstruction = content { text(PromptFactory.systemInstruction(request)) }
        )

    /** Schema guarantees the shape; the fallback covers an SDK that hands back plain text. */
    private fun extractRewrite(raw: String): String = runCatching {
        JSONObject(raw).optString("rewrite").trim()
    }.getOrDefault(raw).ifEmpty { raw }.trim()

    /**
     * The SDK raises [ResponseStoppedException] for *every* finish reason that isn't STOP, so a
     * truncated response and a genuine safety block arrive as the same exception type. Split them
     * on the reason itself — reporting a MAX_TOKENS truncation as "blocked by the safety filter"
     * sends the user off rephrasing a message that was never the problem.
     */
    private fun ResponseStoppedException.stopReasonError(): RewriteError =
        when (val reason = response.candidates.firstOrNull()?.finishReason) {
            FinishReason.MAX_TOKENS -> RewriteError.TooLong
            FinishReason.SAFETY,
            FinishReason.PROHIBITED_CONTENT,
            FinishReason.BLOCKLIST,
            FinishReason.SPII -> RewriteError.Blocked
            else -> RewriteError.Service(reason?.name ?: "unknown stop reason")
        }

    private fun Throwable.toRewriteError(): RewriteError = when (this) {
        is RewriteError -> this
        is PromptBlockedException -> RewriteError.Blocked
        is ResponseStoppedException -> stopReasonError()
        is QuotaExceededException -> RewriteError.RateLimited
        is InvalidAPIKeyException -> RewriteError.NotConfigured
        is RequestTimeoutException, is IOException -> RewriteError.Network
        is ServerException -> RewriteError.Service(message.orEmpty())
        is IllegalStateException -> RewriteError.NotConfigured
        else -> RewriteError.Service(message ?: this::class.java.simpleName)
    }

    companion object {
        private val REWRITE_SCHEMA = Schema.obj(
            mapOf("rewrite" to Schema.string("The rewritten message body, nothing else."))
        )
    }
}
