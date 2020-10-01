package com.vivetuaventura.modelos

class Adventure  () {
     var id: String
     var nombreAventura: String
     var creador: String
     var visitas: Int
     var nota: Int
     var publicado: Boolean
     var listaCapitulos: MutableList<Capitulo> = mutableListOf()



   init {
        this.id = ""
        this.nombreAventura = ""
        this.creador = ""
        this.visitas = 0
        this.nota = 0
        this.publicado = false
    }



}