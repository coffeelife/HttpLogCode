package cn.api.gjhealth.cstore.http;

import android.app.Application;

import com.gjhealth.library.http.GHttp;
import com.gjhealth.library.http.cache.CacheMode;
import com.gjhealth.library.http.https.HttpsUtils;
import com.gjhealth.library.http.model.HttpHeaders;
import com.gjhealth.library.http.model.HttpParams;
import com.gjhealth.library.utils.DeviceUtil;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import cn.api.gjhealth.cstore.app.ApiManager;
import cn.api.gjhealth.cstore.app.GlobalEnv;
import cn.api.gjhealth.cstore.http.api.AddCookiesInterceptor;
import cn.api.gjhealth.cstore.http.api.HttpLogInterceptor;
import cn.api.gjhealth.cstore.http.api.ReceivedCookiesInterceptor;
import okhttp3.OkHttpClient;

/**
 * Created by ccx on 2018/07/17
 */
public class HttpManager {
    public static void init(Application context) {
        //---------这里给出的是示例代码,告诉你可以这么传,实际使用的时候,根据需要传,不需要就不传-------------//
        HttpHeaders headers = new HttpHeaders();
        headers.put("platform", "android");    //header不支持中文，不允许有特殊字符
        headers.put("siteId", "cstore");    //header不支持中文，不允许有特殊字符
        headers.put("version", DeviceUtil.getVersionName(context));    //header不支持中文，不允许有特殊字符
        headers.put("versionCode", DeviceUtil.getVersion(context) + "");    //header不支持中文，不允许有特殊字符
        headers.put("channel", GlobalEnv.getChannel());
        headers.put("model", DeviceUtil.getDeviceModel());
        headers.put("manufacturer", DeviceUtil.getDeviceManufacturer());
        headers.put("deviceId", DeviceUtil.getAndroidID(context));
        headers.put("os_version", DeviceUtil.getOSVersion());
        HttpParams params = new HttpParams();
//        params.put("commonParamsKey1", "commonParamsValue1");     //param支持中文,直接传,不要自己编码
//        params.put("commonParamsKey2", "这里支持中文参数");
        //----------------------------------------------------------------------------------------//

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        //log相关
        HttpLogInterceptor loggingInterceptor = new HttpLogInterceptor().setLevel(
                !GlobalEnv.isRelease() ? HttpLogInterceptor.Level.BODY : HttpLogInterceptor.Level.NONE);
        builder.addInterceptor(new AddCookiesInterceptor());                                 //添加OkGo默认debug日志
        builder.addInterceptor(new ReceivedCookiesInterceptor());                                 //添加OkGo默认debug日志
//        builder.addInterceptor(new HttpCacheInterceptor());                                 //添加OkGo默认debug日志
        builder.addInterceptor(loggingInterceptor);                                 //添加OkGo默认debug日志
        //第三方的开源库，使用通知显示当前请求的log，不过在做文件下载的时候，这个库好像有问题，对文件判断不准确
//        builder.addInterceptor(new ChuckInterceptor(this));

        //超时时间设置，默认60秒
        builder.readTimeout(GHttp.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);      //全局的读取超时时间
        builder.writeTimeout(GHttp.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);     //全局的写入超时时间
        builder.connectTimeout(GHttp.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);   //全局的连接超时时间

        //自动管理cookie（或者叫session的保持），以下几种任选其一就行
        //builder.cookieJar(new CookieJarImpl(new SPCookieStore(this)));            //使用sp保持cookie，如果cookie不过期，则一直有效
//        builder.cookieJar(new CookieJarImpl(new DBCookieStore(context)));              //使用数据库保持cookie，如果cookie不过期，则一直有效
        //builder.cookieJar(new CookieJarImpl(new MemoryCookieStore()));            //使用内存保持cookie，app退出后，cookie消失

        //https相关设置，以下几种方案根据需要自己设置
        //方法一：信任所有证书,不安全有风险
        HttpsUtils.SSLParams sslParams1 = HttpsUtils.getSslSocketFactory();
        //方法二：自定义信任规则，校验服务端证书
//        HttpsUtils.SSLParams sslParams2 = HttpsUtils.getSslSocketFactory(new SafeTrustManager());
        //方法三：使用预埋证书，校验服务端证书（自签名证书）
        //HttpsUtils.SSLParams sslParams3 = HttpsUtils.getSslSocketFactory(getAssets().open("srca.cer"));
        //方法四：使用bks证书和密码管理客户端证书（双向认证），使用预埋证书，校验服务端证书（自签名证书）
        //HttpsUtils.SSLParams sslParams4 = HttpsUtils.getSslSocketFactory(getAssets().open("xxx.bks"), "123456", getAssets().open("yyy.cer"));
        builder.sslSocketFactory(sslParams1.sSLSocketFactory, sslParams1.trustManager);
        //配置https的域名匹配规则，详细看demo的初始化介绍，不需要就不要加入，使用不当会导致https握手失败
        builder.hostnameVerifier(new SafeHostnameVerifier());

        // 其他统一的配置
        GHttp.getInstance().init(context)                           //必须调用初始化
                .setBaseUrl(ApiManager.getInstance().getBaseUrl())
                .setOkHttpClient(builder.build())               //建议设置OkHttpClient，不设置会使用默认的
                .setCacheMode(CacheMode.NO_CACHE)               //全局统一缓存模式，默认不使用缓存，可以不传
//                .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE)   //全局统一缓存时间，默认永不过期，可以不传
                .setCacheTime(1000 * 60 * 60 * 24)           //全局统一缓存时间，默认永不过期，可以不传
                .setRetryCount(3)                               //全局统一超时重连次数，默认为三次，那么最差的情况会请求4次(一次原始请求，三次重连请求)，不需要可以设置为0
                .addCommonHeaders(headers)                      //全局公共头
                .addCommonParams(params);                       //全局公共参数

    }

    /**
     * 这里只是我谁便写的认证规则，具体每个业务是否需要验证，以及验证规则是什么，请与服务端或者leader确定
     */
    private static class SafeTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            try {
                for (X509Certificate certificate : chain) {
                    certificate.checkValidity(); //检查证书是否过期，签名是否通过等
                }
            } catch (Exception e) {
                throw new CertificateException(e);
            }
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    /**
     * 这里只是我谁便写的认证规则，具体每个业务是否需要验证，以及验证规则是什么，请与服务端或者leader确定
     */
    private static class SafeHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            //验证主机名是否匹配
            //return hostname.equals("server.xx.com");
            return true;
        }
    }
}