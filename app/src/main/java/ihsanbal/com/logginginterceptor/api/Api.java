package ihsanbal.com.logginginterceptor.api;

import ihsanbal.com.logginginterceptor.model.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import rx.Observable;

/**
 * @author ihsan on 09/02/2017.
 */
public interface Api {
    @GET("get")
    Observable<ResponseBody> get();

    @DELETE("delete")
    Observable<ResponseBody> delete();

    @POST("post")
    @Headers("Cache-Control: Custom-Max-Value=640000")
    Observable<ResponseBody> post(@Body RequestBody requestBody);

    @PATCH("segment/patch")
    Observable<ResponseBody> patch();
}
