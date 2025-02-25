package com.davidmcdeveloper.trainernotes10.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.davidmcdeveloper.trainernotes10.screens.AddTeamScreen
import com.davidmcdeveloper.trainernotes10.screens.HomeScreen
import com.davidmcdeveloper.trainernotes10.screens.LoginScreen
import com.davidmcdeveloper.trainernotes10.screens.TeamDetailsScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.net.URLDecoder

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object AddTeam : Screen("add_team")
    object TeamDetails : Screen("teamDetails/{teamName}"){
        fun createRoute(teamName: String) = "teamDetails/$teamName"
    }
}

@Composable
fun AppNavGraph(navController: NavHostController, auth: FirebaseAuth, startDestination: String) {
    val db = FirebaseFirestore.getInstance() // Inicializamos Firestore

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) {
            LoginScreen(navController = navController, auth = auth, context = navController.context)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.AddTeam.route) {
            AddTeamScreen(navController = navController, db = db) // Pasamos Firestore
        }
        composable(
            route = Screen.TeamDetails.route,
            arguments = listOf(
                navArgument("teamName") { type = NavType.StringType }
            )
        ){ backStackEntry ->
            val teamName = backStackEntry.arguments?.getString("teamName") ?: ""
            if (teamName != null) {
                TeamDetailsScreen(navController = navController, teamName = teamName, db = db)
            }
        }
    }
}

