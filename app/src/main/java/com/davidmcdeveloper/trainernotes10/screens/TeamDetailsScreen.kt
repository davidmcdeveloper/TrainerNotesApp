package com.davidmcdeveloper.trainernotes10.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDetailsScreen(
    navController: NavController,
    db: FirebaseFirestore,
    teamName: String
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var isDeleting by remember { mutableStateOf(false) }
    var imageUrl by remember { mutableStateOf("") }

    // Carga equipos desde Firestore
    LaunchedEffect(Unit) {
        db.collection("equipos")
            .whereEqualTo("nombre", teamName)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    imageUrl = document.getString("imagenUrl") ?: ""
                }
                isLoading = false
            }
            .addOnFailureListener {
                Log.e("Firestore", "Error al obtener equipos", it)
            }
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(teamName) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("home") }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver a la lista de equipos")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        isDeleting = true
                        val teamRef = db.collection("equipos").document(teamName)
                        val storageRef = FirebaseStorage.getInstance()
                            .getReferenceFromUrl(imageUrl)

                        storageRef.delete()
                            .addOnSuccessListener {
                                teamRef.delete()
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            context,
                                            "Equipo eliminado correctamente",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        navController.navigate("home") {
                                            popUpTo("home") { inclusive = true }
                                        }
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            context,
                                            "Error al eliminar el equipo",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    context,
                                    "Error al eliminar la imagen del equipo",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnCompleteListener { isDeleting = false }
                    }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar equipo")
                    }
                }
            )
        },
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (isLoading || isDeleting) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Escudo del equipo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Spacer(modifier = Modifier.height(24.dp))
                        Row {
                            IconButton(onClick = {
                                isDeleting = true
                                val teamRef = db.collection("equipos").document(teamName)
                                val storageRef = FirebaseStorage.getInstance()
                                    .getReferenceFromUrl(imageUrl)

                                storageRef.delete()
                                    .addOnSuccessListener {
                                        teamRef.delete()
                                            .addOnSuccessListener {
                                                Toast.makeText(
                                                    context,
                                                    "Equipo eliminado correctamente",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                navController.navigate("home") {
                                                    popUpTo("home") { inclusive = true }
                                                }
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(
                                                    context,
                                                    "Error al eliminar el equipo",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            context,
                                            "Error al eliminar la imagen del equipo",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    .addOnCompleteListener { isDeleting = false }
                            }) {
                            }
                        }
                    }
                }

            }
        }
    }
}
