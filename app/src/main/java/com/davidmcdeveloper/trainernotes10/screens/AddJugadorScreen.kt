package com.davidmcdeveloper.trainernotes10.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.davidmcdeveloper.trainernotes10.R
import com.google.firebase.firestore.FirebaseFirestore
import com.davidmcdeveloper.trainernotes10.dataclass.Jugador
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddJugadorScreen(navController: NavController, categoryName: String, db: FirebaseFirestore) {
    val context = LocalContext.current
    //Variables
    var nombre by remember { mutableStateOf("") }
    var primerApellido by remember { mutableStateOf("") }
    //Posicion Primaria
    var posicionPrimariaExpanded by remember { mutableStateOf(false) }
    var posicionPrimariaSelectedText by remember { mutableStateOf("") }
    val posicionPrimariaOptions =
        listOf("Primera Línea", "Segunda Línea", "Flanker", "Ocho", "Medio Melé", "Apertura", "Ala Interno", "Ala Externo", "Primer Centro", "Segundo Centro", "Zaguero")
    //Posicion Secundaria
    var posicionSecundariaExpanded by remember { mutableStateOf(false) }
    var posicionSecundariaSelectedText by remember { mutableStateOf("") }
    val posicionSecundariaOptions =
        listOf("Primera Línea", "Segunda Línea", "Flanker", "Ocho", "Medio Melé", "Apertura", "Ala Interno", "Ala Externo", "Primer Centro", "Segundo Centro", "Zaguero")
    //Peso
    var pesoExpanded by remember { mutableStateOf(false) }
    var pesoSelectedText by remember { mutableStateOf("") }
    val pesoOptions = (25..200).map { it.toString() + " kg" } // Rango de 25 a 200 kg
    //Altura
    var alturaExpanded by remember { mutableStateOf(false) }
    var alturaSelectedText by remember { mutableStateOf("") }
    val alturaOptions = (50..220).map { (it.toFloat() / 100).toString() + " m" } // Rango de 0.50 a 2.20 m
    //Fecha de nacimiento
    var showDatePicker by remember { mutableStateOf(false) }
    var fechaNacimiento by remember { mutableStateOf("") }
    //DatePicker
    val datePickerState = rememberDatePickerState()
    val selectedDate = datePickerState.selectedDateMillis?.let {
        convertMillisToDate(it)
    } ?: ""
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    fechaNacimiento = selectedDate
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
    //Image
    var jugadorImageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        jugadorImageUri = uri
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Añadir Jugador") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            //Añadir Foto
            Spacer(modifier = Modifier.height(16.dp))
            //AÑADIMOS BOX PARA SUPERPONER ELEMENTOS
            Box(
                modifier = Modifier
                    .size(150.dp)
            ) {
                //IMAGEN DEL JUGADOR
                if (jugadorImageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(jugadorImageUri),
                        contentDescription = "Foto Jugador",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else { //Muestra la foto por defecto
                    Image(
                        painter = painterResource(id = R.drawable.defaultteam),
                        contentDescription = "Añadir Foto",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                //BOTON ADD
                Button(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 4.dp)
                        .size(30.dp)
                ) {
                    Icon(Icons.Filled.AddCircle, contentDescription = "Añadir imagen")
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
                        .padding(16.dp)
                )
                OutlinedTextField(
                    value = primerApellido,
                    onValueChange = { primerApellido = it },
                    label = { Text("Primer Apellido") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
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
                        .padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = posicionPrimariaSelectedText,
                        onValueChange = { posicionPrimariaSelectedText = it },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = posicionPrimariaExpanded) },
                        modifier = Modifier.menuAnchor(),
                        label = { Text("Posicion Primaria") }
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
                        .padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = posicionSecundariaSelectedText,
                        onValueChange = { posicionSecundariaSelectedText = it },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = posicionSecundariaExpanded) },
                        modifier = Modifier.menuAnchor(),
                        label = { Text("Posicion Secundaria") }
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
                        .padding(16.dp)
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
                        .padding(16.dp)
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
            //Fecha de nacimiento
            OutlinedTextField(
                value = if (fechaNacimiento.isEmpty()) "Seleccionar Fecha de Nacimiento" else fechaNacimiento,
                onValueChange = { },
                readOnly = true,
                label = { Text(text = "Fecha de Nacimiento") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable { showDatePicker = true },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.DateRange,
                        contentDescription = "Seleccionar Fecha",
                        modifier = Modifier.clickable { showDatePicker = true }
                    )
                }
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = {
                //Comprobamos los datos
                if (nombre.isEmpty() || primerApellido.isEmpty() || posicionPrimariaSelectedText.isEmpty() || posicionSecundariaSelectedText.isEmpty() || pesoSelectedText.isEmpty() || alturaSelectedText.isEmpty()) {
                    Toast.makeText(context, "Debes rellenar todos los campos", Toast.LENGTH_SHORT).show()
                } else {
                    val idJugador = generateJugadorId(nombre, primerApellido)
                    //Si la jugadorImageUri no es nula, subimos la foto a Firebase
                    if (jugadorImageUri != null) {
                        uploadImageToFirebaseStorage(
                            jugadorImageUri!!,
                            idJugador,
                            context
                        ) { jugadorImageUrl ->
                            saveJugadorToFirestore(
                                db,
                                idJugador,
                                nombre,
                                primerApellido,
                                posicionPrimariaSelectedText,
                                posicionSecundariaSelectedText,
                                pesoSelectedText,
                                alturaSelectedText,
                                fechaNacimiento,
                                categoryName,
                                jugadorImageUrl,
                                context,
                                navController
                            )
                        }
                    } else {
                        //En caso de que no haya imagen, subimos los datos sin la foto
                        saveJugadorToFirestore(
                            db,
                            idJugador,
                            nombre,
                            primerApellido,
                            posicionPrimariaSelectedText,
                            posicionSecundariaSelectedText,
                            pesoSelectedText,
                            alturaSelectedText,
                            fechaNacimiento,
                            categoryName,
                            "", // Aquí se pasa una cadena vacía porque no hay imagen
                            context,
                            navController
                        )
                    }
                }
            },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)) {
                Text("Guardar Jugador")
            }
        }
    }
}

//Funcion para subir la imagen a FirebaseStorage
fun uploadImageToFirebaseStorage(imageUri: Uri, playerId: String, context: Context, onComplete: (String) -> Unit) {
    val storageRef = FirebaseStorage.getInstance().reference
    val imageRef = storageRef.child("images/jugadores/${playerId}")
    val uploadTask = imageRef.putFile(imageUri)

    uploadTask.addOnSuccessListener {
        imageRef.downloadUrl.addOnSuccessListener { uri ->
            val imageUrl = uri.toString()
            onComplete(imageUrl) // Llama a la función de callback con la URL de la imagen
        }.addOnFailureListener {
            Toast.makeText(context, "Error al obtener la URL de la imagen", Toast.LENGTH_SHORT).show()
            onComplete("")
        }
    }.addOnFailureListener {
        Toast.makeText(context, "Error al subir la imagen", Toast.LENGTH_SHORT).show()
        onComplete("")
    }
}

//Funcion para generar la id del jugador
fun generateJugadorId(nombre: String, primerApellido: String): String {
    val uuid = UUID.randomUUID().toString()
    return "${nombre}${primerApellido}$uuid"
}
//Funcion para guardar un jugador en Firestore
fun saveJugadorToFirestore(
    db: FirebaseFirestore,
    id: String,
    nombre: String,
    primerApellido: String,
    posicionPrimaria: String,
    posicionSecundaria: String,
    peso: String,
    altura: String,
    fechaNacimiento: String,
    categoryName: String,
    jugadorImageUrl: String,
    context: Context,
    navController: NavController
) {
    val jugador = Jugador(
        id = id,
        nombre = nombre,
        primerApellido = primerApellido,
        posicionPrimaria = posicionPrimaria,
        posicionSecundaria = posicionSecundaria,
        peso = peso,
        categoria = categoryName,
        altura = altura,
        fechaNacimiento = fechaNacimiento,
        fotoUrl = jugadorImageUrl
    )
    db.collection("jugadores").document(id).set(jugador)
        .addOnSuccessListener {
            Toast.makeText(context, "Jugador añadido correctamente", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
        .addOnFailureListener {
            Toast.makeText(context, "Error al añadir jugador", Toast.LENGTH_SHORT).show()
        }
}
//Funcion para convertir millis a fecha
fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = millis
    return formatter.format(calendar.time)
}