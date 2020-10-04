package com.vivetuaventura.Utilidades

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.vivetuaventura.Interfaces.AventuraFirebaseCallback
import com.vivetuaventura.Interfaces.FirebaseCallback
import com.vivetuaventura.Interfaces.ImagenFirebaseCallback
import com.vivetuaventura.R
import com.vivetuaventura.SalvarPreferencias.DatabaseHelper
import com.vivetuaventura.modelos.Adventure
import com.vivetuaventura.modelos.Capitulo
import java.io.File

class FirebaseUtils (val context: Context) {

    private var listener: FirebaseCallback? = null //instance of your interface
    private var aventuraListener : AventuraFirebaseCallback? = null
    private var imageListener : ImagenFirebaseCallback? = null
    lateinit var storage: FirebaseStorage
    var contador = 0

    fun subirAventuraFirebase (db : SQLiteDatabase, adventure : Adventure, usuario : String) {

        val databaseHelper = DatabaseHelper(context)
        var firebaseDatabase: FirebaseFirestore = FirebaseFirestore.getInstance()
        var listaCapitulos: MutableList<Capitulo> = mutableListOf()
        listaCapitulos = databaseHelper.cargarCapitulos(db, adventure.id)
        adventure.listaCapitulos = listaCapitulos

        // Guardar en firebase
        firebaseDatabase.collection(usuario).document(adventure.id)
            .set(adventure)
            .addOnSuccessListener { documentReference ->
                Log.d("Miapp", "DocumentSnapshot added with ID: ${adventure.id}")
            }
            .addOnFailureListener { e ->
                Log.w("Miapp", "Error adding document", e)
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
                Log.d("Miapp" , "Error al subir foto a Firebase")
            }.addOnSuccessListener { taskSnapshot ->
                // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                // ...
                Log.d("Miapp" , "Subida foto a Firebase")
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
            Log.d("Miapp" , "Error al descargar imagen")
        }
    }

    fun recuperarAventuraFirebase  (usuario: String , idAventura:String)  {

        var  aventuraCargada = Adventure()

        var firebaseDatabase: FirebaseFirestore = FirebaseFirestore.getInstance()

        val docRef = firebaseDatabase.collection(usuario).document(idAventura)
        docRef.get().addOnSuccessListener { documentSnapshot ->
            aventuraCargada = documentSnapshot.toObject(Adventure::class.java)!!
            aventuraListener!!.onAventuraLoaded(aventuraCargada)
        }


    }
    
    
    fun recuperarListaAventurasFirebase (usuario: String) : MutableList<Adventure> {
        
        var listaAventuras : MutableList<Adventure> = mutableListOf()
        var firebaseDatabase: FirebaseFirestore = FirebaseFirestore.getInstance()

        firebaseDatabase.collection(usuario)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val aventuraTMP = document.toObject(Adventure::class.java)
                    Log.d("Miapp" , "Aventura ${document.id} del usuario ${usuario}")
                    listaAventuras.add(aventuraTMP)
                }

                // Comunicamos al interface que ha terminado la tarea y devolvemos los datos
                listener!!.onListLoaded(listaAventuras)
            }

        return listaAventuras
        
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

    /*
    fun getJobsOnADate(Date date) {
        ...
    }
     */


}