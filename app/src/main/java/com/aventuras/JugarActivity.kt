package com.aventuras

import android.app.Dialog
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

import com.aventuras.Interfaces.AventuraFirebaseCallback
import com.aventuras.Interfaces.ImagenFirebaseCallback
import com.aventuras.Interfaces.NumeroAventurasCallback
import com.aventuras.SalvarPreferencias.DatabaseHelper
import com.aventuras.Utilidades.FirebaseUtils
import com.aventuras.Utilidades.ImagesHelper
import com.aventuras.modelos.Adventure
import com.aventuras.modelos.Capitulo
import kotlinx.android.synthetic.main.activity_jugar.*

class JugarActivity : AppCompatActivity(), AventuraFirebaseCallback, ImagenFirebaseCallback , NumeroAventurasCallback {

    lateinit var databaseHelper: DatabaseHelper
    lateinit var db: SQLiteDatabase
    lateinit var imagesHelper: ImagesHelper
    lateinit var capituloActivo: Capitulo
    lateinit var firebaseUtils: FirebaseUtils
    lateinit var botonPublicar : FloatingActionButton
    lateinit var textViewAventura : TextView
    private lateinit var auth: FirebaseAuth
    private var user = ""
    var aventuraLocal = true

    // Creamos una aventura nueva
    var aventuraNueva = Adventure()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jugar)

        // Scroll Text View
        textViewAventura = findViewById(R.id.textoJugarTV)
        textViewAventura.movementMethod = ScrollingMovementMethod()

        // Floating button de publicar
        botonPublicar = findViewById(R.id.publicarBTN)

        // Accedemos a la BD
        databaseHelper = DatabaseHelper(this)
        db = databaseHelper.writableDatabase

        // Usuario
        // Initialize Firebase Auth
        auth = Firebase.auth
        val currentUser = auth.currentUser
        if (currentUser == null) {
            signInAnonymously()
        } else {
            user = currentUser!!.uid
        }


        // utlidades de Firebase
        firebaseUtils = FirebaseUtils(this)
        firebaseUtils.setAventuraListener(this)
        firebaseUtils.setImageListener(this)
        firebaseUtils.setNumeroAventurasListener(this)

        // creamos una instancia de la clase para manipular la im�genes
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
            // Comprobamos si esta historia est� publicada
            if (aventuraNueva.publicado) {
                botonPublicar.visibility = View.GONE
            }

        } else {
            // La aventura es de Firebase
            aventuraLocal = false
            // Ocultamos el bot�n de publicar
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
            if (capituloActivo.textoOpcion2.equals("FIN")) {
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
                // (Como m�ximo se permitir�n 10 aventuras publicadas por usuario para no saturar Firebase)
                // Cuando Firebase devuelva el resultado se llamar� a la funci�n NumeroAventurasUsuario mediante el Interface implementado
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
        textViewAventura.scrollTo(0, 0)
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

    }

    private fun cargarCapitulo(idCapitulo: String) {
        for (capituloTMP in aventuraNueva.listaCapitulos) {
            if (idCapitulo.equals(capituloTMP.id)) {
                capituloActivo = capituloTMP
            }
        }
    }


    override fun onAventuraLoaded(aventura: Adventure) {
        // Aqu� hemos recibido los datos de Firebase mendiante el callback de FirebaseUtils
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
                    // Guardar im�genes en Firebase Storage
                    firebaseUtils.subirImagenesFirebase(aventuraNueva.listaCapitulos, aventuraNueva.id)
                    dialog.dismiss()
                }
                dialog.show()
            } else {
                Toast.makeText(this, getString(R.string.max_aventuras), Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, getString(R.string.max_adv_app), Toast.LENGTH_LONG).show()
        }

    }

    private fun signInAnonymously() {
        // [START signin_anonymously]
        auth.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        user = auth.currentUser!!.uid

                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(baseContext, R.string.eror_usuario,
                                Toast.LENGTH_SHORT).show()
                    }
                }
    }

    override fun onBackPressed() {
        dialogoVotarAventura()
    }


    fun dialogoVotarAventura() {
        if (!user.equals(aventuraNueva.usuario)) {
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