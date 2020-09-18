package com.vivetuaventura.Utilidades

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.vivetuaventura.modelos.Capitulo
import java.io.*
import java.util.*


class ImagesHelper(context: Context) {

    fun guardarImagenEnMemoriaInterna () {
        TODO("Guardar copia de una imagen en la memoria interna de la app")
    }

    fun cargarImagenDeMemoriaInterna () {

    }

    fun redimensionarImagen(bitmap: Bitmap) : Bitmap {
        TODO("Reducir la resolución de la imágen y darle un aspecto retro '8bits'")
    }



    @Throws(FileNotFoundException::class, IOException::class)
    fun obtenerBitmap(context: Context, uri: Uri?): Bitmap? {

        val THUMBNAIL_SIZE = 400

        var input: InputStream? = context.getContentResolver().openInputStream(uri!!)
        val onlyBoundsOptions = BitmapFactory.Options()
        onlyBoundsOptions.inJustDecodeBounds = true
      //  onlyBoundsOptions.inDither = true //optional
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888 //optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions)
        input!!.close()
        if (onlyBoundsOptions.outWidth == -1 || onlyBoundsOptions.outHeight == -1) {
            return null
        }
        val ratio = 1
        val bitmapOptions = BitmapFactory.Options()
        bitmapOptions.inSampleSize = ratio
      //  bitmapOptions.inDither = true //optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888 //
        input = context.getContentResolver().openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions)
        input!!.close()
        return bitmap
    }




    // Método para guardar un bitmap en la memoria interna, devuelve su ruta (URI)
     fun guardarBitmapEnMemoria (context: Context , bitmap:Bitmap , capitulo: Capitulo): Uri {
        // Get the context wrapper
        val wrapper = ContextWrapper(context)

        // Initialize a new file instance to save bitmap object
        var file = wrapper.getDir("Images",Context.MODE_PRIVATE)
        file = File(file,"IMAGEN_"+ capitulo.id + ".jpg")

        try{
            // Compress the bitmap and save in jpg format
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
            stream.flush()
            stream.close()
        }catch (e:IOException){
            e.printStackTrace()
        }

        // Return the saved bitmap uri
        return Uri.parse(file.absolutePath)
    }
}

