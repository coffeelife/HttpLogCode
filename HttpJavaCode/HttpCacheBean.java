package com.gjhealth.library.http.model;

import java.io.Serializable;

public class HttpCacheBean implements Serializable {
    public static String TAG = HttpCacheBean.class.getSimpleName();
    public String url;
    public String requestTime;
    public int code;
    public String time;
    public String reqHeaders;
    public String resHeaders;
    public String requestBody;
    public String protocol;
    public String method;
    public String msg;
    public String responseBody;
    public String reqContentType;
    public String resContentType;
    public String error;
    public String reqContentLength;
    public String resContentLength;
    public String requestMsg;
    public String responseMsg;
}
