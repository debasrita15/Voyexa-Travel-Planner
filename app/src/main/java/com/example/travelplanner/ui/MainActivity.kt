package com.example.travelplanner.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.core.view.WindowCompat
import com.example.travelplanner.ui.navigation.NavGraph
import com.example.travelplanner.ui.theme.TravelPlannerTheme
//import com.example.travelplanner.ui.theme.TravelPlannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            TravelPlannerTheme {

                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {

                    NavGraph()

                }

            }
        }
    }
}
