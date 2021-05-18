package fr.smartapps.lib_sma_packagemanager.download;

/**
 * Created by vincentchann on 27/08/16.
 */
public enum DownloadStatus {

    STATUS_PAUSED,      // waiting for reconnection
    STATUS_PENDING,     // about to start
    STATUS_RUNNING,     // in progress
    STATUS_SUCCESSFUL,  // finish
    STATUS_FAILED       // fail ...

}
