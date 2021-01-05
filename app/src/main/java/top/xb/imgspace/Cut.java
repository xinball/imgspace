package top.xb.imgspace;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

import site.iway.androidhelpers.ImageCropper;
import site.iway.androidhelpers.UnitHelper;

public class Cut extends AppCompatActivity {
    private ImageCropper mImageCropper;
    private String file;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cut);
        //以下代码是github上找的剪切库需要使用的代码，更改setCropRatio()可以改变初始裁剪框的长宽比例，只有参数为0时才可以拖动裁剪框的四角和四边。
        //其他参数（>0)只能改变四边
        Intent intent=getIntent();
        file=intent.getStringExtra("file");
        mImageCropper = (ImageCropper) findViewById(R.id.image_cut);
        mImageCropper.setBitmap(Edit.bitmap);
        mImageCropper.setCropDrawable(new ColorDrawable(0x00000000));
        mImageCropper.setCoverDrawable(new ColorDrawable(0x88000000));
        mImageCropper.setCropRatio(0);
        mImageCropper.setMinCropDrawableRectSideLength(UnitHelper.dipToPx(this, 100));
        mImageCropper.setCropBorderWidth(UnitHelper.dipToPx(this, 10));
        mImageCropper.setMultiSamplingEnabled(true);
    }

    public void back(View view) {
        finish();
    }


    public void Yes(View view) {
        //点击yes时代表确定裁剪，所有直接改变Edit.bitmap的值
        Edit.bitmap=mImageCropper.getCroppedBitmap();
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }
}