package com.vivetuaventura

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.vivetuaventura.Interfaces.AventuraFirebaseCallback
import com.vivetuaventura.Interfaces.ImagenFirebaseCallback
import com.vivetuaventura.SalvarPreferencias.DatabaseHelper
import com.vivetuaventura.Utilidades.FirebaseUtils
import com.vivetuaventura.Utilidades.ImagesHelper
import com.vivetuaventura.modelos.Adventure
import com.vivetuaventura.modelos.Capitulo
import kotlinx.android.synthetic.main.activity_jugar.*
import java.net.URI

class JugarActivity : AppCompatActivity() , AventuraFirebaseCallback , ImagenFirebaseCallback {

    lateinit var databaseHelper: DatabaseHelper
    lateinit var db: SQLiteDatabase
    lateinit var imagesHelper: ImagesHelper
    lateinit var capituloActivo: Capitulo
    lateinit var firebaseUtils : FirebaseUtils
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
        Log.d("Miapp" , "Usuario: " + user)

        // utlidades de Firebase
        firebaseUtils = FirebaseUtils(this)
        firebaseUtils.setAventuraListener(this)
        firebaseUtils.setImageListener(this)

        // creamos una instancia de la clase para manipular la im�genes
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
            firebaseUtils.recuperarAventuraFirebase(user , recuperarID)
        }



        clickHandler()

    }



    private fun clickHandler() {

        val clickDecision1 = findViewById(R.id.decision1JugarBTN) as Button
        clickDecision1.setOnClickListener {

                if (capituloActivo.textoOpcion1.equals("FIN")){
                    Log.d("Miapp" , "Este capitulo es final")
                    //    Toast.makeText(applicationContext, "Esta historia termina aqu�, vuelve a jugar!!" , Toast.LENGTH_LONG).show()
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
            if (capituloActivo.textoOpcion2.equals("FIN")){
                Log.d("Miapp" , "Este capitulo es final")
                dialogoFin()
             //   Toast.makeText(applicationContext, "Esta historia termina aqu�, vuelve a jugar!!" , Toast.LENGTH_LONG).show()
            }

            if (!capituloActivo.capitulo2.equals("")) {
                cargarCapitulo(capituloActivo.capitulo1)
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
                setTheme(R.style.AppTheme)
                setMessage("La historia deber� ser aprobada, normalmente tarda 1 d�a y si es correcto aparecer� en las historias compartidas y cualquiera podr� jugar y ver tu historia!!!")
                setPositiveButton("ENVIAR",
                    DialogInterface.OnClickListener {  dialog, id ->
                        firebaseUtils.subirAventuraFirebase(db, aventuraNueva, user)

                        // Guardar im�genes en Firebase Storage
                        firebaseUtils.subirImagenesFirebase(aventuraNueva.listaCapitulos, aventuraNueva.id)

                        // TODO Poner en alg�n lado indicador de historias que hay pendientes de aprobaci�n

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

        // TODO SI LA HISTORIA ES DE LA WEB PODEMOS PONERLE NOTA (HABR�A QUE COMPROBAR QUE NO SEA EL MISMO USUARIO QUE SE VOTE A SI MISMO)

        dialog.show()

    }

    private fun cargarCapituloEnPantalla() {
        setTitle(aventuraNueva.nombreAventura + " (" + aventuraNueva.creador + ")")
        if (aventuraNueva.publicado) {
            // Si esta historia ya est� publicada, ocultamos el bot�n
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


       //    Log.d("Miapp", "Hay " + aventuraNueva.listaCapitulos.size + " capitulos")
    }

    private fun cargarCapitulo(idCapitulo : String) {
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
     //   Log.d("Miapp", "Cargada Imagen de Firebase " +  imagen)
    }


}