package com.madao.oad;


import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.madao.net.IDataHelperSink;
import com.madao.net.SendDataThreadTask;
import com.madao.net.model.RespMsg;
import com.madao.net.model.RqstMsg;
import com.madao.oad.entry.Firmware;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * com.madao.net
 *
 * @auth or
 * @sinced on 2016/5/28.
 */
public class NetHelper {

    public interface IQueryVersionCallback {
        void onQuery(Firmware firmware);
    }


    public interface IDownloadCallback {
        void onDownload(String filePath, int code);
    }

    private final String TAG = NetHelper.class.getSimpleName();

    private NetHelper() {
    }

    private static NetHelper mInstance;
    private IQueryVersionCallback mQueryVersionCallback;
    private IDownloadCallback mDownloadCallback;

    public static NetHelper getInstance() {
        if (mInstance == null) {
            synchronized (NetHelper.class) {
                if (mInstance == null) {
                    mInstance = new NetHelper();
                }
            }
        }
        return mInstance;
    }


    public void setQueryVersionCallback(IQueryVersionCallback queryVersionCallback) {
        mQueryVersionCallback = queryVersionCallback;
    }

    public void setDownloadCallback(IDownloadCallback downloadCallback) {
        mDownloadCallback = downloadCallback;
    }

    public void queryVersion() {

        RqstMsg rqstMsg = new RqstMsg();
        rqstMsg.setTag(1);
        rqstMsg.setDstAddress("http://in.madaogo.com/MDWS/ws.do");
        rqstMsg.setRqstType("queryStopwatchVersion");
        String reqStr = rqstMsg.toServiceString("");
        rqstMsg.setData(reqStr);

        SendDataThreadTask task = new SendDataThreadTask(rqstMsg, new VersionSkin());

        new Thread(task).start();
    }


    private class VersionSkin implements IDataHelperSink {

        @Override
        public int onResponse(RespMsg respMsg) {
            Firmware firmware = parseVersionResult(respMsg.getStrData());
            if (mQueryVersionCallback != null) {
                mQueryVersionCallback.onQuery(firmware);
            }
            return 0;
        }
    }


    private Firmware parseVersionResult(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        try {
            Firmware firmware = JSON.parseObject(str, Firmware.class);
            return firmware;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void downloadFw(final String saveDir, final String fileName, final String imgUrl) {
        DownloadTask task = new DownloadTask(saveDir, fileName, imgUrl);
        new Thread(task).start();
    }

    private class DownloadTask implements Runnable {
        private String saveDir, fileName, imgUrl;

        public DownloadTask(String saveDir, String fileName, String imgUrl) {
            this.saveDir = saveDir;
            this.fileName = fileName;
            this.imgUrl = imgUrl;
        }


        @Override
        public void run() {
            try {
                File file = new File(saveDir);
                if (!file.exists()) {
                    file.mkdirs();
                }

                String path = saveDir + File.separator + fileName;
                File fileBin = new File(path);

                URL url = new URL(imgUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.setUseCaches(true);
                conn.setConnectTimeout(2 * 1000);
                conn.setRequestMethod("GET");

                if (conn.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                    if (mDownloadCallback != null) {
                        mDownloadCallback.onDownload(null, -1);
                    }

                    return;
                }

                fileBin.delete();
                FileHelper.createFile(path);
                InputStream in = conn.getInputStream();
                FileOutputStream fileOut = new FileOutputStream(fileBin);
                byte[] buffer = new byte[1024];
                int length = -1;
                try {
                    while ((length = in.read(buffer)) != -1) {
                        fileOut.write(buffer, 0, length);
                    }

                    if (mDownloadCallback != null) {
                        mDownloadCallback.onDownload(path, 0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (mDownloadCallback != null) {
                        mDownloadCallback.onDownload(null, -1);
                    }
                } finally {
                    if (null != in) {
                        in.close();
                    }
                    if (null != conn) {
                        conn.disconnect();
                    }
                    if (null != fileOut) {
                        fileOut.flush();
                        fileOut.close();
                    }
                }

            } catch ( Exception e){
                e.printStackTrace();
                if (mDownloadCallback != null) {
                    mDownloadCallback.onDownload(null, -1);
                }
            }
        }
    }

}
