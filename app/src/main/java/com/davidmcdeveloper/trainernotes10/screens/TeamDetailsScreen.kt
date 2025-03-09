package com.davidmcdeveloper.trainernotes10.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.davidmcdeveloper.trainernotes10.navigation.Screen
import com.davidmcdeveloper.trainernotes10.utils.getEquipoCategories
import com.davidmcdeveloper.trainernotes10.utils.deleteJugadoresByCategory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDetailsScreen(navController: NavController, db: FirebaseFirestore, teamId: String) {
    val context = LocalContext.current
    var imageUrl by remember { mutableStateOf("") }
    var teamName by remember { mutableStateOf("") }
    var categories by remember { mutableStateOf<List<String>>(emptyList()) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var isCategoryLoading by remember { mutableStateOf(true) }
    var isImageLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = teamId) {
        val teamDocument = db.collection("equipos").document(teamId).get().await()
        if (teamDocument.exists()) {
            imageUrl = teamDocument.getString("imagenUrl") ?: ""
            teamName = teamDocument.getString("nombre") ?: ""
        } else {
            Toast.makeText(
                context,
                "No se han encontrado los detalles del equipo",
                Toast.LENGTH_SHORT
            ).show()
        }
        isLoading = false
    }
    LaunchedEffect(key1 = teamId) {
        db.collection("equipos").document(teamId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
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
                    categories = emptyList()
                }
                isCategoryLoading = false
            }
    }

    Scaffold(
        floatingActionButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FloatingActionButton(onClick = { navController.popBackStack() }, //Se ha cambiado.
                    modifier = Modifier
                        .padding(start = 30.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver al listado de equipos")
                }

                FloatingActionButton(onClick = {
                    navController.navigate(Screen.AddCategory.createRoute(teamId))
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "Añadir categoría")
                }
            }

        },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(teamName) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { //Se ha cambiado.
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver a la lista de equipos"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteConfirmationDialog = true }) {
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
            if (isLoading || isDeleting || isCategoryLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Escudo del equipo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            onLoading = {
                                isImageLoading = true
                            },
                            onSuccess = {
                                isImageLoading = false
                            },
                            onError = {
                                isImageLoading = false
                            }
                        )
                        if (isImageLoading) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    if (categories.isNotEmpty()) {
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(categories) { categoryName ->
                                Button(
                                    onClick = {
                                        navController.navigate(
                                            Screen.CategoryHome.createRoute(
                                                categoryName
                                            )
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(text = categoryName)
                                }
                            }
                        }
                    } else {
                        Text(text = "Aun no hay categorías creadas para este equipo")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            // Diálogo de confirmación
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
                            scope.launch {
                                val teamCategories =
                                    getEquipoCategories(db, teamId) //Obtenemos las categorias del equipo.
                                for (categoryName in teamCategories) {
                                    deleteJugadoresByCategory(
                                        db,
                                        categoryName,
                                        context
                                    ) //Borramos los jugadores de cada categoría.
                                }
                                val teamRef = db.collection("equipos").document(teamId) //Borramos las categorias.
                                teamRef.collection("categorias").get().await().documents.forEach {
                                    it.reference.delete()
                                }

                                val storageRef =
                                    FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
                                storageRef.delete().await() //Eliminamos la foto del equipo.
                                teamRef.delete().await() //Eliminamos el equipo.
                                Toast.makeText(
                                    context,
                                    "Equipo y categorías eliminados correctamente",
                                    Toast.LENGTH_SHORT
                                ).show()
                                navController.popBackStack() //Se ha cambiado.
                            }
                            showDeleteConfirmationDialog = false
                        }) {
                            if (isDeleting) {
                                CircularProgressIndicator()
                            } else {
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