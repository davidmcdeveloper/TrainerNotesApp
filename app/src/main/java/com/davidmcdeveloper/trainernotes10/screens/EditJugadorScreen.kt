package com.davidmcdeveloper.trainernotes10.screens

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.davidmcdeveloper.trainernotes10.R
import com.davidmcdeveloper.trainernotes10.dataclass.Jugador
import com.davidmcdeveloper.trainernotes10.utils.convertMillisToDate
import com.davidmcdeveloper.trainernotes10.utils.deleteJugadorImage
import com.davidmcdeveloper.trainernotes10.utils.uploadImageToFirebaseStorage
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditJugadorScreen(navController: NavController, db: FirebaseFirestore, jugadorId: String) {
    val context = LocalContext.current
    //Variables del jugador
    var jugador by remember { mutableStateOf<Jugador?>(null) }
    var isLoading by remember { mutableStateOf(true) } // Para saber si el jugador esta cargando.
    var isUpdating by remember { mutableStateOf(false) }
    var nombre by remember { mutableStateOf("") }
    var primerApellido by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    var fotoUrl by remember { mutableStateOf("") }
    //ExposedDropdownMenuBox
    //Posicion Primaria
    var posicionPrimariaOptions =
        listOf("1ª Línea", "2ª Línea", "Talona", "Flanker", "Ocho", "Medio Melé", "Apertura", "Ala Interno", "Ala Externo", "Primer Centro", "Segundo Centro", "Zaguero")
    var posicionPrimariaExpanded by remember { mutableStateOf(false) }
    var posicionPrimariaSelectedText by remember { mutableStateOf("") }
    //Posicion Secundaria
    var posicionSecundariaOptions =
        listOf("1ª Línea", "2ª Línea", "Talona", "Flanker", "Ocho", "Medio Melé", "Apertura", "Ala Interno", "Ala Externo", "Primer Centro", "Segundo Centro", "Zaguero")
    var posicionSecundariaExpanded by remember { mutableStateOf(false) }
    var posicionSecundariaSelectedText by remember { mutableStateOf("") }
    //Peso
    var pesoOptions =
        (50..200).map { it.toString() + " kg" } // Rango de 50 a 200 kg
    var pesoExpanded by remember { mutableStateOf(false) }
    var pesoSelectedText by remember { mutableStateOf("") }
    //Altura
    var alturaOptions =
        (110..220).map { (it.toFloat() / 100).toString() + " m" } // Rango de 1.10 a 2.20 m
    var alturaExpanded by remember { mutableStateOf(false) }
    var alturaSelectedText by remember { mutableStateOf("") }
    //Fecha de Nacimiento
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    //Image
    var jugadorImageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        jugadorImageUri = uri
    }
    //Con el LaunchedEffect vamos a obtener los datos del jugador.
    LaunchedEffect(key1 = jugadorId) {
        Log.d("EditJugadorScreen", "LaunchedEffect: Iniciado para jugadorId: $jugadorId")
        jugador = getJugadorById(db, jugadorId, context)
        jugador?.let {
            nombre = it.nombre
            primerApellido = it.primerApellido
            fechaNacimiento = it.fechaNacimiento
            posicionPrimariaSelectedText = it.posicionPrimaria
            posicionSecundariaSelectedText = it.posicionSecundaria
            pesoSelectedText = it.peso
            alturaSelectedText = it.altura
            fotoUrl = it.fotoUrl //Recogemos la foto.
            Log.d("EditJugadorScreen", "LaunchedEffect: Foto recuperada de Firestore: $fotoUrl")
        }
        isLoading = false
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { //Cogemos el millis
                        fechaNacimiento = convertMillisToDate(it)
                    }
                    showDatePicker = false
                }) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Editar Jugador") },
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
            //Dentro del Scaffold
if (!isLoading) { // Si no está cargando, mostramos el contenido.
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        //Añadir Foto
                        Spacer(modifier = Modifier.height(8.dp)) //Reducimos el spacer.
                        //AÑADIMOS BOX PARA SUPERPONER ELEMENTOS
                        Box(
                            modifier = Modifier
                                .size(100.dp) //Reducimos el tamaño.
                        ) {
                            //IMAGEN DEL JUGADOR
                            if (jugadorImageUri != null) {
                                Log.d("EditJugadorScreen", "Image: Mostrando nueva imagen. URI: $jugadorImageUri")
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(jugadorImageUri)
                                            .crossfade(true)
                                            .build()
                                    ),
                                    contentDescription = "Foto Jugador",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                if (fotoUrl.isNotEmpty()) {
                                    Log.d("EditJugadorScreen", "Image: Mostrando imagen antigua. URL: $fotoUrl")
                                    Image(
                                        painter = rememberAsyncImagePainter(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(fotoUrl)
                                                .crossfade(true)
                                                .build()
                                        ),
                                        contentDescription = "Foto Jugador",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {//Mostramos la foto por defecto
                                    Log.d("EditJugadorScreen", "Image: Mostrando imagen por defecto.")
                                    Image(
                                        painter = painterResource(id = R.drawable.jugadordefault),
                                        contentDescription = "Añadir Foto",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .alpha(0.8f),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                            //BOTON ADD
                            Button(
                                onClick = { launcher.launch("image/*") },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .align(Alignment.BottomEnd)
                                    .alpha(0.0f),
                            ) {
                                Icon(
                                    Icons.Filled.AddCircle,
                                    contentDescription = "Añadir imagen",
                                    tint = Color.Black,
                                )
                            }
                        }
                        //Formulario
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            OutlinedTextField(
                                value = nombre,
                                onValueChange = { nombre = it },
                                label = { Text("Nombre") },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(8.dp) //Reducimos el padding.
                            )
                            OutlinedTextField(
                                value = primerApellido,
                                onValueChange = { primerApellido = it },
                                label = { Text("Primer Apellido") },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(8.dp) //Reducimos el padding.
                            )
                        }
                        //POSICION PRIMARIA Y SECUNDARIA EN FILA
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            //Posicion Primaria
                            ExposedDropdownMenuBox(
                                expanded = posicionPrimariaExpanded,
                                onExpandedChange = { posicionPrimariaExpanded = !posicionPrimariaExpanded },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(8.dp) //Reducimos el padding.
                            ) {
                                OutlinedTextField(
                                    value = posicionPrimariaSelectedText,
                                    onValueChange = { posicionPrimariaSelectedText = it },
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = posicionPrimariaExpanded) },
                                    modifier = Modifier.menuAnchor(),
                                    label = { Text("1ª Posición") }
                                )
                                //Este es el código del desplegable
                                DropdownMenu(
                                    expanded = posicionPrimariaExpanded,
                                    onDismissRequest = { posicionPrimariaExpanded = false },
                                    modifier = Modifier.exposedDropdownSize()
                                ) {
                                    posicionPrimariaOptions.forEach { item ->
                                        DropdownMenuItem(
                                            text = { Text(text = item) },
                                            onClick = {
                                                posicionPrimariaSelectedText = item
                                                posicionPrimariaExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            //Posicion Secundaria
                            ExposedDropdownMenuBox(
                                expanded = posicionSecundariaExpanded,
                                onExpandedChange = { posicionSecundariaExpanded = !posicionSecundariaExpanded },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(8.dp) //Reducimos el padding.
                            ) {
                                OutlinedTextField(
                                    value = posicionSecundariaSelectedText,
                                    onValueChange = { posicionSecundariaSelectedText = it },
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = posicionSecundariaExpanded) },
                                    modifier = Modifier.menuAnchor(),
                                    label = { Text("2ª Posición") }
                                )
                                //Este es el código del desplegable
                                DropdownMenu(
                                    expanded = posicionSecundariaExpanded,
                                    onDismissRequest = { posicionSecundariaExpanded = false },
                                    modifier = Modifier.exposedDropdownSize()
                                ) {
                                    posicionSecundariaOptions.forEach { item ->
                                        DropdownMenuItem(
                                            text = { Text(text = item) },
                                            onClick = {
                                                posicionSecundariaSelectedText = item
                                                posicionSecundariaExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        //Peso y Altura
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            //Peso
                            ExposedDropdownMenuBox(
                                expanded = pesoExpanded,
                                onExpandedChange = { pesoExpanded = !pesoExpanded },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(8.dp) //Reducimos el padding.
                            ) {
                                OutlinedTextField(
                                    value = pesoSelectedText,
                                    onValueChange = { pesoSelectedText = it },
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = pesoExpanded) },
                                    modifier = Modifier.menuAnchor(),
                                    label = { Text("Peso") }
                                )
                                //Este es el código del desplegable
                                DropdownMenu(
                                    expanded = pesoExpanded,
                                    onDismissRequest = { pesoExpanded = false },
                                    modifier = Modifier.exposedDropdownSize()
                                ) {
                                    pesoOptions.forEach { item ->
                                        DropdownMenuItem(
                                            text = { Text(text = item) },
                                            onClick = {
                                                pesoSelectedText = item
                                                pesoExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            //Altura
                            ExposedDropdownMenuBox(
                                expanded = alturaExpanded,
                                onExpandedChange = { alturaExpanded = !alturaExpanded },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(8.dp) //Reducimos el padding.
                            ) {
                                OutlinedTextField(
                                    value = alturaSelectedText,
                                    onValueChange = { alturaSelectedText = it },
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = alturaExpanded) },
                                    modifier = Modifier.menuAnchor(),
                                    label = { Text("Altura") }
                                )
                                //Este es el código del desplegable
                                DropdownMenu(
                                    expanded = alturaExpanded,
                                    onDismissRequest = { alturaExpanded = false },
                                    modifier = Modifier.exposedDropdownSize()
                                ) {
                                    alturaOptions.forEach { item ->
                                        DropdownMenuItem(
                                            text = { Text(text = item) },
                                            onClick = {
                                                alturaSelectedText = item
                                                alturaExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        //Fecha de Nacimiento
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            OutlinedTextField(
                                value = fechaNacimiento,
                                onValueChange = { },
                                readOnly = true,
                                modifier = Modifier.clickable { showDatePicker = true },
                                label = { Text("Fecha de Nacimiento") },
                                trailingIcon = {
                                    Icon(Icons.Filled.DateRange, contentDescription = "Seleccionar fecha")
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    Log.d("EditJugadorScreen", "onClick: Inicio")
                                    isUpdating = true // Activamos el indicador de actualizando
                                    Log.d("EditJugadorScreen", "onClick: isUpdating activado")

                                    // Comprobamos los datos antes de subir nada.
                                    Log.d("EditJugadorScreen", "onClick: Comprobando campos...")
                                    if (nombre.isEmpty() || primerApellido.isEmpty() || posicionPrimariaSelectedText.isEmpty() || posicionSecundariaSelectedText.isEmpty() || pesoSelectedText.isEmpty() || alturaSelectedText.isEmpty()) {
                                        Log.d("EditJugadorScreen", "onClick: Faltan campos")
                                        snackbarHostState.showSnackbar("Debes rellenar todos los campos")
                                    } else {
                                        Log.d("EditJugadorScreen", "onClick: Todos los campos rellenos")

                                        //Declaramos la variable para usarla en ambos casos.
                                        var jugadorImageUrl = fotoUrl
                                        //Si la jugadorImageUri no es nula, subimos la foto a Firebase
                                        if (jugadorImageUri != null) {
                                            Log.d("EditJugadorScreen", "onClick: Subiendo nueva imagen...")
                                            try {
                                                jugadorImageUrl = uploadImageToFirebaseStorage(
                                                    jugadorImageUri!!,
                                                    jugadorId,
                                                    context
                                                )
                                                Log.d(
                                                    "EditJugadorScreen",
                                                    "onClick: Imagen subida correctamente. Nueva URL: $jugadorImageUrl"
                                                )
                                            } catch (e: Exception) {
                                                Log.e("EditJugadorScreen", "onClick: Error al subir la imagen", e)
                                                snackbarHostState.showSnackbar("Error al subir la imagen")
                                                jugadorImageUrl = ""
                                                jugadorImageUri = null
                                            }
                                        } else {
                                            Log.d("EditJugadorScreen", "onClick: No hay nueva imagen, se mantiene la anterior")
                                        }
                                        //Comprobamos si la imagen se ha subido correctamente (o si no se ha subido ninguna, que conserve la antigua)
                                        Log.d("EditJugadorScreen", "onClick: Comprobando si la imagen se ha subido...")
                                        if (jugadorImageUrl.isNotEmpty()){
                                            Log.d("EditJugadorScreen", "onClick: Imagen correcta")
                                            //Aqui es donde actualizamos la funcion
                                            Log.d("EditJugadorScreen", "onClick: Actualizando datos en Firestore...")
                                            try {
                                                updateJugadorInFirestore(
                                                    db,
                                                    jugadorId,
                                                    nombre,
                                                    primerApellido,
                                                    posicionPrimariaSelectedText,
                                                    posicionSecundariaSelectedText,
                                                    pesoSelectedText,
                                                    alturaSelectedText,
                                                    fechaNacimiento,
                                                    jugadorImageUrl,
                                                    context,
                                                    fotoUrl //Añadimos la url antigua
                                                )
                                                //Hemos sacado el cambio de pantalla, para realizarlo mas adelante.
                                            } catch (e: Exception) {
                                                Log.e("EditJugadorScreen", "onClick: Error al actualizar el jugador", e)
                                                snackbarHostState.showSnackbar("Error al actualizar el jugador")
                                            } finally {
                                                Log.d("EditJugadorScreen", "onClick: Pasando por finally...")
                                                isUpdating = false
                                                //Aqui es donde vamos a poner el cambio de pantalla
                                                navController.popBackStack()
                                            }
                                        } else {
                                            Log.e("EditJugadorScreen", "onClick: Error al actualizar el jugador, no se ha subido la imagen")
                                            snackbarHostState.showSnackbar("Error al actualizar el jugador, no se ha subido la imagen")
                                        }
                                    }

                                    Log.d("EditJugadorScreen", "onClick: Fin")
                                }
                            },
                            enabled = !isUpdating // Desactivamos el botón cuando esté actualizando
                        ) {
                            if (isUpdating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Text(text = "Actualizar datos")
                            }
                        }


                    }
                }
            } else {
                Text(text = "Cargando jugador...")
            }
        }
    }
}


//Funcion para actualizar un jugador en Firestore
suspend fun updateJugadorInFirestore(
    db: FirebaseFirestore,
    id: String,
    nombre: String,
    primerApellido: String,
    posicionPrimaria: String,
    posicionSecundaria: String,
    peso: String,
    altura: String,
    fechaNacimiento: String,
    jugadorImageUrl: String,
    context: Context,
    oldImageUrl: String //Añadimos la url antigua
) {
    Log.d("EditJugadorScreen", "updateJugadorInFirestore: Inicio. JugadorId: $id, nueva URL: $jugadorImageUrl, antigua URL: $oldImageUrl")
    val jugador = hashMapOf(
        "nombre" to nombre,
        "primerApellido" to primerApellido,
        "fechaNacimiento" to fechaNacimiento,
        "posicionPrimaria" to posicionPrimaria,
        "posicionSecundaria" to posicionSecundaria,
        "peso" to peso,
        "altura" to altura,
        "fotoUrl" to jugadorImageUrl
    )
    try {
        Log.d("EditJugadorScreen", "updateJugadorInFirestore: Actualizando datos...")
        db.collection("jugadores").document(id).update(jugador as Map<String, Any>).await()
        Log.d("EditJugadorScreen", "updateJugadorInFirestore: Datos actualizados")
        //Si hay una imagen antigua y es diferente a la nueva, la eliminamos.
        if (oldImageUrl.isNotEmpty() && oldImageUrl != jugadorImageUrl) {
            Log.d("EditJugadorScreen", "updateJugadorInFirestore: Eliminando imagen antigua...")
            deleteJugadorImage(oldImageUrl, context)
            Log.d("EditJugadorScreen", "updateJugadorInFirestore: Imagen antigua eliminada")
        } else {
            Log.d("EditJugadorScreen", "updateJugadorInFirestore: No hay imagen antigua o es igual a la nueva")
        }
        Toast.makeText(context, "Jugador actualizado correctamente", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Log.e("EditJugadorScreen", "updateJugadorInFirestore: Error al actualizar jugador", e)
        Toast.makeText(context, "Error al actualizar jugador", Toast.LENGTH_SHORT).show()
    } finally {
        Log.d("EditJugadorScreen", "updateJugadorInFirestore: Fin")
    }
}

//Funcion para obtener un jugador en Firestore
suspend fun getJugadorById(db: FirebaseFirestore, jugadorId: String, context: Context): Jugador? {
    return try {
        val document = db.collection("jugadores").document(jugadorId).get().await()
        if (document.exists()) {
            Jugador(
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
        } else {
            Toast.makeText(context, "Jugador no encontrado", Toast.LENGTH_SHORT).show()
            null
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error al obtener jugador", Toast.LENGTH_SHORT).show()
        null
    }
}