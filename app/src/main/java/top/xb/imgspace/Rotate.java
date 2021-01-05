package top.xb.imgspace;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.zjun.widget.RuleView;

import java.io.ByteArrayOutputStream;

public class Rotate extends AppCompatActivity {
    private RuleView ruleView;
    private String file;
    private ImageView imageView;
    private float lastValue=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rotate);

        Intent intent=getIntent();
        file=intent.getStringExtra("Rotate");
        imageView=(ImageView)findViewById(R.id.image_rotate);
        ruleView = (RuleView) findViewById(R.id.ruler);
        imageView.setImageBitmap(Edit.bitmap);
        //监听滑动尺所在位置，并时时改变图片，这里用的是ImageView自带的旋转函数
        ruleView.setOnValueChangedListener(new RuleView.OnValueChangedListener() {
            @Override
            public void onValueChanged(float value) {
                imageView.setPivotX(imageView.getWidth()>>1);
                imageView.setPivotY(imageView.getHeight() >> 1);//支点在图片中心
                float degree=imageView.getRotation();
                imageView.setRotation(degree+value-lastValue);
                lastValue=value;

            }
        });
    }

    public void back(View view) {
        finish();
    }


    public void Yes(View view) {
        //获得图片旋转的度数，并通过矩阵方法获得旋转后的图片。如果在滑动尺监听方法那里使用矩阵法，将会十分卡。
        Bitmap bitmap = Edit.bitmap;
        Matrix matrix  = new Matrix();
        matrix.setRotate(imageView.getRotation());
        Edit.bitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    public void rotate90(View view) {
        imageView.setPivotX(imageView.getWidth()>>1);
        imageView.setPivotY(imageView.getHeight()>>1);//支点在图片中心
        float value=imageView.getRotation();
        imageView.setRotation(90+value);
    }
}