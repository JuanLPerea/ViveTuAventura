package com.aventuras

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*
import androidx.core.view.drawToBitmap
import com.aventuras.SalvarPreferencias.DatabaseHelper
import com.aventuras.Utilidades.EfectosImagen
import com.aventuras.Utilidades.ImagesHelper
import com.aventuras.modelos.Adventure
import com.aventuras.modelos.Capitulo
import kotlinx.android.synthetic.main.activity_crear_aventura.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class CrearAventuraActivity : AppCompatActivity() {

    lateinit var databaseHelper: DatabaseHelper
    lateinit var db: SQLiteDatabase
    lateinit var imagesHelper: ImagesHelper
    lateinit var capituloActivo: Capitulo

    // Creamos una aventura nueva
    var aventuraActiva = Adventure()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_aventura)

        imagenCrearAventura.setImageResource(R.drawable.selecciona_imagen)

        // Ocultamos el progress bar
        progressBarCrearAventura.setVisibility(View.INVISIBLE)

        // Establecemos las acciones para los botones y click en los componentes
        clickHandler()

        // Recuperamos id de la aventura del intent
        aventuraActiva.id = intent.getStringExtra("ID_AVENTURA")!!
        val esNuevaAventura = intent.getBooleanExtra("ESNUEVO" , true)

        // Accedemos a la BD
        databaseHelper = DatabaseHelper(this)
        db = databaseHelper.writableDatabase

        // creamos una instancia de la clase para manipular la imágenes
        imagesHelper = ImagesHelper(this)

        // Cargamos la aventura de la BD
        aventuraActiva = databaseHelper.recuperarAventura(db, aventuraActiva.id)
        setTitle(aventuraActiva.nombreAventura + " (" + aventuraActiva.creador + ")")

        // Comprobamos si la aventura ya existe (Editar) o es nueva (Crear)
        // Insertamos o cargamos el primer capítulo que editaremos mediante la aplicación
        if (esNuevaAventura) {
            capituloActivo = databaseHelper.crearCapituloBD(db, aventuraActiva.id, "")
            aventuraActiva.listaCapitulos.add(capituloActivo)
        } else {
            capituloActivo = databaseHelper.cargarCapituloRaiz(db, aventuraActiva.id)
        }
        // Mostrar el capitulo en pantalla
        cargarCapituloEnPantalla()

    }


    private fun clickHandler() {

        val seleccionarImagen = findViewById(R.id.imagenCrearAventura) as ImageView
        seleccionarImagen.setOnClickListener {

            //check runtime permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED
                ) {
                    //permission denied
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE);
                    //show popup to request runtime permission
                    requestPermissions(permissions, PERMISSION_CODE);
                } else {
                    //permission already granted
                    pickImageFromGallery();
                }
            } else {
                //system OS is < Marshmallow
                pickImageFromGallery();
            }
        }

        val decision1Click = findViewById(R.id.botonDecision1CA) as Button
        decision1Click.setOnClickListener {
            // Comprobar si ya hay un capitulo en esta posición
            if (!capituloActivo.capitulo1.equals("")) {
                // Si existe cargamos el capitulo existente
                capituloActivo =
                    databaseHelper.cargarCapitulo(db, aventuraActiva.id, capituloActivo.capitulo1)
            } else {
                if (aventuraActiva.listaCapitulos.size < 30) {
                    // Pedimos al usuario que introduzca un texto que se mostrará en el botón para esta decisión
                    pedirTexto(decision1Click)

                } else {
                    Toast.makeText(applicationContext, "Como máximo puedes crear 30 capitulos, no puedes crear mas", Toast.LENGTH_LONG).show()
                }
            }
            // Mostrar el capitulo en pantalla
            cargarCapituloEnPantalla()
        }

        val decision2Click = findViewById(R.id.botonDecision2CA) as Button
        decision2Click.setOnClickListener {
            // Comprobar si ya hay un capitulo en esta posición
            if (!capituloActivo.capitulo2.equals("")) {
                // Si existe cargamos el capitulo existente
                capituloActivo =
                    databaseHelper.cargarCapitulo(db, aventuraActiva.id, capituloActivo.capitulo2)
            } else {
                if (aventuraActiva.listaCapitulos.size < 30) {
                    // Pedimos al usuario que introduzca un texto que se mostrará en el botón para esta decisión
                    pedirTexto(decision2Click)

                } else {
                    Toast.makeText(applicationContext, "Como máximo puedes crear 30 capitulos, no puedes crear mas", Toast.LENGTH_LONG).show()
                }
            }

            // Mostrar el capitulo en pantalla
            cargarCapituloEnPantalla()
        }

        val atrasClick = findViewById(R.id.botonAtrasCA) as ImageButton
        atrasClick.setOnClickListener {
            // Al hacer click en el botón atrás navegamos al nodo padre, si ya estamos en el nodo raiz, no hacemos nada
            if (!capituloActivo.capituloPadre.equals("")) {
                capituloActivo = databaseHelper.cargarCapitulo(db, aventuraActiva.id, capituloActivo.capituloPadre
                )
                cargarCapituloEnPantalla()
            }
        }

        val guardarClick = findViewById(R.id.botonTerminarCA) as ImageButton
        guardarClick.setOnClickListener {
            // Botón terminar guardar los cambios y podemos publicar de nuevo la historia
            aventuraActiva.publicado = false
            databaseHelper.actualizarAventura(db, aventuraActiva)
            // Salir de la activity
            finish()
        }

        val editarTextoListener = findViewById(R.id.editTextCrearAventura) as EditText
        editarTextoListener.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // guardar cambios cuando editemos el texto
                val textoView = editTextCrearAventura.text.toString()
                capituloActivo.textoCapitulo = textoView
                databaseHelper.actualizarCapitulo(db, aventuraActiva.id, capituloActivo)
            }
        })

        val borrarClick = findViewById(R.id.botonBorrarNodoCA) as ImageButton
        borrarClick.setOnClickListener {
            // Comprobamos que no sea el capítulo raiz, que no se puede borrar.
            if (!capituloActivo.capituloPadre.equals("")) {
                // Borrar un nodo. También borraremos los nodos que dependan de éste
                val dialogBorrar = Dialog(this)
                dialogBorrar.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialogBorrar.setCancelable(true)
                dialogBorrar.setContentView(R.layout.confirmar_dialog)

                val textoConfirmarET =
                    dialogBorrar.findViewById(R.id.texto_dialog_confirmarTV) as TextView
                textoConfirmarET.setText("¿Borrar capítulo? ¡OJO! Se borrarán también todos los capitulos que dependan de éste y no se puede deshacer")

                val yesBtn = dialogBorrar.findViewById(R.id.aceptar_confirmar_dialog_BTN) as Button
                yesBtn.setOnClickListener {
                    // volver al nodo padre
                    // borrar mensaje opcion (hay que saber si venimos de la opcion 1 o la 2
                    // Borrar nodo
                    // ahora el nodo activo es el nodo padre
                    // actualizamos la base de datos
                    var capituloTMP = databaseHelper.cargarCapitulo(
                        db,
                        aventuraActiva.id,
                        capituloActivo.capituloPadre
                    )
                    if (capituloTMP.capitulo1.equals(capituloActivo.id)) {
                        capituloTMP.textoOpcion1 = ""
                        capituloTMP.capitulo1 = ""
                        Log.d("Miapp", "veniamos de la opcion 1")
                    } else if (capituloTMP.capitulo2.equals(capituloActivo.id)) {
                        Log.d("Miapp", "veniamos de la opcion 2")
                        capituloTMP.textoOpcion2 = ""
                        capituloTMP.capitulo2 = ""
                    }
                    databaseHelper.borrarCapituloBD(db, aventuraActiva.id, capituloActivo)
                    capituloActivo = capituloTMP
                    databaseHelper.actualizarCapitulo(db, aventuraActiva.id, capituloActivo)
                    aventuraActiva.listaCapitulos.removeAll(aventuraActiva.listaCapitulos)
                    aventuraActiva.listaCapitulos = databaseHelper.cargarCapitulos(db, aventuraActiva.id)
                    cargarCapituloEnPantalla()
                    dialogBorrar.dismiss()
                }

                val noBtn = dialogBorrar.findViewById(R.id.cancelar_confirmar_dialog_BTN) as Button
                noBtn.setOnClickListener {
                    dialogBorrar.dismiss()
                }

                dialogBorrar.show()
            } else {
                Toast.makeText(applicationContext, "No se puede borrar el primer capítulo" , Toast.LENGTH_LONG).show()
            }


        }

    }

    private fun pedirTexto(botonPulsado: Button) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.pedir_texto_dialog)

        val textoDecisionET = dialog.findViewById(R.id.textoDescisionDLG) as EditText

        val yesBtn = dialog.findViewById(R.id.aceptar_texto_dialog_BTN) as Button
        yesBtn.setOnClickListener {
            val textoRespuestas = textoDecisionET.text.toString()

            if (textoRespuestas.equals("")) {
                Toast.makeText(this, "Debes introducir al menos una palabra", Toast.LENGTH_LONG)
                    .show()
            } else {
                // al capitulo activo tenemos que poner en la decisión 1 el id del nuevo capitulo que creemos
                // añadimos un nuevo capitulo a nuestra historia y guardamos que el capitulo padre es el actual
                //Primero guardamos el capitulo activo en una variable temporal
                var capituloTMP = capituloActivo
                // Despues creamos el capitulo nuevo y le indicamos el capitulo padre que es el que hemos guardado
                capituloActivo = databaseHelper.crearCapituloBD(db, aventuraActiva.id, capituloTMP.id)

                // Añadimos el capitulo nuevo a la lista de la aventura
                aventuraActiva.listaCapitulos.add(capituloActivo)

                when (botonPulsado.id) {
                    R.id.botonDecision1CA -> {
                        capituloTMP.textoOpcion1 = textoRespuestas
                        // Al capitulo que estabamos antes actualizamos el campo decisión2 con el id del nuevo capitulo
                        capituloTMP.capitulo1 = capituloActivo.id
                    }
                    R.id.botonDecision2CA -> {
                        capituloTMP.textoOpcion2 = textoRespuestas
                        // Al capitulo que estabamos antes actualizamos el campo decisión2 con el id del nuevo capitulo
                        capituloTMP.capitulo2 = capituloActivo.id

                    }
                }
                // actualizamos la base de datos
                databaseHelper.actualizarCapitulo(db, aventuraActiva.id, capituloTMP)
                cargarCapituloEnPantalla()
                dialog.dismiss()
            }

        }

        val noBtn = dialog.findViewById(R.id.cancelar_texto_dialog_BTN) as Button
        noBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun cargarCapituloEnPantalla() {

        // Visualizamos la imagen en el ImageView
        if (capituloActivo.imagenCapitulo.equals("")) {
            imagenCrearAventura.setImageResource(R.drawable.selecciona_imagen)
        } else {
            imagenCrearAventura.setImageBitmap(imagesHelper.recuperarImagenMemoriaInterna(capituloActivo.imagenCapitulo))
        }
        // Visualizamos los textos
        if (capituloActivo.textoCapitulo.equals("")) {
            editTextCrearAventura.setText("")
        } else {
            editTextCrearAventura.setText(capituloActivo.textoCapitulo)
        }

        if (capituloActivo.textoOpcion1.equals("")) {
            botonDecision1CA.setText("Pulsa para elegir lo que pasa")
        } else {
            botonDecision1CA.setText(capituloActivo.textoOpcion1)
        }

        if (capituloActivo.textoOpcion2.equals("")) {
            botonDecision2CA.setText("Pulsa para elegir lo que pasa")
        } else {
            botonDecision2CA.setText(capituloActivo.textoOpcion2)
        }

    //    Log.d("Miapp", "Hay " + aventuraNueva.listaCapitulos.size + " capitulos")

    }


    // ------------------------------------------------------------------------------------------------------------------------------------------


    private fun pickImageFromGallery() {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    companion object {
        //image pick code
        private val IMAGE_PICK_CODE = 1000;

        //Permission code
        private val PERMISSION_CODE = 1001;
    }

    //handle requested permission result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    //permission from popup granted
                    pickImageFromGallery()
                } else {
                    //permission from popup denied
                    Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //handle result of picked image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {


            // Convertimos el URI de la imagen seleccionada en un Bitmap
            val imageURI = data?.data
            val bitmap = imagesHelper.obtenerBitmap(applicationContext, imageURI)
            // redimensionamos la imagen
            var resizedBitmap = Bitmap.createScaledBitmap(bitmap!!, 256, 192, false)

            // filtro a la imagen
          //  resizedBitmap = EfectosImagen.sketch(resizedBitmap)

            //  imagenCrearAventura.setImageBitmap(resizedBitmap)

            // guardamos la imagen en la memoria interna
            val rutaImagen = imagesHelper.guardarBitmapEnMemoria(
                applicationContext,
                resizedBitmap,
                capituloActivo
            )

            // Convertimos la ruta del archivo a String y lo guardamos en la BD
            capituloActivo.imagenCapitulo = rutaImagen.toString()
            databaseHelper.actualizarCapitulo(db, aventuraActiva.id, capituloActivo)

            // Visualizamos la imagen en el ImageView
            imagenCrearAventura.setImageBitmap(
                imagesHelper.recuperarImagenMemoriaInterna(
                    capituloActivo.imagenCapitulo
                )
            )

        }
    }

    override fun onBackPressed() {
        Toast.makeText(this, "Pulsa en el botón de la bandera para salir", Toast.LENGTH_LONG).show()
    }


    fun filtroClicked(view: View) {

        var miBitmap = imagenCrearAventura.drawToBitmap()
        if (view is ImageButton) {

            progressBarCrearAventura.setVisibility(View.VISIBLE)

            CoroutineScope(Default).launch {
                AplicarFiltro(view.id, miBitmap)
            }

        }
    }

    // Suspend function
    suspend fun AplicarFiltro (id : Int, bitmap: Bitmap)  {

        var miBitmap : Bitmap
        miBitmap = bitmap

        when (id) {
            R.id.checkbox_boost -> {
                miBitmap = EfectosImagen.hue(bitmap, 10f)
            }
            R.id.checkbox_sepia -> {
                  miBitmap = EfectosImagen.saturation(bitmap, 20)
            }
            R.id.checkbox_vigneta -> {
                   miBitmap = EfectosImagen.vignette(bitmap)
            }
            R.id.checkbox_sketch -> {
                   miBitmap = EfectosImagen.brightness(bitmap, 30)
            }
            R.id.checkbox_gammma -> {
                   miBitmap = EfectosImagen.invert(bitmap)
            }
        }

        imagesHelper.guardarBitmapEnMemoria(applicationContext, miBitmap, capituloActivo)

        withContext(Main){
            imagenCrearAventura.setImageBitmap(miBitmap)
            progressBarCrearAventura.setVisibility(View.INVISIBLE)
        }

    }

}


