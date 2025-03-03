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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.davidmcdeveloper.trainernotes10.navigation.Screen
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDetailsScreen(
    navController: NavController,
    db: FirebaseFirestore,
    teamId: String
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var isDeleting by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) } // Estado para el diálogo
    var imageUrl by remember { mutableStateOf("") }
    var teamName by remember { mutableStateOf("")}
    var categories by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(key1 = teamId) {
        val teamDocument = db.collection("equipos").document(teamId).get().await()
        if (teamDocument.exists()) {
            imageUrl = teamDocument.getString("imagenUrl") ?: ""
            teamName = teamDocument.getString("nombre") ?: "" //Obtenemos el nombre.
        } else {
            Toast.makeText(context, "No se han encontrado los detalles del equipo", Toast.LENGTH_SHORT).show()
        }
        isLoading = false
    }
    LaunchedEffect(key1 = teamId) {
        db.collection("equipos").document(teamId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("DetailsScreen", "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val data = snapshot.data
                    if (data != null) {
                        val categoriasList = data["categorias"]
                        if (categoriasList is List<*> && categoriasList.all { it is String }) {
                            categories = categoriasList as List<String>
                        } else {
                            categories = emptyList()
                        }
                    }
                } else {
                    Log.d("DetailsScreen", "Current data: null")
                    categories = emptyList()
                }
            }
    }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate(Screen.AddCategory.createRoute(teamId)) //Pasamos el ID.
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Añadir categoría")
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(teamName) }, //Mostramos el nombre.
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
                },
                actions = {
                    IconButton(onClick = {showDeleteConfirmationDialog = true}) {
                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar equipo")
                    }
                }
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (isLoading || isDeleting) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Escudo del equipo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(categories) { categoryName -> //Ahora recibimos el nombre.
                            Button(
                                onClick = {
                                    navController.navigate(Screen.CategoryHome.createRoute(categoryName)) //Pasamos el nombre
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(text = categoryName) //Mostramos el nombre.
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

            }
            // Dialogo de confirmacion
            if (showDeleteConfirmationDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmationDialog = false },
                    title = { Text("Eliminar Equipo") },
                    text = {
                        Column {
                            Text("¿Estás seguro de que quieres eliminar el equipo '$teamName' y todo su contenido?")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Esta acción es irreversible.", fontWeight = FontWeight.Bold)
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            isDeleting = true
                            val teamRef = db.collection("equipos").document(teamId)
                            val storageRef = FirebaseStorage.getInstance()
                                .getReferenceFromUrl(imageUrl)
                            val categoriesRef = teamRef.collection("categorias")

                            categoriesRef.get()
                                .addOnSuccessListener { querySnapshot ->
                                    val deleteCategoryTasks =
                                        mutableListOf<com.google.android.gms.tasks.Task<Void>>()

                                    for (document in querySnapshot.documents) {
                                        deleteCategoryTasks.add(document.reference.delete())
                                    }

                                    com.google.android.gms.tasks.Tasks.whenAll(deleteCategoryTasks)
                                        .addOnSuccessListener {
                                            storageRef.delete()
                                                .addOnSuccessListener {
                                                    teamRef.delete()
                                                        .addOnSuccessListener {
                                                            Toast.makeText(
                                                                context,
                                                                "Equipo y categorías eliminados correctamente",
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
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(
                                                context,
                                                "Error al eliminar las categorías del equipo",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        context,
                                        "Error al obtener las categorías del equipo",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    isDeleting = false
                                }
                                .addOnCompleteListener { isDeleting = false }
                            showDeleteConfirmationDialog = false
                        }) {
                            if (isDeleting){
                                CircularProgressIndicator()
                            }else{
                                Text("Eliminar")
                            }
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
        }
    }
}