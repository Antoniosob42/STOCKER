package com.example.stocker2

data class Productos2(
    var nombre: String = "",
    val calorias:Int=0,
    val gramos:Int=0,
    val id:Int=0,
    val indiceSano:Int=0,
    val urlImagen:String="",
    var cantidadStock: Int = 0
)