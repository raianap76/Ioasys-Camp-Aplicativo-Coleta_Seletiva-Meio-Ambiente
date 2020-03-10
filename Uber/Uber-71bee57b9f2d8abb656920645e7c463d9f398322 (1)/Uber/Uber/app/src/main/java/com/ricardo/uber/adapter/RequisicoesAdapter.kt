package com.ricardo.uber.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.ricardo.uber.R
import com.ricardo.uber.helper.Local
import com.ricardo.uber.model.Requisicao
import com.ricardo.uber.model.Usuario
import kotlinx.android.synthetic.main.adapter_requisicoes.view.*

class RequisicoesAdapter(private var requisicoes : List<Requisicao>,
                         private  var context : Context,
                         private var motorista : Usuario
) : RecyclerView.Adapter<RequisicoesAdapter.MyViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val view = LayoutInflater.from(context).inflate(R.layout.adapter_requisicoes,parent, false)
            return MyViewHolder(view)
    }

    override fun getItemCount(): Int = requisicoes.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val requisicao : Requisicao = requisicoes.get(position)
        val passageiro : Usuario = requisicao.passageiro!!

        holder.nome.text = passageiro.nome

        if (motorista.latitude != null && motorista.longitude != null){

            val latPas = java.lang.Double.parseDouble(passageiro.latitude)
            val lonPas = java.lang.Double.parseDouble(passageiro.longitude)
            val localPassageiro : LatLng = LatLng(latPas,lonPas)

            val latDest = java.lang.Double.parseDouble(motorista.latitude)
            val lonDest = java.lang.Double.parseDouble(motorista.longitude)
            val localMotorista : LatLng = LatLng(latDest,lonDest)

            val distancia : Float = Local.calcularDistancia(localPassageiro,localMotorista)
            val distanciaFormatada : String = Local.formatarDistancia(distancia)
            holder.distancia.text = distanciaFormatada +" aproximadamente"

        }
    }

    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView ){

        val nome : TextView = itemView.textRequisicaoNome
        val distancia : TextView = itemView.textRequisicaoDistancia

    }


}