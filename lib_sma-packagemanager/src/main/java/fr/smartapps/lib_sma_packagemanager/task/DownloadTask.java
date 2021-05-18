package fr.smartapps.lib_sma_packagemanager.task;

import fr.smartapps.lib_sma_packagemanager.package_manager.SMAPackageListener;

/**
 * Created by vincentchann on 29/08/16.
 */
public class DownloadTask {

    public String url;
    public String packageName;
    public SMAPackageListener packageListener;

    public DownloadTask(String url, String packageName, SMAPackageListener packageListener) {
        this.url = url;
        this.packageName = packageName;
        this.packageListener = packageListener;
    }
}
