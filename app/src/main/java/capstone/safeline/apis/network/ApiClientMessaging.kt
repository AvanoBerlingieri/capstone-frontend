package capstone.safeline.apis.network

import android.content.Context
import capstone.safeline.apis.ApiServiceMessage
import capstone.safeline.apis.AuthInterceptor
import capstone.safeline.data.local.DataStoreManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClientMessaging {
    private const val BASE_URL = "http://10.0.2.2:9000/api/messaging/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    fun provideMessageApiService(
        context: Context,
        dataStoreManager: DataStoreManager
    ): ApiServiceMessage {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(dataStoreManager))
            .addInterceptor(ApiClientMessaging.logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiServiceMessage::class.java)
    }
}