package raianapereira.maps.recycle.reciclev.activity

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.Window
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_mapa.*
import raianapereira.maps.recycle.reciclev.adapter.PontosAdapter
import raianapereira.maps.recycle.reciclev.model.*
import raianapereira.maps.recycle.reciclev.retrofit.InterfaceRetroFitListener

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Gerencia o mapa e lista que irá exibir os pontos de coleta.
 */

class  MapaActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient : FusedLocationProviderClient
    private lateinit var pontosAdapter: PontosAdapter
    private var fechou = false
    private var saiu = true

    /**
     * Inicializa o Layout e o mapa, configura o listener do segmented control,seta fonte do segmented control,listener do botão de saiba mais, o de fechar o pop up e o de refresh do mapa e da lista.
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        setContentView(raianapereira.maps.recycle.reciclev.R.layout.activity_mapa)
        SegmentedControl.setSelectedSegment(0)
        SegmentedControl.setOnSegmentSelectRequestListener {
            if (it.column == 1) {
                Mapa.visibility = View.VISIBLE
                SearchCEP.visibility = View.GONE
                (bttnLocal as View).visibility = View.VISIBLE
                (bttnLess as View).visibility = View.VISIBLE
                (bttnAdd as View).visibility = View.VISIBLE
                if (!fechou) {
                    txtSaibaMais.visibility = View.GONE
                    bttnSaibaMais.visibility = View.GONE
                    (imageSaibaMais as View).visibility = View.GONE
                    (rectangle as View).visibility = View.GONE
                    bttnClose.visibility = View.GONE
                }
                recyclerPontos.visibility = View.GONE
                refresh.visibility = View.GONE
            }
            else
            {
                Mapa.visibility = View.GONE
                SearchCEP.visibility = View.VISIBLE
                (bttnLocal as View).visibility = View.GONE
                (bttnLess as View).visibility = View.GONE
                (bttnAdd as View).visibility = View.GONE
                if (!fechou) {
                    txtSaibaMais.visibility = View.VISIBLE
                    bttnSaibaMais.visibility = View.VISIBLE
                    (imageSaibaMais as View).visibility = View.VISIBLE
                    (rectangle as View).visibility = View.VISIBLE
                    bttnClose.visibility = View.VISIBLE
                    val params = SearchCEP.layoutParams as ConstraintLayout.LayoutParams
                    params.topMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,200F,this.resources.displayMetrics).toInt()
                    SearchCEP.layoutParams = params
                }
                refresh.visibility = View.VISIBLE
                recyclerPontos.visibility = View.VISIBLE
            }
            true
        }
        Mapa.onCreate(savedInstanceState)
        Mapa.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val typeFace = ResourcesCompat.getFont(this,raianapereira.maps.recycle.reciclev.R.font.stolzlregular)
        val searchViewFont = SearchCEP.findViewById<TextView>(SearchCEP.context.resources.getIdentifier("android:id/search_src_text",null,null))
        searchViewFont.setTypeface(typeFace)
        SegmentedControl.setTypeFace(typeFace)
        SegmentedControl.notifyConfigIsChanged()
        bttnSaibaMais.setOnClickListener {
            startActivity(Intent(this,ComoFuncionaActivity::class.java))
        }
        bttnClose.setOnClickListener {
            bttnClose.visibility = View.GONE
            txtSaibaMais.visibility = View.GONE
            bttnSaibaMais.visibility = View.GONE
            (imageSaibaMais as View).visibility = View.GONE
            (rectangle as View).visibility = View.GONE
            val params = SearchCEP.layoutParams as ConstraintLayout.LayoutParams
            params.topMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,10F,this.resources.displayMetrics).toInt()
            SearchCEP.layoutParams = params
            fechou = true
        }
        getEmpresaCooperativaPontoDeColeta()
        refresh.setOnRefreshListener {
            if (saiu)
            {
                saiu = false
                getEmpresaCooperativaPontoDeColeta()
            }
        }
        SearchCEP.setOnQueryTextListener(object: SearchView.OnQueryTextListener
        {
            override fun onQueryTextChange(p0: String?): Boolean {
                return (true)
            }

            override fun onQueryTextSubmit(p0: String): Boolean {
                SearchCEP.clearFocus()
                searchUser(p0)
                searchPontoDeColeta(p0)
                return (true)
            }
        })
    }

    /**
     * Inserir empresa ou cooperativa na API.
     */

    fun insertEmpresaOuCooperativa(cep: String,tipodescarte: String,name: String,end: raianapereira.maps.recycle.reciclev.model.Address,confirmaSenha: String,senha: String,email: String,tipo: String)
    {
        val dataHoraAtual = Calendar.getInstance()
        val formatar = SimpleDateFormat("yyyy-MM-dd")
        val dataFormatada = formatar.format(dataHoraAtual.time)
        Service.retroFit.inserirEmpresaOuCooperativa(cep,tipodescarte,name,end,confirmaSenha,senha,email,dataFormatada,dataFormatada,tipo).enqueue(object: Callback<User>
        {
            override fun onFailure(call: Call<User>, t: Throwable) {

            }

            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful)
                {
                    if (tipo[0] == 'E' || tipo[0] == 'e') {
                        getEmpresaCooperativaPontoDeColeta()
                        Toast.makeText(this@MapaActivity,"Empresa inserida com sucesso!",Toast.LENGTH_LONG).show()
                    }
                    else
                    {
                        getEmpresaCooperativaPontoDeColeta()
                        Toast.makeText(this@MapaActivity,"Cooperativa inserida com sucesso!",Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    /**
     * Inserir ponto de coleta na API.
     */

    fun insertPontoDeColeta(cep: String,tipodescarte: String,nome: String,end: PointAddress)
    {
        Service.retroFit.inserirPontoDeColeta(cep,tipodescarte,nome,end).enqueue(object: Callback<CollectPoint>
        {
            override fun onFailure(call: Call<CollectPoint>, t: Throwable) {
            }

            override fun onResponse(call: Call<CollectPoint>, response: Response<CollectPoint>) {
                if (response.isSuccessful)
                {
                    getEmpresaCooperativaPontoDeColeta()
                    Toast.makeText(this@MapaActivity,"Estação de Coleta Seletiva inserida com sucesso!",Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    /**
     * Buscar ponto de coleta na API.
     */

    fun searchPontoDeColeta(cep: String)
    {
        Service.retroFit.buscarPontoDeColeta(cep).enqueue(object: Callback<ListaPontosDeColeta>
        {
            override fun onFailure(call: Call<ListaPontosDeColeta>, t: Throwable) {

            }

            override fun onResponse(call: Call<ListaPontosDeColeta>, response: Response<ListaPontosDeColeta>) {
                response.body()?.let {pontoDeColeta ->
                    if (pontoDeColeta.collect_point.isNotEmpty()) {
                        val retornado = ArrayList<User>()
                        retornado.add(User(pontoDeColeta.collect_point[0].__v,pontoDeColeta.collect_point[0]._id,raianapereira.maps.recycle.reciclev.model.Address(pontoDeColeta.collect_point[0].point_address.city,pontoDeColeta.collect_point[0].point_address.neighbourhood,pontoDeColeta.collect_point[0].point_address.number,pontoDeColeta.collect_point[0].point_address.street,pontoDeColeta.collect_point[0].point_address.uf),pontoDeColeta.collect_point[0].cep,"","",pontoDeColeta.collect_point[0].discard_type,"",pontoDeColeta.collect_point[0].name,"","Estação de Coleta Seletiva",""))
                        val adapter = PontosAdapter(ArrayList())
                        { view: View, user: User ->
                            mostrarRotaMaps(user)
                        }
                        pontosAdapter = adapter
                        recyclerPontos.layoutManager = LinearLayoutManager(applicationContext)
                        recyclerPontos.adapter = pontosAdapter
                    }
                    else
                    {
                        Toast.makeText(this@MapaActivity,"Nenhuma empresa ou estação de coleta seletiva ou cooperativa encontrada",Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    /**
     * Buscar usuario na API
     */

    fun searchUser(cep: String)
    {
        Service.retroFit.buscarEmpresaCooperativa(cep).enqueue(object: Callback<ListaEmpresaCooperativa>
        {
            override fun onFailure(call: Call<ListaEmpresaCooperativa>, t: Throwable) {

            }

            override fun onResponse(call: Call<ListaEmpresaCooperativa>,response: Response<ListaEmpresaCooperativa>) {
                response.body()?.let {empresacooperativa ->
                    if (empresacooperativa.user.isNotEmpty()) {
                        val adapter = PontosAdapter(ArrayList(empresacooperativa.user))
                        { view: View, user: User ->
                            mostrarRotaMaps(user)
                        }
                        pontosAdapter = adapter
                        recyclerPontos.layoutManager = LinearLayoutManager(applicationContext)
                        recyclerPontos.adapter = pontosAdapter
                    }
                    else
                    {
                        Toast.makeText(this@MapaActivity,"Nenhuma empresa ou estação de coleta seletiva ou cooperativa encontrada",Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }



    /**
     * Realiza o get na API de usuarios.
     */

    fun getUser(listener: InterfaceRetroFitListener)
    {
        Service.retroFit.listarEmpresasCooperativa().enqueue(object : Callback<ListaEmpresaCooperativa>
        {
            override fun onFailure(call: Call<ListaEmpresaCooperativa>, t: Throwable) {
                listener.onError()
            }

            override fun onResponse(call: Call<ListaEmpresaCooperativa>,response: Response<ListaEmpresaCooperativa>)
            {
                response.body()?.let {empresacooperativa ->
                    listener.onSucess(empresacooperativa.user)
                }
            }
        })
    }

    /**
     * Realiza o get na API de ponto de coleta.
     */

    fun getPontoDeColeta(listaAux: List<User>)
    {
        Service.retroFit.listarPonto().enqueue(object : Callback<ListaPontosDeColeta>
        {
            override fun onFailure(call: Call<ListaPontosDeColeta>, t: Throwable) {
                refresh.isRefreshing = false
                saiu = true
            }

            override fun onResponse(call: Call<ListaPontosDeColeta>, response: Response<ListaPontosDeColeta>) {
                refresh.isRefreshing = false
                saiu = true
                response.body()?.let {points ->
                    val listaFinal = ArrayList<User>()
                    for (usuarios in listaAux)
                    {
                        listaFinal.add(usuarios)
                    }
                    for (pontoDeColeta in points.collect_point)
                    {
                        listaFinal.add(User(pontoDeColeta.__v,pontoDeColeta._id,raianapereira.maps.recycle.reciclev.model.Address(pontoDeColeta.point_address.city,pontoDeColeta.point_address.neighbourhood,pontoDeColeta.point_address.number,pontoDeColeta.point_address.street,pontoDeColeta.point_address.uf),pontoDeColeta.cep,"","",pontoDeColeta.discard_type,"",pontoDeColeta.name,"","Estação de Coleta Seletiva",""))
                    }
                    val adapter = PontosAdapter(listaFinal)
                    {view: View, user: User ->
                        mostrarRotaMaps(user)
                    }
                    pontosAdapter = adapter
                    recyclerPontos.layoutManager = LinearLayoutManager(applicationContext)
                    recyclerPontos.adapter = pontosAdapter
                    try
                    {
                        val geo = Geocoder(applicationContext, Locale("pt","BR"))
                        for (point in listaFinal) {
                            val endereco: List<Address> = geo.getFromLocationName(concatenarEndereco(point), 1)
                            val latLngEnd = LatLng(endereco[0].latitude, endereco[0].longitude)
                            mMap.addMarker(MarkerOptions().position(latLngEnd).title(point.name).snippet("Tipo de descarte: " + point.discard_type)).setIcon(BitmapDescriptorFactory.fromResource(raianapereira.maps.recycle.reciclev.R.mipmap.location))
                        }
                    }
                    catch (ex: Exception)
                    {
                        ex.printStackTrace()
                    }
                }
            }
        })
    }

    /**
     * Realiza o get inicialmente na api de usuarios e apos isso na de ponto de coleta.
     */

    fun getEmpresaCooperativaPontoDeColeta()
    {
       getUser(object:
           InterfaceRetroFitListener
       {
           override fun onError() {
               saiu = true
               refresh.isRefreshing = false
           }

           override fun onSucess(listaAux: List<User>) {
               getPontoDeColeta(listaAux)
           }
       })
    }

    /**
     * Concatena o endereço para ser usado para buscar a latitude e longitude.
     */

    fun concatenarEndereco(user: User) : String
    {
        return (user.address.street + ","  + user.address.number + " " + user.address.neighbourhood + " " + user.address.city + " " + user.cep)
    }

    /**
     * Realiza o intent com app do Maps para mostrar opções de rota.
     */

    fun mostrarRotaMaps(user: User)
    {
        try {
            val geo = Geocoder(applicationContext, Locale("pt","BR"))
            val endereco: List<Address> = geo.getFromLocationName(concatenarEndereco(user), 1)
            val latLngEnd = LatLng(endereco[0].latitude, endereco[0].longitude)
            val uri = Uri.parse("google.navigation:q="+latLngEnd.latitude+","+latLngEnd.longitude)
            val intentMaps = Intent(Intent.ACTION_VIEW,uri)
            intentMaps.setPackage("com.google.android.apps.maps")
            startActivity(intentMaps)
        }
        catch (ex: Exception)
        {
            ex.printStackTrace()
        }
    }

    /**
     * Quando o mapa está disponível seta marcações, verifica permissão de localização, desabilita o toolbar do maps, habilita o controle de zoom e seta o listener do botão de localização,zoom e dos marcadores.
     */

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            mMap.isMyLocationEnabled = true
            setLocalListener()
        }
        else
        {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),10)
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-19.9208300,-43.9377800),11.0F))
        mMap.setOnMarkerClickListener { p0 ->
            val alertDialog = AlertDialog.Builder(this@MapaActivity)
            alertDialog.setMessage("Deseja que mostre opções de rotas para o local escolhido?")
            alertDialog.setCancelable(true)
            alertDialog.setPositiveButton("SIM") { _, _ ->
                val uri = Uri.parse("google.navigation:q="+p0?.position?.latitude+","+p0?.position?.longitude)
                val intentMaps = Intent(Intent.ACTION_VIEW,uri)
                intentMaps.setPackage("com.google.android.apps.maps")
                startActivity(intentMaps)
            }
            alertDialog.setNegativeButton("NÃO") { _, _ ->
            }
            alertDialog.create().show()
            (false)
        }
        mMap.uiSettings.isMapToolbarEnabled = false
        mMap.uiSettings.isMyLocationButtonEnabled = false
        bttnAdd.setOnClickListener {
            mMap.animateCamera(CameraUpdateFactory.zoomIn())
        }
        bttnLess.setOnClickListener {
            mMap.animateCamera(CameraUpdateFactory.zoomOut())
        }
    }

    /**
     * Se tiver sido autorizado pelo usúario a permissão de localização, é ativado o botão de marcar sua localização atual no mapa.
     */

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 10)
        {
            if (permissions.size == 1 && permissions[0] == android.Manifest.permission.ACCESS_FINE_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                mMap.isMyLocationEnabled = true
                setLocalListener()
            }
        }
    }

    /**
     * Seta o listener do image button de localização.
     */

    fun setLocalListener()
    {

        bttnLocal.setOnClickListener {
                val localAux = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                if (localAux.isProviderEnabled(LocationManager.GPS_PROVIDER))
                {
                    fusedLocationClient.lastLocation.addOnSuccessListener {
                        if (it != null)
                        {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude,it.longitude), 18.0F))
                        }
                    }
                }
                else
                {
                    MostrarHabilitarGPS()
                }
            }
    }

    /**
     *  Irá solicitar ao usúario se deseja ativar o GPS para utilizar a funcionalidade de localização.
     */


    fun MostrarHabilitarGPS()
    {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val localtionRequestBuilder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val resultLocationRequestBuilder : Task<LocationSettingsResponse> = LocationServices.getSettingsClient(this).checkLocationSettings(localtionRequestBuilder.build())
        resultLocationRequestBuilder.addOnCompleteListener { task ->
            try {
                task.getResult(ApiException::class.java)
                fusedLocationClient.lastLocation.addOnSuccessListener {
                    if (it != null)
                    {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude,it.longitude), 18.0F))
                    }
                }
            }
            catch (ex : ApiException)
            {
                if (ex.statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED)
                {
                    try {
                        val resolucao : ResolvableApiException = ex as ResolvableApiException
                        resolucao.startResolutionForResult(this,LocationRequest.PRIORITY_HIGH_ACCURACY)
                    }
                    catch (ex : IntentSender.SendIntentException)
                    {
                        ex.printStackTrace()
                    }
                    catch (ex : ClassCastException)
                    {
                        ex.printStackTrace()
                    }
                }
            }
        }
    }


    /**
     * Ao voltar do segundo plano chama o método onResume() da classe GoogleMap.
     */

    override fun onResume() {
        Mapa.onResume()
        super.onResume()
    }

    /**
     * Quando o processo estiver suspenso chama o método onPause() da classe GoogleMap.
     */

    override fun onPause() {
        Mapa.onPause()
        super.onPause()
    }

    /**
     * Quando o processo for encerrado chama o método onDestroy() da classe GoogleMap.
     */

    override fun onDestroy() {
        Mapa.onDestroy()
        super.onDestroy()
    }

    /**
     * Se houver pouca memória disponivel chama o metodo onLowMemomy() da classe GoogleMap.
     */

    override fun onLowMemory() {
        Mapa.onLowMemory()
        super.onLowMemory()
    }

    /**
     * Retorna o mapa para o estado salvo recente.
     */

    override fun onSaveInstanceState(outState: Bundle) {
        Mapa.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }
}
