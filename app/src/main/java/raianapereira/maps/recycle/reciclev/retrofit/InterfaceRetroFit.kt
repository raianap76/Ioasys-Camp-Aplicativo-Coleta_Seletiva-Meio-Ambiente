package raianapereira.maps.recycle.reciclev.retrofit

import raianapereira.maps.recycle.reciclev.model.*
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Interface para definir os metodos de listagem, de busca e inserção na API.
 */

interface InterfaceRetroFit {

    @GET("list/")
    fun listarEmpresasCooperativa() : Call<ListaEmpresaCooperativa>

    @GET("listpoints/")
    fun listarPonto() : Call<ListaPontosDeColeta>

    @GET("search/")
    fun buscarEmpresaCooperativa(@Query("cep") cep: String) : Call<ListaEmpresaCooperativa>

    @GET("searchpoints/")
    fun buscarPontoDeColeta(@Query("cep")cep: String) : Call<ListaPontosDeColeta>

    @POST("register/")
    fun inserirEmpresaOuCooperativa(@Field("cep")cep: String, @Field("discard_type")discard_type: String, @Field("name")name: String, @Field("address")adress: Address, @Field("confirmPassword")confirmPassword: String, @Field("password")password: String, @Field("mail")mail: String, @Field("createdAt")createdAt: String, @Field("updatedAt")updatedAt: String, @Field("type")type: String) : Call<User>

    @POST("registerpoints/")
    fun inserirPontoDeColeta(@Field("cep")cep: String,@Field("discard_type")discard_type: String,@Field("name")name: String,@Field("point_address")point_address: PointAddress) : Call<CollectPoint>
}