package com.zen.android.analysis.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends BaseActivity {

    TextView mTextMain;
    Button   mBtnGo;

    @Override
    protected void afterCreate(@Nullable Bundle state) {
        Log.d("MAIN", "set fail!");
        mTextMain.setText(InitManager.getContext().getString(R.string.fail));
    }

    @Override
    protected void onBaseCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        mTextMain = (TextView) findViewById(R.id.tv_main);
        mBtnGo = (Button) findViewById(R.id.btn_go);
    }

    @Override
    protected void onObsResume() {
        super.onObsResume();
        mTextMain.setText(InitManager.getContext().getString(R.string.success));
        mBtnGo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View listener) {
                InitManager.clear();
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });
    }
}
