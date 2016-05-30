package com.madao.net.dataparser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.madao.net.model.ServiceRespHeader;

public class JasonParser {
    public static void reverseParser(String value, ServiceRespHeader respHeader){
        JSONObject jObject = JSONObject.parseObject(value);
        jObject = jObject.getJSONObject("service");
        String headerValue = jObject.getString("head");
        ServiceRespHeader tmp = JSON.parseObject(headerValue, ServiceRespHeader.class);
        respHeader.copy(tmp);
    }

    public static Object reverseParser(String value, Class clazz) {

        return JSON.toJavaObject(JSON.parseObject(value), clazz);
    }

    public static String getBody(String value) {
        JSONObject jObject = JSONObject.parseObject(value);
        jObject = jObject.getJSONObject("service");
        return jObject.getString("body");
    }

	public static String getHeader(String value) {
        JSONObject jObject = JSONObject.parseObject(value);
        jObject = jObject.getJSONObject("service");
        return jObject.getString("head");
	}

	public static String parser(Object obj) {
        return JSON.toJSONString(obj);
	}

}
