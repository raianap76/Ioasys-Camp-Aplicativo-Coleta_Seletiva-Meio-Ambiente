package com.ricardo.uber.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ricardo.uber.R
import com.ricardo.uber.config.ConfiguracaoFirebase
import com.ricardo.uber.helper.Local
import com.ricardo.uber.helper.UsuarioFirebase
import com.ricardo.uber.model.Destino
import com.ricardo.uber.model.Requisicao
import com.ricardo.uber.model.Usuario

import kotlinx.android.synthetic.main.activity_passageiro.*
import java.io.IOException
import java.lang.StringBuilder
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList

class PassageiroActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var localPassageiro: LatLng
    private var cancelarUber: Boolean = false
    private lateinit var firebaseRef: DatabaseReference
    private lateinit var requisicao: Requisicao
    private lateinit var passageiro: Usuario
    private  var statusRequisicao: String= ""
    private lateinit var destino: Destino
    private var marcadorMotorista: Marker? = null
    private var marcadorPassageiro: Marker? = null
    private var marcadorDestino: Marker? = null
    private lateinit var motorista: Usuario
    private var localMotorista: LatLng? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_passageiro)
        toolbar.setTitle("Iniciar uma viagem")
        setSupportActionBar(toolbar)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase()

        verificaStatusRequisicao()


    }

    private fun verificaStatusRequisicao() {

        val usuarioLogado: Usuario = UsuarioFirebase.getDadosUsuarioLogado()
        val requisicoes: DatabaseReference = firebaseRef.child("requisicoes")
        val requisicaoPesquisa: Query = requisicoes.orderByChild("passageiro/id")
            .equalTo(usuarioLogado.id)

        requisicaoPesquisa.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

                val lista: MutableList<Requisicao> = ArrayList()
                for (ds: DataSnapshot in p0.children) {
                    lista.add(ds.getValue(Requisicao::class.java)!!)
                }
                Collections.reverse(lista)

                if (lista != null && lista.size > 0) {

                    requisicao = lista.get(0)

                    if (requisicao != null) {
                        if (!requisicao.status.equals(Requisicao.STATUS_ENCERRADA)) {

                            passageiro = requisicao.passageiro!!

                            val latPass = java.lang.Double.parseDouble(passageiro.latitude)
                            val lonPass = java.lang.Double.parseDouble(passageiro.longitude)
                            localPassageiro = LatLng(
                                latPass,
                                lonPass
                            )

                            statusRequisicao = requisicao.status
                            destino = requisicao.destino!!

                            if (requisicao.motorista != null) {

                                motorista = requisicao.motorista!!
                                val latMot = java.lang.Double.parseDouble(motorista.latitude!!)
                                val lonMot = java.lang.Double.parseDouble(motorista.longitude!!)
                                localMotorista = LatLng(latMot, lonMot)
                            }
                            alteraInterfaceStatusRequisicao(statusRequisicao)
                        }
                    }
                }
            }
        })

    }

    private fun alteraInterfaceStatusRequisicao(status: String) {

        if (status != null && !status.isEmpty()) {
            cancelarUber = false
            when (status) {
                Requisicao.STATUS_AGUARDANDO -> {
                    requisicaoAguardando()
                }
                Requisicao.STATUS_A_CAMINHO -> {
                    requisicaoACaminho()
                }
                Requisicao.STATUS_VIAGEM -> {
                    requisicaoViagem()
                }
                Requisicao.STATUS_FINALIZADA -> {
                    requisicaoFinalizada()
                }
                Requisicao.STATUS_CANCELADA -> {
                    requisicaoCancelada()
                }
            }
        }else{
            adicionarMarcadorPassageiro(localPassageiro, "Seu local")
            centralizarMarcador(localPassageiro)
        }
    }

    private fun requisicaoCancelada() {
        linearLayoutDestino.visibility = View.VISIBLE
        chamarUber.text = "Chamar Uber"
        cancelarUber = false

    }

    private fun requisicaoFinalizada() {
        linearLayoutDestino.visibility = View.GONE
        chamarUber.isEnabled = false


        val localDestino: LatLng = LatLng(
            java.lang.Double.parseDouble(destino.latitude!!),
            java.lang.Double.parseDouble(destino.longitude!!)
        )
        adicionarMarcadorDestino(localDestino, "Destino")
        centralizarMarcador(localDestino)

        val distancia : Float = Local.calcularDistancia(localPassageiro!!,localDestino)
        val valor : Float = distancia * 4
        val decimal : DecimalFormat = DecimalFormat("0.00")
        val resultado : String = decimal.format(valor)

        chamarUber.text = "Corrida finalizada"+ resultado

        val builder : AlertDialog.Builder = AlertDialog.Builder(this).
                setTitle("Total de viagem").
                setMessage("Sua viagem ficou : R$ "+ resultado).
                setCancelable(false).
                setNegativeButton("Encerrar viagem" , object : DialogInterface.OnClickListener{
                    override fun onClick(dialog: DialogInterface?, which: Int) {

                        requisicao.status = Requisicao.STATUS_ENCERRADA
                        requisicao.atualizarStatus()

                        finish()
                        startActivity(Intent(intent))
                    }
                })

        val dialog : AlertDialog = builder.create()
        dialog.show()

    }

    private fun requisicaoViagem() {
        linearLayoutDestino.visibility = View.GONE
        chamarUber.text = "A caminho do destinho"
        chamarUber.isEnabled = false


        adicionarMarcadorMotorista(localMotorista!!, motorista.nome!!)

        val localDestino: LatLng = LatLng(
            java.lang.Double.parseDouble(destino.latitude!!),
            java.lang.Double.parseDouble(destino.longitude!!)
        )
        adicionarMarcadorDestino(localDestino, "Destino")

        centralizarDoisMarcadores(marcadorMotorista!!, marcadorDestino!!)

    }

    private fun requisicaoACaminho() {
        linearLayoutDestino.visibility = View.GONE
        chamarUber.text = "Motorista a caminho"
        chamarUber.isEnabled = false

        adicionarMarcadorPassageiro(localPassageiro, passageiro.nome!!)

        adicionarMarcadorMotorista(localMotorista!!, motorista.nome!!)

        centralizarDoisMarcadores(marcadorMotorista!!, marcadorPassageiro!!)

    }

    private fun requisicaoAguardando() {
        linearLayoutDestino.visibility = View.GONE
        chamarUber.text = "Cancelar Uber"
        cancelarUber = true

        adicionarMarcadorPassageiro(localPassageiro, passageiro.nome!!)

        centralizarMarcador(localPassageiro)

    }

    private fun centralizarDoisMarcadores(marcador1: Marker, marcador2: Marker) {

        val builder: LatLngBounds.Builder = LatLngBounds.Builder()
        builder.include(marcador1.position)
        builder.include(marcador2.position)

        val bounds: LatLngBounds = builder.build()

        val largura: Int = resources.displayMetrics.widthPixels
        val altura: Int = resources.displayMetrics.heightPixels
        val espacoInterno: Int = (largura * 0.20).toInt()
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, largura, altura, espacoInterno))


    }

    private fun adicionarMarcadorMotorista(localizacao: LatLng, titulo: String) {

        if (marcadorMotorista != null) {
            marcadorMotorista!!.remove()
        }

        marcadorMotorista = mMap.addMarker(
            MarkerOptions()
                .position(localizacao)
                .title(titulo)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.carro))
        )
    }

    private fun centralizarMarcador(local: LatLng) {

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(local, 20F))

    }

    private fun adicionarMarcadorPassageiro(localizacao: LatLng, titulo: String) {

        if (marcadorPassageiro != null) {
            marcadorPassageiro!!.remove()
        }

        marcadorPassageiro = mMap.addMarker(
            MarkerOptions()
                .position(localizacao)
                .title(titulo)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.usuario))
        )
    }

    private fun adicionarMarcadorDestino(localizacao: LatLng, titulo: String) {

        if (marcadorPassageiro != null) {
            marcadorPassageiro!!.remove()
        }

        if (marcadorDestino != null) {
            marcadorDestino!!.remove()
        }

        marcadorDestino = mMap.addMarker(
            MarkerOptions()
                .position(localizacao)
                .title(titulo)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.destino))
        )
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        recuperarLocalizacaoUsuario()


    }

    private fun recuperarEndereco(endereco: String): Address? {

        val geocoder: Geocoder = Geocoder(this, Locale.getDefault())

        try {

            val listaEndereco: List<Address> = geocoder.getFromLocationName(endereco, 1)

            if (listaEndereco != null && listaEndereco.size > 0) {

                val address: Address = listaEndereco.get(0)
                return address
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    fun chamarUber(view: View) {

        if (cancelarUber) {

            requisicao.status = Requisicao.STATUS_CANCELADA
            requisicao.atualizarStatus()

        } else {

            val enderecoDestino: String = editDestino.text.toString()

            if (!enderecoDestino.equals("") || enderecoDestino != null) {

                val addressDestino: Address? = recuperarEndereco(enderecoDestino)
                if (addressDestino != null) {

                    val destino: Destino = Destino()
                    destino.apply {
                        cidade = addressDestino.subAdminArea
                        cep = addressDestino.postalCode
                        bairro = addressDestino.subLocality
                        rua = addressDestino.thoroughfare
                        numero = addressDestino.featureName
                        latitude = addressDestino.latitude.toString()
                        longitude = addressDestino.longitude.toString()
                    }

                    val mensagem: StringBuilder = StringBuilder()
                    mensagem.apply {
                        append("Rua: " + destino.rua)
                        append("\nNúmero: " + destino.numero)
                        append("\nBairro: " + destino.bairro)
                        append("\nCidade: " + destino.cidade)
                        append("\nCep: " + destino.cep)
                    }

                    val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                        .setTitle("Confirme seu Endereço")
                        .setMessage(mensagem)
                        .setPositiveButton("Confirmar", DialogInterface.OnClickListener { dialog, which ->

                            salvarRequisicao(destino)

                        }).setNegativeButton("Cancelar", DialogInterface.OnClickListener { dialog, which ->


                        })

                    val dialog: AlertDialog = builder.create()
                    dialog.show()
                }

            } else {

                Toast.makeText(this, "Informe o endereço de destino!", Toast.LENGTH_LONG).show()
            }

        }
    }

    private fun salvarRequisicao(destino: Destino) {

        val requisicao: Requisicao = Requisicao()
        requisicao.destino = destino

        val usuarioPassageiro: Usuario = UsuarioFirebase.getDadosUsuarioLogado()
        usuarioPassageiro.latitude = localPassageiro.latitude.toString()
        usuarioPassageiro.longitude = localPassageiro.longitude.toString()

        requisicao.passageiro = usuarioPassageiro
        requisicao.status = Requisicao.STATUS_AGUARDANDO
        requisicao.salvar()
        linearLayoutDestino.visibility = View.GONE
        chamarUber.text = "Cancelar Uber"
    }

    fun recuperarLocalizacaoUsuario() {

        locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener {
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            }

            override fun onProviderEnabled(provider: String?) {
            }

            override fun onProviderDisabled(provider: String?) {
            }

            override fun onLocationChanged(location: Location?) {

                val latitude: Double = location!!.latitude
                val longitude: Double = location.longitude
                localPassageiro = LatLng(latitude, longitude)

                UsuarioFirebase.atualizarDadosLocalizacao(latitude, longitude)


                alteraInterfaceStatusRequisicao(statusRequisicao)

                if (statusRequisicao != null && !statusRequisicao.isEmpty()) {

                    if (statusRequisicao.equals(Requisicao.STATUS_VIAGEM)
                        || statusRequisicao.equals(Requisicao.STATUS_FINALIZADA)
                    ) {
                        locationManager.removeUpdates(locationListener)
                    }else{

                        if (ActivityCompat.checkSelfPermission(
                                applicationContext,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                        ) with(locationManager)
                        {


                            requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                5000,
                                10F,
                                locationListener
                            )


                        }
                    }

                }
            }
        }
            if (ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            ) with(locationManager)
            {


                requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    5000,
                    10F,
                    locationListener
                )


            }

        }

        override fun onCreateOptionsMenu(menu: Menu?): Boolean {

            menuInflater.inflate(R.menu.manu_main, menu)
            return true
        }

        override fun onOptionsItemSelected(item: MenuItem?): Boolean {

            val autenticacao: FirebaseAuth = ConfiguracaoFirebase.getFirebaseAutenticacao()

            when (item?.itemId) {
                R.id.menuSair -> {
                    autenticacao.signOut()
                    finish()
                }


            }
            return super.onOptionsItemSelected(item)
        }

    }