package capstone.safeline.apis.network

import android.content.Context
import capstone.safeline.apis.ApiServiceAuth
import capstone.safeline.apis.AuthInterceptor
import capstone.safeline.data.local.DataStoreManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.logging.HttpLoggingInterceptor

object ApiClientAuth {
    private const val BASE_URL = "http://10.0.2.2:9000/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    fun provideApiService(context: Context, dataStoreManager: DataStoreManager): ApiServiceAuth {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(dataStoreManager))
            .addInterceptor(ApiClientAuth.logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiServiceAuth::class.java)
    }
}