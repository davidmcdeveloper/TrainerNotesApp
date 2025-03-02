package com.davidmcdeveloper.trainernotes10.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.davidmcdeveloper.trainernotes10.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.io.InputStream
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTeamScreen(navController: NavController, db: FirebaseFirestore) {
    val context = LocalContext.current
    var teamName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val storage = Firebase.storage
    val bitmap = imageUri?.let { loadBitmapFromUri(context, it) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> imageUri = uri }
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Añadir equipo") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("home") }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver a la lista de equipos"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                    ) {
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Imagen seleccionada",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.defaultteam),
                                contentDescription = "Escudo por defecto",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Button(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(bottom = 4.dp)
                                .size(24.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Añadir imagen")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = teamName,
                        onValueChange = { teamName = it },
                        label = { Text("Nombre del equipo") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = {
                            if (teamName.isNotEmpty() && imageUri != null) {
                                isLoading = true
                                val imageName = "${UUID.randomUUID()}.jpg"
                                val storageRef = storage.reference.child("teams/$imageName")

                                storageRef.putFile(imageUri!!)
                                    .addOnSuccessListener {
                                        storageRef.downloadUrl
                                            .addOnSuccessListener { uri ->
                                                val imageUrl = uri.toString()
                                                saveTeamToFirestore(db, teamName, imageUrl, context, navController)
                                                isLoading = false
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(context, "Error al subir la imagen", Toast.LENGTH_SHORT).show()
                                                isLoading = false
                                            }
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Error al subir la imagen", Toast.LENGTH_SHORT).show()
                                        isLoading = false
                                    }
                            } else {
                                Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Crear equipo", textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    var input: InputStream? = null
    try {
        input = context.contentResolver.openInputStream(uri)
        return BitmapFactory.decodeStream(input)
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        try {
            input?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    return null
}

fun saveTeamToFirestore(
    db: FirebaseFirestore,
    teamName: String,
    imageUrl: String,
    context: android.content.Context,
    navController: NavController
) {
    val teamId = UUID.randomUUID().toString()
    val teamData = hashMapOf(
        "nombre" to teamName,
        "imagenUrl" to imageUrl
    )

    db.collection("equipos").document(teamId)
        .set(teamData)
        .addOnSuccessListener {
            Toast.makeText(context, "Equipo creado correctamente", Toast.LENGTH_SHORT).show()
            navController.navigate("home")
        }
        .addOnFailureListener {
            Toast.makeText(context, "Error al crear equipo", Toast.LENGTH_SHORT).show()
        }
}