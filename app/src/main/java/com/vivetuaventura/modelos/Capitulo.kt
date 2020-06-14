package com.vivetuaventura.modelos

class Capitulo ( id:Int, nombreCapitulo:String, textoCapitulo:String, imagenCapitulo:String, finHistoria:Boolean) {

     var id:Int
     var nombreCapitulo:String
     var textoCapitulo:String
     var imagenCapitulo:String
     var finHistoria:Boolean

    init {
        this.id = id
        this.nombreCapitulo = nombreCapitulo
        this.textoCapitulo = textoCapitulo
        this.imagenCapitulo = imagenCapitulo
        this.finHistoria = finHistoria
    }

}