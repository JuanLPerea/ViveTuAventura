package com.aventuras.Interfaces

import com.aventuras.modelos.Adventure

interface FirebaseCallback {
    fun onListLoaded(listaAventuras : MutableList<Adventure>)
}