package ihsanbal.com.logginginterceptor.ui;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;
import ihsanbal.com.logginginterceptor.R;
import ihsanbal.com.logginginterceptor.api.Api;
import ihsanbal.com.logginginterceptor.base.BaseCompatActivity;
import ihsanbal.com.logginginterceptor.model.Body;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends BaseCompatActivity {

    private final int PERMISSION_REQUEST_CODE = 1000;
    private File outputFile;

    @Inject
    Api api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getInjector().inject(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    @OnClick(R.id.button_post)
    void callPost() {
        api.post(new Body())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(getSubscriber());
    }

    @OnClick(R.id.button_zip)
    void callZip() {
        Observable<ResponseBody> observablePost = api.post(new Body())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
        Observable<ResponseBody> observableGet = api.get()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
        Observable.zip(observablePost, observableGet, (o, o1) -> o).subscribe(getSubscriber());
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
        api.patch("q2")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(getSubscriber());
    }

    @OnClick(R.id.button_put)
    void callPut() {
        api.put()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(getSubscriber());
    }

    @OnClick(R.id.button_pdf)
    void callPdf() {
        if (checkPermission()) {
            api.pdf()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Observer<ResponseBody>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onNext(ResponseBody responseBody) {
                            try {
                                downloadFile(responseBody);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } else {
            requestPermission();
        }
    }

    @OnClick(R.id.button_pdf_upload)
    void callUpload() {
        if (outputFile == null) {
            Toast.makeText(this, "Click 'File' for create file", Toast.LENGTH_SHORT).show();
            return;
        }
        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse("application/pdf"),
                        outputFile
                );

        MultipartBody.Part body =
                MultipartBody.Part.createFormData("picture", outputFile.getName(), requestFile);

        String descriptionString = "hello, this is description speaking";
        RequestBody description =
                RequestBody.create(
                        okhttp3.MultipartBody.FORM, descriptionString);

        api.post(description, body)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {

                    }
                });
    }

    public Observer<? super ResponseBody> getSubscriber() {
        return new Observer<ResponseBody>() {

            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callPdf();
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Dained", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    private void downloadFile(ResponseBody body) throws IOException {
        int count;
        byte data[] = new byte[1024 * 4];
        InputStream bis = new BufferedInputStream(body.byteStream(), 1024 * 8);
        outputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "file.zip");
        OutputStream output = new FileOutputStream(outputFile);
        while ((count = bis.read(data)) != -1) {
            output.write(data, 0, count);
        }

        output.flush();
        output.close();
        bis.close();

        Intent target = new Intent(Intent.ACTION_VIEW);
        target.setDataAndType(Uri.fromFile(outputFile), "application/pdf");
        target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        Intent intent = Intent.createChooser(target, "Open File");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }
}
