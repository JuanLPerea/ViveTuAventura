package com.vivetuaventura.Interfaces

import com.vivetuaventura.modelos.Adventure

interface FirebaseCallback {
    fun onListLoaded(listaAventuras : MutableList<Adventure>)
}