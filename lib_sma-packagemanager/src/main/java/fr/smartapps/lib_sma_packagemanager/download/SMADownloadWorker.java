package fr.smartapps.lib_sma_packagemanager.download;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import java.util.Iterator;

import fr.smartapps.lib_sma_packagemanager.cookies.cache.SetCookieCache;
import fr.smartapps.lib_sma_packagemanager.package_manager.SMAPackageManager;
import okhttp3.Cookie;

/**
 * Created by vincentchann on 27/08/16.
 */
public class SMADownloadWorker {

    /*
    Attributes
     */
    private String TAG = "SMADownloadWorker";
    protected String url;
    protected String filename;
    protected DownloadListener downloadListener;
    protected DownloadManager downloadManager;
    protected long downloadId;
    protected Handler handler = new Handler();

    /*
    Constructor
     */
    public SMADownloadWorker(final Context context, String url, final String downloadFolder, final String filename, final DownloadListener downloadListener, boolean notification) {
        this(context, url, downloadFolder, filename, downloadListener, notification, null);
    }

    public SMADownloadWorker(final Context context, String url, final String downloadFolder, final String filename, final DownloadListener downloadListener, boolean notification, SetCookieCache setCookieCache) {
        this.url = url;
        this.filename = filename;
        this.downloadListener = downloadListener;

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

        Log.e(TAG, "setCookieCache : " + setCookieCache);
        String cookieContent = "";
        if (setCookieCache != null) {
            Iterator<Cookie> iterator = setCookieCache.iterator();
            while (iterator.hasNext()) {
                Cookie cookie = iterator.next();
                cookieContent += cookie.name() + "=" + cookie.value() + ";";
            }
            Log.e(TAG, "cookieContent = " + cookieContent);
            request.addRequestHeader("Cookie", cookieContent);
        }

        request.allowScanningByMediaScanner();
        if (notification) {
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }
        else {
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        }
        request.setDestinationInExternalFilesDir(context, downloadFolder, filename);

        downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        downloadId = downloadManager.enqueue(request);

        handler.post(new Runnable() {

            @Override
            public void run() {
                // get our download "cursor" with "downloadId"
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadId);
                Cursor cursor = downloadManager.query(query);
                cursor.moveToFirst();

                // get download progress with "cursor"
                long bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                long bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                if (downloadListener != null) {
                    downloadListener.onProgress(bytes_downloaded, bytes_total, status(cursor), SMAPackageManager.getExternalPublicApplicationStorageSuffix(context) + downloadFolder + "/" + filename);
                }

                // check if download finish with "cursor" to finish while loop
                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                    // end of download : stop this worker thread
                } else {
                    handler.postDelayed(this, 500);
                }

                // close "cursor"
                cursor.close();
            }
        });
    }



    protected DownloadStatus status(Cursor c) {
        DownloadStatus status = null;

        switch (c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
            case DownloadManager.STATUS_FAILED:
                status = DownloadStatus.STATUS_FAILED;
                Log.e(TAG, "Download - fail - you should restart");
                break;

            case DownloadManager.STATUS_PAUSED:
                status = DownloadStatus.STATUS_PAUSED;
                Log.e(TAG, "Download - paused - waiting for connexion");
                break;

            case DownloadManager.STATUS_PENDING:
                status = DownloadStatus.STATUS_PENDING;
                Log.i(TAG, "Download - pending - connecting to server");
                break;

            case DownloadManager.STATUS_RUNNING:
                status = DownloadStatus.STATUS_RUNNING;
                //Log.i(TAG, "Download - running - in progress");
                break;

            case DownloadManager.STATUS_SUCCESSFUL:
                status = DownloadStatus.STATUS_SUCCESSFUL;
                Log.i(TAG, "Download - successful - finish");
                break;
        }

        return status;
    }
}
