package fr.smartapps.lib_sma_packagemanager.package_manager;

import java.io.File;

/**
 * Created by vincentchann on 28/08/16.
 */
public interface SMAPackageListener {
    public void onProgress(int progressOn100, File packageFile, PackageStatus status);
}
