package com.davidmcdeveloper.trainernotes10

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.davidmcdeveloper.trainernotes10.ui.theme.TrainerNotesTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance() // Inicializamos FirebaseAuth
        db = FirebaseFirestore.getInstance() // Inicializamos Firestore

        setContent {
            TrainerNotesTheme {
                // Usamos el LoginScreen y pasamos el contexto, auth y db
                LoginScreen(auth = auth, db = db, context = this)
            }
        }
    }
}

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    context: Context
) {
    var email by remember { mutableStateOf(TextFieldValue()) }
    var password by remember { mutableStateOf(TextFieldValue()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { signInUser(auth, email.text, password.text, context) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Iniciar sesión")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { registerUser(auth, email.text, password.text, context) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Registrarse")
        }
    }
}

fun signInUser(auth: FirebaseAuth, email: String, password: String, context: Context) {
    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        Toast.makeText(context, "Por favor ingresa un correo válido", Toast.LENGTH_SHORT).show()
        return
    }

    if (password.length < 6) {
        Toast.makeText(context, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
        return
    }

    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                val intent = Intent(context, HomeScreen()::class.java)
                context.startActivity(intent)
                // Asegúrate de que MainActivity no pueda ser accedida
                (context as ComponentActivity).finish()
            } else {
                Toast.makeText(context, "Error al iniciar sesión", Toast.LENGTH_SHORT).show()
            }
        }
}

fun registerUser(auth: FirebaseAuth, email: String, password: String, context: Context) {
    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        Toast.makeText(context, "Por favor ingresa un correo válido", Toast.LENGTH_SHORT).show()
        return
    }

    if (password.length < 6) {
        Toast.makeText(context, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
        return
    }

    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()
                val intent = Intent(context, HomeScreen::class.java)
                context.startActivity(intent)
                // Asegúrate de que MainActivity no pueda ser accedida
                (context as ComponentActivity).finish()
            } else {
                Toast.makeText(context, "Error al registrar usuario", Toast.LENGTH_SHORT).show()
            }
        }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TrainerNotesTheme {
        // Para la vista previa, el contexto se debe proporcionar en tiempo de ejecución
    }
}


