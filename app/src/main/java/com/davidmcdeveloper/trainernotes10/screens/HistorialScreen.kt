package com.davidmcdeveloper.trainernotes10.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.davidmcdeveloper.trainernotes10.dataclass.Asistencia
import com.davidmcdeveloper.trainernotes10.navigation.Screen
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(navController: NavController, categoryName: String, db: FirebaseFirestore) {
    val context = LocalContext.current
    var historial by remember { mutableStateOf<List<Asistencia>>(emptyList()) }
    var historialCargado by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = categoryName) {
        historial = getHistorialAsistencias(db, categoryName, context)
        historialCargado = true
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
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Historial de Asistencias") },
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
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            LazyColumn {
                if (historialCargado) {
                    if (historial.isNotEmpty()) {
                        items(historial) { asistencia ->
                            ItemHistorial(asistencia = asistencia)
                        }
                    } else {
                        item {
                            Text(text = "No hay datos aun de esta categoria")
                        }
                    }
                } else {
                    item {
                        Text(text = "Cargando...")
                    }
                }
            }
        }
    }
}

@Composable
fun ItemHistorial(asistencia: Asistencia){
    var showDetails by remember { mutableStateOf(false) }
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val fechaFormateada = LocalDate.parse(asistencia.fecha).format(formatter)
    val diaDeLaSemana = LocalDate.parse(asistencia.fecha).getDayOfWeek().getDisplayName(TextStyle.FULL, Locale("es", "ES"))
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { showDetails = !showDetails }
    ){
        Column (modifier = Modifier.padding(8.dp)){
            Text(text = "$fechaFormateada - $diaDeLaSemana")
            if(showDetails){
                asistencia.jugadores.forEach { jugador ->
                    val nombre = jugador["nombre"] as String? ?: ""
                    val asistenciaJugador = jugador["asistencia"] as Boolean? ?: false
                    Row (
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(4.dp)
                    ){
                        Text(text = nombre, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        if(asistenciaJugador){
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = "Presente",
                                tint = Color.Green
                            )
                        }else{
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Ausente",
                                tint = Color.Red
                            )
                        }

                    }
                }
            }
        }
    }
}

//Funcion para recuperar los datos.
suspend fun getHistorialAsistencias(db: FirebaseFirestore, categoryName: String, context: Context): List<Asistencia> {
    return try {
        val querySnapshot = db.collection("asistencias")
            .whereEqualTo("categoria", categoryName)
            .get()
            .await()

        val asistenciasList = mutableListOf<Asistencia>()
        for (document in querySnapshot.documents) {
            val fecha = document.getString("fecha") ?: ""
            val categoria = document.getString("categoria") ?: ""
            //Aquí hacemos el cambio
            val jugadores = document.get("jugadores") as? List<Map<String, Any>> ?: emptyList()
            val asistenciaData = Asistencia(fecha, categoria, jugadores)
            asistenciasList.add(asistenciaData)
        }
        //Ordenamos los datos por fecha.
        asistenciasList.sortedByDescending { LocalDate.parse(it.fecha) }
    } catch (e: Exception) {
        Toast.makeText(context, "Error al obtener historial de asistencias", Toast.LENGTH_SHORT).show()
        emptyList()
    }
}