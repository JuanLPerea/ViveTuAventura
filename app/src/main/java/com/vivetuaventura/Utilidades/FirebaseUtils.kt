package com.vivetuaventura.Utilidades

import android.content.Context
import android.content.res.Resources
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.vivetuaventura.Interfaces.AventuraFirebaseCallback
import com.vivetuaventura.Interfaces.FirebaseCallback
import com.vivetuaventura.Interfaces.ImagenFirebaseCallback
import com.vivetuaventura.Interfaces.NumeroAventurasCallback
import com.vivetuaventura.R
import com.vivetuaventura.SalvarPreferencias.DatabaseHelper
import com.vivetuaventura.modelos.Adventure
import com.vivetuaventura.modelos.Capitulo
import java.io.File

class FirebaseUtils (val context: Context) {

    private var listener: FirebaseCallback? = null //instance of your interface
    private var aventuraListener : AventuraFirebaseCallback? = null
    private var imageListener : ImagenFirebaseCallback? = null
    private var numeroAventurasUsuarioListener : NumeroAventurasCallback? = null
    lateinit var storage: FirebaseStorage
    var contador = 0

    fun subirAventuraFirebase (db : SQLiteDatabase, adventure : Adventure) {

        val databaseHelper = DatabaseHelper(context)
        var firebaseDatabase: FirebaseFirestore = FirebaseFirestore.getInstance()
        var listaCapitulos: MutableList<Capitulo> = mutableListOf()
        listaCapitulos = databaseHelper.cargarCapitulos(db, adventure.id)
        adventure.listaCapitulos = listaCapitulos

        // Guardar en firebase
        firebaseDatabase.collection("AVENTURAS").document(adventure.id)
            .set(adventure)
            .addOnSuccessListener { documentReference ->
//                Log.d("Miapp", "DocumentSnapshot added with ID: ${adventure.id}")
                Toast.makeText(context, "Historia Subida, pendiente de publicación.", Toast.LENGTH_LONG).show()
                // Actualizamos la base de datos local para saber que esta aventura ya está publicada
                adventure.publicado = true
                databaseHelper.actualizarAventura(db , adventure)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error $e", Toast.LENGTH_LONG).show()
                Log.d("Miapp", "Error adding document", e)
            }
    }

    fun subirImagenesFirebase (listaCapitulos: MutableList<Capitulo> , idAventura: String) {
        storage = FirebaseStorage.getInstance()

        if (contador < listaCapitulos.size) {
            val storageRef = storage.getReference()
            var file = Uri.fromFile(File(listaCapitulos.get(contador).imagenCapitulo))
            val idCapitulo = listaCapitulos.get(contador).id
            val imageRef = storageRef.child("images/" + idAventura + "/" + idCapitulo + ".jpg")
            var uploadTask = imageRef.putFile(file)

            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener {
                // Handle unsuccessful uploads
                //Log.d("Miapp" , "Error al subir foto a Firebase")
                Toast.makeText(context, "Error al subir imágenes", Toast.LENGTH_LONG).show()
            }.addOnSuccessListener { taskSnapshot ->
                // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                // ...
                //Log.d("Miapp" , "Subida foto a Firebase")
                Toast.makeText(context, "Imágenes subidas", Toast.LENGTH_LONG).show()
                contador++
                subirImagenesFirebase(listaCapitulos, idAventura)
            }
        } else if (contador ==  listaCapitulos.size) {
            contador = 0
        }
    }

    fun cargarImagenFirebase (idAventura: String, idCapitulo : String)  {
        storage = FirebaseStorage.getInstance()
        val storageRef = storage.getReference()
        val imageRef = storageRef.child("images/" + idAventura + "/" + idCapitulo + ".jpg")
        val ONE_MEGABYTE: Long = 1024 * 1024
        imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener { bytes ->
            val bitmap = BitmapFactory.decodeByteArray(bytes,0, bytes.size)
            imageListener!!.onImageLoaded(bitmap)
        }.addOnFailureListener{
            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.sinimagen)
            imageListener!!.onImageLoaded(bitmap)
            Log.d("Miapp" , "Error al descargar imagen")
        }
    }

    fun recuperarAventuraFirebase  (idAventura:String)  {
        var  aventuraCargada = Adventure()
        var firebaseDatabase: FirebaseFirestore = FirebaseFirestore.getInstance()
        val docRef = firebaseDatabase.collection("AVENTURAS").document(idAventura)
        docRef.get().addOnSuccessListener { documentSnapshot ->
            aventuraCargada = documentSnapshot.toObject(Adventure::class.java)!!
            aventuraListener!!.onAventuraLoaded(aventuraCargada)
        }
    }

    fun getNumAventurasUsuario (usuario : String) {

        var firebaseDatabase: FirebaseFirestore = FirebaseFirestore.getInstance()
        var numeroAventuras = 0

        firebaseDatabase.collection("AVENTURAS")
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        val aventuraTMP = document.toObject(Adventure::class.java)
                        if (aventuraTMP.usuario.equals(usuario)) {
                            numeroAventuras++
                        }
                    }
                    numeroAventurasUsuarioListener!!.NumeroAventurasUsuario(numeroAventuras)
                    }
                .addOnFailureListener {exception ->
                    Toast.makeText(context, "Error al publicar aventura" , Toast.LENGTH_LONG).show()
                    Log.d("Miapp" , "Error: " + exception)
                }
    }



    fun recuperarListaAventurasFirebase (nombreAventura:String, autorAventura:String, soloNoPublicados:Boolean) {

        var listaAventuras : MutableList<Adventure> = mutableListOf()
        var firebaseDatabase: FirebaseFirestore = FirebaseFirestore.getInstance()

        firebaseDatabase.collection("AVENTURAS")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val aventuraTMP = document.toObject(Adventure::class.java)

                    // Filtrar ...........................
                    //

                    var filtrarAdd = false
                    // Si no filtramos por ningún criterio, añadir siempre
                    if (nombreAventura.equals("") && autorAventura.equals("")) {
                        filtrarAdd = true
                    } else {
                        // si filtramos por nombre, añadir si contiene el texto buscado
                        if (!nombreAventura.equals("") && aventuraTMP.nombreAventura.contains(nombreAventura)) {
                            filtrarAdd = true
                        }
                        // si filtramos por autor, añadir si contiene el texto buscado
                        if (!autorAventura.equals("") && aventuraTMP.creador.contains(autorAventura)) {
                            filtrarAdd = true
                        }
                    }
                    // Si queremos ver solo los no publicados (No tiene en cuenta si se ha filtrado por nombre o autor
                    if (soloNoPublicados && !aventuraTMP.publicado) filtrarAdd = true

                    if (filtrarAdd) listaAventuras.add(aventuraTMP)

                }
                // Comunicamos al interface que ha terminado la tarea y devolvemos los datos
                listener!!.onListLoaded(listaAventuras)
            }
                .addOnFailureListener {
                    Log.d("Miapp" , "Error al descargar lista")
                }

    }


    fun setListener(listener : FirebaseCallback) {
        this.listener = listener
    }

    fun setAventuraListener(listenerAventura : AventuraFirebaseCallback) {
        this.aventuraListener = listenerAventura
    }

    fun setImageListener(listenerImagenes : ImagenFirebaseCallback) {
        this.imageListener = listenerImagenes
    }

    fun setNumeroAventurasListener(numeroAventurasCallback: NumeroAventurasCallback) {
        this.numeroAventurasUsuarioListener = numeroAventurasCallback
    }

    fun actualizarNotaAventura( aventura: Adventure) {
        var firebaseDatabase: FirebaseFirestore = FirebaseFirestore.getInstance()
        firebaseDatabase.collection("AVENTURAS").document(aventura.id)
                .update("nota" , aventura.nota)
                .addOnSuccessListener {
                    Log.d("Miapp" , "Nota actualizada")
                }
                .addOnFailureListener { e ->
                    Log.d("Miapp" , "Error: $e")
                }
    }

    fun actualizarVisitasAventura(aventura: Adventure) {
        var firebaseDatabase: FirebaseFirestore = FirebaseFirestore.getInstance()
        firebaseDatabase.collection("AVENTURAS").document(aventura.id)
                .update("visitas" , aventura.visitas)
                .addOnSuccessListener {
                    Log.d("Miapp" , "Nota actualizada")
                }
                .addOnFailureListener { e ->
                    Log.d("Miapp" , "Error: $e")
                }
    }


}