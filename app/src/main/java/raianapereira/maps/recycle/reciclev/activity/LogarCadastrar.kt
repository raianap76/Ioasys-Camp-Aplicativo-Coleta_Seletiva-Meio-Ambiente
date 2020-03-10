package raianapereira.maps.recycle.reciclev.activity

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_logar_cadastrar.*

import raianapereira.maps.recycle.reciclev.R
import raianapereira.maps.recycle.reciclev.config.ConfiguracaoFirebase
import raianapereira.maps.recycle.reciclev.helper.Permissoes
import raianapereira.maps.recycle.reciclev.helper.UsuarioFirebase



class LogarCadastrar : AppCompatActivity() {

    private var permissoes  = arrayListOf<String>(Manifest.permission.ACCESS_FINE_LOCATION)
    private var autenticacao: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        setContentView(R.layout.activity_logar_cadastrar)
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao()
        autenticacao?.signOut()

        supportActionBar?.hide()



        Permissoes.validarPermissoes(permissoes,this,0)
        bttnLogar.setOnClickListener {
            irParaLogin()
        }
        bttnCadastrar.setOnClickListener {
            irParaCadastrar()
        }
        bttnEntrarSemLogar.setOnClickListener {
            irParaTelaPrincipal()
        }
    }

    fun irParaLogin()
    {
        startActivity(Intent(this,LoginActivity::class.java))
    }

    fun irParaCadastrar()
    {
        startActivity(Intent(this,CadastroActivity::class.java))
    }

    fun irParaTelaPrincipal()
    {
        startActivity(Intent(this,MapaActivity::class.java))
    }

    override fun onStart() {
        super.onStart()
        UsuarioFirebase.redirecionaUsuarioLogado(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        for (permissaoResultado: Int  in grantResults){
            if (permissaoResultado == PackageManager.PERMISSION_DENIED) {
                alertaValidacaoPermissao()
            }
        }
    }
    private fun alertaValidacaoPermissao() {

        val builder : AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Permissões Negadas")
        builder.setMessage("Para utilizar o App é necessário aceitar as permmissões")
        builder.setCancelable(false)
        builder.setPositiveButton("Confirmar", DialogInterface.OnClickListener { dialog, which ->

            finish()
        })
        val dialog : AlertDialog = builder.create()
        dialog.show()

    }

}
