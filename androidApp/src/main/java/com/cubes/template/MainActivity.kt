package com.cubes.template

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.cubes.template.shared.ui.app.App

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appCompositionRoot = (application as MainApplication).appCompositionRoot

        setContent {
            App(appCompositionRoot = appCompositionRoot)
        }
    }
}
