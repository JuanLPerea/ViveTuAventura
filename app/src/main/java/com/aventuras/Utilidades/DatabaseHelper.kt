package com.aventuras.SalvarPreferencias

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.aventuras.modelos.Adventure
import com.aventuras.modelos.AventuraContract
import com.aventuras.modelos.Capitulo

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "DB_AVENTURAS", null, 1) {

    override fun onCreate(db: SQLiteDatabase?) {
        Log.d("MIAPP", "Crear BD")
        val CREATE_TABLE_AVENTURA =
            "CREATE TABLE AVENTURA (ID TEXT, NOMBRE TEXT, CREADOR TEXT, NOTA INTEGER, PUBLICADO BOOLEAN, VISITAS INTEGER, USUARIO TEXT)"
        val CREATE_TABLE_CAPITULOS =
            "CREATE TABLE CAPITULOS ( IDAVENTURA TEXT, ID TEXT, CAPITULOPADRE TEXT, CAPITULO1 TEXT, TEXTOOPCION1 TEXT,CAPITULO2 TEXT, TEXTOOPCION2 TEXT, TEXTOCAPITULO TEXT, IMAGENCAPITULO TEXT, FINHISTORIA BOOLEAN)"
        val CREATE_TABLE_VOTOS = "CREATE TABLE VOTOS (IDAVENTURA TEXT)"
        db!!.execSQL(CREATE_TABLE_AVENTURA)
        db!!.execSQL(CREATE_TABLE_CAPITULOS)
        db!!.execSQL(CREATE_TABLE_VOTOS)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val DROP_TABLE = "DROP TABLE IF EXISTS " + AventuraContract.AventuraEntry.NOMBRE_AVENTURA
        db!!.execSQL(DROP_TABLE)
        onCreate(db)
    }

    fun crearAventuraBD(db: SQLiteDatabase?, nombreAventura: String, autorAventura: String , usuario:String): String {

        val rnds = (0..100000).random()
        val aventuraUUID =
            nombreAventura + "_" + System.currentTimeMillis().toString() + "_" + rnds.toString()

        val ADD_NODO = "INSERT INTO AVENTURA VALUES ('" + aventuraUUID + "' , '" + nombreAventura + "' , '" + autorAventura + "' , 0 , 0, 0, '" + usuario + "')"

        db!!.execSQL(ADD_NODO)

        return aventuraUUID
        Log.d("MIAPP", "Creada Aventura en la BD")
    }

    fun actualizarAventura(db: SQLiteDatabase?, adventure:Adventure) {
        var publicado = 0
        if (adventure.publicado == true) publicado = 1
        val UPDATE_AVENTURA = "UPDATE AVENTURA SET ID = '" + adventure.id + "' , NOMBRE = '" + adventure.nombreAventura + "' , CREADOR = '" +  adventure.creador + "' , NOTA = " + adventure.nota +", PUBLICADO = " + publicado + " , VISITAS = "  + adventure.visitas + ", USUARIO = '" + adventure.usuario + "' WHERE ID = '" + adventure.id + "'"
        db!!.execSQL(UPDATE_AVENTURA)
    }

    fun aventurasVotadasAdd (db: SQLiteDatabase , adventure: Adventure) {
        val UPDATE_AVENTURA = "INSERT INTO VOTOS VALUES ('" + adventure.id + "')"
        db!!.execSQL(UPDATE_AVENTURA)
    }


    fun aventuraYaVotada(db: SQLiteDatabase, idAventura: String) : Boolean {
        var votada = false
        val datosBruto = db.rawQuery("SELECT * FROM VOTOS WHERE IDAVENTURA ='" + idAventura + "'", null)
        if (datosBruto!!.moveToFirst()) {
            votada = true
        }
        return votada
    }


    fun cargarListaAventurasBD(db: SQLiteDatabase, nombreAventura: String, autorAventura: String, soloNoPublicados : Boolean): MutableList<Adventure> {

        var listaAdventures: MutableList<Adventure> = mutableListOf()

        val datosBruto = db.rawQuery("SELECT * FROM AVENTURA", null)

        if (datosBruto!!.moveToFirst()) {
            do {
                val idTMP = datosBruto.getString(datosBruto.getColumnIndex("ID"))
                val nombreTMP = datosBruto.getString(datosBruto.getColumnIndex("NOMBRE"))
                val creadorTMP = datosBruto.getString(datosBruto.getColumnIndex("CREADOR"))
                val notaTMP = datosBruto.getInt(datosBruto.getColumnIndex("NOTA"))
                val publicadoTMP = datosBruto.getInt(datosBruto.getColumnIndex("PUBLICADO"))
                val visitasTMP = datosBruto.getInt(datosBruto.getColumnIndex("VISITAS"))
                val usuarioTMP = datosBruto.getString(datosBruto.getColumnIndex("USUARIO"))

                val adventureTMP = Adventure()
                adventureTMP.id = idTMP
                adventureTMP.nombreAventura = nombreTMP
                adventureTMP.creador = creadorTMP
                adventureTMP.nota = notaTMP
                adventureTMP.usuario = usuarioTMP
                if (publicadoTMP == 0) {
                    adventureTMP.publicado = false
                } else {
                    adventureTMP.publicado = true
                }
                adventureTMP.visitas = visitasTMP

                // Filtrar ...........................
                //

                var filtrarAdd = false
                // Si no filtramos por ningún criterio, añadir siempre
                if (nombreAventura.equals("") && autorAventura.equals("")) {
                    filtrarAdd = true
                } else {
                    // si filtramos por nombre, añadir si contiene el texto buscado
                    if (!nombreAventura.equals("") && adventureTMP.nombreAventura.contains(nombreAventura)) {
                        filtrarAdd = true
                    }
                    // si filtramos por autor, añadir si contiene el texto buscado
                    if (!autorAventura.equals("") && adventureTMP.creador.contains(autorAventura)) {
                        filtrarAdd = true
                    }
                }
                // Si queremos ver solo los no publicados (No tiene en cuenta si se ha filtrado por nombre o autor
                if (soloNoPublicados && !adventureTMP.publicado) filtrarAdd = true

                if (filtrarAdd) listaAdventures.add(adventureTMP)


            } while (datosBruto.moveToNext())
        }

        datosBruto.close()

        return listaAdventures
    }

    fun eliminarAventuraBD(db: SQLiteDatabase, id: String) {
        // Borramos en la tabla de aventura y la de capitulos todas las filas que le pertenezcan
        db.delete("AVENTURA", "  ID = '" + id + "'", null)
        db.delete("CAPITULOS", "IDAVENTURA = '" + id + "'", null)
      //  Log.d("Miapp", "Borrar linea de la tabla de aventuras")
    }

    fun recuperarAventura(db: SQLiteDatabase, idAventura: String): Adventure {
        var adventureTMP: Adventure
        adventureTMP = Adventure()
        val datosBruto = db.rawQuery("SELECT * FROM AVENTURA WHERE ID ='" + idAventura + "'", null)
        if (datosBruto!!.moveToFirst()) {
            val idTMP = datosBruto.getString(datosBruto.getColumnIndex("ID"))
            val nombreTMP = datosBruto.getString(datosBruto.getColumnIndex("NOMBRE"))
            val creadorTMP = datosBruto.getString(datosBruto.getColumnIndex("CREADOR"))
            val notaTMP = datosBruto.getInt(datosBruto.getColumnIndex("NOTA"))
            val publicadoTMP = datosBruto.getInt(datosBruto.getColumnIndex("PUBLICADO"))
            val visitasTMP = datosBruto.getInt(datosBruto.getColumnIndex("VISITAS"))
            val usuarioTMP = datosBruto.getString(datosBruto.getColumnIndex("USUARIO"))

            adventureTMP.id = idTMP
            adventureTMP.nombreAventura = nombreTMP
            adventureTMP.creador = creadorTMP
            adventureTMP.nota = notaTMP
            adventureTMP.usuario = usuarioTMP
            if (publicadoTMP == 0) {
                adventureTMP.publicado = false
            } else {
                adventureTMP.publicado = true
            }
            adventureTMP.visitas = visitasTMP
        }

        datosBruto.close()
        return adventureTMP

    }


    fun actualizarCapitulo(db: SQLiteDatabase, idAventura: String, capitulo: Capitulo) {
        var finhistoria = 0
        if (capitulo.finHistoria == true) finhistoria = 1
        val UPDATE_CAPITULO = "UPDATE CAPITULOS SET ID = '" + capitulo.id + "' , IDAVENTURA = '" + idAventura + "' , CAPITULOPADRE = '" +  capitulo.capituloPadre + "' , CAPITULO1 = '" + capitulo.capitulo1 +"', TEXTOOPCION1 = '" + capitulo.textoOpcion1 + "' , CAPITULO2 = '"  + capitulo.capitulo2 + "', TEXTOOPCION2 = '" + capitulo.textoOpcion2  + "' , TEXTOCAPITULO = '" + capitulo.textoCapitulo + "' , IMAGENCAPITULO = '" + capitulo.imagenCapitulo + "' , FINHISTORIA = " + finhistoria + " WHERE  IDAVENTURA = '" + idAventura + "' AND ID = '" + capitulo.id + "'"
        db!!.execSQL(UPDATE_CAPITULO)
    }

    fun cargarCapitulos(db: SQLiteDatabase, idAventura: String) : MutableList<Capitulo> {
        var listaCapitulos : MutableList<Capitulo> = mutableListOf()

        val datosBruto = db.rawQuery("SELECT * FROM CAPITULOS WHERE IDAVENTURA = '" + idAventura + "'", null)

        if (datosBruto!!.moveToFirst()) {
            do {
                val idTMP = datosBruto.getString(datosBruto.getColumnIndex("ID"))
                val idAventuraTMP = datosBruto.getString(datosBruto.getColumnIndex("IDAVENTURA"))
                val capituloPadreTMP = datosBruto.getString(datosBruto.getColumnIndex("CAPITULOPADRE"))
                val capitulo1 = datosBruto.getString(datosBruto.getColumnIndex("CAPITULO1"))
                val textoOpcion1 = datosBruto.getString(datosBruto.getColumnIndex("TEXTOOPCION1"))
                val capitulo2 = datosBruto.getString(datosBruto.getColumnIndex("CAPITULO2"))
                val textoOpcion2 = datosBruto.getString(datosBruto.getColumnIndex("TEXTOOPCION2"))
                val textoTMP = datosBruto.getString(datosBruto.getColumnIndex("TEXTOCAPITULO"))
                val imagenTMP = datosBruto.getString(datosBruto.getColumnIndex("IMAGENCAPITULO"))
                val finhistoriaTMP = datosBruto.getInt(datosBruto.getColumnIndex("FINHISTORIA"))

                var finhistoria = false
                if (finhistoriaTMP == 1) finhistoria = true

                val capituloTMP: Capitulo = Capitulo()
                capituloTMP.idAventura = idAventuraTMP
                capituloTMP.id = idTMP
                capituloTMP.capituloPadre = capituloPadreTMP
                capituloTMP.capitulo1 = capitulo1
                capituloTMP.textoOpcion1 = textoOpcion1
                capituloTMP.capitulo2 = capitulo2
                capituloTMP.textoOpcion2 = textoOpcion2
                capituloTMP.textoCapitulo = textoTMP
                capituloTMP.imagenCapitulo = imagenTMP
                capituloTMP.finHistoria = finhistoria

                listaCapitulos.add(capituloTMP)

            } while (datosBruto.moveToNext())
        }
        datosBruto.close()

        return  listaCapitulos
    }

    fun cargarCapitulo(db: SQLiteDatabase, idAventura: String, id: String) : Capitulo {

        val capituloRecuperado : Capitulo
        capituloRecuperado = Capitulo()
        capituloRecuperado.idAventura = idAventura
        capituloRecuperado.id = id

        val datosBruto = db.rawQuery("SELECT * FROM CAPITULOS WHERE IDAVENTURA ='" + idAventura + "' AND ID = '" + id + "'", null)
        if (datosBruto!!.moveToFirst()) {
            val capituloPadreTMP = datosBruto.getString(datosBruto.getColumnIndex("CAPITULOPADRE"))
            val capitulo1TMP = datosBruto.getString(datosBruto.getColumnIndex("CAPITULO1"))
            val textoOpcion1TMP = datosBruto.getString(datosBruto.getColumnIndex("TEXTOOPCION1"))
            val capitulo2TMP = datosBruto.getString(datosBruto.getColumnIndex("CAPITULO2"))
            val textoOpcion2TMP = datosBruto.getString(datosBruto.getColumnIndex("TEXTOOPCION2"))
            val textoTMP = datosBruto.getString(datosBruto.getColumnIndex("TEXTOCAPITULO"))
            val imagenTMP = datosBruto.getString(datosBruto.getColumnIndex("IMAGENCAPITULO"))
            val finhistoriaTMP = datosBruto.getInt(datosBruto.getColumnIndex("FINHISTORIA"))

            var finhistoria = false
            if (finhistoriaTMP == 1) finhistoria = true

            capituloRecuperado.capituloPadre = capituloPadreTMP
            capituloRecuperado.capitulo1 = capitulo1TMP
            capituloRecuperado.textoOpcion1 = textoOpcion1TMP
            capituloRecuperado.capitulo2 = capitulo2TMP
            capituloRecuperado.textoOpcion2 = textoOpcion2TMP
            capituloRecuperado.textoCapitulo = textoTMP
            capituloRecuperado.imagenCapitulo = imagenTMP
            capituloRecuperado.finHistoria = finhistoria
        }
        datosBruto.close()

        return capituloRecuperado

    }


    fun crearCapituloBD(db: SQLiteDatabase?, aventuraUUID: String , capituloPadreID:String): Capitulo {
        val nuevoCapitulo:Capitulo
        val rnds = (0..100000).random()
        val capituloUUID = aventuraUUID + "_CAPITULO_" + System.currentTimeMillis().toString() + "_" + rnds.toString()

        val ADD_NODO = "INSERT INTO CAPITULOS VALUES ('" + aventuraUUID + "' , '" + capituloUUID + "' , '" + capituloPadreID + "' , '' , '', '' , '' , '', '', 0)"
        db!!.execSQL(ADD_NODO)
        nuevoCapitulo = Capitulo()
        nuevoCapitulo.idAventura = aventuraUUID
        nuevoCapitulo.id = capituloUUID
        nuevoCapitulo.capituloPadre = capituloPadreID
        return nuevoCapitulo
    }

    fun borrarCapituloBD(db: SQLiteDatabase, aventuraUUID: String, capitulo: Capitulo) {

        // crear lista con capitulos que dependen del que vamos a borrar
        var listaCapitulosBorrar: MutableList<String> = mutableListOf()
        var capituloTMP:Capitulo
        var fin : Boolean = false
        capituloTMP = capitulo
        listaCapitulosBorrar.add(capituloTMP.id)
        // Recorrer el árbol e ir añadiendo los capitulos que dependan del que vamos a borrar
        do {

            if (!capituloTMP.capitulo1.equals("") && !listaCapitulosBorrar.contains(capituloTMP.capitulo1)) {
                // Miramos si hay algún nodo en la opción 1
                capituloTMP = cargarCapitulo(db, aventuraUUID, capituloTMP.capitulo1)
                listaCapitulosBorrar.add(capituloTMP.id)
            } else if (!capituloTMP.capitulo2.equals("") && !listaCapitulosBorrar.contains(capituloTMP.capitulo2)) {
                // Si no hay opcion 1 miramos si hay opción 2
                capituloTMP = cargarCapitulo(db, aventuraUUID, capituloTMP.capitulo2)
                listaCapitulosBorrar.add(capituloTMP.id)
            } else if (!capituloTMP.id.equals(capitulo.id)){
                // volver al nodo anterior
                capituloTMP = cargarCapitulo(db, aventuraUUID, capituloTMP.capituloPadre)
            }
            // el bucle termina en estos casos:
            // 1 - no hay opcion 1 ni 2 y estamos en el nodo en el que empezamos
            // 2 - hay opcion 1 pero está en la lista y no hay opcion 2
            // 3 - no hay opcion 1 y si hay en la 2 pero está en la lista
            // 4 - estamos en el nodo origen, hay opcion 1 y 2 pero están en la lista (solo comprobamos que los 2 estén en la lista, no que haya)
            if (capituloTMP.capitulo1.equals("") && capituloTMP.capitulo2.equals("") && capituloTMP.id.equals(capitulo.id)) fin = true
            if (!capituloTMP.capitulo1.equals("") && listaCapitulosBorrar.contains(capituloTMP.capitulo1) && capituloTMP.capitulo2.equals("") && capituloTMP.id.equals(capitulo.id)) fin = true
            if (capituloTMP.capitulo1.equals("") && listaCapitulosBorrar.contains(capituloTMP.capitulo2) && !capituloTMP.capitulo2.equals("") && capituloTMP.id.equals(capitulo.id)) fin = true
            if (listaCapitulosBorrar.contains(capituloTMP.capitulo1) && listaCapitulosBorrar.contains(capituloTMP.capitulo2) && capituloTMP.id.equals(capitulo.id)) fin = true

        } while (!fin)

        // borrar los capitulos en la base de datos
        for (capituloBorrado : String in listaCapitulosBorrar) {
            db.delete("CAPITULOS", "  ID = '" + capituloBorrado + "' AND IDAVENTURA = '" + aventuraUUID + "'", null)
        }

    }

    fun cargarCapituloRaiz (db: SQLiteDatabase, idAventura: String) : Capitulo {

        val capituloRecuperado : Capitulo
        capituloRecuperado = Capitulo()
        capituloRecuperado.idAventura = idAventura
        val datosBruto = db.rawQuery("SELECT * FROM CAPITULOS WHERE IDAVENTURA ='" + idAventura + "' AND CAPITULOPADRE = ''", null)
        if (datosBruto!!.moveToFirst()) {
            val capituloId = datosBruto.getString((datosBruto.getColumnIndex("ID")))
            val capituloPadreTMP = datosBruto.getString(datosBruto.getColumnIndex("CAPITULOPADRE"))
            val capitulo1TMP = datosBruto.getString(datosBruto.getColumnIndex("CAPITULO1"))
            val textoOpcion1TMP = datosBruto.getString(datosBruto.getColumnIndex("TEXTOOPCION1"))
            val capitulo2TMP = datosBruto.getString(datosBruto.getColumnIndex("CAPITULO2"))
            val textoOpcion2TMP = datosBruto.getString(datosBruto.getColumnIndex("TEXTOOPCION2"))
            val textoTMP = datosBruto.getString(datosBruto.getColumnIndex("TEXTOCAPITULO"))
            val imagenTMP = datosBruto.getString(datosBruto.getColumnIndex("IMAGENCAPITULO"))
            val finhistoriaTMP = datosBruto.getInt(datosBruto.getColumnIndex("FINHISTORIA"))

            var finhistoria = false
            if (finhistoriaTMP == 1) finhistoria = true

            capituloRecuperado.id = capituloId
            capituloRecuperado.capituloPadre = capituloPadreTMP
            capituloRecuperado.capitulo1 = capitulo1TMP
            capituloRecuperado.textoOpcion1 = textoOpcion1TMP
            capituloRecuperado.capitulo2 = capitulo2TMP
            capituloRecuperado.textoOpcion2 = textoOpcion2TMP
            capituloRecuperado.textoCapitulo = textoTMP
            capituloRecuperado.imagenCapitulo = imagenTMP
            capituloRecuperado.finHistoria = finhistoria
        }
        datosBruto.close()


        return capituloRecuperado
    }



}