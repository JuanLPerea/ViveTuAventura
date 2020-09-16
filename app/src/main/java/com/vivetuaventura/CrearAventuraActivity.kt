package com.vivetuaventura

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.vivetuaventura.SalvarPreferencias.DatabaseHelper
import com.vivetuaventura.modelos.Aventura
import com.vivetuaventura.modelos.Capitulo
import kotlinx.android.synthetic.main.activity_crear_aventura.*


class CrearAventuraActivity : AppCompatActivity() {

    var capituloActual = 0
    lateinit var databaseHelper: DatabaseHelper
    lateinit var db : SQLiteDatabase
    lateinit var capituloActivo : Capitulo

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
        capituloActivo = Capitulo(aventuraNueva.id, 0, 0, 0, 0, "","imagenURL", false)
        aventuraNueva.listaCapitulos.add(capituloActivo)

        // Guardamos el capitulo nuevo en la BD
        databaseHelper.guardarCapitulo(db, aventuraNueva.id, capituloActivo)

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
            val indiceNuevoCapitulo = aventuraNueva.listaCapitulos.size

            // Comprobar si ya hay un capitulo en esta posición
            // Si existe cargamos el capitulo existente
            if (aventuraNueva.listaCapitulos.get(capituloActual).capitulo1 != 0) {
                capituloActual = aventuraNueva.listaCapitulos.get(capituloActual).capitulo1
            } else {

                // Si no lo hay añadimos un nuevo nodo a nuestra historia y guardamos que el capitulo padre es el actual
                aventuraNueva.listaCapitulos.add(
                    Capitulo(
                        aventuraNueva.id,
                        indiceNuevoCapitulo,
                        0,
                        0,
                        0,
                        "" ,
                        "" ,
                        false
                    )
                )
                aventuraNueva.listaCapitulos.get(indiceNuevoCapitulo).capituloPadre = capituloActual

                // Al capitulo actual le ponemos que el nodo1 es el nuevo capitulo creado
                aventuraNueva.listaCapitulos.get(capituloActual).capitulo1 = indiceNuevoCapitulo
                // ahora el capitulo actual será el nuevo capitulo creado
                capituloActual = indiceNuevoCapitulo
            }

            // lo mostramos en pantalla
            cargarCapituloEnPantalla()


        }

        val editarTextoListener = findViewById(R.id.editTextCrearAventura) as EditText
        editarTextoListener.addTextChangedListener (object : TextWatcher {
            override fun afterTextChanged(s : Editable) {}
            override fun beforeTextChanged (s : CharSequence, start : Int , count : Int, after : Int) {
            }
            override fun onTextChanged (s : CharSequence, start : Int, before : Int, count : Int) {
                   // guardar cambios cuando editemos el texto
                Log.d("Miapp" , "Texto cambiado")
                val textoView = editTextCrearAventura.text.toString()
                aventuraNueva.listaCapitulos.get(capituloActual).textoCapitulo = textoView
                databaseHelper.actualizarCapitulo(db, aventuraNueva.id, aventuraNueva.listaCapitulos.get(capituloActual))

            }
        })


    }


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


    fun cargarCapituloEnPantalla() {

    }

}


