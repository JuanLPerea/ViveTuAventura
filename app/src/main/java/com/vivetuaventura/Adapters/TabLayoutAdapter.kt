package com.vivetuaventura.Adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.vivetuaventura.Fragments.FragmentAventurasLocal
import com.vivetuaventura.Fragments.FragmentAventurasWeb

@Suppress("DEPRECATION")
internal class TabLayoutAdapter (

    var context: Context,
    fm: FragmentManager,
    var totalTabs: Int,
    var fragmentAventurasLocal: FragmentAventurasLocal,
    var fragmentAventurasWeb: FragmentAventurasWeb
) :
    FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                fragmentAventurasWeb
            }
            1 -> {
                fragmentAventurasLocal
            }
            else -> getItem(position)
        }
    }
    override fun getCount(): Int {
        return totalTabs
    }

}