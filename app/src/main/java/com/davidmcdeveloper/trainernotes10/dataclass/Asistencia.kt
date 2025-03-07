package com.davidmcdeveloper.trainernotes10.dataclass

data class Asistencia(
    val fecha: String = "",
    val categoria: String = "",
    val jugadores: List<Map<String, Any>>
)