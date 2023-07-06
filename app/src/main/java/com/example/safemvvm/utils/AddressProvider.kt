package com.example.safemvvm.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

class AddressProvider(val context: Context) {

    fun getAddress(latLng: LatLng, language: String): String {
        val geocoder = Geocoder(context, Locale(language))
            val address = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (address!!.isNotEmpty()) {
                return buildReadableAddress(address[0])
        }
        return ""
    }

    private fun buildReadableAddress(address: Address): String {
        val street = address.thoroughfare ?: ""
        val subLocality = address.subLocality ?: ""
        val city = address.locality ?: ""
        val state = address.adminArea ?: ""
        val country = address.countryName ?: ""

        return StringBuilder()
            .append(street).append(", ")
            .append(subLocality).append(", ")
            .append(city).append(", ")
            .append(state).append(", ")
            .append(country).append(", ")
            .toString()
    }

}