package fr.smartapps.lib_sma_packagemanager.download;

/**
 * Created by vincentchann on 27/08/16.
 */
public interface DownloadListener {
    public void onProgress(long currentProgress, long finalProgress, DownloadStatus status, String filePath);
}
