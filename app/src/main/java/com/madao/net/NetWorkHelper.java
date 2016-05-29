package com.madao.net;

import android.os.Build;


import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.nio.charset.Charset;

import internal.org.apache.http.entity.mime.MultipartEntity;
import internal.org.apache.http.entity.mime.content.StringBody;

public class NetWorkHelper {


    private static final int NETWORK_SO_TOMEOUT = 10000; // 10000ms

    public static  HttpClient getHttpClient(int soTimeOut) throws Exception {
        // 设置HttpClient超时参数
        
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);

        if (soTimeOut == 0) {
            HttpConnectionParams.setSoTimeout(httpParameters, NETWORK_SO_TOMEOUT);
        }
        else {
            HttpConnectionParams.setSoTimeout(httpParameters, soTimeOut);
        }

        DefaultHttpClient httpclient = new DefaultHttpClient(httpParameters);

        httpclient.getParams().setParameter("http.protocol.content-charset",HTTP.UTF_8);  
        httpclient.getParams().setParameter(HTTP.CONTENT_ENCODING, HTTP.UTF_8);  
        httpclient.getParams().setParameter(HTTP.CHARSET_PARAM, HTTP.UTF_8);  
        httpclient.getParams().setParameter(HTTP.DEFAULT_PROTOCOL_CHARSET,HTTP.UTF_8);  
        DefaultHttpRequestRetryHandler d =new DefaultHttpRequestRetryHandler(3, true);
        httpclient.setHttpRequestRetryHandler(d);  
        return httpclient;
    }

    public static HttpPost getHttpPost(String dstAddress) {
        String model = Build.MANUFACTURER	+ "_" + Build.MODEL;
        HttpPost httppost = new HttpPost(dstAddress);
        httppost.addHeader("Hardware",model);

        httppost.getParams().setParameter("http.protocol.content-charset",HTTP.UTF_8);
        httppost.getParams().setParameter(HTTP.CONTENT_ENCODING, HTTP.UTF_8);  
        httppost.getParams().setParameter(HTTP.CHARSET_PARAM, HTTP.UTF_8);  
        httppost.getParams().setParameter(HTTP.DEFAULT_PROTOCOL_CHARSET, HTTP.UTF_8);
     
        return httppost;
    }


   public static MultipartEntity getMultipartEntity(String data) throws IOException {
        MultipartEntity reqEntity = new MultipartEntity();
        reqEntity.addPart("Data", new StringBody(data, Charset.forName("UTF-8")));
        return reqEntity;
    }

    
}
