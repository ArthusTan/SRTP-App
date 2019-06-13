package com.test.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.test.myapplication.deeplearning.Comfirm;
import com.test.myapplication.deeplearning.GetInfo;
import com.test.myapplication.objects.Position;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ChoosePhoto extends AppCompatActivity {
    public LocationClient mLocationClient;

    private ImageView imageView;
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_photo);

        SDKInitializer.initialize(ChoosePhoto.this);
        SDKInitializer.setCoordType(CoordType.BD09LL);

        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyMyLocationListener());

        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setScanSpan(1000);
        option.setOpenGps(true);
        option.setLocationNotify(true);
        option.setIgnoreKillProcess(false);
        mLocationClient.setLocOption(option);

        mLocationClient.start();


        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        initView();

        Uri uri = getIntent().getData();
        Bitmap bit = null;
        try {
            bit = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d("tag", e.getMessage());
            Toast.makeText(this, "程序崩溃", Toast.LENGTH_SHORT).show();
        }
        imageView.setImageBitmap(bit);
    }

    void initView() {
        imageView = (ImageView) findViewById(R.id.image);
        mButton = (Button) findViewById(R.id.button_discern);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO 自动生成的方法存根
                imageView.buildDrawingCache(true);
                imageView.buildDrawingCache();
                Bitmap bitmap = imageView.getDrawingCache();
                String tmp_path = saveBitmapFile(bitmap);

                imageView.setDrawingCacheEnabled(false);
                imageView.setDrawingCacheEnabled(false);

                Comfirm comf = new Comfirm(tmp_path);

                Toast.makeText(ChoosePhoto.this, "Uploading...", Toast.LENGTH_LONG).show();

                String tmp_comf = comf.Search();

                Toast.makeText(ChoosePhoto.this, tmp_comf, Toast.LENGTH_LONG).show();

                if (!tmp_comf.equals("非植物")) {
                    GetInfo info = new GetInfo(tmp_comf);
                    String tmp_info = info.Search();

                    Position.strURL = tmp_info;

                    Toast.makeText(ChoosePhoto.this, Position.strLAT + " , " + Position.strLON, Toast.LENGTH_LONG).show();

                    Position.write();

//                    Toast.makeText(ChoosePhoto.this,"GetInfo:"+tmp_info,Toast.LENGTH_LONG).show();
                    Intent i = new Intent(ChoosePhoto.this, ViewActivity.class);
                    i.putExtra("data", tmp_info);
                    startActivity(i);
                } else {
//                    Intent i = new Intent(ChoosePhoto.this, ViewActivity.class);
//                    i.putExtra("data","https://www.baidu.com");

                    Intent i = new Intent(ChoosePhoto.this, Main2Activity.class);
                    Toast.makeText(ChoosePhoto.this, "这个不是植物", Toast.LENGTH_LONG).show();
                    startActivity(i);
                }
            }

        });
    }

    public String saveBitmapFile(Bitmap bitmap) {

        File temp = new File("/sdcard/flower-comf/");//要保存文件先创建文件夹
        if (!temp.exists()) {
            temp.mkdir();
        }
        ////重复保存时，覆盖原同名图片
        String name = System.currentTimeMillis() + ".jpg";
        String path = "/sdcard/flower-comf/" + name;

        Position.strIMG = path;

        File file = new File(path);//将要保存图片的路径和图片名称
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return path;
    }


    public class MyMyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(final BDLocation location) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    StringBuilder currentPosition = new StringBuilder();
//                    currentPosition.append("纬度").append(location.getLatitude()).append(",经线").append(location.getLongitude());
//                    Toast.makeText(ChoosePhoto.this, currentPosition, Toast.LENGTH_LONG).show();

                    //5#804 39.993948,116.367319
                    //ERROR 39.988437,116.360717
                    //DELTA 00.005511,000.006602

//                    Position.LATITUDE = location.getLatitude() + 00.005511;
//                    Position.LONGITUDE = location.getLongitude() + 000.006602;
                    Position.LATITUDE = location.getLatitude();
                    Position.LONGITUDE = location.getLongitude();

//                    double y = location.getLatitude();
//                    double x = location.getLongitude();
//                    double z = Math.sqrt(x*x+y*y) + 0.00002 *Math.sin(y*Math.PI);
//                    double temp =Math.atan2(y, x)  + 0.000003 * Math.cos(x*Math.PI);
//
//                    Position.LATITUDE = z * Math.sin(temp) + 0.006;
//                    Position.LONGITUDE = z * Math.cos(temp) + 0.0065;
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
    }

}
