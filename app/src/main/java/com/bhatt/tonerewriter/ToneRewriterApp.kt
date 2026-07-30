package com.bhatt.tonerewriter

import android.app.Application
import com.google.firebase.FirebaseApp

/**
 * Installs a Firebase App Check provider before any AI Logic call runs.
 *
 * The provider itself differs per build type — debug uses the debug provider (whose token you
 * register once in the console), release uses Play Integrity — so [installAppCheck] lives in
 * the src/debug and src/release source sets. That keeps the debug-only dependency off the
 * release classpath instead of failing the release build.
 */
class ToneRewriterApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        installAppCheck()
    }
}
