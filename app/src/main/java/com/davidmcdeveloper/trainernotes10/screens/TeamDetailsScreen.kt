package com.davidmcdeveloper.trainernotes10.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

@Composable
fun TeamDetailsScreen(navController: NavController, db: FirebaseFirestore, teamName: String, teamImageUrl: String) {
    Log.d("TeamDetailsScreen", "teamName: $teamName, teamImageUrl: $teamImageUrl")

    val context = LocalContext.current
    var teamData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isDeleting by remember { mutableStateOf(false) }

    //Vamos a eliminar la peticion de datos, ya que los recibimos por parametro
    teamData = mapOf("nombre" to teamName, "imagenUrl" to teamImageUrl)
    isLoading = false


    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (isLoading || isDeleting) {
            CircularProgressIndicator()
        } else {
            teamData?.let {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val imageUrl = it["imagenUrl"] as? String ?: ""
                    val nombre = it["nombre"] as? String ?: ""

                    Image(
                        painter = rememberAsyncImagePainter(imageUrl),
                        contentDescription = "Escudo del equipo",
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.Gray, CircleShape)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(nombre)

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            isDeleting = true
                            val teamRef = db.collection("equipos").document(teamName)
                            val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)

                            storageRef.delete()
                                .addOnSuccessListener {
                                    teamRef.delete()
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Equipo eliminado correctamente", Toast.LENGTH_SHORT).show()
                                            navController.navigate("home") {
                                                popUpTo("home") { inclusive = true }
                                            }
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(context, "Error al eliminar el equipo", Toast.LENGTH_SHORT).show()
                                        }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Error al eliminar la imagen del equipo", Toast.LENGTH_SHORT).show()
                                }
                                .addOnCompleteListener { isDeleting = false }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Eliminar equipo", color = Color.White)
                    }
                }
            } ?: Text("No se encontraron los datos del equipo.", color = Color.Red)
        }
    }
}




