package com.davidmcdeveloper.trainernotes10.screens

import android.content.Context
import android.util.Log
import android.util.Patterns
import android.widget.Toast
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.davidmcdeveloper.trainernotes10.R
import com.davidmcdeveloper.trainernotes10.navigation.Screen
import com.davidmcdeveloper.trainernotes10.utils.MyDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController, auth: FirebaseAuth) {
    var nombreCompleto by remember { mutableStateOf(TextFieldValue("")) }
    var fechaNacimiento by remember { mutableStateOf("") } //Ahora es String
    var email by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }
    var confirmPassword by remember { mutableStateOf(TextFieldValue("")) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var acceptTerms by remember { mutableStateOf(false) }
    val context = LocalContext.current
    //Estado para errores
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var confirmPasswordError by remember { mutableStateOf(false) }
    val db = FirebaseFirestore.getInstance()
    //Estado para el DatePicker
    var showDatePicker by remember { mutableStateOf(false) }

    Log.d("RegisterScreen", "showDatePicker inicial: $showDatePicker")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro") },
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
                .fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = R.drawable.addbackground),
                contentDescription = "Background Login",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                TextField(
                    value = nombreCompleto,
                    onValueChange = { nombreCompleto = it },
                    label = { Text("Nombre Completo") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0.8f)
                )
                Spacer(modifier = Modifier.height(16.dp))

                //Aqui hacemos el cambio
                TextField(
                    value = if (fechaNacimiento.isEmpty()) "Seleccionar Fecha de Nacimiento" else fechaNacimiento,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text(text = "Fecha de Nacimiento") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showDatePicker = true
                            Log.d("RegisterScreen", "showDatePicker click: $showDatePicker")
                        },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.DateRange,
                            contentDescription = "Seleccionar Fecha",
                            modifier = Modifier.clickable {
                                showDatePicker = true
                                Log.d("RegisterScreen", "showDatePicker click: $showDatePicker")
                            }
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = !Patterns.EMAIL_ADDRESS.matcher(it.text).matches()
                    },
                    label = { Text("Email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0.8f),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
                    isError = emailError
                )
                if (emailError) {
                    Text("Introduce un correo válido", color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = it.text.length < 6
                    },
                    label = { Text("Contraseña") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0.8f),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(
                            onClick = { passwordVisible = !passwordVisible }
                        ) {
                            Icon(
                                painter = painterResource(id = if (passwordVisible) R.drawable.visibilityoff else R.drawable.visibilityon),
                                contentDescription = "Mostrar/ocultar contraseña"
                            )
                        }
                    },
                    isError = passwordError
                )
                if (passwordError) {
                    Text("La contraseña debe tener al menos 6 caracteres.", color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        confirmPasswordError = it.text != password.text
                    },
                    label = { Text("Confirmar Contraseña") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0.8f),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(
                            onClick = { confirmPasswordVisible = !confirmPasswordVisible }
                        ) {
                            Icon(
                                painter = painterResource(id = if (confirmPasswordVisible) R.drawable.visibilityoff else R.drawable.visibilityon),
                                contentDescription = "Mostrar/ocultar contraseña"
                            )
                        }
                    },
                    isError = confirmPasswordError
                )
                if (confirmPasswordError) {
                    Text("Las contraseñas no coinciden.", color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = acceptTerms,
                        onCheckedChange = { acceptTerms = it }
                    )
                    Text(text = "Acepto los términos y condiciones")
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        registerUser(auth, email.text, password.text, nombreCompleto.text, fechaNacimiento, context, navController, db)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = acceptTerms && !emailError && !passwordError && !confirmPasswordError && password.text.isNotEmpty() && confirmPassword.text.isNotEmpty() && email.text.isNotEmpty() && nombreCompleto.text.isNotEmpty() && fechaNacimiento.isNotEmpty()
                ) {
                    Text("Registrarse")
                }
            }
        }
        //Si esta activado el DatePicker, lo abrimos
        Log.d("RegisterScreen", "showDatePicker antes de if: $showDatePicker")
        if (showDatePicker) {
            Log.d("RegisterScreen", "DatePicker mostrado")
            MyDatePicker(
                onDateSelected = {
                    fechaNacimiento = it
                    Log.d("RegisterScreen", "Fecha seleccionada: $fechaNacimiento")
                },
                onDismissRequest = {
                    showDatePicker = false
                    Log.d("RegisterScreen", "DatePicker cerrado")
                }
            )
        }
    }
}

fun registerUser(auth: FirebaseAuth, email: String, password: String, nombre: String, fechaNacimiento: String, context: Context, navController: NavController, db: FirebaseFirestore) {
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Guardar información adicional del usuario en Firestore
                val user = auth.currentUser
                user?.let {
                    val userMap = hashMapOf(
                        "userId" to it.uid,
                        "email" to email,
                        "nombreCompleto" to nombre,
                        "fechaNacimiento" to fechaNacimiento,
                    )
                    db.collection("users").document(it.uid).set(userMap)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Usuario registrado y verifica tu correo", Toast.LENGTH_SHORT).show()
                            // Enviar correo de verificación
                            user.sendEmailVerification()
                            //Redirigir al login
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Error al guardar los datos en Firestore: $e", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                Toast.makeText(context, "Error al registrar el usuario", Toast.LENGTH_SHORT).show()
            }
        }
}