package com.vivetuaventura

import android.app.Dialog
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import com.vivetuaventura.SalvarPreferencias.DatabaseHelper
import com.vivetuaventura.Utilidades.ImagesHelper
import com.vivetuaventura.modelos.Aventura
import com.vivetuaventura.modelos.Capitulo
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

        val clickDecision1 = findViewById(R.id.decision1JugarBTN) as Button
        clickDecision1.setOnClickListener {
            if (capituloActivo.textoOpcion1.equals("FIN")){
                Log.d("Miapp" , "Este capitulo es final")
            //    Toast.makeText(applicationContext, "Esta historia termina aquí, vuelve a jugar!!" , Toast.LENGTH_LONG).show()
                dialogoFin()
            }

            if (!capituloActivo.capitulo1.equals("")) {
                    capituloActivo = databaseHelper.cargarCapitulo(db, aventuraNueva.id, capituloActivo.capitulo1)
                    if (capituloActivo.capitulo1.equals("") && capituloActivo.capitulo2.equals((""))) {
                        capituloActivo.textoOpcion1 = "FIN"
                        capituloActivo.textoOpcion2 = "FIN"
                    }
                    cargarCapituloEnPantalla()
            }
        }

        val clickDecision2 = findViewById(R.id.decision2JugarBTN) as Button
        clickDecision2.setOnClickListener {
            if (capituloActivo.textoOpcion2.equals("FIN")){
                Log.d("Miapp" , "Este capitulo es final")
                dialogoFin()
             //   Toast.makeText(applicationContext, "Esta historia termina aquí, vuelve a jugar!!" , Toast.LENGTH_LONG).show()
            }

            if (!capituloActivo.capitulo2.equals("")) {
                capituloActivo = databaseHelper.cargarCapitulo(db, aventuraNueva.id, capituloActivo.capitulo2)
                if (capituloActivo.capitulo1.equals("") && capituloActivo.capitulo2.equals((""))) {
                    capituloActivo.textoOpcion1 = "FIN"
                    capituloActivo.textoOpcion2 = "FIN"
                }
                cargarCapituloEnPantalla()
            }
        }

    }

    private fun dialogoFin() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialogo_fin_jugar)

        val volverAEmpezarBtn = dialog.findViewById(R.id.reiniciar_jugar_dialog_BTN) as Button
        volverAEmpezarBtn.setOnClickListener {
           // Volver a empezar
            capituloActivo = databaseHelper.cargarCapituloRaiz(db, aventuraNueva.id)
            cargarCapituloEnPantalla()
            dialog.dismiss()
            }

        val salirBtn = dialog.findViewById(R.id.salir_jugar_dialog_BTN) as Button
        salirBtn.setOnClickListener {
            // Volver a empezar
            finish()
            dialog.dismiss()
        }

        dialog.show()

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