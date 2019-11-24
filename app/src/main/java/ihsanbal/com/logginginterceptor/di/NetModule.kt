package ihsanbal.com.logginginterceptor.di

import android.content.res.AssetManager
import android.util.Log.INFO
import com.ihsanbal.logging.BufferListener
import com.ihsanbal.logging.Level
import com.ihsanbal.logging.LoggingInterceptor
import dagger.Module
import dagger.Provides
import ihsanbal.com.logginginterceptor.BuildConfig
import ihsanbal.com.logginginterceptor.api.Api
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.source
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
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
        client.readTimeout(15, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(LoggingInterceptor.Builder()
                        .loggable(BuildConfig.DEBUG)
                        .setLevel(Level.BASIC)
                        .log(INFO)
                        .addHeader("version", BuildConfig.VERSION_NAME)
                        .addQueryParam("query", "1990")
                        .enableAndroidStudioLogHack(true)
                        .enableMock(BuildConfig.MOCK, 1000L, object : BufferListener {
                            override fun getJsonResponse(request: Request?): String? {
                                val segment = request!!.url.pathSegments[0]
                                return mAssetManager.open(String.format("mock/%s.json", segment)).source().buffer().readUtf8()
                            }
                        })
                        .executor(Executors.newSingleThreadExecutor())
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