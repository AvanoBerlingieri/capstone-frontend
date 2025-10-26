package capstone.safeline.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/createUser")
    Call<User> createUser();

    @GET("/loginUser")
    Call<User> loginUser();
}
