package com.vivetuaventura

import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import  com.vivetuaventura.R
import com.vivetuaventura.SalvarPreferencias.DatabaseHelper
import com.vivetuaventura.Utilidades.ImagesHelper
import com.vivetuaventura.modelos.Aventura
import com.vivetuaventura.modelos.Capitulo

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

        // Recuperamos id de la aventura del intent
        aventuraNueva.id = intent.getStringExtra("ID_AVENTURA")

        // Accedemos a la BD
        databaseHelper = DatabaseHelper(this)
        db = databaseHelper.writableDatabase

        // creamos una instancia de la clase para manipular la imágenes
        imagesHelper = ImagesHelper(this)

        // Cargamos la aventura de la BD
        aventuraNueva = databaseHelper.recuperarAventura(db, aventuraNueva.id)
        setTitle(aventuraNueva.nombreAventura + " (" + aventuraNueva.creador + ")")

        clickHandler()

    }

    private fun clickHandler() {

    }
}