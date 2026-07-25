package com.bhatt.tonerewriter

import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

/**
 * Release attestation via Play Integrity. Only produces valid tokens for builds signed with
 * the Play upload/app-signing key and distributed through Google Play.
 */
fun ToneRewriterApp.installAppCheck() {
    Firebase.appCheck.installAppCheckProviderFactory(
        PlayIntegrityAppCheckProviderFactory.getInstance()
    )
}
