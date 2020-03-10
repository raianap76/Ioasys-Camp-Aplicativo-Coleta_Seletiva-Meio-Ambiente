package com.ricardo.uber.helper

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import java.text.DecimalFormat

class Local {
    companion object {

        fun calcularDistancia(latLngInicial: LatLng, latLngFinal: LatLng): Float {


            val localInicial : Location = Location("Local inicial")
            localInicial.latitude = latLngInicial.latitude
            localInicial.longitude = latLngInicial.longitude

            val localFinal : Location = Location("Local final")
            localFinal.latitude = latLngFinal.latitude
            localFinal.longitude = latLngFinal.longitude

            val distancia : Float = localInicial.distanceTo(localFinal)/1000

            return distancia
        }

        fun formatarDistancia (distancia : Float) : String{

            var distanciaFormatada : String =""

            if (distancia < 1){
                val distancia = distancia*1000
                distanciaFormatada = Math.round(distancia).toString() +"M"
            }else{
                val decimal : DecimalFormat = DecimalFormat("0.0")
                distanciaFormatada = decimal.format(distancia) + "KM"
            }
            return distanciaFormatada
        }
    }
}