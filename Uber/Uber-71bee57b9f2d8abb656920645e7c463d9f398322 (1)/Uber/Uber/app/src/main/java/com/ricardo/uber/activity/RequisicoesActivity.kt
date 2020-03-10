package com.ricardo.uber.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ricardo.uber.R
import com.ricardo.uber.adapter.RequisicoesAdapter
import com.ricardo.uber.config.ConfiguracaoFirebase
import com.ricardo.uber.helper.RecyclerItemClickListener
import com.ricardo.uber.helper.UsuarioFirebase
import com.ricardo.uber.model.Requisicao
import com.ricardo.uber.model.Usuario
import kotlinx.android.synthetic.main.activity_requisicoes.*

class RequisicoesActivity : AppCompatActivity() {

    private lateinit var firebaseRef: DatabaseReference
    val listaRequisicoes: MutableList<Requisicao> = ArrayList()
    private lateinit var adapter: RequisicoesAdapter
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var motorista: Usuario


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_requisicoes)

        supportActionBar?.title = "Requisições"

        firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase()
        motorista = UsuarioFirebase.getDadosUsuarioLogado()


        recuperarRequisicoes()
        recuperarLocalizacaoUsuario()



        adapter = RequisicoesAdapter(
            listaRequisicoes,
            applicationContext, motorista
        )
        recyclerRequisicoes.layoutManager = LinearLayoutManager(this)
        recyclerRequisicoes.setHasFixedSize(true)
        recyclerRequisicoes.adapter = adapter


    }

    fun adicionaEventoCliqueRecyclerView(){

        recyclerRequisicoes.addOnItemTouchListener(
            RecyclerItemClickListener(this,
                recyclerRequisicoes, object : RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View?, position: Int) {

                        val requisicao: Requisicao = listaRequisicoes.get(position)

                        abrirTelaCorrida(requisicao.id, motorista, false)

                    }

                    override fun onLongItemClick(view: View?, position: Int) {

                    }

                    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    }
                })
        )
    }

    override fun onStart() {
        super.onStart()

        verificaStatusRequisicao()
    }

    fun verificaStatusRequisicao() {

        val usuarioLogado: Usuario = UsuarioFirebase.getDadosUsuarioLogado()
        val firebaseRef: DatabaseReference = ConfiguracaoFirebase.getFirebaseDatabase()

        val requisicoes: DatabaseReference = firebaseRef.child("requisicoes")

        val requisicoesPesquisa: Query = requisicoes.orderByChild("motorista/id")
            .equalTo(usuarioLogado.id)

        requisicoesPesquisa.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (ds: DataSnapshot in dataSnapshot.children) {

                    val requisicao: Requisicao? = ds.getValue(Requisicao::class.java)

                    if (requisicao?.status.equals(Requisicao.STATUS_A_CAMINHO)
                        || requisicao?.status.equals(Requisicao.STATUS_VIAGEM)
                        || requisicao?.status.equals(Requisicao.STATUS_FINALIZADA)
                    ) {

                        if (requisicao != null) {
                            motorista = requisicao.motorista!!
                            abrirTelaCorrida(requisicao.id, motorista, true)
                        }

                    }
                }
            }
        })
    }

    fun abrirTelaCorrida(idRequisicao: String, motorista: Usuario, requisicaoAtiva : Boolean) {

        val i: Intent = Intent(this@RequisicoesActivity, CorridaActivity::class.java)
        i.putExtra("idRequisicao", idRequisicao)
        i.putExtra("motorista", motorista)
        i.putExtra("requisicaoAtiva",requisicaoAtiva)
        startActivity(i)

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

                val latitude: String = location!!.latitude.toString()
                val longitude: String = location.longitude.toString()

                UsuarioFirebase.atualizarDadosLocalizacao(location.latitude
                    ,location.longitude)


                if (!latitude.isEmpty() && !longitude.isEmpty()) {

                    motorista.latitude = latitude
                    motorista.longitude = longitude
                    adicionaEventoCliqueRecyclerView()
                    locationManager.removeUpdates(locationListener)

                    adapter.notifyDataSetChanged()
                }

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

    private fun recuperarRequisicoes() {

        val requisicoes: DatabaseReference = firebaseRef.child("requisicoes")

        val requisicaoPesquisa: Query = requisicoes.orderByChild("status")
            .equalTo(Requisicao.STATUS_AGUARDANDO)

        requisicaoPesquisa.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot.childrenCount > 0) {
                    textResultados.visibility = View.GONE
                    recyclerRequisicoes.visibility = View.VISIBLE
                } else {
                    textResultados.visibility = View.VISIBLE
                    recyclerRequisicoes.visibility = View.GONE
                }
                listaRequisicoes.clear()
                for (ds: DataSnapshot in dataSnapshot.children) {
                    val requisicao: Requisicao = ds.getValue(Requisicao::class.java)!!
                    listaRequisicoes.add(requisicao)

                }
                adapter.notifyDataSetChanged()

            }


        })
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
