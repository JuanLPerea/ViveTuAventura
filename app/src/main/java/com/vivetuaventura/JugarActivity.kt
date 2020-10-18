package com.vivetuaventura

import android.app.Dialog
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

import com.vivetuaventura.Interfaces.AventuraFirebaseCallback
import com.vivetuaventura.Interfaces.ImagenFirebaseCallback
import com.vivetuaventura.Interfaces.NumeroAventurasCallback
import com.vivetuaventura.SalvarPreferencias.DatabaseHelper
import com.vivetuaventura.Utilidades.FirebaseUtils
import com.vivetuaventura.Utilidades.ImagesHelper
import com.vivetuaventura.modelos.Adventure
import com.vivetuaventura.modelos.Capitulo
import kotlinx.android.synthetic.main.activity_jugar.*

class JugarActivity : AppCompatActivity(), AventuraFirebaseCallback, ImagenFirebaseCallback , NumeroAventurasCallback {

    lateinit var databaseHelper: DatabaseHelper
    lateinit var db: SQLiteDatabase
    lateinit var imagesHelper: ImagesHelper
    lateinit var capituloActivo: Capitulo
    lateinit var firebaseUtils: FirebaseUtils
    lateinit var botonPublicar : FloatingActionButton
    private lateinit var auth: FirebaseAuth
    private var user = ""
    var aventuraLocal = true

    // Creamos una aventura nueva
    var aventuraNueva = Adventure()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jugar)

        // Floating button de publicar
        botonPublicar = findViewById(R.id.publicarBTN)

        // Accedemos a la BD
        databaseHelper = DatabaseHelper(this)
        db = databaseHelper.writableDatabase

        // Usuario
        // Initialize Firebase Auth
        auth = Firebase.auth
        val currentUser = auth.currentUser
        if (currentUser == null) signInAnonymously()

        // utlidades de Firebase
        firebaseUtils = FirebaseUtils(this)
        firebaseUtils.setAventuraListener(this)
        firebaseUtils.setImageListener(this)
        firebaseUtils.setNumeroAventurasListener(this)

        // creamos una instancia de la clase para manipular la imágenes
        imagesHelper = ImagesHelper(this)

        // Recuperamos id de la aventura del intent
        val recuperarID = intent.getStringExtra("ID_AVENTURA")!!

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
            // Comprobamos si esta historia está publicada
            if (aventuraNueva.publicado) {
                botonPublicar.visibility = View.GONE
            }

        } else {
            // La aventura es de Firebase
            aventuraLocal = false
            // Ocultamos el botón de publicar
            botonPublicar.visibility = View.GONE
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

        if (!aventuraNueva.publicado && aventuraLocal) {
            publicarClick.setOnClickListener {
                // Publicar la historia
                // Hacemos una consulta a Firebase para ver cuantas aventuras tiene este usuario
                // (Como máximo se permitirán 10 aventuras publicadas por usuario para no saturar Firebase)
                // Cuando Firebase devuelva el resultado se llamará a la función NumeroAventurasUsuario mediante el Interface implementado
                Toast.makeText(this, "Publicando aventura!!" , Toast.LENGTH_LONG).show()
                botonPublicar.setVisibility(View.GONE)
                firebaseUtils.getNumAventurasUsuario(user)
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
            capituloActivo = aventuraNueva.listaCapitulos.get(0)
            cargarCapituloEnPantalla()
            dialog.dismiss()
        }

        val salirBtn = dialog.findViewById(R.id.salir_jugar_dialog_BTN) as Button
        salirBtn.setOnClickListener {
            // Salir
            dialogoVotarAventura()
            dialog.dismiss()
        }
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
        // Aquí hemos recibido los datos de Firebase mendiante el callback de FirebaseUtils
        capituloActivo = aventura.listaCapitulos.get(0)
        aventuraNueva = aventura
        aventuraNueva.visitas = aventura.visitas + 1
        firebaseUtils.actualizarVisitasAventura(aventuraNueva)
        // Actualizamos las views
        textoJugarTV.setText(capituloActivo.textoCapitulo)
        decision1JugarBTN.setText(capituloActivo.textoOpcion1)
        decision2JugarBTN.setText(capituloActivo.textoOpcion2)
        cargarCapituloEnPantalla()
    }

    override fun onImageLoaded(bitmap: Bitmap) {
        jugarIV.setImageBitmap(bitmap)
    }

    override fun NumeroAventurasUsuario(numeroAventurasUsuario: Int , totalAventuras : Int) {
       // Log.d("Miapp" , "Numero de aventuras en Firebase de este usuario: " + numeroAventurasUsuario)
        if (totalAventuras < 100) {
            if (numeroAventurasUsuario < 10) {
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
        } else {
            Toast.makeText(this, "Se ha llegado al máximo de aventuras que puede tener esta APP, haz una donación para aumentar la capacidad de nuestro servidor", Toast.LENGTH_LONG).show()
        }

    }

    private fun signInAnonymously() {
        // [START signin_anonymously]
        auth.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("Miapp", "signInAnonymously:success - " + auth.uid)
                        user = auth.currentUser!!.uid

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Miapp", "signInAnonymously:failure", task.exception)
                        Toast.makeText(baseContext, "Fallo al crear usuario",
                                Toast.LENGTH_SHORT).show()
                    }
                }
    }

    override fun onBackPressed() {
        dialogoVotarAventura()
    }


    fun dialogoVotarAventura() {
        if (!aventuraLocal) {
            if (!databaseHelper.aventuraYaVotada(db, aventuraNueva.id)) {
                val dialog = Dialog(this)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setCancelable(true)
                dialog.setContentView(R.layout.dialog_votar)

                val pickerNumber = dialog.findViewById<NumberPicker>(R.id.numberPickerVotar)
                pickerNumber.minValue = 0
                pickerNumber.maxValue = 10
                pickerNumber.value = 5

                val votarBtn = dialog.findViewById(R.id.votar_dialog_BTN) as Button
                votarBtn.setOnClickListener {
                    // Votar
                    val nota = ((aventuraNueva.nota + pickerNumber.value) / 2) as Int
                    if (aventuraNueva.visitas == 1) {
                        aventuraNueva.nota = pickerNumber.value
                    } else {
                        aventuraNueva.nota = nota
                    }
                    databaseHelper.aventurasVotadasAdd(db, aventuraNueva)
                    firebaseUtils.actualizarNotaAventura(aventuraNueva)
                    finish()
                    dialog.dismiss()
                }
                dialog.show()
            } else {
                finish()
            }
        } else {
            finish()
        }
    }
}