package com.ricardo.uber.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.ricardo.uber.config.ConfiguracaoFirebase
import com.ricardo.uber.R
import com.ricardo.uber.helper.UsuarioFirebase
import com.ricardo.uber.model.Usuario
import kotlinx.android.synthetic.main.activity_cadastro.*

class CadastroActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)
    }


    fun validarCadastroUsuario(view: View) {

        val textoNome: String = txtNome.text.toString()
        val textoEmail: String = txtEmail.text.toString()
        val textoSenha: String = txtSenha.text.toString()

        if (!textoNome.isEmpty()) {
            if (!textoEmail.isEmpty()) {
                if (!textoSenha.isEmpty()) {

                    val usuario: Usuario = Usuario("",textoNome, textoEmail, textoSenha, verificaTipoUsuario())

                    cadastrarUsuario(usuario)
                } else {
                    Toast.makeText(this, "Preencha a Senha !", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Preencha o Email !", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Preencha o nome !", Toast.LENGTH_LONG).show()
        }
    }

    private fun cadastrarUsuario(usuario: Usuario) {

        val autenticacao: FirebaseAuth = ConfiguracaoFirebase.getFirebaseAutenticacao()

        autenticacao.createUserWithEmailAndPassword(usuario.email!!, usuario.senha!!).addOnCompleteListener(this,
            OnCompleteListener {

                if (it.isSuccessful) {

                    val idUsuario : String = it.getResult()!!.user.uid
                    usuario.id = idUsuario
                    usuario.salvar()

                    UsuarioFirebase.atualizarNomeUsuario(usuario.nome!!)

                    if (verificaTipoUsuario() == "P"){

                        startActivity(Intent(this,PassageiroActivity ::class.java))
                        finish()
                        Toast.makeText(this, "Sucesso ao Cadastrar Passageiro !",
                            Toast.LENGTH_LONG).show()

                    }else{

                        startActivity(Intent(this,RequisicoesActivity ::class.java))
                        finish()
                        Toast.makeText(this, "Sucesso ao Cadastrar Motorista !",
                            Toast.LENGTH_LONG).show()

                    }


                } else {

                    Toast.makeText(this, "deu ruim !", Toast.LENGTH_LONG).show()

                }

            })

    }

    fun verificaTipoUsuario(): String {
        val tipoUsuario: Switch = switchTipo
        return if (tipoUsuario.isChecked) "M" else "P"
    }
}
