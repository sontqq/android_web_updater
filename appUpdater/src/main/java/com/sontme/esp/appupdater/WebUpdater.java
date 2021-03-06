package com.sontme.esp.appupdater;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebUpdater {

    // USAGE
    /*
        WebUpdater updater = new WebUpdater(
            getApplicationContext(),
            "update_file.apk",
            "https://example.com/update_file.apk"
        );
        updater.startUpdate();

     */

    private Context ctx;
    private String fileName;
    private String updateUrl;

    public WebUpdater(Context c, String fileName, String updateUrl) {
        this.ctx = c;
        this.fileName = fileName;
        this.updateUrl = updateUrl;
    }

    public void startUpdate(){
        downloadUpdate_Oreo_Http(ctx,fileName,updateUrl);
    }

    public void downloadUpdate_Oreo_Http(Context _ctx, String _fileName, String _updateUrl) {
        final File file = new File(Environment.getExternalStorageDirectory(), _fileName);
        final DownloadTask downloadTask = new DownloadTask(_ctx, file, "Updating...", _fileName);
        downloadTask.execute(_updateUrl);
    }

}

class DownloadTask extends AsyncTask<String, Integer, String> {
    private ProgressDialog mPDialog;
    private Context mContext;
    private PowerManager.WakeLock mWakeLock;
    private File mTargetFile;
    private String filename;

    public DownloadTask(Context context, File targetFile, String dialogMessage, String filename) {
        this.mContext = context;
        this.filename = filename;
        this.mTargetFile = targetFile;
        mPDialog = new ProgressDialog(context);

        mPDialog.setMessage(dialogMessage);
        mPDialog.setIndeterminate(true);
        mPDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mPDialog.setCancelable(true);
        final DownloadTask me = this;
        mPDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                me.cancel(true);
            }
        });
        Log.i("DownloadTask","Constructor done");
    }

    @Override
    protected String doInBackground(String... sUrl) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(sUrl[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage();
            }
            Log.i("DownloadTask","Response " + connection.getResponseCode());

            // this will be useful to display download percentage
            // might be -1: server did not report the length
            int fileLength = connection.getContentLength();

            // download the file
            input = connection.getInputStream();
            output = new FileOutputStream(mTargetFile,false);

            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                // allow canceling with back button
                if (isCancelled()) {
                    Log.i("DownloadTask","Cancelled");
                    input.close();
                    return null;
                }
                total += count;
                // publishing the progress....
                if (fileLength > 0) // only if total length is known
                    publishProgress((int) (total * 100 / fileLength));
                output.write(data, 0, count);
            }
        } catch (Exception e) {
            return e.toString();
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }

            if (connection != null)
                connection.disconnect();
        }
        return null;
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // take CPU lock to prevent CPU from going off if the user
        // presses the power button during download
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                getClass().getName());
        mWakeLock.acquire();

        mPDialog.show();

    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        // if we get here, length is known, now set indeterminate to false
        mPDialog.setIndeterminate(false);
        mPDialog.setMax(100);
        mPDialog.setProgress(progress[0]);

    }

    @Override
    protected void onPostExecute(String result) {
        Log.i("DownloadTask", "Work Done! PostExecute");
        mWakeLock.release();
        mPDialog.dismiss();
        if (result != null)
            Toast.makeText(mContext,"Download error: "+result, Toast.LENGTH_LONG).show();
        else{
            installAPK(mContext, filename);
        }
    }

    public Uri getFileUri(Context context, File file) {
        return FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".com.sontme.esp.tauriqueuewatcher.provider", file);
    }

    public void installAPK(Context context, String filename) {
        final File file = new File(Environment.getExternalStorageDirectory(), filename);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri downloaded_apk = getFileUri(context,file);
            Intent intent = new Intent(Intent.ACTION_VIEW).setDataAndType(downloaded_apk,
                    "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(intent);
        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file),
                    "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

}

