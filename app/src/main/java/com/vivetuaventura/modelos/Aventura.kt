package com.vivetuaventura.modelos

class Aventura  (nombreAventura: String, creador:String, visitas:Int, nota:Int) {
     var nombreAventura: String
     var creador: String
     var visitas: Int
     var nota: Int
     var mutableList: MutableList<Capitulo> = mutableListOf()

   init {
        this.nombreAventura = nombreAventura
        this.creador = creador
        this.visitas = visitas
        this.nota = nota
    }


}