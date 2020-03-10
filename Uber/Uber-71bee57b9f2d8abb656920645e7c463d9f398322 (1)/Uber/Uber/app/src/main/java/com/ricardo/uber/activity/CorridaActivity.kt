package com.ricardo.uber.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.ricardo.uber.R
import com.ricardo.uber.config.ConfiguracaoFirebase
import com.ricardo.uber.helper.Local
import com.ricardo.uber.helper.UsuarioFirebase
import com.ricardo.uber.model.Destino
import com.ricardo.uber.model.Requisicao
import com.ricardo.uber.model.Usuario

import kotlinx.android.synthetic.main.activity_corrida.*
import java.text.DecimalFormat

class CorridaActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private var localMotorista: LatLng? = null
    private var localPassageiro: LatLng? = null
    private lateinit var motorista: Usuario
    private lateinit var passageiro: Usuario
    private lateinit var idRequisicao: String
    private var requisicaoAtiva: Boolean = true
    private lateinit var requisicao: Requisicao
    private lateinit var firebaseRef: DatabaseReference
    private var marcadorMotorista: Marker? = null
    private var marcadorPassageiro: Marker? = null
    private var marcadorDestino: Marker? = null
    private lateinit var statusRequisicao: String
    private lateinit var destino: Destino

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_corrida)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setSupportActionBar(toolbar)
        supportActionBar?.title = "Iniciar corrida"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase()


        if (intent.extras.containsKey("idRequisicao") &&
            intent.extras.containsKey("motorista")
        ) {

            val extras: Bundle = intent.extras
            motorista = extras.getSerializable("motorista") as Usuario
            val latMot = java.lang.Double.parseDouble(motorista.latitude)
            val lonMot = java.lang.Double.parseDouble(motorista.longitude)
            localMotorista = LatLng(latMot, lonMot)
            idRequisicao = extras.getString("idRequisicao")
            requisicaoAtiva = extras.getBoolean("requisicaoAtiva")

            verificaStatusRequisicao()
        }

        fabRota.setOnClickListener {

            val status: String = statusRequisicao
            if (status != null && !status.isEmpty()) {

                var lat: String = ""
                var lon: String = ""

                when (status) {

                    Requisicao.STATUS_A_CAMINHO -> {
                        lat = localPassageiro!!.latitude.toString()
                        lon = localPassageiro!!.longitude.toString()
                    }
                    Requisicao.STATUS_VIAGEM -> {
                        lat = destino.latitude
                        lon = destino.longitude

                    }
                }

                val latLong: String = lat + "," + lon
                val uri: Uri = Uri.parse("google.navigation:q=" + latLong + "&mode=d")
                val mapIntent: Intent = Intent(Intent.ACTION_VIEW, uri)
                mapIntent.setPackage("com.google.android.apps.maps")
                startActivity(mapIntent)
            }

        }
    }

    private fun verificaStatusRequisicao() {

        val requisicoes: DatabaseReference = firebaseRef.child("requisicoes").child(idRequisicao)

        requisicoes.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                requisicao = p0.getValue(Requisicao::class.java)!!

                if (requisicao != null) {

                    passageiro = requisicao.passageiro!!

                    val latPass = java.lang.Double.parseDouble(passageiro.latitude)
                    val lonPass = java.lang.Double.parseDouble(passageiro.longitude)
                    localPassageiro = LatLng(
                        latPass,
                        lonPass
                    )

                    statusRequisicao = requisicao.status
                    destino = requisicao.destino!!
                    alteraInterfaceStatusRequisicao(statusRequisicao)
                }
            }
        })
    }

    fun alteraInterfaceStatusRequisicao(status: String) {

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
    }

    private fun requisicaoCancelada() {
        Toast.makeText(applicationContext,"Requisição foi cancelada pelo passageiro !",
            Toast.LENGTH_LONG).show()

        startActivity(Intent(applicationContext,RequisicoesActivity :: class.java))
    }

    @SuppressLint("RestrictedApi")
    private fun requisicaoFinalizada() {

        fabRota.visibility = View.GONE
        requisicaoAtiva = false

        if (marcadorMotorista != null) {
            marcadorMotorista!!.remove()
        }
        if (marcadorDestino != null) {
            marcadorDestino!!.remove()
        }
        val latDest = java.lang.Double.parseDouble(destino.latitude)
        val lonDest = java.lang.Double.parseDouble(destino.longitude)
        val localDestino : LatLng = LatLng(latDest,lonDest)

        adicionarMarcadorDestino(localDestino,"Destino")

        centralizarMarcador(localDestino)

        val distancia : Float = Local.calcularDistancia(localPassageiro!!,localDestino)
        val valor : Float = distancia * 4
        val decimal : DecimalFormat = DecimalFormat("0.00")
        val resultado : String = decimal.format(valor)

        buttonAceitarCorrida.text = "Corrida finalizada - R$ " + resultado
    }

    private fun centralizarMarcador(local: LatLng) {

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(local, 20F))

    }

    @SuppressLint("RestrictedApi")
    private fun requisicaoViagem() {

        buttonAceitarCorrida.text = "A caminho do destino"
        fabRota.visibility = View.VISIBLE

        adicionarMarcadorMotorista(localMotorista!!, motorista.nome!!)

        val latDest = java.lang.Double.parseDouble(destino.latitude)
        val lonDest = java.lang.Double.parseDouble(destino.longitude)
        val localDestino: LatLng = LatLng(latDest, lonDest)

        adicionarMarcadorDestino(localDestino, "Destino")

        centralizarDoisMarcadores(marcadorMotorista!!, marcadorDestino!!)

        iniciarMonitoramento(motorista, localDestino, Requisicao.STATUS_FINALIZADA)

    }

    @SuppressLint("RestrictedApi")
    private fun requisicaoACaminho() {

        buttonAceitarCorrida.text = "A caminho do passageiro"
        fabRota.visibility = View.VISIBLE

        adicionarMarcadorMotorista(localMotorista!!, motorista.nome!!)

        adicionarMarcadorPassageiro(localPassageiro!!, passageiro.nome!!)

        centralizarDoisMarcadores(marcadorMotorista!!, marcadorPassageiro!!)

        iniciarMonitoramento(motorista, localPassageiro!!, Requisicao.STATUS_VIAGEM)

    }

    private fun iniciarMonitoramento(uOrigem: Usuario, localDestino: LatLng, status: String) {

        val localUsuario: DatabaseReference = ConfiguracaoFirebase.getFirebaseDatabase()
            .child("local_usuario")
        val geoFire: GeoFire = GeoFire(localUsuario)

        val circulo: Circle = mMap.addCircle(
            CircleOptions()
                .center(localDestino)
                .radius(50.0)
                .fillColor(Color.argb(90, 255, 153, 0))
                .strokeColor(Color.argb(190, 255, 153, 0))
        )

        val geoQuery: GeoQuery = geoFire.queryAtLocation(
            GeoLocation(localDestino.latitude, localDestino.longitude),
            0.05
        )

        geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {
            override fun onGeoQueryReady() {
            }

            override fun onKeyEntered(key: String?, location: GeoLocation?) {

                if (key.equals(uOrigem.id)) {

                    requisicao.status = status
                    requisicao.atualizarStatus()

                    geoQuery.removeAllListeners()
                    circulo.remove()
                }
            }

            override fun onKeyMoved(key: String?, location: GeoLocation?) {
            }

            override fun onKeyExited(key: String?) {
            }

            override fun onGeoQueryError(error: DatabaseError?) {
            }

        })

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

    private fun requisicaoAguardando() {

        buttonAceitarCorrida.text = "Aceitar corrida"

        adicionarMarcadorMotorista(localMotorista!!, motorista.nome!!)

        centralizarMarcador(localMotorista!!)

    }

    fun aceitarCorrida(view: View) {

        requisicao = Requisicao()
        requisicao.id = idRequisicao
        requisicao.motorista = motorista
        requisicao.status = Requisicao.STATUS_A_CAMINHO

        requisicao.atualizar()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        recuperarLocalizacaoUsuario()


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
                localMotorista = LatLng(latitude, longitude)

                UsuarioFirebase.atualizarDadosLocalizacao(latitude, longitude)

                motorista.latitude = latitude.toString()
                motorista.longitude = longitude.toString()
                requisicao.motorista = motorista
                requisicao.atualizarLocalizacaMotorista()

                alteraInterfaceStatusRequisicao(statusRequisicao)


            }

        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) with(locationManager) {


            requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                0,
                0F,
                locationListener
            )
        }

    }

    override fun onSupportNavigateUp(): Boolean {

        if (requisicaoAtiva == true) {
            Toast.makeText(this, "Necissário encerrar a requisição atual", Toast.LENGTH_LONG).show()
        } else {
            val i: Intent = Intent(this, RequisicoesActivity::class.java)
            startActivity(i)
        }

        if (statusRequisicao != null && !statusRequisicao.isEmpty()){

            requisicao.status = Requisicao.STATUS_ENCERRADA
            requisicao.atualizarStatus()
        }
        return false
    }
}
