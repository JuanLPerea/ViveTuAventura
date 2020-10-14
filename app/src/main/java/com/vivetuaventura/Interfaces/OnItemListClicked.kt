package com.vivetuaventura.Interfaces

import android.view.View

interface OnItemListClicked {
    fun itemListClicked (idAventura : String , itemView : View , publicado : Boolean)
}