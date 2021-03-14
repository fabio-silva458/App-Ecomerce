package com.example.admin.jprod;


import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HttpConnectionUtil {
    private String errorMsg;
    private String TAG="HttpConnectionUtil";

    public String getOutputFromUrl(String url) {
        StringBuffer output = new StringBuffer("");
        InputStream stream = null;
        try {
            stream = getHttpConnection(url);

            if (stream != null) {
                BufferedReader buffer = new BufferedReader(
                        new InputStreamReader(stream));
                String s = "";
                while ((s = buffer.readLine()) != null)
                    output.append(s);
            }
        } catch (Exception e1) {
            errorMsg = "0@Unable to connect server";
            output.append(errorMsg);
            System.out.println("-ex->" + e1);
        }
        if (stream == null) {
            output.append(errorMsg);
        }
        return output.toString();
    }

    public String getOutputFromUrl_old(String url, Hashtable<String, String> params) {
        StringBuffer output = new StringBuffer("");
        try {
            InputStream stream = getHttpConnection(url, params);
            if (stream != null) {
                BufferedReader buffer = new BufferedReader(
                        new InputStreamReader(stream));
                String s = "";
                while ((s = buffer.readLine()) != null)
                    output.append(s);
            } else {
                output.append(errorMsg);
            }
        } catch (Exception e1) {

            errorMsg = "0@Unable to connect server";
            output.append(errorMsg);
        }
        return output.toString();
    }

    public String getOutputFromUrl(String urlString, Hashtable<String, String> params) {
        StringBuffer output = new StringBuffer("");
        InputStream stream = null;
        URL url = null;
        try {
            urlString = urlString.replaceAll(" ", "%20");
            url = new URL(urlString);
            URLConnection connection = url.openConnection();
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            httpConnection.setRequestMethod("POST");
            httpConnection.setDoOutput(true);
            Set set = params.entrySet();
            Iterator it = set.iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                // System.out.println("->"+entry.getKey().toString().trim()+ ": " + entry.getValue());
                httpConnection.setRequestProperty((String) entry.getKey(), (String) entry.getValue());
            }
            httpConnection.setConnectTimeout(30000);
            httpConnection.connect();
            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                stream = httpConnection.getInputStream();
            } else {
                errorMsg = "0@Unable to connect server";
            }
            if (stream != null) {
                BufferedReader buffer = new BufferedReader(
                        new InputStreamReader(stream));
                String s = "";
                while ((s = buffer.readLine()) != null)
                    output.append(s);
            }
        } catch (Exception e) {
            System.out.println(TAG+" "+e);
            errorMsg = "0@Unable to connect server";
        }
        if (stream == null) {
            output.append(errorMsg);
        }
        return output.toString();
    }

//    public String getOutputFromUrl(String urlString, Map<String, myClassorder> params, String sign_img, String photo_img) {
//        StringBuffer output = new StringBuffer("");
//        InputStream stream = null;
//
//
//        URL url = null;
//        try {
//            urlString= urlString.replaceAll(" ", "%20");
//            url = new URL(urlString);
//            URLConnection connection = url.openConnection();
//            HttpPost httppost = new HttpPost(urlString);
//            HttpURLConnection httpConnection = (HttpURLConnection) connection;
//            httpConnection.setRequestMethod("POST");
//            httpConnection.setDoOutput(true);
//
//            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
//            nameValuePairs.add(new BasicNameValuePair("signimage", sign_img));
//            nameValuePairs.add(new BasicNameValuePair("photoimg", photo_img));
//            for (String key : params.keySet()) {
//                myClassorder productinfo = params.get(key);
//                String productnumber1 = productinfo.getProductno();
//                String productname1 = productinfo.getProductname();
//                String quantity1 = productinfo.getQuantity();
//                String slno1 = productinfo.getSlno();
//                nameValuePairs.add(new BasicNameValuePair("pcode", productnumber1));
//                nameValuePairs.add(new BasicNameValuePair("pname", productname1));
//                nameValuePairs.add(new BasicNameValuePair("qty", quantity1));
//                nameValuePairs.add(new BasicNameValuePair("slno", slno1));
//            }
//            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
//            HttpClient client = new DefaultHttpClient();
//            HttpResponse httpResponse = client.execute(httppost);
//
//            String responseText = EntityUtils.toString(httpResponse.getEntity());
//            Log.v("Responsetext: ", responseText);
//
//            httpConnection.setConnectTimeout(30000);
//            httpConnection.connect();
//            if (responseText != null) {
//                output.append(responseText);
//                Log.v("responseText:", responseText);
//                Log.v("Output:", output.toString());
//            }
//        } catch (Exception e) {
//            errorMsg = "0@Unable to connect server";
//        }
//
//        if (stream == null) {
//            output.append(errorMsg);
//        }
//        return output.toString();
//    }

    public String getOutputFromUrl(String urlString, String strproddesc) {
        StringBuffer output = new StringBuffer("");
        InputStream stream = null;


        URL url = null;
        try {
            urlString = urlString.replaceAll(" ", "%20");
            url = new URL(urlString);
            URLConnection connection = url.openConnection();
            HttpPost httppost = new HttpPost(urlString);
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            httpConnection.setRequestMethod("POST");
            httpConnection.setDoOutput(true);

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("pdesc", strproddesc));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpClient client = new DefaultHttpClient();
            HttpResponse httpResponse = client.execute(httppost);
            String responseText = EntityUtils.toString(httpResponse.getEntity());
            Log.v("Responsetext: ", responseText);

            httpConnection.setConnectTimeout(30000);
            httpConnection.connect();
            if (responseText != null) {
                output.append(responseText);
                Log.v("responseText:", responseText);
                Log.v("Output:", output.toString());
            }
        } catch (Exception e) {
            errorMsg = "0@Unable to connect server";
            System.out.println(errorMsg + "---" + e);
            e.printStackTrace();
        }

        if (stream == null) {
            output.append(errorMsg);
        }
        return output.toString();
    }


    private InputStream getHttpConnection(String urlString, Hashtable<String, String> params)
            throws IOException {
        InputStream stream = null;
        URL url = null;
        try {


            url = new URL(urlString);

            URLConnection connection = url.openConnection();


            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            httpConnection.setReadTimeout(10000 /* milliseconds */);
            httpConnection.setConnectTimeout(15000 /* milliseconds */);
            httpConnection.setRequestMethod("POST");
            httpConnection.setDoInput(true);


            Set set = params.entrySet();
            Iterator it = set.iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                // System.out.println("->"+entry.getKey().toString().trim()+ ": " + entry.getValue());
                httpConnection.setRequestProperty((String) entry.getKey(), (String) entry.getValue());
            }

            httpConnection.connect();

            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                stream = httpConnection.getInputStream();
            } else {
                errorMsg = "0@Unable to connect server";
            }
        } catch (Exception e) {


            errorMsg = "0@Unable to connect server";
        }

        return stream;
    }

    // Makes HttpURLConnection and returns InputStream
    public InputStream getHttpConnection(String urlString) {
        InputStream stream = null;
        try {
            //System.out.println(urlString);

            URL url = new URL(urlString);

            URLConnection connection = url.openConnection();


            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            httpConnection.setReadTimeout(10000 /* milliseconds */);
            httpConnection.setConnectTimeout(15000 /* milliseconds */);
            httpConnection.setRequestMethod("GET");
            httpConnection.setDoInput(true);

            httpConnection.connect();
            //System.out.println("--1->"+httpConnection.getResponseCode());
            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                stream = httpConnection.getInputStream();
            }
        } catch (Exception ex) {
            errorMsg = "0@Unable to connect server";
            System.out.println("-ex->" + ex);
        }
        return stream;
    }
}
