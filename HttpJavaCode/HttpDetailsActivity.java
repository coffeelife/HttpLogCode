package cn.api.gjhealth.cstore.module.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbnb.lottie.parser.ColorParser;
import com.gjhealth.library.http.model.HttpCacheBean;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import butterknife.Bind;
import butterknife.OnClick;
import cn.api.gjhealth.cstore.R;
import cn.api.gjhealth.cstore.base.BaseSwipeBackActivity;
import cn.api.gjhealth.cstore.utils.DateFormatUtils;
import cn.api.gjhealth.cstore.utils.StringUtil;
import cn.api.gjhealth.cstore.utils.datautils.DateTimeUtil;

public class HttpDetailsActivity extends BaseSwipeBackActivity {
    HttpCacheBean httpCacheBean;
    @Bind(R.id.ll_back)
    LinearLayout llBack;
    @Bind(R.id.index_app_name)
    TextView indexAppName;
    @Bind(R.id.tv_title_right)
    TextView tvTitleRight;
    @Bind(R.id.tv_req_allUrl)
    TextView tvReqAllUrl;
    @Bind(R.id.tv_req_url)
    TextView tvReqUrl;
    @Bind(R.id.tv_req_code)
    TextView tvReqCode;
    @Bind(R.id.tv_req_type)
    TextView tvReqType;
    @Bind(R.id.tv_req_header)
    TextView tvReqHeader;
    @Bind(R.id.tv_req_data)
    TextView tvReqData;
    @Bind(R.id.ll_request)
    LinearLayout llRequest;
    @Bind(R.id.tv_res_allUrl)
    TextView tvResAllUrl;
    @Bind(R.id.tv_res_url)
    TextView tvResUrl;
    @Bind(R.id.tv_res_time)
    TextView tvResTime;
    @Bind(R.id.tv_res_code)
    TextView tvResCode;
    @Bind(R.id.tv_res_type)
    TextView tvResType;
    @Bind(R.id.tv_res_header)
    TextView tvResHeader;
    @Bind(R.id.tv_res_error)
    TextView tvResError;
    @Bind(R.id.tv_res_body)
    TextView tvResBody;
    @Bind(R.id.ll_response)
    LinearLayout llResponse;
    @Bind(R.id.tv_content_type)
    TextView tvContentType;
    @Bind(R.id.tv_req_time)
    TextView tvReqTime;

    @Override
    protected void onInitialization(Bundle bundle) {
        httpCacheBean = (HttpCacheBean) bundle.getSerializable(HttpCacheBean.TAG);
    }

    @Override
    protected void initView(Bundle bundle) {
        indexAppName.setText("请求详情");

    }

    @Override
    protected void initData(Bundle bundle) {
        if (httpCacheBean != null) {
            if (!TextUtils.isEmpty(httpCacheBean.url) && !TextUtils.isEmpty(httpCacheBean.method)) {
                if (httpCacheBean.method.equals("GET")) {
                    try {
                        URL url = new URL(URLDecoder.decode(httpCacheBean.url));
                        httpCacheBean.requestBody = url.getQuery();
                    } catch (MalformedURLException e) {
                    }
                }

            }
            tvReqTime.setText(httpCacheBean.requestTime);
            tvReqAllUrl.setText(TextUtils.isEmpty(httpCacheBean.requestMsg) ? "--" : URLDecoder.decode(httpCacheBean.requestMsg));
            tvReqUrl.setText(TextUtils.isEmpty(httpCacheBean.url) ? "--" : URLDecoder.decode(httpCacheBean.url));
            tvReqCode.setText(TextUtils.isEmpty(httpCacheBean.code + "") ? "--" : httpCacheBean.code + "");
            tvReqHeader.setText(TextUtils.isEmpty(httpCacheBean.reqHeaders) ? "--" : StringUtil.formatJson(httpCacheBean.reqHeaders));
            tvReqData.setText(TextUtils.isEmpty(httpCacheBean.requestBody) ? "--" : StringUtil.formatJson(httpCacheBean.requestBody));
            tvReqType.setText(TextUtils.isEmpty(httpCacheBean.method) ? "--" : httpCacheBean.method);
            tvContentType.setText(TextUtils.isEmpty(httpCacheBean.reqContentType) ? "--" : httpCacheBean.reqContentType);
            tvResAllUrl.setText(TextUtils.isEmpty(httpCacheBean.responseMsg) ? "--" : URLDecoder.decode(httpCacheBean.responseMsg));
            tvResUrl.setText(TextUtils.isEmpty(httpCacheBean.url) ? "--" : URLDecoder.decode(httpCacheBean.url));
            tvResCode.setText(TextUtils.isEmpty(httpCacheBean.code + "") ? "--" : httpCacheBean.code + "");
            tvResHeader.setText(TextUtils.isEmpty(httpCacheBean.resHeaders) ? "--" : StringUtil.formatJson(httpCacheBean.resHeaders));
            tvResBody.setText(TextUtils.isEmpty(httpCacheBean.responseBody) ? "--" : httpCacheBean.responseBody);
            tvResTime.setText(TextUtils.isEmpty(httpCacheBean.time) ? "--" : httpCacheBean.time);
            tvResType.setText(TextUtils.isEmpty(httpCacheBean.resContentType) ? "--" : httpCacheBean.resContentType);
            tvResError.setText(TextUtils.isEmpty(httpCacheBean.error) ? "--" : httpCacheBean.error);
            if (httpCacheBean.code >= 200 && httpCacheBean.code < 300) {
                tvReqCode.setTextColor(Color.parseColor("#00FF00"));
                tvResCode.setTextColor(Color.parseColor("#00FF00"));
            }else if (httpCacheBean.code >= 400) {
                tvReqCode.setTextColor(Color.parseColor("#FF0000"));
                tvResCode.setTextColor(Color.parseColor("#FF0000"));
            } else {
                tvReqCode.setTextColor(Color.parseColor("#0000FF"));
                tvResCode.setTextColor(Color.parseColor("#0000FF"));
            }
        }

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_http_details_layout;
    }

    @OnClick(R.id.ll_back)
    public void onViewClicked() {
        finish();
    }
}
