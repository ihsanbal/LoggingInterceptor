package ihsanbal.com.logginginterceptor.api;

import ihsanbal.com.logginginterceptor.model.Body;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import rx.Observable;

/**
 * @author ihsan on 09/02/2017.
 */
public interface Api {
    @GET("get")
    Observable<ResponseBody> get();

    @DELETE("delete")
    Observable<ResponseBody> delete();

    @POST("post?query=q")
    @Headers("Cache-Control: Custom-Max-Value=640000")
    Observable<ResponseBody> post(@retrofit2.http.Body Body requestBody);

    @PATCH("segment/patch")
    Observable<ResponseBody> patch(@Query("query") String q);

    @PUT("put")
    Observable<ResponseBody> put();

    @Streaming
    @GET("http://che.org.il/wp-content/uploads/2016/12/pdf-sample.pdf")
    Observable<ResponseBody> pdf();

    @Multipart
    @POST("post")
    Observable<ResponseBody> post(@Part("description") RequestBody description, @Part MultipartBody.Part file);
}
