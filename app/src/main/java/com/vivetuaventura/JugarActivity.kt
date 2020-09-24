package com.vivetuaventura

import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import  com.vivetuaventura.R
import com.vivetuaventura.SalvarPreferencias.DatabaseHelper
import com.vivetuaventura.Utilidades.ImagesHelper
import com.vivetuaventura.modelos.Aventura
import com.vivetuaventura.modelos.Capitulo
import kotlinx.android.synthetic.main.activity_crear_aventura.*
import kotlinx.android.synthetic.main.activity_jugar.*

class JugarActivity : AppCompatActivity() {

    lateinit var databaseHelper: DatabaseHelper
    lateinit var db: SQLiteDatabase
    lateinit var imagesHelper: ImagesHelper
    lateinit var capituloActivo: Capitulo

    // Creamos una aventura nueva
    var aventuraNueva = Aventura("ejemplo", "-", "-", 0, 0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jugar)

        // creamos una instancia de la clase para manipular la imágenes
        imagesHelper = ImagesHelper(this)

        // TODO Diferenciar si la historia que queremos ver es local o de Firebase

        // Recuperamos id de la aventura del intent
        aventuraNueva.id = intent.getStringExtra("ID_AVENTURA")
        val tipoAlmacenamiento = intent.getStringExtra("ALMACENADO")


        if (tipoAlmacenamiento.equals("LOCAL")) {
            cargarLocal()
        }

        clickHandler()

    }

    private fun cargarLocal() {
        // Accedemos a la BD
        databaseHelper = DatabaseHelper(this)
        db = databaseHelper.writableDatabase

        // Cargamos la aventura de la BD
        aventuraNueva = databaseHelper.recuperarAventura(db, aventuraNueva.id)
        setTitle(aventuraNueva.nombreAventura + " (" + aventuraNueva.creador + ")")

        // cargar el primer capitulo
        capituloActivo = databaseHelper.cargarCapituloRaiz(db, aventuraNueva.id)

        cargarCapituloEnPantalla()

    }

    private fun clickHandler() {

    }

    private fun cargarCapituloEnPantalla() {
        textoJugarTV.setText(capituloActivo.textoCapitulo)
        decision1JugarBTN.setText(capituloActivo.textoOpcion1)
        decision2JugarBTN.setText(capituloActivo.textoOpcion2)
        // Visualizamos la imagen en el ImageView
        jugarIV.setImageBitmap(imagesHelper.recuperarImagenMemoriaInterna(capituloActivo.imagenCapitulo))

    //    Log.d("Miapp", "Hay " + aventuraNueva.listaCapitulos.size + " capitulos")

    }
}