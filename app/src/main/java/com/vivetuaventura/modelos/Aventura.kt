package com.vivetuaventura.modelos

class Aventura  (id:String, nombreAventura: String, creador:String, visitas:Int, nota:Int) {
     var id: String
     var nombreAventura: String
     var creador: String
     var visitas: Int
     var nota: Int
     var publicado: Boolean
     var listaCapitulos: MutableList<Capitulo> = mutableListOf()

   init {
        this.id = id
        this.nombreAventura = nombreAventura
        this.creador = creador
        this.visitas = visitas
        this.nota = nota
        this.publicado = false
    }



}