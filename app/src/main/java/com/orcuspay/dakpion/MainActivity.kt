package com.orcuspay.dakpion

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.orcuspay.dakpion.presentation.screens.NavGraphs
import com.orcuspay.dakpion.presentation.theme.DakpionTheme
import com.orcuspay.dakpion.worker.SyncScheduler
import com.ramcosta.composedestinations.DestinationsNavHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var text: String

    companion object {
        /** True while the UI is visible; the background self-test uses it. */
        @Volatile
        var inForeground: Boolean = false
            private set
    }

    override fun onStop() {
        inForeground = false
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        inForeground = true
        // Opening the app always forces a sync, so a merchant checking the
        // app never has to wait for the periodic job or press "Sync now".
        SyncScheduler.enqueueImmediate(applicationContext)
        // And (re)start the keep-alive service once SMS access is granted, so
        // the receiver keeps firing after the merchant leaves the app.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            SmsKeepAliveService.start(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DakpionTheme {
                Log.d("kraken", "main activity")
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colors.background)
                ) {
                    Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                        DestinationsNavHost(navGraph = NavGraphs.root)
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    DakpionTheme {
        Greeting("Android")
    }
}