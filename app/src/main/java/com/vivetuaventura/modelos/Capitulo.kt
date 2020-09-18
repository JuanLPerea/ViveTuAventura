package com.vivetuaventura.modelos

class Capitulo ( idAventura: String, id:String , capituloPadre: String, capitulo1:String, textoOpcion1:String, capitulo2:String, textoOpcion2:String, textoCapitulo:String, imagenCapitulo:String, finHistoria:Boolean) {

     var idAventura:String
     var capituloPadre:String
     var capitulo1:String
     var textoOpcion1:String
     var capitulo2:String
     var textoOpcion2:String
     var id:String
     var textoCapitulo:String
     var imagenCapitulo:String
     var finHistoria:Boolean

    init {
        this.idAventura = idAventura
        this.id = id
        this.capituloPadre = capituloPadre;
        this.capitulo1 = capitulo1;
        this.textoOpcion1 = textoOpcion1
        this.capitulo2 = capitulo1;
        this.textoOpcion2 = textoOpcion2
        this.textoCapitulo = textoCapitulo
        this.imagenCapitulo = imagenCapitulo
        this.finHistoria = finHistoria
    }

}