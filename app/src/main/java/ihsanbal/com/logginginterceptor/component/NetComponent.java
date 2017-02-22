package ihsanbal.com.logginginterceptor.component;

import javax.inject.Singleton;

import dagger.Component;
import ihsanbal.com.logginginterceptor.module.NetModule;
import ihsanbal.com.logginginterceptor.ui.MainActivity;

/**
 * @author ihsan on 09/02/2017.
 */

@Singleton
@Component(modules = {NetModule.class})
public interface NetComponent {
    void inject(MainActivity activity);
}
