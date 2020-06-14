package com.vivetuaventura.modelos

class Usuario (idUsuario : String, nombreUsuario:String, valoracionUsuario: Int) {
    var idUsuario:String
    var nombreUsuario:String
    var valoracionUsuario:Int

    init {
        this.idUsuario = idUsuario
        this.nombreUsuario = nombreUsuario
        this.valoracionUsuario = valoracionUsuario
    }
}