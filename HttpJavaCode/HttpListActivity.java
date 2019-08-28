package cn.api.gjhealth.cstore.module.demo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.gjhealth.library.adapter.base.BaseQuickAdapter;
import com.gjhealth.library.http.model.HttpCacheBean;
import com.gjhealth.library.utils.ArrayUtils;
import com.gjhealth.library.utils.SharedUtil;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import cn.api.gjhealth.cstore.R;
import cn.api.gjhealth.cstore.app.BaseApp;
import cn.api.gjhealth.cstore.base.BaseSwipeBackActivity;
import cn.api.gjhealth.cstore.http.HttpLogManager;
import cn.api.gjhealth.cstore.module.achievement.weekcalendar.ItemDecoration;
import cn.api.gjhealth.cstore.utils.jsonutils.GsonUtil;
import cn.api.gjhealth.cstore.view.ListEmptyView;
import cn.api.gjhealth.cstore.view.widget.PwdClearEditText;
import cn.api.gjhealth.cstore.view.widget.RecycleViewDivider;

public class HttpListActivity extends BaseSwipeBackActivity {
    @Bind(R.id.index_app_name)
    TextView indexAppName;
    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;
    @Bind(R.id.smart_rl)
    SmartRefreshLayout refreshLayout;
    @Bind(R.id.et_name)
    EditText etName;
    private HttpListAdapter adapter;
    private String mKeyWords = "";
    List<HttpCacheBean> mListBeans;

    @Override
    protected void onInitialization(Bundle bundle) {
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            adapter.setNewData(mListBeans);
            refreshLayout.finishRefresh();
        }
    };

    @Override
    protected void initView(Bundle bundle) {
        indexAppName.setText("Http请求Log日志");
        initList();
        setListDemo();
        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mKeyWords = charSequence.toString().trim();
                setListDemo();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void setListDemo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mListBeans = HttpLogManager.getLogLists(mKeyWords);
                mHandler.sendEmptyMessage(0);
            }
        }).start();
    }


    private void initList() {
        ListEmptyView emptyView = new ListEmptyView(getContext());
        adapter = new HttpListAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(HttpCacheBean.TAG, (HttpCacheBean) adapter.getItem(position));
                gStartActivity(HttpDetailsActivity.class, bundle);
            }
        });
        adapter.setEmptyView(emptyView);
        refreshLayout.setEnableLoadMore(false);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                setListDemo();
            }
        });
    }

    @Override
    protected void initData(Bundle bundle) {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_httplog_layout;
    }


    @OnClick(R.id.img_back)
    public void onViewClicked() {
        finish();
    }
}
