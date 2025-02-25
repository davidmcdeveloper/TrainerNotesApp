package com.davidmcdeveloper.trainernotes10.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

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

    Box(modifier = Modifier.fillMaxSize()) {
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

                    Text(teamName)

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
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
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Eliminar equipo", color = Color.White)
                    }
                }
            }

        }
    }
}
