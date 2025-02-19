package com.davidmcdeveloper.trainernotes10

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID

@Composable
fun AddTeamScreen(navController: NavController, db: FirebaseFirestore) {
    val context = LocalContext.current
    var teamName by remember { mutableStateOf(TextFieldValue()) }
    var category by remember { mutableStateOf(TextFieldValue()) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val storageRef = FirebaseStorage.getInstance().reference

    // Selector de imagen desde galería
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // Imagen del equipo
        imageUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = "Escudo del equipo",
                modifier = Modifier
                    .size(100.dp)
                    .aspectRatio(1f)
            )
        }

        Button(onClick = { imagePickerLauncher.launch("image/*") }) {
            Text("Seleccionar imagen")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = teamName,
            onValueChange = { teamName = it },
            label = { Text("Nombre del equipo") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Categoría (ej. Senior Masculino)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (teamName.text.isNotEmpty() && category.text.isNotEmpty() && imageUri != null) {
                    uploadImageAndSaveTeam(db, storageRef, teamName.text, category.text, imageUri!!, context, navController)
                } else {
                    Toast.makeText(context, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Añadir equipo")
        }
    }
}

fun uploadImageAndSaveTeam(
    db: FirebaseFirestore,
    storageRef: StorageReference,
    teamName: String,
    category: String,
    imageUri: Uri,
    context: android.content.Context,
    navController: NavController
) {
    val fileName = "teams/${UUID.randomUUID()}"
    val imageRef = storageRef.child(fileName)

    imageRef.putFile(imageUri)
        .addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                saveTeamToFirestore(db, teamName, category, uri.toString(), context, navController)
            }
        }
        .addOnFailureListener {
            Toast.makeText(context, "Error al subir imagen", Toast.LENGTH_SHORT).show()
        }
}


fun saveTeamToFirestore(
    db: FirebaseFirestore,
    teamName: String,
    category: String,
    imageUrl: String,
    context: android.content.Context,
    navController: NavController
) {
    val teamData = hashMapOf(
        "nombre" to teamName,
        "categoria" to category,
        "imagenUrl" to imageUrl
    )

    db.collection("equipos").document(teamName)
        .set(teamData)
        .addOnSuccessListener {
            Toast.makeText(context, "Equipo añadido correctamente", Toast.LENGTH_SHORT).show()
            navController.navigate("home")
        }
        .addOnFailureListener {
            Toast.makeText(context, "Error al guardar equipo", Toast.LENGTH_SHORT).show()
        }
}

