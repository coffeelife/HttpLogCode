package cn.api.gjhealth.cstore.http.api;

import com.gjhealth.library.http.model.HttpCacheBean;
import com.gjhealth.library.utils.ArrayUtils;
import com.gjhealth.library.utils.SharedUtil;
import com.google.gson.JsonObject;

import cn.api.gjhealth.cstore.app.BaseApp;
import cn.api.gjhealth.cstore.http.HttpLogManager;
import cn.api.gjhealth.cstore.utils.DateFormatUtils;
import okhttp3.*;
import okhttp3.internal.http.HttpHeaders;
import okio.Buffer;
import okio.BufferedSource;

import java.io.EOFException;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by ccx on 2018/07/11
 */
public class HttpLogInterceptor implements Interceptor {
    private static final Charset UTF8 = Charset.forName("UTF-8");

    public enum Level {
        /**
         * No logs.
         */
        NONE,
        /**
         * Logs request and response lines.
         *
         * <p>Example:
         * <pre>{@code
         * --> POST /greeting http/1.1 (3-byte body)
         *
         * <-- 200 OK (22ms, 6-byte body)
         * }</pre>
         */
        BASIC,
        /**
         * Logs request and response lines and their respective headers.
         *
         * <p>Example:
         * <pre>{@code
         * --> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         * <-- END HTTP
         * }</pre>
         */
        HEADERS,
        /**
         * Logs request and response lines and their respective headers and bodies (if present).
         *
         * <p>Example:
         * <pre>{@code
         * --> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         *
         * Hi?
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         *
         * Hello!
         * <-- END HTTP
         * }</pre>
         */
        BODY
    }

    public interface Logger {
        void log(String message);

        void log(String tag, String message);

        void json(String tag, String message);

        /**
         * A {@link HttpLogInterceptor.Logger} defaults output appropriate for the current platform.
         */
        HttpLogInterceptor.Logger DEFAULT = new HttpLogInterceptor.Logger() {
            @Override
            public void log(String message) {
                com.gjhealth.library.utils.log.Logger.t("http").d(message);
            }

            @Override
            public void log(String tag, String message) {
                com.gjhealth.library.utils.log.Logger.t("http" + tag).d(message);
            }

            @Override
            public void json(String tag, String message) {
                com.gjhealth.library.utils.log.Logger.t(tag).json(message);
            }
        };
    }

    public HttpLogInterceptor() {
        this(HttpLogInterceptor.Logger.DEFAULT);
    }

    public HttpLogInterceptor(HttpLogInterceptor.Logger logger) {
        this.logger = logger;
    }

    private final HttpLogInterceptor.Logger logger;

    private volatile HttpLogInterceptor.Level level = HttpLogInterceptor.Level.NONE;

    /**
     * Change the level at which this interceptor logs.
     */
    public HttpLogInterceptor setLevel(HttpLogInterceptor.Level level) {
        if (level == null) throw new NullPointerException("level == null. Use Level.NONE instead.");
        this.level = level;
        return this;
    }

    public HttpLogInterceptor.Level getLevel() {
        return level;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        HttpLogInterceptor.Level level = this.level;
        long startNs = System.nanoTime();

        Request request = chain.request();
        HttpCacheBean bean = new HttpCacheBean();
        bean.requestTime = DateFormatUtils.getCurrentDateTime();
        if (level == HttpLogInterceptor.Level.NONE) {
            return chain.proceed(request);
        }

        boolean logBody = level == HttpLogInterceptor.Level.BODY;
        boolean logHeaders = logBody || level == HttpLogInterceptor.Level.HEADERS;

        RequestBody requestBody = request.body();
        boolean hasRequestBody = requestBody != null;

        Connection connection = chain.connection();
        Protocol protocol = connection != null ? connection.protocol() : Protocol.HTTP_1_1;
        String requestStartMessage = "--> " + request.method() + ' ' + request.url() + ' ' + protocol;
        bean.method = request.method();
        bean.url = request.url().toString();
        bean.protocol = protocol + "";
        if (!logHeaders && hasRequestBody) {
            requestStartMessage += " (" + requestBody.contentLength() + "-byte body)";
            bean.reqContentLength = requestStartMessage;
        }
        logger.log(requestStartMessage);
        bean.requestMsg = requestStartMessage;
        if (logHeaders) {
//            if (hasRequestBody) {
//                // Request body headers are only present when installed as a network interceptor. Force
//                // them to be included (when available) so there values are known.
//                if (requestBody.contentType() != null) {
//                    logger.log("Content-Type: " + requestBody.contentType());
//                }
//                if (requestBody.contentLength() != -1) {
//                    logger.log("Content-Length: " + requestBody.contentLength());
//                }
//            }

            Headers headers = request.headers();
//            StringBuffer sbReq = new StringBuffer();
            if (headers.size() > 0) {
                JsonObject jsonHeaders = new JsonObject();
                for (int i = 0, count = headers.size(); i < count; i++) {
                    String value = headers.value(i);
                    if (value != null) {
                        value = URLDecoder.decode(value, "UTF-8");
                    }
                    jsonHeaders.addProperty(headers.name(i), value);
//                    sbReq.append(headers.name(i) + ":" + value + "\n");
                }
                logger.json("httphead-req", jsonHeaders.toString());
                bean.reqHeaders = jsonHeaders.toString();
            }

            if (!logBody || !hasRequestBody) {
//                logger.log("--> END " + request.method());
            } else if (bodyEncoded(request.headers())) {
//                logger.log("--> END " + request.method() + " (encoded body omitted)");
            } else {
                Buffer buffer = new Buffer();
                requestBody.writeTo(buffer);

                Charset charset = UTF8;
                MediaType contentType = requestBody.contentType();
                if (contentType != null) {
                    charset = contentType.charset(UTF8);
                }
                if (isPlaintext(buffer)) {
                    logger.json("httprequest", buffer.clone().readString(charset));
                    bean.requestBody = buffer.readString(charset);
                } else {
                    logger.log("--> END " + request.method() + " (binary "
                            + requestBody.contentLength() + "-byte body omitted)");
                }
                bean.reqContentType = contentType.toString();
            }
        }


        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            logger.log("<-- HTTP FAILED: " + e);
            bean.error = e.toString();
            throw e;
        }
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

        ResponseBody responseBody = response.body();
        long contentLength = responseBody.contentLength();
        String bodySize = contentLength != -1 ? contentLength + "-byte" : "unknown-length";
        logger.log("<-- " + response.code() + ' ' + response.message() + ' '
                + response.request().url() + " (" + tookMs + "ms" + (!logHeaders ? ", "
                + bodySize + " body" : "") + ')');
        bean.responseMsg = "<-- " + response.code() + ' ' + response.message() + ' '
                + response.request().url() + " (" + tookMs + "ms" + (!logHeaders ? ", "
                + bodySize + " body" : "") + ')';
        bean.resContentLength = bodySize;
        bean.code = response.code();
        bean.msg = response.message();
        bean.time = tookMs + "ms";

        if (logHeaders) {
//            StringBuffer sbRes = new StringBuffer();
            Headers headers = response.headers();
            JsonObject jsonHeaders = new JsonObject();
            for (int i = 0, count = headers.size(); i < count; i++) {
                String value = headers.value(i);
                if (value != null) {
                    value = URLDecoder.decode(value, "UTF-8");
                }
                jsonHeaders.addProperty(headers.name(i), value);
//                sbRes.append(headers.name(i) + ":" + value + "\n");
//                logger.log(headers.name(i) + ": " + headers.value(i));

            }
            logger.json("httphead-rsp", jsonHeaders.toString());
            bean.resHeaders = jsonHeaders.toString();

            if (!logBody || !HttpHeaders.hasBody(response)) {

            } else if (bodyEncoded(response.headers())) {
                logger.log("<-- END HTTP (encoded body omitted)");
            } else if (isPlaintext(responseBody.contentType())) {
                BufferedSource source = responseBody.source();
                source.request(Long.MAX_VALUE); // Buffer the entire body.
                Buffer buffer = source.buffer();

                Charset charset = UTF8;
                MediaType contentType = responseBody.contentType();
                if (contentType != null) {
                    try {
                        charset = contentType.charset(UTF8);
                    } catch (UnsupportedCharsetException e) {
                        logger.log("");
                        logger.log("Couldn't decode the response body; charset is likely malformed.");
                        logger.log("<-- END HTTP");

                        return response;
                    }
                }
                bean.resContentType = contentType.toString();

//                if (!isPlaintext(buffer)) {
//                    logger.log("<-- END HTTP (binary " + buffer.size() + "-byte body omitted)");
//                    return response;
//                }

                if (contentLength != 0) {
//                    logger.log("");
                    logger.log("body", buffer.clone().readString(charset));
                    bean.responseBody = buffer.clone().readString(charset);
                }

                logger.log("<-- END HTTP (" + buffer.size() + "-byte body)");
            }
        }

        HttpLogManager.saveLogBean(bean);
        return response;
    }

    private static boolean isPlaintext(MediaType mediaType) {
        if (mediaType == null) return false;
        if (mediaType.type() != null && mediaType.type().equals("text")) {
            return true;
        }
        String subtype = mediaType.subtype();
        if (subtype != null) {
            subtype = subtype.toLowerCase();
            if (subtype.contains("x-www-form-urlencoded") || subtype.contains("json") || subtype.contains("xml") || subtype.contains("html")) //
                return true;
        }
        return false;
    }

    /**
     * Returns true if the body in question probably contains human readable text. Uses a small sample
     * of code points to detect unicode control characters commonly used in binary file signatures.
     */
    static boolean isPlaintext(Buffer buffer) {
        try {
            Buffer prefix = new Buffer();
            long byteCount = buffer.size() < 64 ? buffer.size() : 64;
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                int codePoint = prefix.readUtf8CodePoint();
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            return false; // Truncated UTF-8 sequence.
        }
    }

    private boolean bodyEncoded(Headers headers) {
        String contentEncoding = headers.get("Content-Encoding");
        return contentEncoding != null && !contentEncoding.equalsIgnoreCase("identity");
    }
}

