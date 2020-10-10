package com.vivetuaventura

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.vivetuaventura.Interfaces.AventuraFirebaseCallback
import com.vivetuaventura.Interfaces.ImagenFirebaseCallback
import com.vivetuaventura.Interfaces.NumeroAventurasCallback
import com.vivetuaventura.SalvarPreferencias.DatabaseHelper
import com.vivetuaventura.Utilidades.FirebaseUtils
import com.vivetuaventura.Utilidades.ImagesHelper
import com.vivetuaventura.modelos.Adventure
import com.vivetuaventura.modelos.Capitulo
import kotlinx.android.synthetic.main.activity_jugar.*
import java.net.URI

class JugarActivity : AppCompatActivity(), AventuraFirebaseCallback, ImagenFirebaseCallback , NumeroAventurasCallback {

    lateinit var databaseHelper: DatabaseHelper
    lateinit var db: SQLiteDatabase
    lateinit var imagesHelper: ImagesHelper
    lateinit var capituloActivo: Capitulo
    lateinit var firebaseUtils: FirebaseUtils
    private lateinit var auth: FirebaseAuth
    private var user = ""
    var aventuraLocal = true

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
        Log.d("Miapp", "Usuario: " + user)

        // utlidades de Firebase
        firebaseUtils = FirebaseUtils(this)
        firebaseUtils.setAventuraListener(this)
        firebaseUtils.setImageListener(this)
        firebaseUtils.setNumeroAventurasListener(this)

        // creamos una instancia de la clase para manipular la imágenes
        imagesHelper = ImagesHelper(this)

        // Recuperamos id de la aventura del intent
        val recuperarID = intent.getStringExtra("ID_AVENTURA")

        // Diferenciar si la historia que queremos ver es local o de Firebase
        aventuraNueva = databaseHelper.recuperarAventura(db, recuperarID)
        if (!aventuraNueva.id.equals("")) {
            // Cargar de la BD del dispositivo
            aventuraLocal = true
            // cargar los capitulos
            aventuraNueva.listaCapitulos = databaseHelper.cargarCapitulos(db, recuperarID)
            // cargar el primer capitulo
            capituloActivo = databaseHelper.cargarCapituloRaiz(db, aventuraNueva.id)
            cargarCapituloEnPantalla()

        } else {
            aventuraLocal = false
            // Cargar de firebase
            firebaseUtils.recuperarAventuraFirebase(recuperarID)
        }

        clickHandler()

    }


    private fun clickHandler() {

        val clickDecision1 = findViewById(R.id.decision1JugarBTN) as Button
        clickDecision1.setOnClickListener {

            if (capituloActivo.textoOpcion1.equals("FIN")) {
                Log.d("Miapp", "Este capitulo es final")
                //    Toast.makeText(applicationContext, "Esta historia termina aquí, vuelve a jugar!!" , Toast.LENGTH_LONG).show()
                dialogoFin()
            }
            if (!capituloActivo.capitulo1.equals("")) {
                cargarCapitulo(capituloActivo.capitulo1)
                if (capituloActivo.capitulo1.equals("") && capituloActivo.capitulo2.equals((""))) {
                    capituloActivo.textoOpcion1 = "FIN"
                    capituloActivo.textoOpcion2 = "FIN"
                }
                cargarCapituloEnPantalla()
            }
        }

        val clickDecision2 = findViewById(R.id.decision2JugarBTN) as Button
        clickDecision2.setOnClickListener {
            if (capituloActivo.textoOpcion2.equals("FIN")) {
                Log.d("Miapp", "Este capitulo es final")
                dialogoFin()
            }

            if (!capituloActivo.capitulo2.equals("")) {
                cargarCapitulo(capituloActivo.capitulo2)
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
            // Hacemos una consulta a Firebase para ver cuantas aventuras tiene este usuario
            // (Como máximo se permitirán 10 aventuras publicadas por usuario para no saturar Firebase)
            // Cuando Firebase devuelva el resultado se llamará a la función NumeroAventurasUsuario mediante el Interface implementado
            firebaseUtils.getNumAventurasUsuario(user)

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
            capituloActivo = aventuraNueva.listaCapitulos.get(0)
            cargarCapituloEnPantalla()
            dialog.dismiss()
        }

        val salirBtn = dialog.findViewById(R.id.salir_jugar_dialog_BTN) as Button
        salirBtn.setOnClickListener {
            // Salir
            finish()
            dialog.dismiss()
        }

        // TODO SI LA HISTORIA ES DE LA WEB PODEMOS PONERLE NOTA (HABRÍA QUE COMPROBAR QUE NO SEA EL MISMO USUARIO QUE SE VOTE A SI MISMO)

        dialog.show()

    }

    private fun cargarCapituloEnPantalla() {
        setTitle(aventuraNueva.nombreAventura + " (" + aventuraNueva.creador + ")")
        if (aventuraNueva.publicado) {
            // Si esta historia ya está publicada, ocultamos el botón
            publicarBTN.hide()
        }
        textoJugarTV.setText(capituloActivo.textoCapitulo)
        decision1JugarBTN.setText(capituloActivo.textoOpcion1)
        decision2JugarBTN.setText(capituloActivo.textoOpcion2)
        // Visualizamos la imagen en el ImageView
        if (aventuraLocal) {
            jugarIV.setImageBitmap(imagesHelper.recuperarImagenMemoriaInterna(capituloActivo.imagenCapitulo))
        } else {
            firebaseUtils.cargarImagenFirebase(aventuraNueva.id, capituloActivo.id)
        }

    }

    private fun cargarCapitulo(idCapitulo: String) {
        for (capituloTMP in aventuraNueva.listaCapitulos) {
            if (idCapitulo.equals(capituloTMP.id)) {
                capituloActivo = capituloTMP
            }
        }
    }


    override fun onAventuraLoaded(aventura: Adventure) {
        capituloActivo = aventura.listaCapitulos.get(0)
        aventuraNueva = aventura
        textoJugarTV.setText(capituloActivo.textoCapitulo)
        decision1JugarBTN.setText(capituloActivo.textoOpcion1)
        decision2JugarBTN.setText(capituloActivo.textoOpcion2)
        cargarCapituloEnPantalla()
    }

    override fun onImageLoaded(bitmap: Bitmap) {
        jugarIV.setImageBitmap(bitmap)
    }

    override fun NumeroAventurasUsuario(numeroAventuras: Int) {
        Log.d("Miapp" , "Numero de aventuras en Firebase de este usuario: " + numeroAventuras)
        if (numeroAventuras < 10) {
            val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(true)
            dialog.setContentView(R.layout.dialogo_publicar)

            val cancelarBtn = dialog.findViewById(R.id.cancelar_publicar_dialog_BTN) as Button
            cancelarBtn.setOnClickListener {
                // Salir
                dialog.dismiss()
            }

            val publicarBtn = dialog.findViewById(R.id.aceptar_publicar_dialog_BTN) as Button
            publicarBtn.setOnClickListener {
                // Publicar la historia en Firebase
                firebaseUtils.subirAventuraFirebase(db, aventuraNueva)
                // Guardar imágenes en Firebase Storage
                firebaseUtils.subirImagenesFirebase(aventuraNueva.listaCapitulos, aventuraNueva.id)
                dialog.dismiss()
            }
            dialog.show()
        } else {
            Toast.makeText(this, "Como máximo puedes publicar 10 historias. Borra alguna de la Web si quieres publicar esta", Toast.LENGTH_LONG).show()
        }
    }

}