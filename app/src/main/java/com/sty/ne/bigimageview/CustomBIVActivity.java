package com.sty.ne.bigimageview;

import android.os.Bundle;

import java.io.IOException;
import java.io.InputStream;

import androidx.appcompat.app.AppCompatActivity;

public class CustomBIVActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_biv);

        initView();
    }

    private void initView() {
        BigImageView bigImageView = findViewById(R.id.biv_image);
        InputStream is = null;
        try {
            is = getAssets().open("big.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
        bigImageView.setImage(is);
    }
}
