package com.davidmcdeveloper.trainernotes10.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.davidmcdeveloper.trainernotes10.dataclass.Jugador
import com.davidmcdeveloper.trainernotes10.navigation.Screen
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JugadoresScreen(navController: NavController, categoryName: String, db: FirebaseFirestore) {
    val context = LocalContext.current
    var imageUrl by remember { mutableStateOf("") }
    var teamName by remember { mutableStateOf("") }
    var jugadores by remember { mutableStateOf<List<Jugador>>(emptyList()) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = categoryName) {
        teamName = getEquipoName(db, categoryName, context)
        imageUrl = getEquipoImageUrl(db, teamName, context)
        jugadores = getJugadoresByCategoria(db, categoryName, context)
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

                FloatingActionButton(onClick = { navController.navigate(Screen.AddJugador.createRoute(categoryName)) }) {
                    Icon(Icons.Filled.Add, contentDescription = "Añadir Jugador")
                }

            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Jugadores") },
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
                    IconButton(onClick = {showDeleteConfirmationDialog = true
                    }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar Jugadores")
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
            Column(
                modifier = Modifier.fillMaxSize(),
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
                if (jugadores.isNotEmpty()) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) { //Modificacion
                        items(jugadores) { jugador ->
                            JugadorCard(jugador = jugador)
                        }
                    }
                } else {
                    Text(text = "Aun no hay jugadores registrados")
                }
            }
            // Diálogo de confirmación
            if (showDeleteConfirmationDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmationDialog = false },
                    title = { Text("Eliminar Jugadores") },
                    text = {
                        Column {
                            Text("¿Estás seguro de que quieres eliminar todos los jugadores de la categoría '$categoryName'?")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Esta acción es irreversible.", fontWeight = FontWeight.Bold)
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            isDeleting = true
                            scope.launch {
                                deleteJugadoresByCategory(db, categoryName, context)
                                isDeleting = false
                            }
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
                            showDeleteConfirmationDialog = false // Ocultar el diálogo
                        }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun JugadorCard(jugador: Jugador) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)) { //Modificado
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (jugador.fotoUrl.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(jugador.fotoUrl),
                    contentDescription = "Foto del jugador",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = rememberAsyncImagePainter(com.davidmcdeveloper.trainernotes10.R.drawable.defaultteam),
                    contentDescription = "Foto Jugador",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.padding(16.dp))
            Column() {
                Text(text = "${jugador.nombre} ${jugador.primerApellido}")
                Text(text = "Posiciones: ${jugador.posicionPrimaria}, ${jugador.posicionSecundaria}")
            }
        }
    }

}

suspend fun getEquipoName(db: FirebaseFirestore, categoryName: String, context: Context): String {
    val categoryRef = db.collection("equipos")
        .whereArrayContains("categorias", categoryName)
    val querySnapshot = categoryRef.get().await()
    if (!querySnapshot.isEmpty) {
        return querySnapshot.documents[0].id
    } else {
        Toast.makeText(
            context,
            "No se ha encontrado el equipo al que pertenece la categoría",
            Toast.LENGTH_SHORT
        ).show()
        return ""
    }
}

suspend fun getEquipoImageUrl(db: FirebaseFirestore, teamName: String, context: Context): String {
    if (teamName.isNotEmpty()) {
        val teamDocument = db.collection("equipos").document(teamName).get().await()
        if (teamDocument.exists()) {
            return teamDocument.getString("imagenUrl") ?: ""
        } else {
            Toast.makeText(
                context,
                "No se ha encontrado la imagen del equipo",
                Toast.LENGTH_SHORT
            ).show()
            return ""
        }
    }
    return ""
}

suspend fun getJugadoresByCategoria(db: FirebaseFirestore, categoryName: String, context: Context): List<Jugador> {
    val jugadoresRef = db.collection("jugadores").whereEqualTo("categoria", categoryName)
    return try {
        val querySnapshot = jugadoresRef.get().await()
        val jugadoresList = mutableListOf<Jugador>()
        for (document in querySnapshot.documents) {
            val jugador = Jugador(
                id = document.id,
                nombre = document.getString("nombre") ?: "",
                primerApellido = document.getString("primerApellido") ?: "",
                posicionPrimaria = document.getString("posicionPrimaria") ?: "",
                posicionSecundaria = document.getString("posicionSecundaria") ?: "",
                peso = document.getString("peso") ?: "",
                categoria = document.getString("categoria") ?: "",
                fotoUrl = document.getString("fotoUrl") ?: "",
                altura = document.getString("altura") ?: "",
                fechaNacimiento = document.getString("fechaNacimiento") ?: ""
            )
            jugadoresList.add(jugador)
        }
        jugadoresList
    } catch (e: Exception) {
        Toast.makeText(context, "Error al obtener jugadores", Toast.LENGTH_SHORT).show()
        emptyList()
    }
}

suspend fun deleteJugadoresByCategory(db: FirebaseFirestore, categoryName: String, context: Context) {
    val jugadoresRef = db.collection("jugadores").whereEqualTo("categoria", categoryName)
    try {
        val querySnapshot = jugadoresRef.get().await()
        for (document in querySnapshot.documents) {
            db.collection("jugadores").document(document.id).delete().await()
        }
        Toast.makeText(context, "Jugadores eliminados correctamente", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Error al eliminar jugadores", Toast.LENGTH_SHORT).show()
    }
}

fun LoadBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(
                context.contentResolver,
                uri
            )
        } else {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}