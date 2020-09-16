package com.vivetuaventura.modelos

class Capitulo ( idAventura: String, id:Int , capituloPadre: Int, capitulo1:Int, capitulo2:Int, textoCapitulo:String, imagenCapitulo:String, finHistoria:Boolean) {

     var idAventura:String
     var capituloPadre:Int
     var capitulo1:Int
     var capitulo2:Int
     var id:Int
     var textoCapitulo:String
     var imagenCapitulo:String
     var finHistoria:Boolean

    init {
        this.idAventura = "."
        this.id = id
        this.capituloPadre = 0;
        this.capitulo1 = 0;
        this.capitulo2 = 0;
        this.textoCapitulo = textoCapitulo
        this.imagenCapitulo = imagenCapitulo
        this.finHistoria = finHistoria
    }

}