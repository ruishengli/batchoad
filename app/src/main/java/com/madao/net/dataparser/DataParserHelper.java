package com.madao.net.dataparser;

import com.madao.net.model.ServiceRespHeader;


public class DataParserHelper {
    public static String parser(Object obj) {
        return JasonParser.parser(obj);
    }

    public static void reverseParser(String value, ServiceRespHeader respHeader){
        JasonParser.reverseParser(value, respHeader);
    }

    public static Object reverseParser(String value, Class clazz) {
        return JasonParser.reverseParser(value, clazz);
    }

    public static String getBody(String value) {
        return JasonParser.getBody(value);
    }

    public static String getHeader(String value) {
        return JasonParser.getHeader(value);
    }
}
