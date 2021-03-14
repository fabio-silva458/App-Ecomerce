package com.example.admin.jprod;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Imageupload extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    Button btnPickImg, btnupload;
    TableLayout product_table;
    TableRow row;
    String TAG = "Imageupload";
    private View mLayout;
    String mediaPath;
    ImageView imgView;
    ProgressDialog progressDialog;
    TextView str1;
    String appurl = "";
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    AutoCompleteTextView actproduct;
    List<String> listpimgs = new ArrayList<>();
    List<String> prodsList = new ArrayList<String>();
    String[] arrproductslist;
    ArrayAdapter<String> adapter;
    String actprodno;
    ArrayList<ProductList> productLists = new ArrayList<ProductList>();
    ArrayList<String> productImages = new ArrayList<>();
    Map<String, ProductList> hmap = new HashMap<String, ProductList>();
    HashMap<String, List<String>> hmaplist = new HashMap<>();
    Hashtable<String, String> params = new Hashtable<String, String>();
    private static final int DIALOG_ALERT = 10;
    private static final int DIALOG_ERROR = 11;
    private static final int DIALOG_CONFIRM = 12;
    private String errorMsg;
    private static final int REQUEST_READ_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageupload);
        Log.v(TAG, "onCreate");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.imgmaster);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mLayout = findViewById(R.id.imglyt);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPref.edit();
        appurl = sharedPref.getString("urllink", "Not Available");

        product_table = (TableLayout) findViewById(R.id.product_table);
        btnPickImg = (Button) findViewById(R.id.pickimg);
        btnupload = (Button) findViewById(R.id.btnupload);
        str1 = (TextView) findViewById(R.id.txtpath);
        imgView = (ImageView) findViewById(R.id.image_view);
        actproduct = (AutoCompleteTextView) findViewById(R.id.autocompproduct);

        btnPickImg.setVisibility(View.GONE);
        imgView.setVisibility(View.GONE);
        btnupload.setVisibility(View.GONE);
        str1.setVisibility(View.GONE);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");

        fetchproducts();

        btnPickImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                boolean result = Utility.checkPermission(Imageupload.this);
//                if (result) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(galleryIntent, 0);
                } else {
                    // Permission is missing and must be requested.
                    requestWritePermission();
                }
            }
        });

        btnupload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFile();
            }
        });

        actproduct.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.v(TAG, "AutoComp-item clicked");

                for (Map.Entry m : hmaplist.entrySet()) {
                    System.out.println("AC hmaplist=" + m.getKey() + " " + m.getValue());
                }
                String actproductname = adapter.getItem(position).toString();
                Log.v(TAG, "actproductname=" + actproductname);
                String[] splitprodno = actproductname.split("-");
                actprodno = splitprodno[0];
                btnPickImg.setVisibility(View.VISIBLE);
                imgView.setVisibility(View.VISIBLE);
                product_table.removeAllViewsInLayout();
                //listpimgs.clear();
                //productImages.clear();

                if (hmaplist.containsKey(actprodno)) {
                    Log.v(TAG, ", pno=" + actprodno);

                    listpimgs = hmaplist.get(actprodno);

                    int i = 0;
                    for (String imgname : listpimgs) {
                        Log.v(TAG, ",i=" + i + " imgname=" + imgname);
                        fillImageTable(i, imgname);
                        i = i + 1;
                    }

                } else {
                    Log.v(TAG, "No Key");
                }
            }
        });

    }

    private void requestWritePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            Log.v(TAG, "Access is required to import images");
            Snackbar.make(mLayout, "Storage access is required to import images.",
                    Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(Imageupload.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_READ_STORAGE);
                }
            }).show();

        } else {
            Log.v(TAG, "Permission is not available. Requesting Storage permission");
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_READ_STORAGE);
        }
    }

    private void fetchproducts() {
        String URLorderfetch = appurl + "/ProductServlet?isFrom=Android&method=JPS";
        GetProductsTask productsTask = new GetProductsTask();
        Log.v(TAG, "URLorderfetch=" + URLorderfetch);
        productsTask.execute(URLorderfetch);
        Log.v(TAG, "GetProductsTask entered");
    }

    private void uploadFile() {
        progressDialog.show();
        Log.v(TAG, "uploadFile");
        // Map is used to multipart the file using okhttp3.RequestBody
        File file = new File(mediaPath);
        String pno = actprodno;
        // Parsing any Media type file
        RequestBody requestBody = RequestBody.create(MediaType.parse("*/*"), file);
        MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
        RequestBody filename = RequestBody.create(MediaType.parse("text/plain"), file.getName());
        Log.v(TAG, "filename=" + file.getName());
        String ext = getFileExtension(file);
        Log.v(TAG, "RequestBodyfilename=" + filename);
        ApiConfig getResponse = AppConfig.getRetrofit(appurl).create(ApiConfig.class);
        Call<ServerResponse> call = getResponse.uploadFile(fileToUpload, filename, pno, ext);
        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                ServerResponse serverResponse = response.body();
                if (serverResponse != null) {
                    if (serverResponse.getSuccess()) {
                        Toast.makeText(getApplicationContext(), serverResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        String pimgs = serverResponse.getPimgs();
                        product_table.removeAllViewsInLayout();
                        listpimgs.clear();
                        if (pimgs != null) {

                            String res[] = pimgs.split("@");
                            for (int i = 0; i < res.length; i++) {
                                //System.out.println("-ert->"+res[i]);
                                listpimgs.add(res[i]);
                            }
//                            for (int i = 0; i < listpimgs.size(); i++) {
//
//                                fillImageTable(i);
//                            }
                            int i = 0;
                            for (String imgname : listpimgs) {
                                fillImageTable(i, imgname);
                                i = i + 1;
                            }

                        }
                    } else {
                        Toast.makeText(getApplicationContext(), serverResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    assert serverResponse != null;
                    Log.v("Response", serverResponse.toString());
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {

            }
        });
    }

    private static String getFileExtension(File file) {
        String fileName = file.getName();
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        else return "";
    }

    private class GetProductsTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            Log.v(TAG, "GetProductsTask DoInBackground");
            String output = null;
            for (String url : urls) {
                output = new HttpConnectionUtil().getOutputFromUrl(url);
            }
            return output;
        }

        @Override
        protected void onPostExecute(String output) {
            System.out.println("--ert->" + output);
            Log.v(TAG, output);
            prodsList.clear();
            hmap.clear();
            prodsList.add("--Select--");
            if (output != null && output.length() > 0) {
                hmaplist.clear();
                productLists.clear();
                productImages.clear();
                String res[] = output.split("@@");
                Log.v(TAG, "res=" + Arrays.toString(res));
                for (int i = 0; i < res.length; i++) {

                    productImages = new ArrayList<>();

                    String ressplit[] = res[i].split("@");
                    Log.v(TAG, "ressplit=" + Arrays.toString(ressplit));
                    String res1[] = ressplit[0].split(">");
                    Log.v(TAG, "res1=" + Arrays.toString(res1));
                    String pno = "";
                    ProductList productList = new ProductList();
                    if (res1[0] != null && res1[0].equalsIgnoreCase("NA")) {
                        productList.setProdno("");
                        pno = "";
                    } else {
                        productList.setProdno(res1[0]);
                        pno = res1[0];
                    }
                    if (res1[1] != null && res1[1].equalsIgnoreCase("NA")) {
                        productList.setProdname("");
                    } else {
                        productList.setProdname(res1[1]);
                    }
                    if (res1[2] != null && res1[2].equalsIgnoreCase("NA")) {
                        productList.setProdtypes("");
                    } else {
                        productList.setProdtypes(res1[2]);
                    }
                    if (res1[3] != null && res1[3].equalsIgnoreCase("NA")) {
                        productList.setProddesc("");
                    } else {
                        productList.setProddesc(res1[3]);
                    }
                    if (res1[4] != null && res1[4].equalsIgnoreCase("NA")) {
                        productList.setProdcost("");
                    } else {
                        productList.setProdcost(res1[4]);
                    }
                    productLists.add(productList);
                    prodsList.add(productList.getProdno() + "-" + productList.getProdname());
                    String res2[] = ressplit[1].split(">");
                    Log.v(TAG, "res2=" + Arrays.toString(res2));
                    if (res2 != null && res2.length > 0) {
                        for (int j = 0; j < res2.length; j++) {
                            if (!res2[j].equalsIgnoreCase("NA")) {
                                productImages.add(res2[j]);
                            }
                        }
                    }
                    hmaplist.put(pno, productImages);
                }

                for (Map.Entry m : hmaplist.entrySet()) {
                    System.out.println("hmaplist=" + m.getKey() + " " + m.getValue());
                }

                arrproductslist = prodsList.toArray(new String[]{});
                adapter = new ArrayAdapter<String>(Imageupload.this, android.R.layout.select_dialog_item, arrproductslist);
                actproduct.setThreshold(1);
                actproduct.setAdapter(adapter);

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "onActivityResult");
        try {
            // When an Image is picked
            if (requestCode == 0 && resultCode == RESULT_OK && null != data) {

                // Get the Image from data
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                assert cursor != null;
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                mediaPath = cursor.getString(columnIndex);
                str1.setText(mediaPath);
                Log.v(TAG, "path=" + mediaPath);

//                File image = new File(mediaPath);
//                Bitmap mBitmapInsurance =Bitmap.createScaledBitmap(BitmapFactory.decodeFile(image.getAbsolutePath()),400,400,false);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;

                Bitmap mBitmapInsurance = BitmapFactory.decodeFile(mediaPath, options);
                // Set the Image in ImageView for Previewing the Media
                //imgView.setImageBitmap(BitmapFactory.decodeFile(mediaPath));
                imgView.setImageBitmap(mBitmapInsurance);
                btnupload.setVisibility(View.VISIBLE);
                str1.setVisibility(View.VISIBLE);
                cursor.close();

            } else {
                Toast.makeText(this, "You haven't picked Image/Video", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
        }

    }

    private void fillImageTable(int i, String imgname) {

        ImageView b1, bdel;
        TextView tslno, t1, tgap, tgap1;

        row = new TableRow(this);
        tslno = new TextView(this);
        tgap = new TextView(this);
        tgap1 = new TextView(this);
        tslno.setTextColor(getResources().getColor(R.color.black));
        tslno.setPadding(2, 5, 0, 5);
        tslno.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
        tslno.setText(String.valueOf(i + 1) + ".");
        tslno.setTypeface(null, 1);

        t1 = new TextView(this);
        t1.setTextColor(getResources().getColor(R.color.black));
        t1.setPadding(5, 5, 0, 5);
        t1.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
        t1.setText(imgname);

        b1 = new ImageView(this);
        bdel = new ImageView(this);
        //b1.setImageResource(R.drawable.bgsplash);

        Glide.with(getApplicationContext()).load(appurl+ "/DisplayImage?type=prod&picname=" + imgname)
                .placeholder(R.drawable.image)
                .error(R.drawable.image)
                .into(b1);

        b1.setPadding(15, 15, 10, 10);
        b1.setScaleType(ImageView.ScaleType.FIT_XY);
        bdel.setPadding(15, 15, 10, 10);
        bdel.setImageResource(R.drawable.delete);

        TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 0.3f);
        TableRow.LayoutParams params1 = new TableRow.LayoutParams(0, 250, 1.8f);
        TableRow.LayoutParams params2 = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 1.4f);
        TableRow.LayoutParams paramsgap = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 0.1f);
        TableRow.LayoutParams paramsdel = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 0.3f);
        //0.1 added in row.addview tgap


        tslno.setLayoutParams(params);
        b1.setLayoutParams(params1);
        t1.setLayoutParams(params2);
        tgap.setLayoutParams(paramsgap);
        bdel.setLayoutParams(paramsdel);
        tgap1.setLayoutParams(paramsgap);

        row.setBackgroundResource(R.drawable.tablebg);
        row.setPadding(5, 5, 5, 5);
        row.addView(tslno);
        row.addView(b1);
        row.addView(t1);
        row.addView(tgap);
        row.addView(bdel);
        row.addView(tgap1);

        product_table.addView(row, new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

        bdel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                AlertDialog.Builder alertdelete = new AlertDialog.Builder(
                        Imageupload.this);

                alertdelete.setTitle("Confirm Delete...");
                alertdelete.setMessage("Do you want to Delete the Image?");
                alertdelete.setIcon(R.drawable.delete);

                alertdelete.setPositiveButton("YES",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                TableRow t = (TableRow) v.getParent();
                                TextView txtprodimgname = (TextView) t.getChildAt(2);
                                String selectedprodimgname = txtprodimgname.getText().toString();
                                System.out.println("selectedprodimgname=" + selectedprodimgname);

                                calldeleteimage(selectedprodimgname);

//                                Toast.makeText(getApplicationContext(), "Removed Successfully", Toast.LENGTH_SHORT).show();
                            }
                        });

                alertdelete.setNegativeButton("NO",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                alertdelete.show();

            }
        });
    }

    private void calldeleteimage(String selectedprodimgname) {
        Log.v(TAG, "Deletebtnimg Clicked");
        System.out.println("delprodimagename=" + selectedprodimgname);
        DeleteImgTask deleteImgTask = new DeleteImgTask();
        String URLdelimgOrders = appurl + "/ProductServlet?isFrom=Android&method=JPDelImg&prodimg=" + selectedprodimgname;
        Log.v(TAG, "URLdelimgOrders=" + URLdelimgOrders);
        params.clear();
        params.put("isFrom", "Android");
        params.put("method", "JPDelImg");
        params.put("prodimg", selectedprodimgname);
        deleteImgTask.execute(new String[]{URLdelimgOrders});
    }

    public class DeleteImgTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            Log.v(TAG, "DoInBackground");
            String output = null;
            for (String url : urls) {
                output = new HttpConnectionUtil().getOutputFromUrl(url, params);
            }
            return output;
        }

        @Override
        protected void onPostExecute(String output) {
            System.out.println("--ert->" + output);
            Log.v(TAG, output);
            if (output.substring(0, 1).equalsIgnoreCase("1")) {
                Log.v(TAG, "Successful");

//                SendNotfn task = new SendNotfn();
//                String URLNotification = appurl + "/GCMNotification1?message=NewProductLaunched--" + etprodno.getText().toString();
//                Log.v(TAG, "URLNotification=" + URLNotification);
//                params.clear();
//                params.put("message", "NewProductLaunched--" + etprodno.getText().toString());
//                task.execute(new String[]{URLNotification});
                errorMsg = "Deleted Succesfully";
            } else {
                Log.v(TAG, "Unsuccessful");
                errorMsg = "Unsuccessful";
            }

            showDialog(DIALOG_ALERT);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        switch (id) {
            case DIALOG_ALERT:
                // Create out AlterDialog

                builder.setMessage(errorMsg);
                builder.setCancelable(true);
                // builder.setPositiveButton("I agree", new OkOnClickListener());
                builder.setNegativeButton("Ok", new OkOnClickListener());
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            case DIALOG_ERROR:
                // Create out AlterDialog

                //builder.setMessage("Please select Reason");
                builder.setMessage(errorMsg);
                builder.setCancelable(true);
                // builder.setPositiveButton("I agree", new OkOnClickListener());
                builder.setNegativeButton("Ok", null);
                AlertDialog errordialog = builder.create();
                errordialog.show();
                break;
            case DIALOG_CONFIRM:
                builder.setMessage("Do you want to confirm?");
                builder.setCancelable(true);
                builder.setPositiveButton("No", null);
                //builder.setNegativeButton("Yes", null);
                builder.setNegativeButton("Yes", new YesOnClickListener());
                AlertDialog errordialog1 = builder.create();
                errordialog1.show();
                break;
        }
        return super.onCreateDialog(id);
    }

    private final class OkOnClickListener implements
            DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            finish();
        }
    }

    private final class YesOnClickListener implements
            DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int which) {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        Log.v(TAG, "onRequestPermissionsResult entered");
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // BEGIN_INCLUDE(onRequestPermissionsResult)
        if (requestCode == REQUEST_READ_STORAGE) {
            // Request for Storage permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Start Storage preview Activity.
                Snackbar.make(mLayout, "Storage permission was granted",
                        Snackbar.LENGTH_SHORT)
                        .show();
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, 0);
            } else {
                // Permission request was denied.
                Snackbar.make(mLayout, "Storage permission request was denied.",
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
        }
        // END_INCLUDE(onRequestPermissionsResult)
    }


}
