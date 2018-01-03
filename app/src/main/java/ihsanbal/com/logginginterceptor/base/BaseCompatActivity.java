package ihsanbal.com.logginginterceptor.base;

import android.support.v7.app.AppCompatActivity;

import ihsanbal.com.logginginterceptor.LogApplication;
import ihsanbal.com.logginginterceptor.di.NetComponent;

/**
 * @author ihsan on 09/02/2017.
 */

public class BaseCompatActivity extends AppCompatActivity {

    public NetComponent getInjector() {
        return ((LogApplication) getApplication()).getAppComponent();
    }

}
