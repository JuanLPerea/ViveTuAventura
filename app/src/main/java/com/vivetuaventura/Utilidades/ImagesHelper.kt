package com.vivetuaventura.Utilidades

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.StrictMode
import android.util.Log
import com.vivetuaventura.modelos.Capitulo
import java.io.*


class ImagesHelper(context: Context) {

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
     fun guardarBitmapEnMemoria(context: Context, bitmap: Bitmap, capitulo: Capitulo): Uri {
        // Get the context wrapper
        val wrapper = ContextWrapper(context)

        // Initialize a new file instance to save bitmap object
        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
        file = File(file, "IMAGEN_" + capitulo.id + ".jpg")

        try{
            // Compress the bitmap and save in jpg format
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        }catch (e: IOException){
            e.printStackTrace()
        }

        // Return the saved bitmap uri
        return Uri.parse(file.absolutePath)
    }

    fun recuperarImagenMemoriaInterna( archivo: String?): Bitmap? {
        var archivo = archivo
        var bitmap: Bitmap? = null
        System.gc()
        if (archivo == null) {
            archivo = "Constantes.ARCHIVO_IMAGEN_JUGADOR"
        }
        try {
            val fileInputStream = FileInputStream(archivo )

            /*
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 3;
            options.inTempStorage = new byte[16 * 1024];
            options.inPurgeable = true;
            */
            bitmap = BitmapFactory.decodeStream(fileInputStream)
        } catch (io: IOException) {
            io.printStackTrace()
        }
        return bitmap
    }

    fun desactivarModoEstricto() {
        if (Build.VERSION.SDK_INT >= 24) {
            try {
                val m = StrictMode::class.java.getMethod("disableDeathOnFileUriExposure")
                m.invoke(null)
            } catch (e: Exception) {
                Log.e("Miapp", "Error al trucar el método disableDeathOnFileUriExposure", e)
            }
        }
    }

}

