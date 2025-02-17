package com.davidmcdeveloper.trainernotes10.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.davidmcdeveloper.trainernotes10.AddTeamScreen
import com.davidmcdeveloper.trainernotes10.HomeScreen
import com.davidmcdeveloper.trainernotes10.LoginScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object AddTeam : Screen("add_team")
}

@Composable
fun AppNavGraph(navController: NavHostController, auth: FirebaseAuth) {
    val db = FirebaseFirestore.getInstance() // Inicializamos Firestore

    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(navController = navController, auth = auth, context = navController.context)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(Screen.AddTeam.route) {
            AddTeamScreen(navController, db) // Pasamos Firestore
        }
    }
}

