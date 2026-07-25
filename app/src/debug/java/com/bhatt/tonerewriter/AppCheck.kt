package com.bhatt.tonerewriter

import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

/**
 * Debug attestation. On first run the provider logs a debug token
 * (`DebugAppCheckProvider: Enter this debug secret ...`). Register that token once in
 * Firebase console → App Check → Apps → ⋮ → Manage debug tokens, and this device is trusted.
 */
fun ToneRewriterApp.installAppCheck() {
    Firebase.appCheck.installAppCheckProviderFactory(
        DebugAppCheckProviderFactory.getInstance()
    )
}
