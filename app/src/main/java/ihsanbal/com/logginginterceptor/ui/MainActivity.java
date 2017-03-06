package ihsanbal.com.logginginterceptor.ui;

import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;
import ihsanbal.com.logginginterceptor.R;
import ihsanbal.com.logginginterceptor.api.Api;
import ihsanbal.com.logginginterceptor.base.BaseCompatActivity;
import ihsanbal.com.logginginterceptor.model.RequestBody;
import okhttp3.ResponseBody;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends BaseCompatActivity {

    @Inject
    Api api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getInjector().inject(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.button_post)
    void callPost() {
        api.post(new RequestBody())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(getSubscriber());
    }

    @OnClick(R.id.button_get)
    void callGet() {
        api.get()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(getSubscriber());
    }

    @OnClick(R.id.button_delete)
    void callDelete() {
        api.delete()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(getSubscriber());
    }

    @OnClick(R.id.button_patch)
    void callPatch() {
        api.patch()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(getSubscriber());
    }

    public Observer<? super ResponseBody> getSubscriber() {
        return new Observer<ResponseBody>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(ResponseBody responseBody) {
                try {
                    Log.w("onNext", responseBody.string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
