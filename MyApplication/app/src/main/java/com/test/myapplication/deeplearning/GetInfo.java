package com.test.myapplication.deeplearning;


public class GetInfo {
    public String name = null;

    public GetInfo(String name) {
//        Position.strNAM = name;
        this.name = name;
    }

    public String Search() {
        String urlString = "";
        String current;
        int head, tail;
        try {
            urlString = "https://wapbaike.baidu.com/item/" + name;    //百度百科
//            urlString = "https://zh.wikipedia.org/wiki/" + name;  //维基百科，中文网
//            urlString = "http://www.baike.com/gwiki/"+name;     //互动百科
//            urlString = "http://www.baidu.com/s?wd=" + name;

//            URL url = new URL("http://www.baidu.com/s?&wd="+name);
//            URLConnection ucn = url.openConnection();
//            HttpURLConnection con = (HttpURLConnection)ucn;
//            BufferedReader in = new BufferedReader(
//                    new InputStreamReader(con.getInputStream()));
//            while((current = in.readLine()) != null){
//                System.out.println(current);
//                if((head = current.indexOf("url:'http://baike.baidu.com"))!=-1){
//                    head+=5;
//                    tail = current.indexOf("'", head);
//                    urlString = current.substring(head,tail);
//                    break;
//                }
//            }
        } catch (Exception e) {
            e.printStackTrace();
            urlString = "ERROR";
        }
        return urlString;
    }
}
