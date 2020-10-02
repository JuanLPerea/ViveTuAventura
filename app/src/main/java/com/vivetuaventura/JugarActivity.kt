package com.vivetuaventura

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.vivetuaventura.Interfaces.AventuraFirebaseCallback
import com.vivetuaventura.SalvarPreferencias.DatabaseHelper
import com.vivetuaventura.Utilidades.FirebaseUtils
import com.vivetuaventura.Utilidades.ImagesHelper
import com.vivetuaventura.modelos.Adventure
import com.vivetuaventura.modelos.Capitulo
import kotlinx.android.synthetic.main.activity_jugar.*

class JugarActivity : AppCompatActivity() , AventuraFirebaseCallback {

    lateinit var databaseHelper: DatabaseHelper
    lateinit var db: SQLiteDatabase
    lateinit var imagesHelper: ImagesHelper
    lateinit var capituloActivo: Capitulo
    lateinit var firebaseUtils : FirebaseUtils
    private lateinit var auth: FirebaseAuth
    private var user = ""

    // Creamos una aventura nueva
    var aventuraNueva = Adventure()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jugar)

        // Accedemos a la BD
        databaseHelper = DatabaseHelper(this)
        db = databaseHelper.writableDatabase

        // Usuario
        // Initialize Firebase Auth
        auth = Firebase.auth
        user = auth.currentUser!!.uid
        Log.d("Miapp" , "Usuario: " + user)

        // utlidades de Firebase
        firebaseUtils = FirebaseUtils(this)
        firebaseUtils.setAventuraListener(this)

        // creamos una instancia de la clase para manipular la imágenes
        imagesHelper = ImagesHelper(this)

        // Recuperamos id de la aventura del intent
        val recuperarID = intent.getStringExtra("ID_AVENTURA")

        // TODO Diferenciar si la historia que queremos ver es local o de Firebase
        aventuraNueva = databaseHelper.recuperarAventura(db, aventuraNueva.id)

        if (!aventuraNueva.id.equals("")) {
            // Cargar de la BD del dispositivo
            cargarLocal()
        } else {
            // Cargar de firebase
            aventuraNueva = firebaseUtils.recuperarAventuraFirebase(user , recuperarID)
        }

        clickHandler()

    }

    private fun cargarLocal() {


        // Cargamos la aventura de la BD

        setTitle(aventuraNueva.nombreAventura + " (" + aventuraNueva.creador + ")")

        if (aventuraNueva.publicado) {
            // Si esta historia ya está publicada, ocultamos el botón
            publicarBTN.hide()
        }

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

        val publicarClick = findViewById(R.id.publicarBTN) as FloatingActionButton
        publicarClick.setOnClickListener {
            // Publicar la historia
            val builder = AlertDialog.Builder(this)
            builder.apply {
                setTitle("Publicar en la APP")
                setMessage("La historia deberá ser aprobada, normalmente tarda 1 día y si es correcto aparecerá en las historias compartidas y cualquiera podrá jugar y ver tu historia!!!")
                setPositiveButton("ENVIAR",
                    DialogInterface.OnClickListener {  dialog, id ->
                        firebaseUtils.subirAventuraFirebase(db, aventuraNueva, user)
                    Log.d("Miapp", "OK publicar")

                        // TODO ENVIAR MAIL PARA REVISAR HISTORIA QUE SE HA SUBIDO

                    })
                setNegativeButton("Cancelar",
                    DialogInterface.OnClickListener {  dialog, id ->

                    Log.d("Miapp", "Cancelar")
                    })
            }
            builder.create()
            builder.show()


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

    private fun cargarCapituloLocal() {

    }


    override fun onAventuraLoaded(aventura: Adventure) {
        capituloActivo = aventura.listaCapitulos.get(0)
        textoJugarTV.setText(capituloActivo.textoCapitulo)
        decision1JugarBTN.setText(capituloActivo.textoOpcion1)
        decision2JugarBTN.setText(capituloActivo.textoOpcion2)
    }


}