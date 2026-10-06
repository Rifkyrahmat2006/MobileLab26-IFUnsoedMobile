package com.example.gempakini.data.model

data class GempaResponse(
    val Infogempa: Infogempa
)

data class Infogempa(
    val gempa: List<Gempa>
)

data class Gempa(
    val Tanggal: String,
    val Jam: String,
    val DateTime: String,
    val Coordinates: String,
    val Lintang: String,
    val Bujur: String,
    val Magnitude: String,
    val Kedalaman: String,
    val Wilayah: String,
    val Potensi: String
)
