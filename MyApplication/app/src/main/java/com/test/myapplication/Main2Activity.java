package com.test.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class Main2Activity extends Activity {
    private ImageView imageView;
    private static final int CROP_PHOTO = 2;
    private static final int REQUEST_CODE_PICK_IMAGE = 3;
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 6;
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE2 = 7;
    private File output;
    private Uri imageUri;

    private ImageButton iB_takephoto;
    private ImageButton iB_choose;
    private ImageButton iB_history;

//    private static final int CROP_PHOTO = 2;
//    private static final int REQUEST_CODE_PICK_IMAGE=3;
//    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 6;
//    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE2 = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //这句在setContentView之前使用也可以隐藏标题栏
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main2);
        /*
        这一段可以把原生的状态栏去掉
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
         */
        File temp = new File("/sdcard/flower-comf/");//要保存文件先创建文件夹
        if (!temp.exists()) {
            temp.mkdir();
        }
        File file = new File("/sdcard/flower-comf/mapInfo.txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        if (Build.VERSION.SDK_INT >= 23) {
            int checkCallPhonePermission = ContextCompat.checkSelfPermission(Main2Activity.this, Manifest.permission.CAMERA);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(Main2Activity.this, new String[]{Manifest.permission.CAMERA}, 101);
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

    }

    private void requestLocation() {

        initView();
        setViewListener();
    }

    private void setViewListener() {
        iB_takephoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });
        iB_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePhone(v);
            }
        });
        iB_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main2Activity.this, MapActivity.class);
                startActivity(intent);
            }
        });
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
                    FileProvider.getUriForFile(Main2Activity.this, "com.test.myapplication.fileprovider", output));
        } else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(output));
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, CROP_PHOTO);

//        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//        startActivityForResult(intent, CROP_PHOTO);
    }

    protected void initView() {
        iB_takephoto = findViewById(R.id.iB_takephoto);
        iB_choose = findViewById(R.id.iB_choose);
        iB_history = findViewById(R.id.iB_history);
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
//        Toast.makeText(Main2Activity.this, "Here", Toast.LENGTH_LONG).show();
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
                        //Bitmap bit = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        //imageView.setImageBitmap(bit);

                        //             Uri uri = data.getData();
                        Intent intent = new Intent(Main2Activity.this, ChoosePhoto.class);
                        intent.setData(imageUri);
                        startActivity(intent);


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
                        Intent intent = new Intent(Main2Activity.this, ChoosePhoto.class);
                        intent.setData(uri);
                        startActivity(intent);
                        // Bitmap bit = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                        //imageView.setImageBitmap(bit);
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
                            Toast.makeText(Main2Activity.this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                } else {
                    Toast.makeText(Main2Activity.this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestLocation();
                } else {
                    Toast.makeText(Main2Activity.this, "相机权限禁用了。请务必开启相机权", Toast.LENGTH_SHORT).show();
                }
                break;
            case MY_PERMISSIONS_REQUEST_CALL_PHONE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhoto();
                } else {
                    // Permission Denied
                    Toast.makeText(Main2Activity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            case MY_PERMISSIONS_REQUEST_CALL_PHONE2:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    choosePhoto();
                } else {
                    // Permission Denied
                    Toast.makeText(Main2Activity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }
}
