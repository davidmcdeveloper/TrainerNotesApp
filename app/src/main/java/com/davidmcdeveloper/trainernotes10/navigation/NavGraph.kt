package com.davidmcdeveloper.trainernotes10.navigation

import AsistenciasScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.davidmcdeveloper.trainernotes10.screens.AddCategoryScreen
import com.davidmcdeveloper.trainernotes10.screens.AddJugadorScreen
import com.davidmcdeveloper.trainernotes10.screens.AddTeamScreen
import com.davidmcdeveloper.trainernotes10.screens.CategoryHomeScreen
import com.davidmcdeveloper.trainernotes10.screens.HistorialScreen
import com.davidmcdeveloper.trainernotes10.screens.HomeScreen
import com.davidmcdeveloper.trainernotes10.screens.JugadoresScreen
import com.davidmcdeveloper.trainernotes10.screens.LoginScreen
import com.davidmcdeveloper.trainernotes10.screens.TeamDetailsScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


const val ARG_TEAM_NAME = "teamName"
const val ARG_CATEGORY_NAME = "categoryName"
const val ARG_TEAM_ID = "teamId"

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Login : Screen("login")
    object AddTeam : Screen("add_team")
    object AddCategory : Screen("addCategory/{$ARG_TEAM_ID}") {
        fun createRoute(teamId: String) = "addCategory/$teamId"
    }

    object TeamDetails : Screen("team_details/{$ARG_TEAM_ID}") {
        fun createRoute(teamId: String) = "team_details/$teamId"
    }

    object CategoryHome : Screen("categoryHome/{$ARG_CATEGORY_NAME}") {
        fun createRoute(categoryName: String) = "categoryHome/$categoryName"
    }

    object Jugadores : Screen("jugadores/{$ARG_CATEGORY_NAME}") {
        fun createRoute(categoryName: String) = "jugadores/$categoryName"
    }

    object AddJugador : Screen("addJugador/{$ARG_CATEGORY_NAME}") {
        fun createRoute(categoryName: String) = "addJugador/$categoryName"
    }

    object Asistencias : Screen("asistencias/{$ARG_CATEGORY_NAME}") {
        fun createRoute(categoryName: String) = "asistencias/$categoryName"
    }
    object Historial : Screen("historial/{$ARG_CATEGORY_NAME}",
        ) {
        fun createRoute(categoryName: String) = "historial/$categoryName"
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
                navArgument(ARG_TEAM_ID) { type = NavType.StringType } // Modificamos el argumento.
            )
        ) { backStackEntry ->
            val teamId = backStackEntry.arguments?.getString(ARG_TEAM_ID)
                ?: "" //Lo obtenemos de forma correcta.
            TeamDetailsScreen(navController = navController, teamId = teamId, db = db)
        }
        composable(
            route = Screen.AddCategory.route,
            arguments = listOf(
                navArgument(ARG_TEAM_ID) { type = NavType.StringType } // Modificamos el argumento.
            )
        ) { backStackEntry ->
            val teamId = backStackEntry.arguments?.getString(ARG_TEAM_ID)
                ?: "" //Lo obtenemos de forma correcta.
            AddCategoryScreen(navController = navController, teamId = teamId, db = db)
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
        composable(
            route = Screen.Jugadores.route,
            arguments = listOf(
                navArgument(ARG_CATEGORY_NAME) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString(ARG_CATEGORY_NAME) ?: ""
            JugadoresScreen(navController = navController, categoryName = categoryName, db = db)
        }
        composable(
            route = Screen.AddJugador.route,
            arguments = listOf(
                navArgument(ARG_CATEGORY_NAME) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString(ARG_CATEGORY_NAME) ?: ""
            AddJugadorScreen(navController = navController, categoryName = categoryName, db = db)
        }
        composable(
            route = Screen.Asistencias.route,
            arguments = listOf(
                navArgument(ARG_CATEGORY_NAME) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString(ARG_CATEGORY_NAME) ?: ""
            AsistenciasScreen(navController = navController, categoryName = categoryName, db = db)
        }
        composable(
            route = Screen.Historial.route,
            arguments = listOf(navArgument(ARG_CATEGORY_NAME) { type = NavType.StringType })
        ) { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString(ARG_CATEGORY_NAME) ?: ""
            HistorialScreen(navController = navController, categoryName = categoryName, db = db)
        }
    }
}

