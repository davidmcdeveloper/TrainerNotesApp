package com.davidmcdeveloper.trainernotes10.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.davidmcdeveloper.trainernotes10.Equipo
import com.davidmcdeveloper.trainernotes10.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    var equipos by remember { mutableStateOf(listOf<Equipo>()) }

    // Carga equipos desde Firestore
    LaunchedEffect(Unit) {
        db.collection("equipos").get()
            .addOnSuccessListener { result ->
                equipos = result.documents.mapNotNull { document ->
                    val nombre = document.getString("nombre")
                    val imagenUrl = document.getString("imagenUrl")
                    Log.d("HomeScreen", "Nombre: $nombre, ImagenUrl: $imagenUrl")
                    if (nombre != null && imagenUrl != null) {
                        Equipo(nombre, imagenUrl)
                    } else {
                        null
                    }
                }
            }
            .addOnFailureListener {
                Log.e("Firestore", "Error al obtener equipos", it)
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("TrainerNotes") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(onClick = {
                        auth.signOut()
                        navController.navigate("login") {
                            popUpTo("login") { inclusive = true }
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar Sesión")
                    }
                }

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
        Column(modifier = Modifier.padding(paddingValues)) {
            if (equipos.isEmpty()) {
                Text("No hay equipos registrados", style = MaterialTheme.typography.bodyMedium)
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(equipos) { equipo ->
                        Card(
                            modifier = Modifier
                                .padding(4.dp)
                                .aspectRatio(1f)
                                .clickable {
                                    navController.navigate("teamDetails/${equipo.nombre.encode()}/${equipo.imagenUrl.encode()}")
                                },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(equipo.imagenUrl),
                                    contentDescription = "Escudo del equipo",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(4.dp))
                                        .aspectRatio(1f),
                                    contentScale = ContentScale.FillWidth
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = equipo.nombre,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
fun String.encode(): String {
    return java.net.URLEncoder.encode(this, Charsets.UTF_8.name())
}


