package raianapereira.maps.recycle.reciclev.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

import android.widget.Switch
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth

import kotlinx.android.synthetic.main.activity_cadastro.*


import raianapereira.maps.recycle.reciclev.R
import raianapereira.maps.recycle.reciclev.config.ConfiguracaoFirebase
import raianapereira.maps.recycle.reciclev.helper.UsuarioFirebase
import raianapereira.maps.recycle.reciclev.model.Usuario

class CadastroActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)
      btnCadastrarMain.setOnClickListener(View.OnClickListener { v -> validarCadastroUsuario(v) })

    }


    fun validarCadastroUsuario(view: View) {

        val textoNome: String = editCadastroNome.text.toString()
        val textoEmail: String = editCadastroEmail.text.toString()
        val textoSenha: String = editCadastroSenha.text.toString()

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

                    if (verificaTipoUsuario() == "C"){

                        startActivity(Intent(this,MapaActivity ::class.java))
                        finish()
                        Toast.makeText(this, "Sucesso ao Cadastrar Cooperativa !",
                            Toast.LENGTH_LONG).show()

                    }else{

                        startActivity(Intent(this,MapaActivity ::class.java))
                        finish()
                        Toast.makeText(this, "Sucesso ao Cadastrar Empresa !",
                            Toast.LENGTH_LONG).show()

                    }


                } else {

                    Toast.makeText(this, "deu ruim !", Toast.LENGTH_LONG).show()

                }

            })

    }

    fun verificaTipoUsuario(): String {
        val tipoUsuario: Switch = switchTipoUsuario
        return if (tipoUsuario.isChecked) "C" else "E"
    }
}
