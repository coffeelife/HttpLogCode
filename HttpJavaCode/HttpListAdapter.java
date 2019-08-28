package cn.api.gjhealth.cstore.module.demo;

import android.text.TextUtils;

import com.gjhealth.library.adapter.base.BaseQuickAdapter;
import com.gjhealth.library.adapter.base.BaseViewHolder;
import com.gjhealth.library.http.model.HttpCacheBean;

import java.net.URLDecoder;

import cn.api.gjhealth.cstore.R;

public class HttpListAdapter extends BaseQuickAdapter<HttpCacheBean, BaseViewHolder> {

    public HttpListAdapter() {
        super(R.layout.item_http_view);
    }

    @Override
    protected void convert(BaseViewHolder helper, HttpCacheBean item) {
        helper.setText(R.id.tv_code, TextUtils.isEmpty(item.code + "") ? "none" : item.code + "");
        helper.setText(R.id.tv_method, TextUtils.isEmpty(item.method) ? "none" : item.method);
        helper.setText(R.id.tv_url, TextUtils.isEmpty(item.url) ? "--" : URLDecoder.decode(item.url));
    }
}
