package com.vivetuaventura

import android.app.Application
import com.vivetuaventura.SalvarPreferencias.GuardarAventuraPrefs

class SharedApp : Application (){
    companion object {
        lateinit var prefs : GuardarAventuraPrefs
    }

    override fun OnCreate() {
        super.onCreate()
        prefs = GuardarAventuraPrefs(applicationContext)
    }
}