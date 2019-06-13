package com.test.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.test.myapplication.objects.MarkerPoint;
import com.test.myapplication.tools.AdjustBitmap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity {

    public LocationClient mLocationClient;
    private TextView positionText;
    private BaiduMap mBaiduMap;
    private MapView mMapView;
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(this);
        SDKInitializer.setCoordType(CoordType.BD09LL);
        //自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
        //包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
        setContentView(R.layout.activity_map);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        positionText = (TextView) findViewById(R.id.position_text_view);
        mMapView = (MapView) findViewById(R.id.bmapView);
        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.setBackgroundResource(R.drawable.wood);
        mWebView.setBackgroundColor(Color.argb(0, 0, 0, 0));

        mBaiduMap = mMapView.getMap();

        //Permission
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(this, permissions, 1);
        } else requestLocation();

    }

    private void requestLocation() {
        initLocation();

        ArrayList<String> arrayList = new ArrayList<>();
//        ArrayList<MarkerPoint> markerPoints = new ArrayList<>();

        try {
            FileReader fileReader = new FileReader("/sdcard/flower-comf/mapInfo.txt");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                arrayList.add(str);
            }
            bufferedReader.close();
            fileReader.close();
            for (String info : arrayList) {
                MarkerPoint markerPoint = new MarkerPoint();
                String[] tmp = info.split(",");
                markerPoint.lat = Double.valueOf(tmp[0]);
                markerPoint.lon = Double.valueOf(tmp[1]);
                markerPoint.imgeURL = tmp[2];
                markerPoint.infoURL = tmp[3];
                drawMarker(markerPoint);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

//        LatLng point = new LatLng(39.996165, 116.365942);
//        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_marka);
//        drawMarker(point, bitmap, "lily-white");
    }

    private void drawMarker(final MarkerPoint markerPoint) {


        LatLng point = new LatLng(markerPoint.lat, markerPoint.lon);

        Bitmap bm = BitmapFactory.decodeFile(markerPoint.imgeURL);

        Bitmap target = AdjustBitmap.getBitmap(bm,120,160);

        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromBitmap(target);

        Bundle mBundle = new Bundle();

        mBundle.putString("img", markerPoint.imgeURL);
        mBundle.putString("url", markerPoint.infoURL);

        MarkerOptions markerTest = new MarkerOptions()
                .position(point)
                .icon(bitmap)
                .draggable(true)
                .extraInfo(mBundle);

        markerTest.animateType(MarkerOptions.MarkerAnimateType.grow);

        mBaiduMap.addOverlay(markerTest);

        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            //marker被点击时回调的方法
            //若响应点击事件，返回true，否则返回false
            //默认返回false
            @Override
            public boolean onMarkerClick(Marker marker) {
                Intent intent = new Intent();
                String url = marker.getExtraInfo().getString("url");
                String img = marker.getExtraInfo().getString("img");

                intent.putExtra("url", url);

                Toast.makeText(MapActivity.this, img, Toast.LENGTH_SHORT).show();

                mWebView.loadUrl(url);

                mWebView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        view.loadUrl(url);
                        return true;
                    }
                });

                return false;
            }
        });
    }

    private void initLocation() {
        mBaiduMap.setMyLocationEnabled(true);

        LatLng cenpt = new LatLng(39.996165, 116.365942);

        //定义地图状态
        MapStatus mMapStatus = new MapStatus.Builder().target(cenpt).zoom(18).build();

        //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);        //改变地图状态

        mBaiduMap.setMapStatus(mMapStatusUpdate);

        //创建一个LocationClient的实例,接受的context通过getApplicationContext()方法获取。
        //调用LocationClient的registerLocationListener()方法来注册一个监听器 当获取到位置信息的时候，就会回调这个定位监听器
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());

        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);

        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);

        //开启地图定位图层
        mLocationClient.start();

    }

    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(final BDLocation location) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    StringBuilder currentPosition = new StringBuilder();

                    currentPosition.append("纬:").append(location.getLatitude()).append(" ");
                    currentPosition.append("经:").append(location.getLongitude()).append(" : ");

//                    Position.latitude = location.getLatitude();
//                    Position.longitude = location.getLongitude();

                    if (location.getLocType() == BDLocation.TypeGpsLocation) {
                        currentPosition.append("GPS");
                    } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
                        currentPosition.append("网络");
                    }
                    currentPosition.append("\n");

                    currentPosition.append(location.getCountry()).append(" : ");
                    currentPosition.append(location.getProvince()).append(" : ");
                    currentPosition.append(location.getCity()).append(" : ");
                    currentPosition.append(location.getDistrict()).append(" : ");
                    currentPosition.append(location.getStreet()).append("\n");

                    positionText.setText(currentPosition);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        mLocationClient.stop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(MapActivity.this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                } else {
                    Toast.makeText(MapActivity.this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                break;
        }
    }
}