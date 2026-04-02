package capstone.safeline.apis.network

import capstone.safeline.apis.calling.ApiServiceCalling
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object CallingApiClient {
    // Change IP to your machine's local IP when testing on real device
    private const val BASE_URL = "http://10.0.2.2:8093/api/"

    val service: ApiServiceCalling by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiServiceCalling::class.java)
    }
}