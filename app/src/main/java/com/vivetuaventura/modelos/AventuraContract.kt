package com.vivetuaventura.modelos

import android.provider.BaseColumns

object AventuraContract {
    // Tabla para guardar los datos de nuestras aventuras en SQLITE
    object AventuraEntry : BaseColumns {
        const val ID = 0
        const val NOMBRE_AVENTURA = "entry"
        const val CREADOR = "entry"
        const val VISITAS = 0
        const val NOTA = 0
        const val PUBLICADO = false
    }

    // TABLA QUE CONTIENE LOS CAPITULOS DE CADA AVENTURA
    object  CapitulosEntry : BaseColumns {
        const val ID = 0
        const val NOMBRE_AVENTURA = "entry"
        const val CAPITULO_PADRE = 0
        const val CAPITULO_1 = 0
        const val CAPITULO_2 = 0
        const val TEXTO_CAPITULO = "TEXTO"
        const val IMAGEN_CAPITULO = "IMAGEN"
        const val FIN_HISTORIA = false
    }
}