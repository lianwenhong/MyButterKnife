package com.lianwenhong.mybutterknife;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.lianwenhong.annotation.BindView;
import com.lianwenhong.butter_knife.ButterKnife;

/**
 * 第二个Activity，可以看到注解解析器也会生成一个OtherActivity$$ViewBinder类
 */
public class OtherActivity extends AppCompatActivity {

    @BindView(R.id.id_tv_nokia)
    public TextView tvNokia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }
}
