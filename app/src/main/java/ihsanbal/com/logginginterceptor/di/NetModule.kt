package ihsanbal.com.logginginterceptor.di

import android.content.res.AssetManager
import android.util.Log.VERBOSE
import com.ihsanbal.logging.Level
import com.ihsanbal.logging.LoggingInterceptor
import dagger.Module
import dagger.Provides
import ihsanbal.com.logginginterceptor.BuildConfig
import ihsanbal.com.logginginterceptor.api.Api
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * @author ihsan on 09/02/2017.
 */
@Module
class NetModule(private val mEndPoint: String, private val mAssetManager: AssetManager) {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val client = OkHttpClient.Builder()
        client.addInterceptor(LoggingInterceptor.Builder()
                .setLevel(Level.BASIC)
                .log(VERBOSE)
                .addHeader("version", BuildConfig.VERSION_NAME)
                .addQueryParam("query", "0")
//              .logger(object : Logger {
//                  override fun log(level: Int, tag: String?, msg: String?) {
//                      Log.e("$tag - $level", "$msg")
//                  }
//              })
//              .enableMock(BuildConfig.MOCK, 1000L, object : BufferListener {
//                  override fun getJsonResponse(request: Request?): String? {
//                      val segment = request?.url?.pathSegments?.getOrNull(0)
//                      return mAssetManager.open(String.format("mock/%s.json", segment)).source().buffer().readUtf8()
//                  }
//              })
                .build())
        return client.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(mEndPoint)
                .client(okHttpClient)
                .build()
    }

    @Provides
    @Singleton
    fun provideApi(retrofit: Retrofit): Api {
        return retrofit.create(Api::class.java)
    }

}