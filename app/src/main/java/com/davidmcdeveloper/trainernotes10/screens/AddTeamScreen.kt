package com.davidmcdeveloper.trainernotes10.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.twotone.AddCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
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

    Column(
        modifier = Modifier
            .fillMaxSize()
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
            ) {
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
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
                    uploadImageAndSaveTeam(db, storageRef, teamName.text, imageUri!!, context, navController) {
                        isUploading = false
                    }
                } else {
                    Toast.makeText(context, "Debes seleccionar un nombre y una imagen", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isUploading
        ) {
            Text("Añadir equipo")
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