package ihsanbal.com.logginginterceptor;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

import ihsanbal.com.logginginterceptor.component.DaggerNetComponent;
import ihsanbal.com.logginginterceptor.component.NetComponent;
import ihsanbal.com.logginginterceptor.module.NetModule;

/**
 * @author ihsan on 09/02/2017.
 */

public class LogApplication extends Application {

    private NetComponent mAppComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        mAppComponent = DaggerNetComponent.builder()
                .netModule(new NetModule("http://demo2961085.mockable.io/"))
                .build();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
    }

    public NetComponent getAppComponent() {
        return mAppComponent;
    }
}
