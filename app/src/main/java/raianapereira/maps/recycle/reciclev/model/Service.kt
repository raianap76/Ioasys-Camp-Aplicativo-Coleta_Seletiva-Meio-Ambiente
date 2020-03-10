package raianapereira.maps.recycle.reciclev.model

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import raianapereira.maps.recycle.reciclev.retrofit.InterfaceRetroFit
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Realiza a configuração do RetroFit
 */

object Service {
    private val BASE_URL = "http://recycleplus.eu-4.evennode.com/"
    val retroFit: InterfaceRetroFit by lazy {
        Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).client(
            okHttpClient.build()).build().create(InterfaceRetroFit::class.java)
    }

    private val okHttpClient: OkHttpClient.Builder by lazy {
        OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor().apply {
          level = HttpLoggingInterceptor.Level.BODY
        })
    }
}