package fr.smartapps.lib_sma_packagemanager.package_manager;

/**
 * Created by vincentchann on 28/08/16.
 */
public enum PackageStatus {

    DOWNLOAD_PAUSED,        // waiting for reconnection
    DOWNLOAD_PENDING,       // about to start
    DOWNLOAD_RUNNING,       // in progress
    DOWNLOAD_SUCCESSFUL,    // finish
    DOWNLOAD_FAILED,        // fail ...

    UNZIP_RUNNING,          // in progress
    UNZIP_SUCCESSFUL,       // finish
    UNZIP_FAILED            // fail ...
}
