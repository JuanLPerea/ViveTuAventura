package com.vivetuaventura.modelos

class Capitulo ( id:Int, textoCapitulo:String, imagenCapitulo:String, finHistoria:Boolean) {

     var capituloPadre:Int
     var capitulo1:Int
     var capitulo2:Int
     var id:Int
     var textoCapitulo:String
     var imagenCapitulo:String
     var finHistoria:Boolean

    init {
        this.capituloPadre = 0;
        this.capitulo1 = 0;
        this.capitulo2 = 0;
        this.id = id
        this.textoCapitulo = textoCapitulo
        this.imagenCapitulo = imagenCapitulo
        this.finHistoria = finHistoria
    }

}