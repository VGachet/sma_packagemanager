package fr.smartapps.smadownloadmanager;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

import fr.smartapps.lib_sma_packagemanager.package_manager.PackageStatus;
import fr.smartapps.lib_sma_packagemanager.package_manager.SMAPackageListener;
import fr.smartapps.lib_sma_packagemanager.package_manager.SMAPackageManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class PackageActivity extends AppCompatActivity {

    private String TAG = "PackageActivity";
    protected Context context;
    protected SMAPackageManager packageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package);
        context = this;
        packageManager = SMAPackageManager.getInstance(context);
        packageManager.clearZipFolder();
        packageManager.deleteAllPackages();
        log();
        startHTTPRequest();

        setDownload(R.id.progress_text_1, R.id.button_1, "http://smartapps-louvre.s3.amazonaws.com/V1/C2U2AyFkKkaXfTRDtkiN_fr_delta_all.zip", "package_1.zip"); // big file
        setDownload(R.id.progress_text_2, R.id.button_2, "http://cdn.publisher.smartapps.fr/v4/2b8d51fb5d6d7941/1b21170bc31f359b0fde42042ce8e497.zip", "package_2.zip");
        setDownload(R.id.progress_text_3, R.id.button_3, "http://cdn.publisher.smartapps.fr/v4/2b8d51fb5d6d7941/1b21170bc31f359b0fde42042ce8e497.zip", "package_3.zip");
    }

    protected void setDownload(int resTextView, int resButton, final String url, final String packageName) {
        final TextView textView = (TextView) findViewById(resTextView);
        final Button button = (Button) findViewById(resButton);

        if (button != null) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    packageManager.downloadUnzipPackage(url, packageName, new SMAPackageListener() {
                        @Override
                        public void onProgress(int progressOn100, File packageFile, PackageStatus status) {
                            switch (status) {
                                case DOWNLOAD_PAUSED:
                                    textView.setText("Waiting for connexion ...");
                                    break;
                                case DOWNLOAD_PENDING:
                                    textView.setText("Connecting ...");
                                    break;
                                case DOWNLOAD_RUNNING:
                                    textView.setText("Download progress : " + progressOn100 + " / 100");
                                    break;
                                case DOWNLOAD_SUCCESSFUL:
                                    textView.setText("Your file has been successfully downloaded to " + packageFile.getAbsolutePath());
                                    break;
                                case DOWNLOAD_FAILED:
                                    textView.setText("Download failed, please try again");
                                    break;

                                case UNZIP_RUNNING:
                                    textView.setText("Unzip progress : " + progressOn100 + " / 100");
                                    break;
                                case UNZIP_SUCCESSFUL:
                                    textView.setText("Your file has been successfully unzip to " + packageFile.getAbsolutePath());
                                    break;
                                case UNZIP_FAILED:
                                    textView.setText("Unzip failed, please try again");
                                    break;
                            }
                            log();
                        }
                    });
                }
            });
        }
    }

    protected void log() {
        TextView logZip = (TextView) findViewById(R.id.log_zip_folder);
        if (logZip != null) {
            String logString = "Zip folder : \n" + packageManager.getZipFolder() + "\n\n" + "Files :\n";
            for (String fileString : packageManager.zipList()) {
                logString += " - " + fileString + "\n";
            }
            logZip.setText(logString);
        }

        TextView logPackage = (TextView) findViewById(R.id.log_package_folder);
        if (logPackage != null) {
            String logString = "Package folder : \n" + packageManager.getPackageFolder() + "\n\n" + "Files :\n";
            for (String fileString : packageManager.packageList()) {
                logString += " - " + fileString + "\n";
            }
            logPackage.setText(logString);
        }
    }

    protected void startHTTPRequest() {
        packageManager.startGETRequest("https://cdn.publisher.smartapps.fr/v4/b1b6e03618b67ea5/packages_list_android.json",
                new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Log.e(TAG, "response : " + response.body().string());
                    }
                });
    }
}
