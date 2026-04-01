package capstone.safeline.apis.network

import android.content.Context
import capstone.safeline.apis.ApiServiceFriends
import capstone.safeline.apis.AuthInterceptor
import capstone.safeline.data.local.DataStoreManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Dedicated Client for Friends API.
 */
object ApiClientFriends {
    private const val BASE_URL = "http://10.0.2.2:9000/api/friends/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    fun provideService(context: Context, dataStoreManager: DataStoreManager): ApiServiceFriends {
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(dataStoreManager))
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiServiceFriends::class.java)
    }
}