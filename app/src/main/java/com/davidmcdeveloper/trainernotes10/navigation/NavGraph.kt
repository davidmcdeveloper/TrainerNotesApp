package com.davidmcdeveloper.trainernotes10.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.davidmcdeveloper.trainernotes10.screens.AddTeamScreen
import com.davidmcdeveloper.trainernotes10.screens.HomeScreen
import com.davidmcdeveloper.trainernotes10.screens.LoginScreen
import com.davidmcdeveloper.trainernotes10.screens.TeamDetailsScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object AddTeam : Screen("add_team")
}

@Composable
fun AppNavGraph(navController: NavHostController, auth: FirebaseAuth, startDestination: String) {
    val db = FirebaseFirestore.getInstance() // Inicializamos Firestore

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) {
            LoginScreen(navController = navController, auth = auth, context = navController.context)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(Screen.AddTeam.route) {
            AddTeamScreen(navController = navController, db = db) // Pasamos Firestore
        }
        composable("teamDetails/{teamName}") { backStackEntry ->
            val teamName = backStackEntry.arguments?.getString("teamName")
            teamName?.let {
                TeamDetailsScreen(navController = navController, teamName = it,db = db) //Pantalla Home equipo
            }
        }
    }
}

