package com.davidmcdeveloper.trainernotes10.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.davidmcdeveloper.trainernotes10.screens.AddCategoryScreen
import com.davidmcdeveloper.trainernotes10.screens.AddTeamScreen
import com.davidmcdeveloper.trainernotes10.screens.CategoryHomeScreen
import com.davidmcdeveloper.trainernotes10.screens.HomeScreen
import com.davidmcdeveloper.trainernotes10.screens.LoginScreen
import com.davidmcdeveloper.trainernotes10.screens.TeamDetailsScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


const val ARG_TEAM_NAME = "teamName"
const val ARG_CATEGORY_NAME = "categoryName"

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Login : Screen("login")
    object AddTeam : Screen("add_team")
    object AddCategory: Screen("addCategory/{$ARG_TEAM_NAME}"){
        fun createRoute(teamName: String) = "addCategory/$teamName"
    }
    object TeamDetails: Screen("teamDetails/{$ARG_TEAM_NAME}"){
        fun createRoute(teamName: String) = "teamDetails/$teamName"
    }
    object CategoryHome: Screen("categoryHome/{$ARG_CATEGORY_NAME}"){
        fun createRoute(categoryName: String) = "categoryHome/$categoryName"
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
                navArgument(ARG_TEAM_NAME) { type = NavType.StringType }
            )
        ){ backStackEntry ->
            val teamName = backStackEntry.arguments?.getString(ARG_TEAM_NAME) ?: ""
            TeamDetailsScreen(navController = navController, teamName = teamName, db = db)
        }
        composable(
            route = Screen.AddCategory.route,
            arguments = listOf(
                navArgument(ARG_TEAM_NAME) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val teamName = backStackEntry.arguments?.getString(ARG_TEAM_NAME) ?: ""
            AddCategoryScreen(navController = navController, teamName = teamName, db = db)
        }
        composable(
            route = Screen.CategoryHome.route,
            arguments = listOf(
                navArgument(ARG_CATEGORY_NAME) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString(ARG_CATEGORY_NAME) ?: ""
            CategoryHomeScreen(navController = navController, categoryName = categoryName, db = db)
        }
    }
}

