package com.yehjunb.nodopamine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.yehjunb.nodopamine.data.DetoxStore
import com.yehjunb.nodopamine.ui.NoDopamineApp
import com.yehjunb.nodopamine.ui.theme.NoDopamineTheme

class MainActivity : ComponentActivity() {

    /** 접근성 권한이 켜져 있는지. 설정에서 돌아올 때마다 다시 확인해요. */
    private var serviceEnabled by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DetoxStore.init(this)
        enableEdgeToEdge()
        setContent {
            NoDopamineTheme {
                NoDopamineApp(serviceEnabled = serviceEnabled)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        serviceEnabled = BlockerService.isEnabled(this)
        DetoxStore.checkCompletion()
    }
}
