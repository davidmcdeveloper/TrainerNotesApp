package com.davidmcdeveloper.trainernotes10.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.davidmcdeveloper.trainernotes10.R
import com.davidmcdeveloper.trainernotes10.navigation.Screen
import com.davidmcdeveloper.trainernotes10.utils.deleteJugadoresByCategory
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryHomeScreen(navController: NavController, categoryName: String, db: FirebaseFirestore) {
    val context = LocalContext.current
    var imageUrl by remember { mutableStateOf("") }
    var teamName by remember { mutableStateOf("") }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var isImageLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    // LaunchedEffect unificado para obtener teamName e imageUrl
    LaunchedEffect(key1 = categoryName) {
        val categoryRef = db.collection("equipos")
            .whereArrayContains("categorias", categoryName)
        val querySnapshot = categoryRef.get().await()
        if (!querySnapshot.isEmpty) {
            val teamDocument = querySnapshot.documents[0]
            teamName = teamDocument.id
            imageUrl = teamDocument.getString("imagenUrl") ?: ""
        } else {
            Toast.makeText(
                context,
                "No se ha encontrado el equipo al que pertenece la categoría",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Scaffold(
        floatingActionButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FloatingActionButton(onClick = { navController.navigate(Screen.Home.route) },
                    modifier = Modifier
                        .padding(start = 30.dp)) {
                    Icon(Icons.Filled.Home, contentDescription = "Volver al listado de equipos")
                }
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(categoryName) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        showDeleteConfirmationDialog = true
                    }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar categoría")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Image(
                painter = painterResource(id = R.drawable.trainernotesbackground),
                contentDescription = "Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Escudo del equipo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        onLoading = {
                            isImageLoading = true
                        },
                        onSuccess = {
                            isImageLoading = false
                        },
                        onError = {
                            isImageLoading = false
                        }
                    )
                    if (isImageLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "¡Bienvenido a $categoryName!",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(32.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(onClick = {
                        navController.navigate(Screen.Asistencias.createRoute(categoryName))
                    },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 30.dp),
                        shape = RoundedCornerShape(16.dp)) {
                        Text(
                            text = "Asistencias",
                            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                    Button(onClick = {
                        navController.navigate(Screen.Jugadores.createRoute(categoryName))
                    },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 30.dp),
                        shape = RoundedCornerShape(16.dp)) {
                        Text(
                            text = "Jugadores",
                            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                    Button(onClick = {
                        Toast.makeText(context, "Has pulsado Objetivos", Toast.LENGTH_SHORT).show()
                    },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 30.dp),
                        shape = RoundedCornerShape(16.dp)) {
                        Text(
                            text = "Objetivos",
                            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // Diálogo de confirmación
            if (showDeleteConfirmationDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmationDialog = false },
                    title = { Text("Eliminar Categoría") },
                    text = {
                        Column {
                            Text("¿Estás seguro de que quieres eliminar la categoría '$categoryName' y todos los jugadores que contiene?")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Esta acción es irreversible.", fontWeight = FontWeight.Bold)
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            isDeleting = true
                            scope.launch {
                                deleteJugadoresByCategory(db, categoryName, context)
                                val teamRef = db.collection("equipos").document(teamName)
                                teamRef.update("categorias", FieldValue.arrayRemove(categoryName)).await()
                                Toast.makeText(
                                    context,
                                    "Categoría eliminada correctamente",
                                    Toast.LENGTH_SHORT
                                ).show()
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                            showDeleteConfirmationDialog = false
                        }) {
                            if (isDeleting) {
                                CircularProgressIndicator()
                            } else {
                                Text("Eliminar")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showDeleteConfirmationDialog = false
                        }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}