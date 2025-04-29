package net.hearnsoft.tcm.utils;

import android.content.Context;

import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.datasource.HttpDataSource;
import androidx.media3.datasource.okhttp.OkHttpDataSource;

import okhttp3.OkHttpClient;

@UnstableApi
public class AlistDataSourceFactory implements DataSource.Factory {
    private Context context;
    private DefaultDataSource.Factory defaultFactory;
    private HttpDataSource.Factory httpFactory;

    public AlistDataSourceFactory(Context context) {
        this.context = context;

        OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .build();

        // 配置HTTP数据源工厂
        httpFactory = new OkHttpDataSource.Factory(client);

        // 使用默认数据源工厂，它能够处理多种URI方案
        defaultFactory = new DefaultDataSource.Factory(context, httpFactory);
    }

    @Override
    public DataSource createDataSource() {
        // 返回可同时处理本地文件和HTTP资源的数据源
        return defaultFactory.createDataSource();
    }
}