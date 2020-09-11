package com.vivetuaventura.modelos

class Capitulo ( id:Int, nombreAventura:String , textoCapitulo:String, imagenCapitulo:String, finHistoria:Boolean) {


     var capituloPadre:Int
     var nombreAventura:String
     var capitulo1:Int
     var capitulo2:Int
     var id:Int
     var textoCapitulo:String
     var imagenCapitulo:String
     var finHistoria:Boolean

    init {
        this.id = id
        this.nombreAventura = "-"
        this.capituloPadre = 0;
        this.capitulo1 = 0;
        this.capitulo2 = 0;
        this.textoCapitulo = textoCapitulo
        this.imagenCapitulo = imagenCapitulo
        this.finHistoria = finHistoria
    }

}