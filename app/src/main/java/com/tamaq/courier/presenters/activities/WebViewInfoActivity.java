package com.tamaq.courier.presenters.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.tamaq.courier.R;
import com.tamaq.courier.dao.UserDAO;
import com.tamaq.courier.presenters.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class WebViewInfoActivity extends BaseActivity {

    public static final String ARG_HTML_CONTENT = "arg_html_content";
    public static final String ARG_TYPE = "arg_type";

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.webView)
    WebView webView;
    @BindView(R.id.loader)
    View loaderView;
    private Unbinder unbinder;
    private Type type;

    public static Intent newInstance(Context context, String htmlContent, Type type) {
        Intent intent = new Intent(context, WebViewInfoActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(ARG_HTML_CONTENT, htmlContent);
        bundle.putSerializable(ARG_TYPE, type);
        intent.putExtras(bundle);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        unbinder = ButterKnife.bind(this);
        ButterKnife.bind(this);
        parseArguments();
        setupNavigationBar();
        setUpViews();
    }

    private void parseArguments() {
        type = (Type) getIntent().getSerializableExtra(ARG_TYPE);
    }

    protected void setupNavigationBar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        switch (type) {
            case INFO_ABOUT_ORDER:
                getSupportActionBar().setTitle(R.string.info_about_order);
                break;
            case INFO:
                getSupportActionBar().setTitle(R.string.info);
                break;
            default:
                getSupportActionBar().setTitle(R.string.info);
        }

        boolean isActiveStatus = UserDAO.getInstance().isActiveStatus();
        getSupportActionBar().setHomeAsUpIndicator(isActiveStatus
                ? R.drawable.ic_close_white : R.drawable.ic_close_dark);
        toolbar.setBackgroundColor(ContextCompat.getColor(this,
                isActiveStatus ? R.color.dark_sky_blue : R.color.white));
        toolbar.setTitleTextColor(ContextCompat.getColor(this,
                isActiveStatus ? R.color.white : R.color.black_87));
    }

    private void setUpViews() {
        webView.setWebViewClient(new MyWebViewClient());

        String url;
        switch (type) {
            case INFO_ABOUT_ORDER:
                url = "https://tamaq.kz/docs/order_executor.html";
                break;

            case INFO:
            default:
                url = "https://tamaq.kz/docs/executor_profile.html";
        }

        webView.loadUrl(url);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        unbinder.unbind();
        super.onDestroy();
    }

    public enum Type {INFO, INFO_ABOUT_ORDER}

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            loaderView.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (loaderView != null) loaderView.setVisibility(View.GONE);
            });
        }
    }
}
