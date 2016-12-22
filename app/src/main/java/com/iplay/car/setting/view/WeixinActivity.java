package com.iplay.car.setting.view;

import android.os.Bundle;
import android.webkit.WebView;

import com.iplay.car.R;
import com.iplay.car.common.base.BaseActivity;

/**
 * Created by Administrator on 2016/12/20.
 */

public class WeixinActivity extends BaseActivity {
    private WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weixin);
        webView = (WebView) findViewById(R.id.webView);
        webView.loadUrl("https://wx.qq.com/");
    }
}
