import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.davidmcdeveloper.trainernotes10.R
import com.davidmcdeveloper.trainernotes10.dataclass.Jugador
import com.davidmcdeveloper.trainernotes10.navigation.Screen
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsistenciasScreen(navController: NavController, categoryName: String, db: FirebaseFirestore) {
    val fechaActual = obtenerFechaActual()
    val context = LocalContext.current
    var jugadores by remember { mutableStateOf<List<Jugador>>(emptyList()) }
    var jugadoresCargados by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var datosEditados by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = categoryName) {
        jugadores = getJugadoresByCategoriaAsistencias(db, categoryName, context)
        jugadoresCargados = true
    }

    val asistencias = remember { mutableStateMapOf<String, Boolean?>() }
    if (jugadoresCargados) {
        // Inicializar el Map de asistencias con null (no marcado) para cada jugador.
        jugadores.forEach { jugador ->
            asistencias["${jugador.nombre} ${jugador.primerApellido}"] = null
        }
    }
    // 2. Estructura de la Pantalla (Scaffold):
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
            // 3. TopBar:
            CenterAlignedTopAppBar(
                title = { Text("Asistencias") },
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
                        navController.navigate("historial/$categoryName")
                    }) {
                        Icon(
                            Icons.Filled.DateRange,
                            contentDescription = "Registro"
                        )
                    }
                    IconButton(
                        onClick = {
                            scope.launch {
                                guardarAsistenciasEnFirebase(db, asistencias, categoryName, context)
                            }
                        },
                        enabled = datosEditados
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.saveicon),
                            contentDescription = "Guardar Asistencias"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Image(
            painter = painterResource(id = R.drawable.addbackground),
            contentDescription = "Background",
            modifier = Modifier.padding(paddingValues).fillMaxSize(),
            contentScale = ContentScale.Crop)

        Column(modifier = Modifier.padding(paddingValues)

        ) {
            // 4. Header (Fecha Actual):
            Header(fechaActual)

            // 5. Lista de Jugadores (LazyColumn - Scrollable):
            if (jugadoresCargados){
                if (jugadores.isNotEmpty()) {
                    LazyColumn {
                        items(jugadores.size) { index ->
                            FilaJugador(jugador = jugadores[index], asistencias = asistencias, onAsistenciaChanged = { datosEditados = true })
                        }
                    }
                }else{
                    Text(text = "Aun no hay jugadores registrados en esta categoria")
                }
            }else{
                Text(text = "Cargando...")
            }
        }
    }
}

// 4. Header (Fecha Actual):
@Composable
fun Header(fechaActual: LocalDate) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val fechaFormateada = fechaActual.format(formatter)
    Text(text = "Asistencias de: $fechaFormateada")
}

@Composable
fun FilaJugador(jugador: Jugador, asistencias: MutableMap<String, Boolean?>, onAsistenciaChanged: () -> Unit) {
    // Estados para los botones Check y Cruz:
    var checkEnabled by remember { mutableStateOf(true) }
    var cruzEnabled by remember { mutableStateOf(true) }
    var colorCheck by remember { mutableStateOf(Color.DarkGray) }
    var colorCruz by remember { mutableStateOf(Color.DarkGray) }

    //Recuperamos los datos del map para actualizar los estados
    val asistenciaJugador = asistencias["${jugador.nombre} ${jugador.primerApellido}"]
    if(asistenciaJugador != null){
        checkEnabled = !asistenciaJugador
        cruzEnabled = asistenciaJugador
        colorCheck = if(asistenciaJugador) Color.Green else Color.DarkGray
        colorCruz = if(!asistenciaJugador) Color.Red else Color.DarkGray
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Imagen del jugador
        if (jugador.fotoUrl.isNotEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(jugador.fotoUrl),
                contentDescription = "Foto de ${jugador.nombre}",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.jugadordefault),
                contentDescription = "Foto por defecto de ${jugador.nombre}",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Información del jugador
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "${jugador.nombre} ${jugador.primerApellido}")
            Text(text = "${jugador.posicionPrimaria} / ${jugador.posicionSecundaria}")
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Botones Check y Cruz
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row {
                Button(
                    onClick = {
                        if (checkEnabled) {
                            checkEnabled = false
                            cruzEnabled = true
                            colorCheck = Color.Green
                            asistencias["${jugador.nombre} ${jugador.primerApellido}"] = true
                            onAsistenciaChanged() //Notificamos del cambio.
                        }
                    },
                    enabled = checkEnabled,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = "Asistencia",
                        tint = colorCheck
                    )
                }

                Button(
                    onClick = {
                        if (cruzEnabled) {
                            checkEnabled = true
                            cruzEnabled = false
                            colorCruz = Color.Red
                            asistencias["${jugador.nombre} ${jugador.primerApellido}"] = false
                            onAsistenciaChanged() //Notificamos del cambio.
                        }
                    },
                    enabled = cruzEnabled,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Falta",
                        tint = colorCruz
                    )
                }
            }
        }
    }
}

suspend fun getJugadoresByCategoriaAsistencias(db: FirebaseFirestore, categoryName: String, context: Context): List<Jugador> {
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

suspend fun guardarAsistenciasEnFirebase(db: FirebaseFirestore, asistencias: Map<String, Boolean?>, categoryName: String, context: Context) {
    val fechaActual = LocalDate.now().toString()
    val documentId = "$fechaActual-$categoryName"

    val asistenciaData = hashMapOf(
        "fecha" to fechaActual,
        "categoria" to categoryName,
        "jugadores" to asistencias.map { (nombre, asistencia) ->
            hashMapOf(
                "nombre" to nombre,
                "asistencia" to asistencia
            )
        }
    )

    try {
        val documentRef = db.collection("asistencias").document(documentId)
        val documentSnapshot = documentRef.get().await()

        if (documentSnapshot.exists()) {
            // Si el documento existe, lo actualizamos
            documentRef.update(asistenciaData as Map<String, Any>).await()
            Toast.makeText(context, "Asistencias actualizadas correctamente", Toast.LENGTH_SHORT).show()
        } else {
            // Si el documento no existe, lo creamos
            documentRef.set(asistenciaData).await()
            Toast.makeText(context, "Asistencias guardadas correctamente", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error al guardar asistencias", Toast.LENGTH_SHORT).show()
    }
}

fun obtenerFechaActual(): LocalDate {
    return LocalDate.now()
}