package ihsanbal.com.logginginterceptor.di;

import android.content.res.AssetManager;

import com.ihsanbal.logging.Level;
import com.ihsanbal.logging.LoggingInterceptor;

import java.util.concurrent.Executors;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ihsanbal.com.logginginterceptor.BuildConfig;
import ihsanbal.com.logginginterceptor.api.Api;
import okhttp3.OkHttpClient;
import okhttp3.internal.platform.Platform;
import okio.Okio;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author ihsan on 09/02/2017.
 */
@Module
public class NetModule {

    private final AssetManager mAssetManager;
    private String mEndPoint;

    public NetModule(String endpoint, AssetManager assetManager) {
        mEndPoint = endpoint;
        mAssetManager = assetManager;
    }

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient() {
        OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.addInterceptor(new LoggingInterceptor.Builder()
                .loggable(BuildConfig.DEBUG)
                .setLevel(Level.BASIC)
                .log(Platform.INFO)
                .addHeader("version", BuildConfig.VERSION_NAME)
                .addQueryParam("query", "0")
                .enableAndroidStudio_v3_LogsHack(true)
                .enableMock(BuildConfig.MOCK, 1000L, request -> {
                    String segment = request.url().pathSegments().get(0);
                    return Okio.buffer(Okio.source(mAssetManager.open(String.format("mock/%s.json", segment)))).readUtf8();
                })
                .executor(Executors.newSingleThreadExecutor())
                .build());
        return client.build();
    }

    @Provides
    @Singleton
    Retrofit provideRetrofit(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(mEndPoint)
                .client(okHttpClient)
                .build();
    }

    @Provides
    @Singleton
    Api provideApi(Retrofit retrofit) {
        return retrofit.create(Api.class);
    }
}
