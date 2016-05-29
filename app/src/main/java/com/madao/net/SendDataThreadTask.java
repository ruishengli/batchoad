package com.madao.net;

import com.alibaba.fastjson.JSONException;
import com.madao.net.dataparser.DataParserHelper;
import com.madao.net.model.RespMsg;
import com.madao.net.model.RqstMsg;
import com.madao.net.model.ServiceRespHeader;
import com.madao.util.Logs;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.util.EntityUtils;

import internal.org.apache.http.entity.mime.MultipartEntity;


public class SendDataThreadTask implements Runnable {
    private final String TAG = "SendDataThreadTask";
    private RqstMsg mRqstMsg;
    private IDataHelperSink mSink;

    public SendDataThreadTask(RqstMsg rqstMsg, IDataHelperSink sink) {
        mRqstMsg = rqstMsg;
        mSink = sink;
    }

    @Override
    public void run() {
        exe();
    }

    private int exe() {
        RespMsg respmsg = new RespMsg();

        Logs.i(TAG, "exe | request type:" + getRqstMsg().getRqstType());
        Logs.d(TAG, "exe | request str:" + getRqstMsg().getData());
        Logs.d(TAG, "exe | request URL:" + getRqstMsg().getDstAddress());

        HttpClient httpclient = null;
        boolean isSuccessful = true;
        try {
            httpclient = NetWorkHelper.getHttpClient(mRqstMsg.getTimeOut());
            HttpPost httppost = NetWorkHelper.getHttpPost(getRqstMsg().getDstAddress());

            MultipartEntity reqEntity = NetWorkHelper.getMultipartEntity(mRqstMsg.getData());

            httppost.setEntity(reqEntity);

            HttpResponse httpresponse = httpclient.execute(httppost);
            int httpRespCode = httpresponse.getStatusLine().getStatusCode();
            if (HttpStatus.SC_OK == httpRespCode) {


                String strresult = EntityUtils.toString(httpresponse.getEntity());

                Logs.d(TAG, "exe | http respcode:" + "," + HttpStatus.SC_OK + "," + strresult + ",length:" + strresult.length());
                ServiceRespHeader respheader = new ServiceRespHeader();
                DataParserHelper.reverseParser(strresult, respheader);
                respmsg.setRespCode(respheader.getReturnCode());
                respmsg.setReason(respheader.getReturnMsg());
                respmsg.setStrData(DataParserHelper.getBody(strresult));
                if (0 != respheader.getReturnCode()) {
                    isSuccessful = false;
                }
            } else {
                Logs.e(TAG, "exe | http respcode:" + httpRespCode + "," + httpresponse.getStatusLine().toString());
                respmsg.setRespCode(httpRespCode);
                respmsg.setReason(httpresponse.getStatusLine().toString());
            }
        } catch (ConnectionPoolTimeoutException ex) {
            Logs.i(TAG, "exe | ConnectionPoolTimeoutException:" + ex.toString());
            respmsg.setRespCode(-1);
            respmsg.setReason(ex.toString());
            ex.printStackTrace();
        } catch (HttpException ex) {
            Logs.i(TAG, "exe | HttpException:" + ex.toString());
            respmsg.setRespCode(-1);
            respmsg.setReason(ex.toString());
            ex.printStackTrace();
        } catch (JSONException ex) {
            Logs.i(TAG, "exe | JSONException:" + ex.toString());
            respmsg.setRespCode(-1);
            respmsg.setReason(ex.toString());
            ex.printStackTrace();
        } catch (Exception ex) {
            Logs.e(TAG, "exe | network exception:" + ex.getMessage());
            respmsg.setRespCode(-1);
            respmsg.setReason(ex.toString());
            ex.printStackTrace();
        }

        if (null != httpclient)
            httpclient.getConnectionManager().shutdown();


       if (getSink() != null)
            getSink().onResponse(respmsg);

        return 0;
    }

    /**
     * @return the mSink
     */
    /*private IDataHelperSink getSink() {
        return mSink;
    }*/

    /**
     * @return the mRqstMsg
     */
    private RqstMsg getRqstMsg() {
        return mRqstMsg;
    }

    private IDataHelperSink getSink() {
        return mSink;
    }
}