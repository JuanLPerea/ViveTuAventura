package com.aventuras.Interfaces

import com.aventuras.modelos.Adventure

interface AventuraFirebaseCallback {
    fun onAventuraLoaded(aventura : Adventure)
}