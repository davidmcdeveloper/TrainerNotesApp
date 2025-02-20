package com.davidmcdeveloper.trainernotes10.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    var equipos by remember { mutableStateOf(listOf<String>()) }

    // Carga equipos desde Firestore
    LaunchedEffect(Unit) {
        db.collection("equipos").get()
            .addOnSuccessListener { result ->
                equipos = result.documents.mapNotNull { it.id }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al obtener equipos", e)
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("TrainerNotes") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_team") },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("+")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Equipos Registrados",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp)
            )

            if (equipos.isEmpty()) {
                Text("No hay equipos registrados", style = MaterialTheme.typography.bodyMedium)
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(equipos) { equipo ->
                        // Card para cada equipo
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable {
                                    // Navegar a la pantalla de detalles del equipo
                                    navController.navigate("teamDetails/$equipo")
                                },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth()
                            ) {
                                // Aquí podrías agregar una imagen para el escudo del equipo, si la tienes
                                Image(
                                    painter = rememberAsyncImagePainter("url_de_imagen_de_equipo"), // Aquí deberías cargar la imagen de cada equipo
                                    contentDescription = "Escudo del equipo",
                                    modifier = Modifier
                                        .size(40.dp)
                                        .align(Alignment.CenterHorizontally)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = equipo,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    auth.signOut()
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Cerrar sesión")
            }
        }
    }
}


