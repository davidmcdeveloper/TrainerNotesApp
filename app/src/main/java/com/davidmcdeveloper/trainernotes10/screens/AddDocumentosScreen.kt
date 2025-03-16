package com.davidmcdeveloper.trainernotes10.screens

import android.content.Intent
import android.util.Log
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.davidmcdeveloper.trainernotes10.R
import com.google.firebase.firestore.FirebaseFirestore
import java.net.MalformedURLException
import java.net.URL
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDocumentosScreen(
    navController: NavController,
    categoryName: String,
    db: FirebaseFirestore
) {
    var nombre by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var documentType by remember { mutableStateOf("Documento") } // Tipo por defecto
    val context = LocalContext.current
    var isUrlValid by remember { mutableStateOf(true) }
    var isAdding by remember { mutableStateOf(false) }

    //Opciones para los radioButton
    val options = listOf("Imagen", "Video", "Documento")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Añadir Documento") },
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
                painter = painterResource(id = R.drawable.addbackground),
                contentDescription = "Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                //TextField para introducir el nombre
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                //TextField para introducir la URL
                OutlinedTextField(
                    value = url,
                    onValueChange = {
                        url = it
                        isUrlValid = isValidUrl(url) //Validamos la url.
                    },
                    label = { Text("Enlace") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isUrlValid //Indicamos si es una url correcta.
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Sube tu documento a Drive, copia la URL y pégala aquí.", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(16.dp))
                //Radio Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    options.forEach { option ->
                        Row(
                            modifier = Modifier.selectable(
                                selected = (documentType == option),
                                onClick = {
                                    documentType = option
                                },
                                role = Role.RadioButton
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (documentType == option),
                                onClick = { documentType = option }
                            )
                            Text(
                                text = option,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                //Boton para abrir drive
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, "https://drive.google.com/".toUri())
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = " Abrir Drive")
                    Text(text = "Abrir Drive")
                }
                Spacer(modifier = Modifier.height(16.dp))
                //Boton para añadir el documento
                Button(onClick = {
                    if (nombre.isNotEmpty() && url.isNotEmpty() && isUrlValid && !isAdding) {
                        isAdding = true
                        val documentId = generateDocumentId(nombre)
                        addDocument(
                            db = db,
                            categoryName = categoryName,
                            documentId = documentId,
                            url = url,
                            documentType = documentType, // Usamos el tipo seleccionado
                            documentName = nombre, // Pasamos el nombre
                            onSuccess = {
                                Toast.makeText(
                                    context,
                                    "Documento añadido correctamente con ID: $documentId",
                                    Toast.LENGTH_SHORT
                                ).show()
                                navController.popBackStack()
                            },
                            onError = { error ->
                                Toast.makeText(
                                    context,
                                    "Error al añadir el documento: $error",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    } else {
                        Toast.makeText(context, "Introduce un nombre y una URL válida", Toast.LENGTH_SHORT)
                            .show()
                    }
                    isAdding = false
                }, enabled = !isAdding) {
                    Text("Añadir")
                }
            }
        }
    }
}
//Funcion para generar el ID del documento
fun generateDocumentId(nombre: String): String {
    val uniqueId = UUID.randomUUID().toString().substring(0, 8) // Tomamos los primeros 8 caracteres del UUID
    val sanitizedNombre = nombre.filter { it.isLetterOrDigit() }.take(15).lowercase() // Tomamos los primeros 15 caracteres del nombre
    return "${sanitizedNombre}-$uniqueId" // Ejemplo: contrato-f4a2b9d1
}

fun isValidUrl(urlString: String): Boolean {
    return try {
        URL(urlString)
        val uri = urlString.toUri()
        uri.scheme == "http" || uri.scheme == "https"
    } catch (e: MalformedURLException) {
        false
    }
}

fun addDocument(
    db: FirebaseFirestore,
    categoryName: String,
    documentId: String,
    url: String,
    documentType: String,
    documentName: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val document = hashMapOf(
        "url" to url,
        "type" to documentType,
        "name" to documentName
    )
    db.collection("categories").document(categoryName).collection("documents").document(documentId)
        .set(document)
        .addOnSuccessListener {
            Log.d("AddDocumentos", "Documento añadido correctamente")
            onSuccess()
        }
        .addOnFailureListener { e ->
            Log.w("AddDocumentos", "Error al añadir el documento", e)
            onError(e.message ?: "Error desconocido")
        }
}