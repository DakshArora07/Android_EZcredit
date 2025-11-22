package sfu.cmpt362.android_ezcredit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import sfu.cmpt362.android_ezcredit.data.gemini.GeminiSetup
import sfu.cmpt362.android_ezcredit.ui.NavigationDrawerScreen
import sfu.cmpt362.android_ezcredit.ui.theme.Android_EZCreditTheme

class MainActivity : ComponentActivity() {
    private val geminiSetup = GeminiSetup()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Android_EZCreditTheme {
                NavigationDrawerScreen()
                geminiSetup.sendToGemini(lifecycleScope)

            }
        }
    }
}
