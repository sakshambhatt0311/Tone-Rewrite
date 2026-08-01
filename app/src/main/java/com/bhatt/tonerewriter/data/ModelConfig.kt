package com.bhatt.tonerewriter.data

import android.util.Log
import com.bhatt.tonerewriter.BuildConfig
import com.bhatt.tonerewriter.R
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings

/**
 * Owns the Gemini model id, so retiring a model is a Firebase console edit rather than an app
 * release.
 *
 * Gemini 2.5 and later dropped the auto-updating `-latest` aliases, which means every shipped
 * build carries a hardcoded model id that will eventually 404 — and when it does, already
 * installed copies break all at once with no way to reach users who never update. Remote Config
 * is the escape hatch: change `model_name` in the console and installed apps pick it up on their
 * next fetch.
 *
 * [FALLBACK_MODEL] still ships inside the APK. Remote Config removes the need for an app release,
 * not the need for a sane starting value.
 */
object ModelConfig {

    /** The console parameter to edit when a model is retired. */
    private const val KEY_MODEL_NAME = "model_name"

    /** Used until the first fetch lands, and any time the console value is missing or blank. */
    const val FALLBACK_MODEL = "gemini-3.6-flash"

    private const val TAG = "ModelConfig"

    private val remoteConfig: FirebaseRemoteConfig
        get() = Firebase.remoteConfig

    /**
     * Read per request rather than cached once: an activate that lands mid-session then takes
     * effect on the next rewrite instead of waiting for a cold start.
     */
    fun modelName(): String =
        remoteConfig.getString(KEY_MODEL_NAME).ifBlank { FALLBACK_MODEL }

    /** Call once from `Application.onCreate`, after `FirebaseApp` is initialised. */
    fun initialize() {
        remoteConfig.setConfigSettingsAsync(
            remoteConfigSettings {
                // Fetches are throttled per app instance, so refetching every launch is a debug
                // -only luxury. Release uses Google's recommended one hour floor.
                minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0 else 3600
            }
        )
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        // A failed fetch is survivable — modelName() falls through to the packaged default — so
        // this logs rather than retries, and never blocks startup.
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.i(TAG, "Remote Config activated, model=${modelName()}")
            } else {
                Log.w(TAG, "Remote Config fetch failed, using ${modelName()}", task.exception)
            }
        }
    }
}
