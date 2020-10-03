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
import com.vivetuaventura.R
import com.vivetuaventura.SalvarPreferencias.DatabaseHelper
import com.vivetuaventura.modelos.Adventure
import com.vivetuaventura.modelos.Capitulo
import java.io.File

class FirebaseUtils (val context: Context) {

    private var listener: FirebaseCallback? = null //instance of your interface
    private var aventuraListener : AventuraFirebaseCallback? = null
    lateinit var storage: FirebaseStorage


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

        // Guardar imágenes en Firebase Storage
        storage = FirebaseStorage.getInstance()
        val storageRef = storage.getReference()


        var file = Uri.fromFile(File(adventure.listaCapitulos.get(0).imagenCapitulo))
        val imageRef = storageRef.child("images/${file.lastPathSegment}")
        var uploadTask = imageRef.putFile(file)

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
            Log.d("Miapp" , "Error al subir foto a Firebase")
        }.addOnSuccessListener { taskSnapshot ->
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...
            Log.d("Miapp" , "Subida foto a Firebase")
        }

    }

    fun recuperarAventuraFirebase  (usuario: String , idAventura:String) : Adventure {

        var  aventuraCargada = Adventure()

        var firebaseDatabase: FirebaseFirestore = FirebaseFirestore.getInstance()

        val docRef = firebaseDatabase.collection(usuario).document(idAventura)
        docRef.get().addOnSuccessListener { documentSnapshot ->
            aventuraCargada = documentSnapshot.toObject(Adventure::class.java)!!
            aventuraListener!!.onAventuraLoaded(aventuraCargada)
        }

        return  aventuraCargada
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

    fun subirImagenesFirebase (db: SQLiteDatabase , usuario: String , idAventura: String) {
        // TODO recuperar las imágenes que correspondan a una aventura y subirlas a Firebase

    }

    fun cargarImagenFirebase (usuario: String , idAventura: String, idImagen : String) : Bitmap {
        // TODO cargar una imágen de Firebase (Necesario Interface para pasar los datos cuando finalice la tarea)
        var bitmapCargado : Bitmap

            bitmapCargado = BitmapFactory.decodeResource(context.resources , R.drawable.brujula)

        return bitmapCargado

    }


    fun setListener(listener : FirebaseCallback) {
        this.listener = listener
    }

    fun setAventuraListener(listenerAventura : AventuraFirebaseCallback) {
        this.aventuraListener = listenerAventura
    }

    /*
    fun getJobsOnADate(Date date) {
        ...
    }
     */


}