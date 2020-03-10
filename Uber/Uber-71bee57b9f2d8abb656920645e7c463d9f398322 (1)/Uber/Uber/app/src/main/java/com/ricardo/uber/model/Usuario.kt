package com.ricardo.uber.model

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Exclude
import com.ricardo.uber.config.ConfiguracaoFirebase
import java.io.Serializable

data class Usuario(
    var id: String? = null,
    var nome: String? = null,
    var email: String? = null,
    @Exclude
    var senha: String? = null,
    var tipo: String? = null,

    var latitude: String? = null,
    var longitude: String? = null
) : Serializable{
    fun salvar() {

        val firebaseRef : DatabaseReference = ConfiguracaoFirebase.getFirebaseDatabase()
        val usuarios : DatabaseReference = firebaseRef.child("usuarios").child(this.id!!)
        usuarios.setValue(this)
    }

}