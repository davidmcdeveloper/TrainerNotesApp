package com.davidmcdeveloper.trainernotes10.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.davidmcdeveloper.trainernotes10.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTeamScreen(navController: NavController, db: FirebaseFirestore) {
    val context = LocalContext.current
    var teamName by remember { mutableStateOf(TextFieldValue()) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    val storageRef = FirebaseStorage.getInstance().reference

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Agregar equipo") },
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
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Añadir padding para evitar superposición con la TopAppBar
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.Gray, CircleShape)
                        .background(if (imageUri == null) Color.LightGray else Color.Transparent) // Fondo gris claro solo si no hay imagen seleccionada
                ) {
                    if (imageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = "Escudo del equipo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.defaultteam),
                            contentDescription = "Escudo del equipo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                IconButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .background(Color.Transparent, CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddCircle,
                        contentDescription = "Seleccionar imagen",
                        tint = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = teamName,
                onValueChange = { teamName = it },
                label = { Text("Nombre del equipo") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isUploading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            Button(
                onClick = {
                    if (teamName.text.isNotEmpty() && imageUri != null) {
                        isUploading = true
                        uploadImageAndSaveTeam(
                            db,
                            storageRef,
                            teamName.text,
                            imageUri!!,
                            context,
                            navController
                        ) {
                            isUploading = false
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "Debes seleccionar un nombre y una imagen",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isUploading
            ) {
                Text("Añadir equipo")
            }
        }
    }
}

fun uploadImageAndSaveTeam(
    db: FirebaseFirestore,
    storageRef: StorageReference,
    teamName: String,
    imageUri: Uri,
    context: Context,
    navController: NavController,
    onComplete: () -> Unit
) {
    val fileName = "teams/${UUID.randomUUID()}"
    val imageRef = storageRef.child(fileName)

    imageRef.putFile(imageUri)
        .addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                saveTeamToFirestore(db, teamName, uri.toString(), context, navController)
                onComplete()
            }
        }
        .addOnFailureListener {
            Toast.makeText(context, "Error al subir la imagen", Toast.LENGTH_SHORT).show()
            onComplete()
        }
}

fun saveTeamToFirestore(
    db: FirebaseFirestore,
    teamName: String,
    imageUrl: String,
    context: Context,
    navController: NavController
) {
    val teamData = hashMapOf(
        "nombre" to teamName,
        "imagenUrl" to imageUrl
    )

    db.collection("equipos").document(teamName)
        .set(teamData)
        .addOnSuccessListener {
            Toast.makeText(context, "Equipo añadido correctamente", Toast.LENGTH_SHORT).show()

            navController.navigate("home") {
                popUpTo("home") { inclusive = true }
            }
        }
        .addOnFailureListener {
            Toast.makeText(context, "Error al guardar equipo", Toast.LENGTH_SHORT).show()
        }
}