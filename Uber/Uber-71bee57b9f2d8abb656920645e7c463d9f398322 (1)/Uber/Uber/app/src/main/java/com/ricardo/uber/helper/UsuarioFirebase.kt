package com.ricardo.uber.helper

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.ricardo.uber.activity.PassageiroActivity
import com.ricardo.uber.activity.RequisicoesActivity
import com.ricardo.uber.config.ConfiguracaoFirebase
import com.ricardo.uber.model.Usuario

class UsuarioFirebase {companion object{

    fun getUsuarioAtual() : FirebaseUser? {

        val usuario : FirebaseAuth = ConfiguracaoFirebase.getFirebaseAutenticacao()
        return usuario.currentUser
    }

    fun getDadosUsuarioLogado() : Usuario{

        val firebaseUser : FirebaseUser? = getUsuarioAtual()
        val usuario : Usuario = Usuario()
        usuario.id = firebaseUser?.uid
        usuario.email = firebaseUser?.email
        usuario.nome = firebaseUser?.displayName

        return usuario
    }

    fun atualizarDadosLocalizacao(lat : Double, lon : Double){

        val localUsuario : DatabaseReference = ConfiguracaoFirebase.getFirebaseDatabase()
            .child("local_usuario")
        val geoFire : GeoFire = GeoFire(localUsuario)

        val usuarioLogado : Usuario = getDadosUsuarioLogado()

        geoFire.setLocation(usuarioLogado.id, GeoLocation(lat,lon),
            object : GeoFire.CompletionListener{
                override fun onComplete(key: String?, error: DatabaseError?) {

                    if (error != null){

                        Log.d("Erro", "Erro ao salvar o local")
                    }
                }

            })
    }

    fun atualizarNomeUsuario(nome : String) : Boolean{

     try {

         val user : FirebaseUser? = this.getUsuarioAtual()
         val profile : UserProfileChangeRequest = UserProfileChangeRequest.Builder()
             .setDisplayName(nome)
             .build()
         user?.updateProfile(profile)?.addOnCompleteListener(OnCompleteListener {

             if (!it.isSuccessful){
                 Log.d("Perfil","Erroao Atualizar o Perfil")
             }

         })
         return true

     }catch (e : Exception){
         e.printStackTrace()
         return false
     }
    }

    fun redirecionaUsuarioLogado(activity: Activity) {

        val user = getUsuarioAtual()
        if (user != null) {
            val usuariosRef = ConfiguracaoFirebase.getFirebaseDatabase()
                .child("usuarios")
                .child(getIdentificadorUsuario())
            usuariosRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    val usuario : Usuario? = dataSnapshot.getValue(Usuario :: class.java)

                    val tipoUsuario = usuario?.tipo
                    if (tipoUsuario == "M") {
                        val i = Intent(activity, RequisicoesActivity::class.java)
                        startActivity(activity,i,null)
                    } else {
                        val i = Intent(activity, PassageiroActivity::class.java)
                        startActivity(activity,i,null)
                    }

                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })
        }

    }

    fun getIdentificadorUsuario(): String{
        return getUsuarioAtual()!!.uid
    }

}
}