package com.davidmcdeveloper.trainernotes10.screens

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.davidmcdeveloper.trainernotes10.R
import com.davidmcdeveloper.trainernotes10.dataclass.Document
import com.davidmcdeveloper.trainernotes10.navigation.Screen
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentosScreen(navController: NavController, categoryName: String, db: FirebaseFirestore) {
    val context = LocalContext.current
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var showDeleteIndividualConfirmationDialog by remember { mutableStateOf(false) }
    var selectedDocumentToDelete by remember { mutableStateOf<Document?>(null) } // Nuevo estado para el documento a eliminar
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var documents by remember { mutableStateOf<List<Document>>(emptyList()) } // Lista de documentos

    val tabTitles = listOf("Imágenes", "Vídeos", "Documentos") // Títulos de las pestañas

    //Efecto secundario para obtener los datos de Firestore
    LaunchedEffect(key1 = categoryName) {
        val querySnapshot: QuerySnapshot =
            db.collection("categories").document(categoryName).collection("documents").get().await()
        documents = querySnapshot.toDocuments()
        Log.d("DocumentosScreen", "Documents fetched: ${documents.size}") // Comprobamos cuantos documentos hemos recogido.
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
                FloatingActionButton(onClick = {
                    navController.navigate(Screen.AddDocumentos.createRoute(categoryName))
                },
                    modifier = Modifier
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Añadir nuevo documento")
                }
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Documentos $categoryName") },
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
                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar documentos")
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
            Column(modifier = Modifier.fillMaxSize()) { //Añadimos el Column principal
                TabRow(selectedTabIndex = selectedTabIndex) { //Añadimos el TabRow
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            text = { Text(title) },
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index }
                        )
                    }
                }
                // Mostrar el contenido según la pestaña seleccionada
                val filteredDocuments = when (selectedTabIndex) {
                    0 -> documents.filter { it.type == "Imagen" }
                    1 -> documents.filter { it.type == "Video" }
                    2 -> documents.filter { it.type == "Documento" }
                    else -> emptyList()
                }
                Log.d("DocumentosScreen", "Filtered documents: ${filteredDocuments.size}") // Comprobamos cuantos documentos filtrados tenemos.

                LazyColumn {
                    items(filteredDocuments) { document ->
                        var expanded by remember { mutableStateOf(false) }
                        Card(modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, document.url.toUri())
                                context.startActivity(intent)
                            }) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(55.dp)
                                    .padding(13.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                when (document.type) {
                                    "Imagen" -> Icon(
                                        painter = painterResource(id = R.drawable.iconimg), // Usamos un icono de imagen
                                        contentDescription = "Imagen",
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .align(Alignment.CenterVertically)
                                    )
                                    "Video" -> Icon(
                                        Icons.Filled.PlayArrow, // Usamos un icono de video
                                        contentDescription = "Video",
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .align(Alignment.CenterVertically)
                                    )
                                    "Documento" -> Icon(
                                        painter = painterResource(id = R.drawable.documenticon), // Usamos un icono de documento
                                        contentDescription = "Documento",
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .align(Alignment.CenterVertically)
                                    )
                                }
                                Text(text = document.name,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 16.dp))

                                // Menu de 3 puntos
                                Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                                    IconButton(onClick = { expanded = true }) {
                                        Icon(
                                            Icons.Filled.MoreVert,
                                            contentDescription = "Opciones"
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Eliminar") },
                                            onClick = {
                                                selectedDocumentToDelete = document
                                                showDeleteIndividualConfirmationDialog = true
                                                expanded = false // Cierra el menú
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // Diálogo de confirmación eliminar todos los documentos
            if (showDeleteConfirmationDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmationDialog = false },
                    title = { Text("Eliminar Documentos") }, //Cambiamos el titulo
                    text = {
                        Column {
                            Text("¿Estás seguro de que quieres eliminar todos los documentos de la categoría '$categoryName'?")//Cambiamos el texto
                            Spacer(modifier = Modifier.padding(8.dp))
                            Text("Esta acción es irreversible.", fontWeight = FontWeight.Bold)
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            // Llamamos a la funcion deleteDocumentsFromCategory para eliminar los documentos
                            CoroutineScope(Dispatchers.Main).launch {
                                val success = withContext(Dispatchers.IO) {
                                    deleteDocumentsFromCategory(categoryName, db)
                                }
                                if (success) {
                                    Toast.makeText(context, "Documentos eliminados correctamente", Toast.LENGTH_SHORT).show()
                                    val querySnapshot: QuerySnapshot =
                                        db.collection("categories").document(categoryName).collection("documents").get().await()
                                    documents = querySnapshot.toDocuments()
                                } else {
                                    Toast.makeText(context, "Error al eliminar documentos", Toast.LENGTH_SHORT).show()
                                }
                                showDeleteConfirmationDialog = false // Cerramos el dialogo
                            }

                        }) {
                            Text("Eliminar")
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
            // Diálogo de confirmación para eliminar individual
            if (showDeleteIndividualConfirmationDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteIndividualConfirmationDialog = false },
                    title = { Text("Eliminar Documento") },
                    text = { Text("¿Estás seguro de que quieres eliminar '${selectedDocumentToDelete?.name}'?") },
                    confirmButton = {
                        TextButton(onClick = {
                            selectedDocumentToDelete?.let { document ->
                                CoroutineScope(Dispatchers.Main).launch {
                                    val success = withContext(Dispatchers.IO) {
                                        deleteDocument(categoryName, document, db)
                                    }
                                    if (success) {
                                        Toast.makeText(context, "Documento '${document.name}' eliminado correctamente", Toast.LENGTH_SHORT).show()
                                        val querySnapshot: QuerySnapshot =
                                            db.collection("categories").document(categoryName).collection("documents").get().await()
                                        documents = querySnapshot.toDocuments()
                                    } else {
                                        Toast.makeText(context, "Error al eliminar documento", Toast.LENGTH_SHORT).show()
                                    }
                                    showDeleteIndividualConfirmationDialog = false
                                }
                            }
                        }) {
                            Text("Eliminar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteIndividualConfirmationDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}

//Extension para mapear los datos de firestore.
fun QuerySnapshot.toDocuments(): List<Document> {
    val documentsList = mutableListOf<Document>()
    this.documents.forEach { document ->
        val id = document.id
        val url = document.getString("url")
        val type = document.getString("type")
        val name = document.getString("name") ?: "" // Recuperamos el nombre
        Log.d("toDocuments", "Document: id=$id, url=$url, type=$type, name=$name") // Imprimimos los datos de cada documento.

        if (url != null && type != null) {
            documentsList.add(Document(id, url, type,name))
        }
    }
    return documentsList
}
// Funcion para eliminar los documentos de firestore
suspend fun deleteDocumentsFromCategory(categoryName: String, db: FirebaseFirestore): Boolean {
    return try {
        val querySnapshot = db.collection("categories").document(categoryName).collection("documents").get().await()
        for (document in querySnapshot.documents) {
            document.reference.delete().await()
        }
        true
    } catch (e: Exception) {
        Log.e("deleteDocumentsFromCategory", "Error al eliminar documentos: ${e.message}")
        false
    }
}

// Funcion para eliminar un documento especifico de firestore
suspend fun deleteDocument(categoryName: String, document: Document, db: FirebaseFirestore): Boolean {
    return try {
        db.collection("categories").document(categoryName).collection("documents").document(document.id).delete().await()
        true
    } catch (e: Exception) {
        Log.e("deleteDocument", "Error al eliminar documento: ${e.message}")
        false
    }
}