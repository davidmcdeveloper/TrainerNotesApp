package com.davidmcdeveloper.trainernotes10.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.navigation.NavController
import com.davidmcdeveloper.trainernotes10.R
import com.davidmcdeveloper.trainernotes10.navigation.Screen
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentosScreen(navController: NavController, categoryName: String, db: FirebaseFirestore) {
    val context = LocalContext.current
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableIntStateOf(0) } // Variable para controlar la pestaña seleccionada

    val tabTitles = listOf("Imágenes", "Vídeos", "Documentos") // Títulos de las pestañas

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
                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar documentos") //Cambiamos el content description
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
                when (selectedTabIndex) { //Añadimos el when para mostrar el contenido
                    0 -> Text("Contenido de Imágenes") //Contenido de la pestaña 0
                    1 -> Text("Contenido de Vídeos")//Contenido de la pestaña 1
                    2 -> Text("Contenido de Documentos")//Contenido de la pestaña 2
                }
            }
            // Diálogo de confirmación
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
                            //Funcion para eliminar todos los documentos.
                            showDeleteConfirmationDialog = false //Se mantiene el cierre del dialog.
                            Toast.makeText(context, "Has pulsado eliminar todos los documentos", Toast.LENGTH_SHORT).show() //Añadimos un Toast
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
        }
    }
}