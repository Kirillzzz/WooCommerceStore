package mx.niluxer.store.data.remote;

import java.util.List;

import mx.niluxer.store.data.model.Product;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface WooCommerceService {
    @GET("products?per_page=100")
    Call<List<Product>> getProducts();

    @GET("products/")
    Call<Product> getProduct(@Query("id") int id);

    @POST("products")
    @FormUrlEncoded
    Call<Product> savePost(@Field("name") String title,
                           @Field("type") String body,
                           @Field("regular_price") long userId);
}
