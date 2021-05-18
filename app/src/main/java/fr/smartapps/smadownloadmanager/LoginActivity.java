package fr.smartapps.smadownloadmanager;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import fr.smartapps.lib_sma_packagemanager.package_manager.PackageStatus;
import fr.smartapps.lib_sma_packagemanager.package_manager.SMAPackageListener;
import fr.smartapps.lib_sma_packagemanager.package_manager.SMAPackageManager;
import fr.smartapps.smadownloadmanager.data.User;
import fr.smartapps.smadownloadmanager.data.UserToken;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by vchann on 16/12/2016.
 */

public class LoginActivity extends Activity {

    private String TAG = "LoginActivity";
    protected String username = "vchann";
    protected String password = "6bSzbTesPwqufwvs3WZB";
    protected String url_login = "https://publisher.smartapps.fr/api/1.0/login";
    protected String url_package_list = "https://publisher.smartapps.fr/api/1.0/package/list?platform=1";
    protected String url_package = "https://publisher.smartapps.fr/p/9112";
    protected Gson gson;

    protected TextView log1;
    protected TextView log2;
    protected SMAPackageManager packageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        log1 = (TextView) findViewById(R.id.log_token);
        log2 = (TextView) findViewById(R.id.log_response);
        gson = new Gson();
        packageManager = SMAPackageManager.getInstance(this);
        packageManager.startGETRequest(url_login, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getContext().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        log1.setText("Fail login");
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final UserToken token = gson.fromJson(response.body().string(), UserToken.class);
                final User user = new User(username, password, token.csrfValue);
                final HashMap<String, String> parameters = new HashMap();
                parameters.put("username", user.username);
                parameters.put("password", user.password);
                parameters.put("PUBLISHER_TOKEN", user.PUBLISHER_TOKEN);

                getContext().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        packageManager.startPOSTRequest(url_login, parameters, new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                getContext().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        log1.setText("Fail login");
                                    }
                                });
                            }

                            @Override
                            public void onResponse(Call call, final Response response) throws IOException {
                                getContext().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        log1.setText("Login is successful : " + response.isSuccessful());
                                        showPackageList();
                                        startDownloadPackage();
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    protected void showPackageList() {
        packageManager.startGETRequest(url_package_list, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log2.setText("Fail get list package");
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String result = response.body().string();
                getContext().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        log2.setText("Response list packages : " + result);
                    }
                });
            }
        });
    }

    protected void startDownloadPackage() {
        packageManager.downloadUnzipPackage(url_package, url_package.split("/")[url_package.split("/").length - 1], new SMAPackageListener() {
            @Override
            public void onProgress(int progressOn100, File packageFile, PackageStatus status) {
                Log.e(TAG, "progressOn100 : " + progressOn100 + " - status : " + status);
            }
        });
    }

    protected LoginActivity getContext() {
        return this;
    }
}
