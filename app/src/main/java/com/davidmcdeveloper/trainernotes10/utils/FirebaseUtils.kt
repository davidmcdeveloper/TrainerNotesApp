package com.davidmcdeveloper.trainernotes10.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import kotlin.text.set

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
            Log.d("FirebaseUtils", "URL de la imagen: $fotoUrl") // Añade esta línea

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
        "categoria" to categoria)
    db.collection("jugadores").document(id).set(jugador).addOnCompleteListener{ task ->
        if (task.isSuccessful){
            Toast.makeText(context, "Jugador Añadido correctamente", Toast.LENGTH_SHORT).show()
            //Eliminamos la navegacion para no tener problemas.
            //navController.popBackStack()
        }else{
            Toast.makeText(context, "Error al añadir jugador", Toast.LENGTH_SHORT).show()
        }
    }
}