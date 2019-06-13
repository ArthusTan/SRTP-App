package com.test.myapplication.deeplearning;

import com.baidu.aip.imageclassify.AipImageClassify;

import org.json.JSONObject;

import java.util.HashMap;

public class Comfirm {
    public String imagePath = null;

    public static final String APP_ID = "10332757";
    public static final String API_KEY = "1lVaIZXQwnhXDKfY6TsgIhaQ";
    public static final String SECRET_KEY = "tgcCLQtfoDQ3LvXZCDBD1LU2rCwZkcrK";

    public Comfirm(String path) {
        this.imagePath = path;
    }

    public String Search() {
        AipImageClassify client = new AipImageClassify(APP_ID,
                API_KEY,
                SECRET_KEY);
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);
        HashMap<String, String> options = new HashMap<String, String>();
//        System.out.println(new File(imagePath).exists());
        JSONObject res = client.plantDetect(imagePath, options);
        String result = res.toString();

//        System.out.println(result);

        int head = result.indexOf("name") + 7;
        int tail = result.indexOf("}", head) - 1;
        return result.substring(head, tail);
    }
}