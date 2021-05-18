package fr.smartapps.lib_sma_packagemanager.unzip;

import fr.smartapps.lib_sma_packagemanager.download.DownloadStatus;

/**
 * Created by vincentchann on 27/08/16.
 */
public interface UnzipListener {
    public void onProgress(int unzipSizeProgress, int zipSize, UnzipStatus status, String folderPath);
}
