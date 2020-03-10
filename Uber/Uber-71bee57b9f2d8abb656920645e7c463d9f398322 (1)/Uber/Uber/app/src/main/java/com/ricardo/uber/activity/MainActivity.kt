package com.ricardo.uber.activity

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ricardo.uber.R
import com.ricardo.uber.helper.Permissoes
import com.ricardo.uber.helper.UsuarioFirebase

class MainActivity : AppCompatActivity() {

    private var permissoes  = arrayListOf<String>(Manifest.permission.ACCESS_FINE_LOCATION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        Permissoes.validarPermissoes(permissoes,this,0)

    }

    fun abrirLogin(view : View){

        startActivity(Intent(this,LoginActivity ::class.java))
    }

    fun abrirCadastro(view : View){

        startActivity(Intent(this,CadastroActivity ::class.java))
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
        val dialog :AlertDialog = builder.create()
        dialog.show()

    }
}
