package top.xb.imgspace;
import top.xb.imgspace.utils.*;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.service.autofill.TextValueSanitizer;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView intv=findViewById(R.id.textViewin);
        TextView outtv=findViewById(R.id.textViewout);
        Button getbtn=findViewById(R.id.main_get_btn);
        intv.setText("{\n" +
                "\t\"action\":\"send\",\n" +
                "\t\"method\":\"altermsg\",\n" +
                "\t\"uid\":\"c10fmc\",\n" +
                "\t\"pwd\":\"123456\",\n" +
                "\t\"mid\":\"201229032605i\",\n" +
                "\t\"message\":\"哈哈哈嘿嘿嘿\"\n" +
                "}");
        JSONObject jsonObject=HttpUtil.postRequest()
    }
}