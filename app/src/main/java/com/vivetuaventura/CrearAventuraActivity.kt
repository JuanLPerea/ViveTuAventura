package com.vivetuaventura

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*
import com.vivetuaventura.SalvarPreferencias.DatabaseHelper
import com.vivetuaventura.modelos.Aventura
import com.vivetuaventura.modelos.Capitulo
import kotlinx.android.synthetic.main.activity_crear_aventura.*
import kotlinx.android.synthetic.main.pedir_texto_dialog.*


class CrearAventuraActivity : AppCompatActivity() {

    lateinit var databaseHelper: DatabaseHelper
    lateinit var db: SQLiteDatabase
    lateinit var capituloActivo: Capitulo

    // Creamos una aventura nueva
    var aventuraNueva = Aventura("ejemplo", "-", "-", 0, 0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_aventura)

        // Establecemos las acciones para los botones y click en los componentes
        clickHandler()

        // Recuperamos id de la aventura del intent
        aventuraNueva.id = intent.getStringExtra("ID_AVENTURA")

        // Accedemos a la BD
        databaseHelper = DatabaseHelper(this)
        db = databaseHelper.writableDatabase

        // Cargamos la aventura de la BD
        aventuraNueva = databaseHelper.recuperarAventura(db, aventuraNueva.id)

        // Insertamos el primer capítulo que editaremos mediante la aplicación
        capituloActivo = databaseHelper.crearCapituloBD(db, aventuraNueva.id, "")
        aventuraNueva.listaCapitulos.add(capituloActivo)

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
                capituloActivo = databaseHelper.cargarCapitulo(db, aventuraNueva.id, capituloActivo.capitulo1)
            } else {
                // al capitulo activo tenemos que poner en la decisión 1 el id del nuevo capitulo que creemos
                // añadimos un nuevo capitulo a nuestra historia y guardamos que el capitulo padre es el actual
                //Primero guardamos el capitulo activo en una variable temporal
                var capituloTMP: Capitulo
                capituloTMP = capituloActivo
                // Despues creamos el capitulo nuevo y le indicamos el capitulo padre que es el que hemos guardado
                capituloActivo = databaseHelper.crearCapituloBD(db, aventuraNueva.id, capituloTMP.id)
                // Al capitulo que estabamos antes actualizamos el campo decisión1 con el id del nuevo capitulo
                capituloTMP.capitulo1 = capituloActivo.id
                // Pedimos al usuario que introduzca un texto que se mostrará en el botón para esta decisión
                pedirTexto(decision1Click , capituloTMP )
                // Añadimos el capitulo nuevo a la lista de la aventura
                aventuraNueva.listaCapitulos.add(capituloActivo)

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
                    databaseHelper.cargarCapitulo(db, aventuraNueva.id, capituloActivo.capitulo2)
            } else {
                // al capitulo activo tenemos que poner en la decisión 1 el id del nuevo capitulo que creemos
                // añadimos un nuevo capitulo a nuestra historia y guardamos que el capitulo padre es el actual
                //Primero guardamos el capitulo activo en una variable temporal
                var capituloTMP: Capitulo
                capituloTMP = capituloActivo
                // Despues creamos el capitulo nuevo y le indicamos el capitulo padre que es el que hemos guardado
                capituloActivo = databaseHelper.crearCapituloBD(db, aventuraNueva.id, capituloTMP.id)
                // Al capitulo que estabamos antes actualizamos el campo decisión2 con el id del nuevo capitulo
                capituloTMP.capitulo2 = capituloActivo.id
                // Pedimos al usuario que introduzca un texto que se mostrará en el botón para esta decisión
                pedirTexto(decision2Click , capituloTMP )
                // Añadimos el capitulo nuevo a la lista de la aventura
                aventuraNueva.listaCapitulos.add(capituloActivo)
            }

            // Mostrar el capitulo en pantalla
            cargarCapituloEnPantalla()
        }

        val atrasClick = findViewById(R.id.botonAtrasCA) as ImageButton
        atrasClick.setOnClickListener {
            // Al hacer click en el botón atrás navegamos al nodo padre, si ya estamos en el nodo raiz, no hacemos nada
            if (!capituloActivo.capituloPadre.equals("")) {
                capituloActivo = databaseHelper.cargarCapitulo(
                    db,
                    aventuraNueva.id,
                    capituloActivo.capituloPadre
                )
                cargarCapituloEnPantalla()
            }
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
                databaseHelper.actualizarCapitulo(db, aventuraNueva.id, capituloActivo)

            }
        })

    }

    private fun pedirTexto(botonPulsado : Button, capituloTMP:Capitulo) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.pedir_texto_dialog)

        val textoDecisionET = dialog.findViewById(R.id.textoDescisionDLG) as EditText

        val yesBtn = dialog.findViewById(R.id.aceptar_texto_dialog_BTN) as Button
        yesBtn.setOnClickListener  {
            val textoRespuestas = textoDecisionET.text.toString()

            if (textoRespuestas.equals("")) {
                Toast.makeText(this, "Debes introducir al menos una palabra", Toast.LENGTH_LONG).show()
            } else {
                when (botonPulsado.id) {
                    R.id.botonDecision1CA -> {
                        capituloTMP.textoOpcion1 = textoRespuestas

                    }
                    R.id.botonDecision2CA -> {
                        capituloTMP.textoOpcion2 = textoRespuestas

                    }
                }
                // actualizamos la base de datos
                databaseHelper.actualizarCapitulo(db,aventuraNueva.id, capituloTMP)
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
        editTextCrearAventura.setText(capituloActivo.textoCapitulo)
        botonDecision1CA.setText(capituloActivo.textoOpcion1)
        botonDecision2CA.setText(capituloActivo.textoOpcion2)
        Log.d("Miapp", "Hay " + aventuraNueva.listaCapitulos.size + " capitulos")

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
            imagenCrearAventura.setImageURI(data?.data)
        }
    }


}


