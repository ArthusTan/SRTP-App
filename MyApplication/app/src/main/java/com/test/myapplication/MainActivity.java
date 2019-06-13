package com.test.myapplication;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.test.myapplication.deeplearning.Comfirm;
import com.test.myapplication.deeplearning.GetInfo;

public class MainActivity extends AppCompatActivity {
    private ImageView imageView;
    private static final int CROP_PHOTO = 2;
    private static final int REQUEST_CODE_PICK_IMAGE = 3;
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 6;
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE2 = 7;
    private File output;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        if (Build.VERSION.SDK_INT >= 23) {
            int checkCallPhonePermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 101);
                return;
            }
        }
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

//        initView();
    }

    private void requestLocation() {
        initView();
    }

    void initView() {
        imageView = (ImageView) findViewById(R.id.image);
        imageView.setOnClickListener(new View.OnClickListener() {

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

                Toast.makeText(MainActivity.this, "Uploading", Toast.LENGTH_LONG).show();

                String tmp_comf = comf.Search();

                Toast.makeText(MainActivity.this, "Comfirm:" + tmp_comf, Toast.LENGTH_LONG).show();

                if (!tmp_comf.equals("非植物")) {
                    GetInfo info = new GetInfo(tmp_comf);
                    String tmp_info = info.Search();
                    Toast.makeText(MainActivity.this, "GetInfo:" + tmp_info, Toast.LENGTH_LONG).show();


                    Intent i = new Intent(MainActivity.this, ViewActivity.class);
                    i.putExtra("data", tmp_info);
                    MainActivity.this.startActivity(i);
                } else {
                    Intent i = new Intent(MainActivity.this, ViewActivity.class);
                    i.putExtra("data", "https://www.baidu.com");
                    startActivity(i);
                }
            }
        });
    }

    public String saveBitmapFile(Bitmap bitmap) {

        File temp = new File("/sdcard/");//要保存文件先创建文件夹
        if (!temp.exists()) {
            temp.mkdir();
        }
        ////重复保存时，覆盖原同名图片
        String name = System.currentTimeMillis() + ".jpg";
        String path = "/sdcard/" + name;
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

    public void takePhone(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_CALL_PHONE2);
        else
            takePhoto();
    }

    /**
     * 拍照
     */
    void takePhoto() {
        /**
         * 最后一个参数是文件夹的名称，可以随便起
         */
        File file = new File(Environment.getExternalStorageDirectory(), "拍照");
        if (!file.exists())
            file.mkdir();
        /**
         * 这里将时间作为不同照片的名称
         */
        output = new File(file, System.currentTimeMillis() + ".jpg");
        /**
         * 如果该文件夹已经存在，则删除它，否则创建一个
         */
        try {
            if (output.exists())
                output.delete();
            output.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        /**
         * 隐式打开拍照的Activity，并且传入CROP_PHOTO常量作为拍照结束后回调的标志
         */
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
//        imageUri = Uri.fromFile(output);
        imageUri = getImageContentUri(output);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT,
                    FileProvider.getUriForFile(MainActivity.this, "com.test.myapplication.fileprovider", output));
        } else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(output));
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, CROP_PHOTO);

//        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//        startActivityForResult(intent, CROP_PHOTO);
    }

    public void showMapInfo(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_CALL_PHONE2);
        else
            showMapInfo();
    }

    void showMapInfo() {
        Intent i = new Intent(MainActivity.this, MapActivity.class);
//        i.putExtra("data",tmp_info);
        startActivity(i);
    }

    /**
     * 从相册选取图片
     */
    public void choosePhone(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_CALL_PHONE2);
        else
            choosePhoto();
    }

    void choosePhoto() {
        /**
         * 打开选择图片的界面
         */
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");//相片类型
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    public void onActivityResult(int req, int res, Intent data) {
        switch (req) {
            /**
             * 拍照的请求标志
             */
            case CROP_PHOTO:
                if (res == RESULT_OK) {
                    try {
                        /**
                         * 该uri就是照片文件夹对应的uri
                         */
                        Bitmap bit = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        imageView.setImageBitmap(bit);
                    } catch (Exception e) {
                        Toast.makeText(this, "程序崩溃", Toast.LENGTH_SHORT).show();
                    }
                } else
                    Log.i("tag", "失败");
                break;
            /**
             * 从相册中选取图片的请求标志
             */
            case REQUEST_CODE_PICK_IMAGE:
                if (res == RESULT_OK) {
                    try {
                        /**
                         * 该uri是上一个Activity返回的
                         */
                        Uri uri = data.getData();
                        Bitmap bit = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                        imageView.setImageBitmap(bit);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d("tag", e.getMessage());
                        Toast.makeText(this, "程序崩溃", Toast.LENGTH_SHORT).show();
                    }
                } else
                    Log.i("liang", "失败");
                break;

            default:
                break;
        }
    }


    /**
     * 转换 content:// uri
     *
     * @param imageFile
     * @return
     */
    public Uri getImageContentUri(File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(MainActivity.this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                } else {
                    Toast.makeText(MainActivity.this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestLocation();
                } else {
                    Toast.makeText(MainActivity.this, "相机权限禁用了。请务必开启相机权", Toast.LENGTH_SHORT).show();
                }
                break;
            case MY_PERMISSIONS_REQUEST_CALL_PHONE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhoto();
                } else {
                    // Permission Denied
                    Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            case MY_PERMISSIONS_REQUEST_CALL_PHONE2:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    choosePhoto();
                } else {
                    // Permission Denied
                    Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }
}