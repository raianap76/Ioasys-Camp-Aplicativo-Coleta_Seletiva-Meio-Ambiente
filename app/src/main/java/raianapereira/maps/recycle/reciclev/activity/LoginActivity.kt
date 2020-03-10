package raianapereira.maps.recycle.reciclev.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.android.synthetic.main.activity_login.*
import raianapereira.maps.recycle.reciclev.R
import raianapereira.maps.recycle.reciclev.config.ConfiguracaoFirebase
import raianapereira.maps.recycle.reciclev.helper.UsuarioFirebase
import raianapereira.maps.recycle.reciclev.model.Usuario


class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

    }

    fun validarLoginUsuario(view : View){

        val textoEmail : String = editLoginEmail.text.toString()
        val textoSenha : String =editLoginSenha.text.toString()

        if (!textoEmail.isEmpty()){
            if (!textoSenha.isEmpty()){

                val usuario : Usuario = Usuario("","",textoEmail,textoSenha,"")
                logarUsuario(usuario)

            }else{
                Toast.makeText(this,"Preencha a senha !",Toast.LENGTH_LONG).show()
            }
        }else{
            Toast.makeText(this,"Preencha o email !",Toast.LENGTH_LONG).show()
        }
    }

    private fun logarUsuario(usuario: Usuario) {


        val autenticacao : FirebaseAuth = ConfiguracaoFirebase.getFirebaseAutenticacao()
        autenticacao.signInWithEmailAndPassword(usuario.email!!, usuario.senha!!)
            .addOnCompleteListener(OnCompleteListener {


                if (it.isSuccessful){
                    Toast.makeText(this," deu Bom !",Toast.LENGTH_LONG).show()

                    UsuarioFirebase.redirecionaUsuarioLogado(this)

                }else{

                    var excecao = ""
                    try {
                        throw it.getException()!!
                    } catch (e: FirebaseAuthInvalidUserException) {
                        excecao = "Usuário não está cadastrado."
                    } catch (e: FirebaseAuthInvalidCredentialsException) {
                        excecao = "E-mail e senha não correspondem a um usuário cadastrado"
                    } catch (e: Exception) {
                        excecao = "Erro ao cadastrar usuário: " + e.message
                        e.printStackTrace()
                    }

                    Toast.makeText(
                        this@LoginActivity,
                        excecao,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })

    }
}
