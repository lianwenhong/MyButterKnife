package com.lianwenhong.mybutterknife;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.lianwenhong.annotation.BindView;
import com.lianwenhong.annotation.OnClick;
import com.lianwenhong.butter_knife.ButterKnife;

/**
 * 第一个Activity，可以看到在ButterKnife注解解析器工作之后会生成一个MainActivity$$ViewBinder类
 */
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.id_tv_hello)
    public TextView tvHello;
    @BindView(R.id.id_tv_moto)
    public TextView tvMoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Log.e("lianwenhong", " >>> tvHello's id str:" + tvHello.getContext().getResources().getResourceEntryName(tvHello.getId()));
    }

    @OnClick({R.id.id_tv_hello, R.id.id_tv_moto})
    public void click(View v) {
        switch (v.getId()) {
            case R.id.id_tv_hello:
                tvHello.setText("我被击中了!!!");
                break;
            case R.id.id_tv_moto:
                tvMoto.setText("我被击中了!!!");
                break;
        }
    }
}