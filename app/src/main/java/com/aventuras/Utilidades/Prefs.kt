package com.aventuras.Utilidades

import android.content.Context
import android.content.SharedPreferences

class Prefs (context : Context){
    val PREFS_NAME = "aventuras.sharedpreferences"
    val SHARED_NAME = "primera_ejecucion"
    val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, 0)
    var primeraEjecucion: Boolean?
        get() = prefs.getBoolean(SHARED_NAME, true)
        set(value) = prefs.edit().putBoolean(SHARED_NAME, value!!).apply()
}