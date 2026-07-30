package com.bhatt.tonerewriter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.bhatt.tonerewriter.ui.rewrite.RewriteRoute
import com.bhatt.tonerewriter.ui.theme.ToneRewriterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ToneRewriterTheme {
                RewriteRoute()
            }
        }
    }
}
