package com.bhatt.tonerewriter.data

import com.bhatt.tonerewriter.domain.RewriteRequest

/**
 * The one seam between the app and any AI provider.
 *
 * The ViewModel depends on this interface only, so swapping Firebase AI Logic for a backend
 * proxy — or for a fake in tests — is a constructor-argument change and nothing else.
 */
interface ToneRewriteService {
    /** Never throws for expected failures — errors arrive as [RewriteError] inside [Result]. */
    suspend fun rewrite(request: RewriteRequest): Result<String>
}

/** Failures the UI can phrase for a human, instead of leaking stack traces. */
sealed class RewriteError(message: String, val retryable: Boolean) : Exception(message) {

    /** Firebase project missing, google-services.json absent, or the AI Logic API not enabled. */
    data object NotConfigured : RewriteError(
        "AI isn't configured for this build. Check the Firebase setup.",
        retryable = false
    ) {
        private fun readResolve(): Any = NotConfigured
    }

    data object Network : RewriteError(
        "Couldn't reach the network. Check your connection and try again.",
        retryable = true
    ) {
        private fun readResolve(): Any = Network
    }

    data object RateLimited : RewriteError(
        "Too many rewrites for now. Wait a moment and try again.",
        retryable = true
    ) {
        private fun readResolve(): Any = RateLimited
    }

    data object Blocked : RewriteError(
        "That message was blocked by the safety filter. Try rephrasing it.",
        retryable = false
    ) {
        private fun readResolve(): Any = Blocked
    }

    data object EmptyResponse : RewriteError(
        "The model returned nothing. Try again.",
        retryable = true
    ) {
        private fun readResolve(): Any = EmptyResponse
    }

    data class Service(val detail: String) : RewriteError(
        "The rewrite service failed. Try again.",
        retryable = true
    )
}
