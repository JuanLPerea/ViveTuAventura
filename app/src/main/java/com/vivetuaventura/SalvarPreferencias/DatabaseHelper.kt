package com.vivetuaventura.SalvarPreferencias

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.vivetuaventura.modelos.Aventura
import com.vivetuaventura.modelos.AventuraContract
import com.vivetuaventura.modelos.Capitulo

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "DB_AVENTURAS", null, 1) {

    override fun onCreate(db: SQLiteDatabase?) {
        Log.d("MIAPP", "Crear BD")
        val CREATE_TABLE_AVENTURA =
            "CREATE TABLE AVENTURA (ID TEXT, NOMBRE TEXT, CREADOR TEXT, NOTA INTEGER, PUBLICADO BOOLEAN, VISITAS INTEGER)"
        val CREATE_TABLE_CAPITULOS =
            "CREATE TABLE CAPITULOS (ID INTEGER, IDAVENTURA TEXT,  CAPITULOPADRE INTEGER, CAPITULO1 INTEGER, CAPITULO2 INTEGER, TEXTOCAPITULO TEXT, IMAGENCAPITULO TEXT, FINHISTORIA BOOLEAN)"
        db!!.execSQL(CREATE_TABLE_AVENTURA)
        db!!.execSQL(CREATE_TABLE_CAPITULOS)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val DROP_TABLE = "DROP TABLE IF EXISTS " + AventuraContract.AventuraEntry.NOMBRE_AVENTURA
        db!!.execSQL(DROP_TABLE)
        onCreate(db)
    }

    fun crearAventuraBD(db: SQLiteDatabase?, nombreAventura: String, autorAventura: String): String {

        val rnds = (0..100000).random()
        val aventuraUUID =
            nombreAventura + "_" + System.currentTimeMillis().toString() + "_" + rnds.toString()

        val ADD_NODO = "INSERT INTO AVENTURA VALUES ('" + aventuraUUID + "' , '" + nombreAventura + "' , '" + autorAventura + "' , 0 , 0, 0)"

        db!!.execSQL(ADD_NODO)

        return aventuraUUID
        Log.d("MIAPP", "Creada Aventura en la BD")
    }


    fun cargarListaAventurasBD(db: SQLiteDatabase): MutableList<Aventura> {

        var listaAventuras: MutableList<Aventura> = mutableListOf()

        val datosBruto = db.rawQuery("SELECT * FROM AVENTURA", null)

        if (datosBruto!!.moveToFirst()) {
            do {
                val idTMP = datosBruto.getString(datosBruto.getColumnIndex("ID"))
                val nombreTMP = datosBruto.getString(datosBruto.getColumnIndex("NOMBRE"))
                val creadorTMP = datosBruto.getString(datosBruto.getColumnIndex("CREADOR"))
                val notaTMP = datosBruto.getInt(datosBruto.getColumnIndex("NOTA"))
                val publicadoTMP = datosBruto.getInt(datosBruto.getColumnIndex("PUBLICADO"))
                val visitasTMP = datosBruto.getInt(datosBruto.getColumnIndex("VISITAS"))

                val aventuraTMP: Aventura =
                    Aventura(idTMP, nombreTMP, creadorTMP, visitasTMP, notaTMP)

                listaAventuras.add(aventuraTMP)


            } while (datosBruto.moveToNext())
        }

        datosBruto.close()

        return listaAventuras
    }

    fun eliminarAventuraBD(db: SQLiteDatabase, id: String) {
        db.delete("AVENTURA", "  ID = '" + id + "'", null)
      //  Log.d("Miapp", "Borrar linea de la tabla de aventuras")
    }

    fun recuperarAventura(db: SQLiteDatabase, idAventura: String): Aventura {
        var aventuraTMP: Aventura
        aventuraTMP = Aventura("Vacio", "Vacio", "Vacio", 0, 0)
        val datosBruto = db.rawQuery("SELECT * FROM AVENTURA WHERE ID ='" + idAventura + "'", null)
        if (datosBruto!!.moveToFirst()) {
            val idTMP = datosBruto.getString(datosBruto.getColumnIndex("ID"))
            val nombreTMP = datosBruto.getString(datosBruto.getColumnIndex("NOMBRE"))
            val creadorTMP = datosBruto.getString(datosBruto.getColumnIndex("CREADOR"))
            val notaTMP = datosBruto.getInt(datosBruto.getColumnIndex("NOTA"))
            val publicadoTMP = datosBruto.getInt(datosBruto.getColumnIndex("PUBLICADO"))
            val visitasTMP = datosBruto.getInt(datosBruto.getColumnIndex("VISITAS"))
            aventuraTMP = Aventura(idTMP, nombreTMP, creadorTMP, visitasTMP, notaTMP)
        }
        datosBruto.close()
        return aventuraTMP

    }

    fun guardarCapitulo(db: SQLiteDatabase, idAventura: String, capitulo: Capitulo) {
        var finhistoria = 0
        if (capitulo.finHistoria == true) finhistoria = 1
        val ADD_CAPITULO = "INSERT INTO CAPITULOS VALUES ('" + capitulo.id + "' , '" + idAventura + "' , " + capitulo.capituloPadre  + " , " + capitulo.capitulo1  + " , " + capitulo.capitulo2   + " , '" + capitulo.textoCapitulo  + "' , '" + capitulo.imagenCapitulo + "' , " + finhistoria + ")"
        db!!.execSQL(ADD_CAPITULO)

    }

    fun actualizarCapitulo(db: SQLiteDatabase, idAventura: String, capitulo: Capitulo) {
        var finhistoria = 0
        if (capitulo.finHistoria == true) finhistoria = 1
        val UPDATE_CAPITULO = "UPDATE CAPITULOS SET ID = '" + capitulo.id + "' , IDAVENTURA = '" + idAventura + "' , CAPITULOPADRE = " +  capitulo.capituloPadre + " , CAPITULO1 = " + capitulo.capitulo1 + " , CAPITULO2 = " + capitulo.capitulo2  + " , TEXTOCAPITULO = '" + capitulo.textoCapitulo + "' , IMAGENCAPITULO = '" + capitulo.imagenCapitulo + "' , FINHISTORIA = " + finhistoria + " WHERE  IDAVENTURA = '" + idAventura + "' AND ID = '" + capitulo.id + "'"
        db!!.execSQL(UPDATE_CAPITULO)
    }

    fun cargarCapitulos(db: SQLiteDatabase, idAventura: String) : MutableList<Capitulo> {

        var listaCapitulos : MutableList<Capitulo> = mutableListOf()


        val datosBruto = db.rawQuery("SELECT * FROM CAPITULOS WHERE IDAVENTURA = '" + idAventura + "'", null)

        if (datosBruto!!.moveToFirst()) {
            do {
                val idTMP = datosBruto.getInt(datosBruto.getColumnIndex("ID"))
                val idAventuraTMP = datosBruto.getString(datosBruto.getColumnIndex("IDAVENTURA"))
                val capituloPadreTMP = datosBruto.getInt(datosBruto.getColumnIndex("CAPITULOPADRE"))
                val capitulo1 = datosBruto.getInt(datosBruto.getColumnIndex("CAPITULO1"))
                val capitulo2 = datosBruto.getInt(datosBruto.getColumnIndex("CAPITULO2"))
                val textoTMP = datosBruto.getString(datosBruto.getColumnIndex("TEXTOCAPITULO"))
                val imagenTMP = datosBruto.getString(datosBruto.getColumnIndex("IMAGENCAPITULO"))
                val finhistoriaTMP = datosBruto.getInt(datosBruto.getColumnIndex("FINHISTORIA"))

                var finhistoria = false
                if (finhistoriaTMP == 1) finhistoria = true

                val capituloTMP: Capitulo =
                    Capitulo(idAventuraTMP, idTMP, capituloPadreTMP, capitulo1, capitulo2, textoTMP, imagenTMP, finhistoria)

                listaCapitulos.add(capituloTMP)

            } while (datosBruto.moveToNext())
        }

        datosBruto.close()




        return  listaCapitulos
    }


}