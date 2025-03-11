package com.davidmcdeveloper.trainernotes10.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.davidmcdeveloper.trainernotes10.R
import com.davidmcdeveloper.trainernotes10.dataclass.Equipo
import com.davidmcdeveloper.trainernotes10.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    var equipos by remember { mutableStateOf<List<Pair<String, Equipo>>>(emptyList()) } //Modificamos la variable.
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.AddTeam.route) }) {
                Icon(Icons.Filled.Add, contentDescription = "Añadir equipo")
            }
        },
        topBar = {
            androidx.compose.material3.CenterAlignedTopAppBar(
                title = { Text("Trainer Notes") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(onClick = {
                        auth.signOut()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar Sesión")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()){
            Image(
                painter = painterResource(id = R.drawable.trainernotesbackground),
                contentDescription = "Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(paddingValues)) {
                if (equipos.isEmpty()) {
                    Text("No hay equipos registrados", style = MaterialTheme.typography.bodyMedium)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(equipos) { (teamId, equipo) ->
                            Card(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth()
                                    .clickable {
                                        scope.launch {
                                            delay(300) // Añadir un retraso de 300ms
                                            navController.navigate(Screen.TeamDetails.createRoute(teamId)) //Pasamos el id.
                                        }
                                    },
                                shape = CircleShape,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    var isLoading by remember { mutableStateOf(true) }
                                    Box(modifier = Modifier.size(80.dp)) {
                                        Image(
                                            painter = rememberAsyncImagePainter(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(equipo.imagenUrl)
                                                    .crossfade(true)
                                                    .build(),
                                                onLoading = { isLoading = true },
                                                onSuccess = { isLoading = false },
                                                onError = { isLoading = false }
                                            ),
                                            contentDescription = "Escudo del equipo",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                        if (isLoading) {
                                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = equipo.nombre,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }

        }
        LaunchedEffect(Unit) {
            db.collection("equipos").get()
                .addOnSuccessListener { result ->
                    equipos = result.documents.mapNotNull { document ->
                        val nombre = document.getString("nombre")
                        val imagenUrl = document.getString("imagenUrl")
                        val id = document.id //Obtenemos el id.
                        Log.d("HomeScreen", "Nombre: $nombre, ImagenUrl: $imagenUrl")
                        if (nombre != null && imagenUrl != null) {
                            Pair(id,Equipo(nombre, imagenUrl)) //Añadimos el id.
                        } else {
                            null
                        }
                    }
                }
                .addOnFailureListener {
                    Log.e("Firestore", "Error al obtener equipos", it)
                }
        }
    }
}