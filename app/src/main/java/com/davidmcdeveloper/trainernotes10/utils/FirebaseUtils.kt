package com.davidmcdeveloper.trainernotes10.utils

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.davidmcdeveloper.trainernotes10.dataclass.Jugador
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

suspend fun getEquipoCategories(db: FirebaseFirestore, teamId: String): List<String> {
    val teamDocument = db.collection("equipos").document(teamId).get().await()
    return if (teamDocument.exists()) {
        val categories = teamDocument.get("categorias") as? List<String>
        categories ?: emptyList()
    } else {
        emptyList()
    }
}

suspend fun deleteJugadoresByCategory(
    db: FirebaseFirestore,
    categoryName: String,
    context: Context
) {
    val jugadoresRef = db.collection("jugadores")
        .whereEqualTo("categoria", categoryName)
    try {
        val querySnapshot = jugadoresRef.get().await()
        for (document in querySnapshot.documents) {
            val fotoUrl = document.getString("fotoUrl") ?: ""

            if (fotoUrl.isNotEmpty()) {
                val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(fotoUrl)
                storageRef.delete().await() // Eliminar la foto del storage.
            }
            db.collection("jugadores").document(document.id).delete().await() // Eliminar el jugador.
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error al eliminar jugadores: $e", Toast.LENGTH_SHORT).show()
    }
}

//Funcion para borrar la imagen del jugador
suspend fun deleteJugadorImage(fotoUrl: String, context: Context) {
    if (fotoUrl.isNotEmpty()) {
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(fotoUrl)
        try {
            storageRef.delete().await() // Eliminar la foto del storage.
            Toast.makeText(context, "Imagen eliminada correctamente", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error al eliminar la imagen", Toast.LENGTH_SHORT).show()
        }
    }
}
//Función para añadir un jugador a Firestore (Mantenemos suspend)
suspend fun addJugadorToFirestore(
    db: FirebaseFirestore,
    id: String,
    nombre: String,
    primerApellido: String,
    posicionPrimaria: String,
    posicionSecundaria: String,
    peso: String,
    altura: String,
    fechaNacimiento: String,
    fotoUrl: String,
    categoria: String,
    context: Context,
    nLicencia: String //Añadimos el parametro
) {
    val jugador = hashMapOf(
        "id" to id,
        "nombre" to nombre,
        "primerApellido" to primerApellido,
        "fechaNacimiento" to fechaNacimiento,
        "posicionPrimaria" to posicionPrimaria,
        "posicionSecundaria" to posicionSecundaria,
        "peso" to peso,
        "altura" to altura,
        "fotoUrl" to fotoUrl,
        "categoria" to categoria,
        "nLicencia" to nLicencia //Añadimos el campo y su valor
    )
    try {
        db.collection("jugadores").document(id).set(jugador).await() //Usamos await()
        Toast.makeText(context, "Jugador Añadido correctamente", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Error al añadir jugador", Toast.LENGTH_SHORT).show()
    }
}
//Funciones de utilidad para convertir entre fechas y milisegundos
fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = millis
    return formatter.format(calendar.time)
}
fun convertDateToMillis(dateString: String): Long {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val date = formatter.parse(dateString)
    val calendar = Calendar.getInstance()
    calendar.time = date!!
    return calendar.timeInMillis
}

//Funcion para subir la imagen a Firebase Storage
suspend fun uploadImageToFirebaseStorage(imageUri: Uri, jugadorId: String, context: Context): String {
    val storageRef = Firebase.storage.reference
    //Usamos el id del jugador como nombre de la imagen, junto con un id unico.
    val imageFileName = "${jugadorId}_${UUID.randomUUID()}"
    val imageRef = storageRef.child("images/jugadores/$imageFileName")
    return try {
        imageRef.putFile(imageUri).await()
        val downloadUrl = imageRef.downloadUrl.await()
        downloadUrl.toString()
    } catch (e: Exception) {
        Toast.makeText(context, "Error al subir imagen", Toast.LENGTH_SHORT).show()
        ""
    }
}
//Funcion para sacar los datos del jugador por el ID
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
                altura = document.getString("altura") ?: "",
                categoria = document.getString("categoria") ?: "",
                fotoUrl = document.getString("fotoUrl") ?: "",
                fechaNacimiento = document.getString("fechaNacimiento") ?: "",
                nLicencia = document.getString("nLicencia")?:""
            )
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}