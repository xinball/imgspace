package top.xb.imgspace;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Other_user_Activity extends AppCompatActivity {

    private Button notchange_every;                     //返回

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otheruser);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {       //头部颜色
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }

        notchange_every = (Button) findViewById(R.id.btn_notchangevery1);

        notchange_every.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Other_user_Activity.this.finish();
            }
        });


    }
}