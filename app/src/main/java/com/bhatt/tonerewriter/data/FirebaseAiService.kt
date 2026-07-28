package com.bhatt.tonerewriter.data

import com.bhatt.tonerewriter.domain.PromptFactory
import com.bhatt.tonerewriter.domain.RewriteRequest
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.InvalidAPIKeyException
import com.google.firebase.ai.type.PromptBlockedException
import com.google.firebase.ai.type.QuotaExceededException
import com.google.firebase.ai.type.RequestTimeoutException
import com.google.firebase.ai.type.ResponseStoppedException
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.ServerException
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
    private val modelName: String = DEFAULT_MODEL
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
            modelName = modelName,
            generationConfig = generationConfig {
                temperature = 0.7f
                maxOutputTokens = 600
                // Constrained decoding: the model must answer {"rewrite": "..."} and cannot
                // prepend "Sure, here's your rewrite:".
                responseMimeType = "application/json"
                responseSchema = REWRITE_SCHEMA
            },
            systemInstruction = content { text(PromptFactory.systemInstruction(request)) }
        )

    /** Schema guarantees the shape; the fallback covers an SDK that hands back plain text. */
    private fun extractRewrite(raw: String): String = runCatching {
        JSONObject(raw).optString("rewrite").trim()
    }.getOrDefault(raw).ifEmpty { raw }.trim()

    private fun Throwable.toRewriteError(): RewriteError = when (this) {
        is RewriteError -> this
        is PromptBlockedException, is ResponseStoppedException -> RewriteError.Blocked
        is QuotaExceededException -> RewriteError.RateLimited
        is InvalidAPIKeyException -> RewriteError.NotConfigured
        is RequestTimeoutException, is IOException -> RewriteError.Network
        is ServerException -> RewriteError.Service(message.orEmpty())
        is IllegalStateException -> RewriteError.NotConfigured
        else -> RewriteError.Service(message ?: this::class.java.simpleName)
    }

    companion object {
        // Rolling alias — always resolves to the current Flash model, so it won't age out
        // the way a pinned "gemini-2.5-flash" did. Pin a dated id only if you need stability.
        const val DEFAULT_MODEL = "gemini-flash-latest"

        private val REWRITE_SCHEMA = Schema.obj(
            mapOf("rewrite" to Schema.string("The rewritten message body, nothing else."))
        )
    }
}
