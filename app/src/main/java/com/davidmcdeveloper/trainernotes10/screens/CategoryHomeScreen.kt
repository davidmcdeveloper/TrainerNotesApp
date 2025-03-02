package com.davidmcdeveloper.trainernotes10.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryHomeScreen(
    navController: NavController,
    categoryName: String,
    db: FirebaseFirestore
) {
    val context = LocalContext.current
    var imageUrl by remember { mutableStateOf("") }
    var teamName by remember { mutableStateOf("") }

    // LaunchedEffect 1: Obtener el teamName
    LaunchedEffect(key1 = categoryName) {
        Log.d("CategoryHomeScreen", "categoryName: $categoryName")
        val categoryRef = db.collection("equipos")
            .whereArrayContains("categorias", categoryName)
        val querySnapshot = categoryRef.get().await()
        if (!querySnapshot.isEmpty) {
            teamName = querySnapshot.documents[0].id
            Log.d("CategoryHomeScreen", "teamName: $teamName - LaunchedEffect 1") // Log 1
        } else {
            Toast.makeText(
                context,
                "No se ha encontrado el equipo al que pertenece la categoría",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // LaunchedEffect 2: Obtener la imageUrl
    LaunchedEffect(key1 = teamName) {
        if (teamName.isNotEmpty()) {
            Log.d("CategoryHomeScreen", "teamName: $teamName - LaunchedEffect 2") // Log 2
            val teamDocument = db.collection("equipos").document(teamName).get().await()
            Log.d("CategoryHomeScreen", "teamDocument: $teamDocument") // Log 3
            if (teamDocument.exists()) {
                imageUrl = teamDocument.getString("imagenUrl") ?: ""
                Log.d("CategoryHomeScreen", "imageUrl: $imageUrl") // Log 4
            } else {
                Log.d("CategoryHomeScreen", "No se ha encontrado la imagen del equipo") // Log 5
                Toast.makeText(
                    context,
                    "No se ha encontrado la imagen del equipo",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    Scaffold(
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
                        // Lógica para eliminar la categoría
                        val categoryRef = db.collection("equipos")
                            .whereArrayContains("categorias", categoryName) //Encuentra el equipo que contiene la categoria.
                        categoryRef.get().addOnSuccessListener { querySnapshot ->
                            if (!querySnapshot.isEmpty) {
                                val docRef = querySnapshot.documents[0].reference // Obtener la referencia del primer documento encontrado
                                val subcollectionRef = docRef.collection("categorias") // Referencia a la subcolección "categorias"
                                subcollectionRef.whereEqualTo("nombre", categoryName) //Encuentra la categoria.
                                    .get().addOnSuccessListener { querySnapshot ->
                                        if (!querySnapshot.isEmpty){
                                            val docToDelete = querySnapshot.documents[0].reference // Obtener la referencia del primer documento encontrado
                                            docToDelete.delete().addOnSuccessListener {
                                                Toast.makeText(context, "Categoría eliminada correctamente", Toast.LENGTH_SHORT).show()
                                                navController.popBackStack()
                                            }
                                                .addOnFailureListener {
                                                    Toast.makeText(context, "Error al eliminar la categoría", Toast.LENGTH_SHORT).show()
                                                }
                                        }else{
                                            Toast.makeText(context, "No se ha encontrado la categoría", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .addOnFailureListener{
                                        Toast.makeText(context, "Error al obtener la categoría", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                Toast.makeText(context, "No se ha encontrado el equipo al que pertenece la categoría", Toast.LENGTH_SHORT).show()
                            }
                        }.addOnFailureListener {
                            Toast.makeText(context, "Error al obtener el equipo al que pertenece la categoría", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar categoría")
                    }
                }
            )
        },
    ) { paddingValues ->
        Box(modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize(),
            contentAlignment = Alignment.Center) {
            Column(modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Escudo del equipo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(32.dp))
                Text(text = "¡Bienvenido a $categoryName!",
                    style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(32.dp))

                Column(modifier = Modifier
                    .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally){
                    Button(onClick = {
                        Toast.makeText(context, "Has pulsado Asistencias", Toast.LENGTH_SHORT).show()
                    },
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)) {
                        Text(text = "Asistencias", style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold))
                    }
                    Button(onClick = {
                        Toast.makeText(context, "Has pulsado Jugadores", Toast.LENGTH_SHORT).show()
                    },
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)) {
                        Text(text = "Jugadores", style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold))
                    }
                    Button(onClick = {
                        Toast.makeText(context, "Has pulsado Objetivos", Toast.LENGTH_SHORT).show()
                    },
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)) {
                        Text(text = "Objetivos", style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}