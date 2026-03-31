package capstone.safeline.apis.network

import android.content.Context
import capstone.safeline.apis.ApiServiceAuth
import capstone.safeline.apis.AuthInterceptor
import capstone.safeline.data.local.DataStoreManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import capstone.safeline.apis.network.ApiServiceMessage

object ApiClient {
    private const val BASE_URL = "http://10.0.2.2:8090/api/"

    fun provideApiService(context: Context, dataStoreManager: DataStoreManager): ApiServiceAuth {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(dataStoreManager))
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiServiceAuth::class.java)
    }

        // Message API
        fun provideMessageApiService(context: Context, dataStoreManager: DataStoreManager): ApiServiceMessage {
            val client = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(dataStoreManager))
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiServiceMessage::class.java)
    }
}