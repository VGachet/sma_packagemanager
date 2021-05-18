package fr.smartapps.lib_sma_packagemanager.package_manager;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import fr.smartapps.lib_sma_packagemanager.cookies.PersistentCookieJar;
import fr.smartapps.lib_sma_packagemanager.cookies.cache.SetCookieCache;
import fr.smartapps.lib_sma_packagemanager.cookies.persistence.SharedPrefsCookiePersistor;
import fr.smartapps.lib_sma_packagemanager.download.DownloadListener;
import fr.smartapps.lib_sma_packagemanager.download.DownloadStatus;
import fr.smartapps.lib_sma_packagemanager.download.SMADownloadWorker;
import fr.smartapps.lib_sma_packagemanager.unzip.SMAUnzipWorker;
import fr.smartapps.lib_sma_packagemanager.unzip.UnzipListener;
import fr.smartapps.lib_sma_packagemanager.unzip.UnzipStatus;
import okhttp3.Callback;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by vincentchann on 27/08/16.
 */
public class SMAPackageManager {


    private String TAG = "SMAPackageManager";
    static public String zipFolderName = "zip_folder"; // use for SMADownloadWorker

    protected String zipFolder;
    protected String packageFolder = "packages";
    protected Context context;
    protected boolean privatePackages = true;
    static protected SMAPackageManager singleInstance;
    static protected OkHttpClient client;
    static protected SetCookieCache setCookieCache;

    /**
     * Singleton SMAPackageManager
     * @param context
     * @return unique instance of SMAPackageManager
     */
    static public SMAPackageManager getInstance(Context context) {
        if (singleInstance == null) {
            singleInstance = new SMAPackageManager(context, true);  // set private package folder by default
        }
        return singleInstance;
    }

    /**
     * Private Constructor
     */
    protected SMAPackageManager(Context context, boolean privatePackages) {
        this.context = context;
        this.privatePackages = privatePackages;
        zipFolder = getExternalPublicApplicationStorageSuffix(context) + zipFolderName;
        if (privatePackages) {
            packageFolder = getExternalPrivateStorageSuffix(context) + packageFolder;
        }
        else {
            packageFolder = getExternalPublicApplicationStorageSuffix(context) + packageFolder;
        }
    }

    /**
     * Main method to download and unzip a package
     */
    public void downloadUnzipPackage(String url, final String packageNameZip, final SMAPackageListener packageListener) {
        if (packageListener == null)
            return;

        // divide 100 percent between download and unzip
        final int downloadPercent = 90;
        final int unzipPercent = 100 - downloadPercent;

        // start download
        new SMADownloadWorker(context, url, SMAPackageManager.zipFolderName, packageNameZip,
                new DownloadListener() {
                    @Override
                    public void onProgress(long currentProgress, long finalProgress, DownloadStatus status, final String filePath) {
                        try {
                            switch (status) {
                                case STATUS_PENDING:
                                    packageListener.onProgress(0, null, PackageStatus.DOWNLOAD_PENDING);
                                    break;
                                case STATUS_FAILED:
                                    packageListener.onProgress(0, null, PackageStatus.DOWNLOAD_FAILED);
                                    break;
                                case STATUS_RUNNING:
                                    packageListener.onProgress((int) ((currentProgress * downloadPercent) / finalProgress), new File(filePath), PackageStatus.DOWNLOAD_RUNNING);
                                    break;
                                case STATUS_PAUSED:
                                    packageListener.onProgress((int) ((currentProgress * downloadPercent) / finalProgress), new File(filePath), PackageStatus.DOWNLOAD_PAUSED);
                                    break;
                                case STATUS_SUCCESSFUL:
                                    packageListener.onProgress((int) ((currentProgress * downloadPercent) / finalProgress), new File(filePath), PackageStatus.DOWNLOAD_SUCCESSFUL);

                                    // unzip when the download is successful
                                    final String unzipFolder = (getPackageFolder() + "/" + packageNameZip).replace(".zip", "");
                                    new SMAUnzipWorker(filePath, unzipFolder, new UnzipListener() {
                                        @Override
                                        public void onProgress(int currentProgress, int finalProgress, UnzipStatus status, String folderPath) {
                                            try {
                                                switch (status) {
                                                    case STATUS_RUNNING:
                                                        packageListener.onProgress(downloadPercent + ((currentProgress * unzipPercent) / finalProgress), new File(filePath), PackageStatus.UNZIP_RUNNING);
                                                        break;
                                                    case STATUS_SUCCESSFUL:
                                                        packageListener.onProgress(downloadPercent + ((currentProgress * unzipPercent) / finalProgress), new File(filePath), PackageStatus.UNZIP_SUCCESSFUL);
                                                        break;
                                                    case STATUS_FAILED:
                                                        packageListener.onProgress(downloadPercent + ((currentProgress * unzipPercent) / finalProgress), new File(filePath), PackageStatus.UNZIP_FAILED);
                                                        break;
                                                }
                                            }
                                            catch (ArithmeticException e) {
                                                // if divided by 0 return 0 progress
                                                packageListener.onProgress(downloadPercent + 0, new File(filePath), PackageStatus.UNZIP_FAILED);
                                            }
                                        }
                                    }).execute();
                                    break;
                            }
                        }
                        catch (ArithmeticException e) {
                            // if divided by 0 return 0 progress
                            packageListener.onProgress(0, new File(filePath), PackageStatus.DOWNLOAD_PAUSED);
                        }
                    }
                }, false, setCookieCache);
    }

    /**
     * Everything that is not a folder (package) will be deleted of package folder and logged.
     * @return list a package from package folder
     */
    public List<String> packageList() {
        List<String> result = new ArrayList<>();
        File packageFolderFile = new File(packageFolder);

        if (packageFolderFile == null || packageFolderFile.listFiles() == null)
            return result;

        for (File file : packageFolderFile.listFiles()) {
            if (file.isDirectory()) {
                result.add(file.getName());
            }
            else {
                Log.e(TAG, "delete file of package directory : " + file.getAbsolutePath());
                FileUtils.deleteQuietly(file);
            }
        }
        return result;
    }

    /**
     * Everything that is not a folder (package) will be deleted of package folder and logged.
     * @return list a package from package folder
     */
    public boolean containsPackage(String packageFolder) {
        for (String currentPackage : packageList()) {
            if (currentPackage.equals(packageFolder)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Everything that is not a zip will be deleted from zip folder and logged.
     * @return list of zip name from zip folder
     */
    public List<String> zipList() {
        List<String> result = new ArrayList<>();
        File zipFolderFile = new File(zipFolder);

        if (zipFolderFile == null || zipFolderFile.listFiles() == null)
            return result;

        for (File file : zipFolderFile.listFiles()) {
            if (file.isDirectory()) {
                try {
                    Log.e(TAG, "delete directory of zip folder : " + file.getAbsolutePath());
                    FileUtils.deleteDirectory(file);
                } catch (IOException e) {
                    Log.e(TAG, "fail deleting directory of zip folder : " + file.getAbsolutePath());
                }
            }
            else {
                if (file.getName().endsWith(".zip") || file.getName().endsWith(".obb")) {
                    result.add(file.getName());
                }
                else {
                    Log.e(TAG, "delete file of zip directory : " + file.getAbsolutePath());
                    FileUtils.deleteQuietly(file);
                }
            }
        }
        return result;
    }

    /**
     * Delete all packages from package folder except packageNamesExceptions.
     * @param packageNamesExceptions : put a list a packages name you don't want to delete
     */
    public void deleteAllPackages(String... packageNamesExceptions) {
        File packageFolderFile = new File(packageFolder);

        if (packageFolderFile.listFiles() == null)
            return;

        for (File file : packageFolderFile.listFiles()) {

            // check each package if they are packageNamesExceptions : do not delete them
            boolean doNotDelete = false;
            for (String packageName : packageNamesExceptions) {
                if (file.getName().equals(packageName)) {
                    doNotDelete = true;
                }
            }

            // delete every directory (package) of package folder
            if (file.isDirectory() && !doNotDelete) {
                try {
                    FileUtils.deleteDirectory(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void deletePackage(String packageName) {
        File packageFolderFile = new File(packageFolder);

        if (packageFolderFile.listFiles() == null)
            return;

        for (File file : packageFolderFile.listFiles()) {
            if (file.isDirectory() && file.getName().equals(packageName)) {
                try {
                    FileUtils.deleteDirectory(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Delete all zip from zip folder.
     */
    public void clearZipFolder() {
        File zipDirectory = new File(getExternalPublicApplicationStorageSuffix(context) + zipFolderName);
        if(!zipDirectory.isDirectory())
            return;

        String[] fileList = zipDirectory.list();
        for(String file : fileList) {
            if(file.endsWith(".zip")) {
                File zipFile = new File(getExternalPublicApplicationStorageSuffix(context) + zipFolderName + "/" + file);
                FileUtils.deleteQuietly(zipFile);
            }
        }
    }

    /**
     * Getter
     * @return file of zip folder path
     */
    public File getZipFolder() {
        return new File(zipFolder);
    }

    /**
     * Getter
     * @return file of package folder path
     */
    public File getPackageFolder() {
        return new File(packageFolder);
    }


    /**
     * https://gist.github.com/granoeste/5574148
     * Public storage path specific of the app, if this app is uninstall, every file in this folder is deleted
     * @return String public storage path
     */
    static public String getExternalPublicApplicationStorageSuffix(Context context) {
        String result = context.getExternalFilesDir(null).getAbsolutePath();
        if (result.endsWith("/")) {
            return result;
        }
        else {
            return result + "/";
        }
    }

    /**
     * https://gist.github.com/granoeste/5574148
     * Private storage path specific of the app, if this app is uninstall, every file in this folder is deleted
     * @return String private storage path
     */
    static public String getExternalPrivateStorageSuffix(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            String packageName = context.getPackageName();
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            return packageInfo.applicationInfo.dataDir + "/";
        }
        catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    /**
     * http://www.vogella.com/tutorials/JavaLibrary-OkHttp/article.html
     * Start http request to a server at URL
     */
    public void startGETRequest(String url, Callback callback) {
        if (client == null && context != null) {
            client = getClient(context);
        }

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public void startPOSTRequest(String url, HashMap<String, String> parameters, Callback callback) {
        if (client == null && context != null) {
            client = getClient(context);
        }

        FormBody.Builder formBuilder = new FormBody.Builder();
        Set keys = parameters.keySet();
        Iterator it = keys.iterator();
        while (it.hasNext()) {
            Object key = it.next();
            Object value = parameters.get(key);
            formBuilder.add((String) key, (String) value);
        }

        RequestBody formBody = formBuilder.build();
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(callback);
    }

    protected OkHttpClient getClient(Context context) {
        setCookieCache = new SetCookieCache();
        CookieJar cookieJar = new PersistentCookieJar(setCookieCache, new SharedPrefsCookiePersistor(context));
        OkHttpClient client = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build();
        return client;
    }
}