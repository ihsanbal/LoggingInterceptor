package ihsanbal.com.logginginterceptor.api

import ihsanbal.com.logginginterceptor.model.Body
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*
import rx.Observable

/**
 * @author ihsan on 09/02/2017.
 */
interface Api {
    @GET("get?test=123")
    fun get(): Observable<ResponseBody?>

    @DELETE("delete")
    fun delete(): Observable<ResponseBody?>

    @POST("post?query=q")
    @Headers("Cache-Control: Custom-Max-Value=640000")
    fun post(@retrofit2.http.Body requestBody: Body?): Observable<ResponseBody?>

    @PATCH("segment/patch")
    fun patch(@Query("query") q: String?): Observable<ResponseBody?>

    @PUT("put")
    fun put(): Observable<ResponseBody?>

    @Streaming
    @GET("http://che.org.il/wp-content/uploads/2016/12/pdf-sample.pdf")
    fun pdf(): Observable<ResponseBody?>

    @Multipart
    @POST("post")
    fun post(@Part("description") description: RequestBody?, @Part file: MultipartBody.Part?): Observable<ResponseBody?>
}