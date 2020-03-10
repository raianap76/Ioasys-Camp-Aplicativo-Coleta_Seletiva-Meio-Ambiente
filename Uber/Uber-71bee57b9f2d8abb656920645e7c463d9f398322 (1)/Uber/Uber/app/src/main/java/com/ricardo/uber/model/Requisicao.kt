package com.ricardo.uber.model

import com.google.firebase.database.DatabaseReference
import com.ricardo.uber.config.ConfiguracaoFirebase

data class Requisicao (

    var id : String = "",
    var status : String = "",
    var passageiro : Usuario? = null,
    var motorista : Usuario? = null,
    var destino : Destino? = null
){
    fun salvar() {
        val firebaseRef : DatabaseReference = ConfiguracaoFirebase.getFirebaseDatabase()
        val requisicoes : DatabaseReference = firebaseRef.child("requisicoes")

        val idRequisicao : String? = requisicoes.push().key
        id = idRequisicao!!

        requisicoes.child(id).setValue(this)

    }

    fun atualizar(){

        val firebaseRef : DatabaseReference = ConfiguracaoFirebase.getFirebaseDatabase()
        val requisicoes : DatabaseReference = firebaseRef.child("requisicoes")

        val requisicao : DatabaseReference = requisicoes.child(id).child("motorista")

        val objeto = mutableMapOf<String, Any>()
        objeto.put("latitude", motorista!!.latitude!!)
        objeto.put("longitude", motorista!!.longitude!!)

        requisicao.updateChildren(objeto)

    }

    fun atualizarLocalizacaMotorista(){

        val firebaseRef : DatabaseReference = ConfiguracaoFirebase.getFirebaseDatabase()
        val requisicoes : DatabaseReference = firebaseRef.child("requisicoes")

        val requisicao : DatabaseReference = requisicoes.child(id)

        val objeto = mutableMapOf<String, Any>()
        objeto.put("motorista", motorista!!)
        objeto.put("status", status)

        requisicao.updateChildren(objeto)

    }

    fun atualizarStatus(){

        val firebaseRef : DatabaseReference = ConfiguracaoFirebase.getFirebaseDatabase()
        val requisicoes : DatabaseReference = firebaseRef.child("requisicoes")

        val requisicao : DatabaseReference = requisicoes.child(id)

        val objeto = mutableMapOf<String, Any>()
        objeto.put("status", status)

        requisicao.updateChildren(objeto)

    }

    companion object{

    val STATUS_AGUARDANDO = "aguardando"
    val STATUS_A_CAMINHO = "acaminho"
    val STATUS_VIAGEM = "viagem"
    val STATUS_FINALIZADA = "finalizada"
    val STATUS_ENCERRADA = "encerrada"
    val STATUS_CANCELADA = "cancelada"


}
}


