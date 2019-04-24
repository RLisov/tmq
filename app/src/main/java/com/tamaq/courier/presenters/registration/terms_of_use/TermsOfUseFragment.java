package com.tamaq.courier.presenters.registration.terms_of_use;


import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.tamaq.courier.R;
import com.tamaq.courier.di.components.AppComponent;
import com.tamaq.courier.di.components.DaggerCommonComponent;
import com.tamaq.courier.di.modules.CommonModule;
import com.tamaq.courier.presenters.base.BaseFragment;
import com.tamaq.courier.utils.HelperCommon;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;


public class TermsOfUseFragment extends BaseFragment implements TermsOfUseContract.View {

    @Inject
    TermsOfUseContract.Presenter presenter;

    @BindView(R.id.webView)
    WebView webView;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;


    public TermsOfUseFragment() {
        // Required empty public constructor
    }

    public static TermsOfUseFragment newInstance() {
        return new TermsOfUseFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView != null) return rootView;
        rootView = inflater.inflate(R.layout.fragment_terms_of_use, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        presenter.attachPresenter(this);
        setChangeToolbarColor(false);

        initializeNavigationBar();
        getSupportActionBar().setTitle(R.string.terms_of_use_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);

        initWebView();

        return rootView;
    }

    private void initWebView() {
        if (HelperCommon.isNetworkConnected(getContext())) {
            progressBar.setMax(100);
            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    super.onProgressChanged(view, newProgress);

                    ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", newProgress);
                    animation.setDuration(500); // 0.5 second
                    animation.setInterpolator(new DecelerateInterpolator());
                    animation.start();
                }
            });
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    return true;
                }

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    progressBar.setVisibility(View.GONE);
                }
            });
            webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            webView.loadUrl(getString(R.string.user_agreement_url));
        }
    }

    @Override
    public void onInjectDependencies(AppComponent appComponent) {
        super.onInjectDependencies(appComponent);
        DaggerCommonComponent.builder()
                .appComponent(appComponent)
                .commonModule(new CommonModule()).build().inject(this);
    }

    @Override
    public void onDestroy() {
        presenter.detachPresenter();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) getFragmentManager().popBackStackImmediate();
        return super.onOptionsItemSelected(item);
    }

}
