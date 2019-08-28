package cn.api.gjhealth.cstore.http;

import android.text.TextUtils;
import com.gjhealth.library.http.model.HttpCacheBean;
import com.gjhealth.library.utils.ArrayUtils;
import com.gjhealth.library.utils.SharedUtil;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import cn.api.gjhealth.cstore.app.BaseApp;
import cn.api.gjhealth.cstore.utils.jsonutils.GsonUtil;

public class HttpLogManager {
    public static List<HttpCacheBean> getLogLists(String keyWords) {
        List<HttpCacheBean> listBeans = null;
        String json = SharedUtil.instance(BaseApp.getContext()).getString(HttpCacheBean.TAG);
        if (GsonUtil.isGoodJson(json)) listBeans = GsonUtil.getEntityList(json, HttpCacheBean.class);
        if (GsonUtil.isBadJson(json) || listBeans == null) listBeans = new ArrayList<>();
        if (TextUtils.isEmpty(keyWords)) {
            Collections.reverse(listBeans);
            return listBeans;
        } else {
            List<HttpCacheBean> listBeansTemp = new ArrayList<>();
            if (!ArrayUtils.isEmpty(listBeans)) {
                for (HttpCacheBean bean : listBeans) {
                    if (bean != null && !TextUtils.isEmpty(bean.url)) {
                        try {
                            URL url = new URL(URLDecoder.decode(bean.url));
                            if (url.getPath().contains(keyWords)) {
                                listBeansTemp.add(bean);
                            }
                        } catch (MalformedURLException e) {
                        }
                    }
                }
            }
            Collections.reverse(listBeansTemp);
            return listBeansTemp;
        }
    }

    public static void saveLogBean(HttpCacheBean bean) {
        List<HttpCacheBean> httpCacheBeans = null;
        String json = SharedUtil.instance(BaseApp.getContext()).getString(HttpCacheBean.TAG);
        if (GsonUtil.isGoodJson(json))
            httpCacheBeans = GsonUtil.getEntityList(json, HttpCacheBean.class);
        if (GsonUtil.isBadJson(json) || httpCacheBeans == null) httpCacheBeans = new ArrayList<>();
        httpCacheBeans.add(bean);
        if (!ArrayUtils.isEmpty(httpCacheBeans) && httpCacheBeans.size() > 100) {
            httpCacheBeans = httpCacheBeans.subList(httpCacheBeans.size() - 100, httpCacheBeans.size());
        }
        SharedUtil.instance(BaseApp.getContext()).saveObject(HttpCacheBean.TAG, httpCacheBeans);
    }
}
