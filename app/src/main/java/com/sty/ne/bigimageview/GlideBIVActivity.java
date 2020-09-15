package com.sty.ne.bigimageview;

import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import androidx.appcompat.app.AppCompatActivity;

public class GlideBIVActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glide_biv);

        initView();
    }

    private void initView() {
        ImageView imageView = findViewById(R.id.biv_image);
        Glide.with(this)
                .load(R.drawable.big)
                .centerCrop()
                .into(imageView);
    }
}
