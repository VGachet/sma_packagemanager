package fr.smartapps.lib_sma_packagemanager.unzip;

/**
 * Created by vincentchann on 27/08/16.
 */

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * http://stackoverflow.com/questions/11947381/progress-bar-with-unzipping-of-file?answertab=active#tab-top
 */
public class SMAUnzipWorker extends AsyncTask<Void, Integer, Integer> {

    protected String TAG = "SMAUnzipWorker";
    protected String zipPath;
    protected String unzipFolderPath;
    protected UnzipListener unzipListener;
    protected boolean unzipSuccessful = true;
    protected int zipSize;

    public SMAUnzipWorker(String zipPath, String unzipFolderPath, UnzipListener unzipListener) {
        this.zipPath = zipPath;
        this.unzipFolderPath = unzipFolderPath;
        this.unzipListener = unzipListener;
        if (this.unzipListener != null) {
            this.unzipListener.onProgress(0, 100, UnzipStatus.STATUS_RUNNING, unzipFolderPath);
        }
    }

    protected Integer doInBackground(Void... voids) {
        Log.e(TAG, "Unzip - zipPath : " + zipPath);
        Log.e(TAG, "Unzip - unzipFolderPath : " + unzipFolderPath);
        InputStream is;
        ZipInputStream zis;
        try
        {
            ZipFile zip = new ZipFile(zipPath);
            zipSize = zip.size();

            File destinationFolder = new File(unzipFolderPath);
            if (!destinationFolder.exists())
                destinationFolder.mkdirs();

            is = new FileInputStream(zipPath);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze = null;
            byte[] buffer = new byte[1024];
            int validator;
            int count = 0;
            String filename;
            while (((ze = zis.getNextEntry()) != null)) {
                filename = ze.getName();
                count++;

                // publish progress
                publishProgress(count, zipSize);

                // Need to create directories if not exists, or it will generate an Exception...
                if (ze.isDirectory()) {
                    File fmd = new File(unzipFolderPath + "/" + filename);
                    fmd.mkdirs();
                    continue;
                }

                FileOutputStream fOut = new FileOutputStream(unzipFolderPath + "/" + filename);
                while ((validator = zis.read(buffer)) != -1) {
                    fOut.write(buffer, 0, validator);
                }

                fOut.close();
                zis.closeEntry();
            }

            zis.close();
            is.close();

            File zipFile = new File(zipPath);
            boolean deleted = zipFile.delete();
            if (!deleted) {
                Log.e(TAG, "Could not delete the zip file");
            }

        }
        catch (IOException e) {
            e.printStackTrace();
            unzipSuccessful = false;
            return null;
        }

        return null;

    }

    protected void onProgressUpdate(Integer... progress) {
        if (unzipListener != null) {
            Log.e(TAG, "Unzip - progress : " + (progress[0] * 100 / progress[1]) + "%");
            unzipListener.onProgress(progress[0], progress[1], UnzipStatus.STATUS_RUNNING, unzipFolderPath);
        }
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
        if (unzipSuccessful) {
            Log.e(TAG, "Unzip - successful");
            unzipListener.onProgress(zipSize, zipSize, UnzipStatus.STATUS_SUCCESSFUL, unzipFolderPath);
        }
        else {
            Log.e(TAG, "Unzip - fail");
            unzipListener.onProgress(0, zipSize, UnzipStatus.STATUS_FAILED, unzipFolderPath);
        }
    }
}
