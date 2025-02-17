package com.davidmcdeveloper.trainernotes10

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.davidmcdeveloper.trainernotes10.navigation.AppNavGraph
import com.davidmcdeveloper.trainernotes10.ui.theme.TrainerNotesTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrainerNotesTheme {
                val navController = rememberNavController()
                val auth = FirebaseAuth.getInstance()
                AppNavGraph(navController = navController, auth = auth)
            }
        }
    }
}



