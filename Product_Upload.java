package com.example.admin.jprod;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Product_Upload extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    Button btnPickImg, btnclickimage, btnupload;
    EditText editprodno, editprodname, editprodcost, editproddesc;
    private Spinner editprod_type_spinner, editcat1_type_spinner, editcat2_type_spinner, editcat3_type_spinner;
    private String[] prod_type_List = {"--Select--", "Platinum", "Gold", "Silver", "Others"};
    TableLayout product_table;
    TableRow row;
    String TAG = "Product_Upload";
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
    List<String> catList = new ArrayList<String>();
    String[] arrcatlist1;
    String[] arrcatlist2;
    String[] arrcatlist3;
    Map<String, CategoryList> hcatmap = new HashMap<String, CategoryList>();
    Map<String, CategoryList> hcatmap2 = new HashMap<String, CategoryList>();
    Map<String, CategoryList> hcatmap3 = new HashMap<String, CategoryList>();
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
    private static final int REQUEST_WRITE_STORAGE = 3;
    public static final int REQUEST_CAMERA = 5;
    private int productid = 0;
    ArrayAdapter<String> selectAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_produpload);
        Log.v(TAG, "onCreate");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.productmastr);
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
        btnclickimage = (Button) findViewById(R.id.clickimg);
        btnupload = (Button) findViewById(R.id.btnupload);
        str1 = (TextView) findViewById(R.id.txtpath);
        imgView = (ImageView) findViewById(R.id.image_view);
        actproduct = (AutoCompleteTextView) findViewById(R.id.autocompproduct);

        editprodno = (EditText) findViewById(R.id.editprodno);
        editprodname = (EditText) findViewById(R.id.editprodname);
        editprodcost = (EditText) findViewById(R.id.editprodcost);
        editproddesc = (EditText) findViewById(R.id.editproddesc);

        // editprod_type_spinner = (Spinner) findViewById(R.id.editprod_type_spinner);
        editcat1_type_spinner = (Spinner) findViewById(R.id.editcat1_type_spinner);
        editcat2_type_spinner = (Spinner) findViewById(R.id.editcat2_type_spinner);
        editcat3_type_spinner = (Spinner) findViewById(R.id.editcat3_type_spinner);

        // addItemsOnSpinner(editprod_type_spinner, prod_type_List);
        String[] sel = {"--Select--"};
        selectAdapter = new ArrayAdapter<String>(Product_Upload.this,
                android.R.layout.simple_spinner_item, sel);
        selectAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);

        btnPickImg.setVisibility(View.VISIBLE);
        btnclickimage.setVisibility(View.VISIBLE);
        imgView.setVisibility(View.GONE);
        btnupload.setVisibility(View.VISIBLE);
        str1.setVisibility(View.GONE);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");

        fetchproducts();

        btnPickImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String pno = editprodno.getText().toString();
                String pname = editprodname.getText().toString();
                String pcost = editprodcost.getText().toString();
                String pdesc = editproddesc.getText().toString();
                //String ptype = editprod_type_spinner.getSelectedItem().toString();
                String cat1 = editcat1_type_spinner.getSelectedItem().toString();
                String cat2 = editcat2_type_spinner.getSelectedItem().toString();
                String cat3 = editcat3_type_spinner.getSelectedItem().toString();
                if (!pno.equals("") && !pname.equals("") && !pcost.equals("") && !cat1.equals("--Select--") && !cat2.equals("--Select--") && !cat3.equals("--Select--") && !pdesc.equals("")) {
//                boolean result = Utility.checkPermission(Imageupload.this);
//                if (result) {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(galleryIntent, 10);
                    } else {
                        // Permission is missing and must be requested.
                        requestWritePermission();
                    }
                } else {
                    if (pno.equals("")) {
                        errorMsg = "Please Enter Product.No";
                    } else if (pname.equals("")) {
                        errorMsg = "Please Enter Product Name";
                    } else if (pcost.equals("")) {
                        errorMsg = "Please Enter Product Cost";
//                    } else if (ptype.equalsIgnoreCase("--Select--")) {
//                        errorMsg = "Please Enter Product Type";
//                    }
                    } else if (cat1.equalsIgnoreCase("--Select--")) {
                        errorMsg = "Please Enter Category-1 Type";
                    } else if (cat2.equalsIgnoreCase("--Select--")) {
                        errorMsg = "Please Enter Category-2 Type";
                    } else if (cat3.equalsIgnoreCase("--Select--")) {
                        errorMsg = "Please Enter Category-3 Type";
                    } else if (pdesc.equals("")) {
                        errorMsg = "Please Enter Product Description";
                    }
                    showDialog(DIALOG_ERROR);
                }
            }
        });

        btnclickimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String pno = editprodno.getText().toString();
                String pname = editprodname.getText().toString();
                String pcost = editprodcost.getText().toString();
                String pdesc = editproddesc.getText().toString();
//                String ptype = editprod_type_spinner.getSelectedItem().toString();
                String cat1 = editcat1_type_spinner.getSelectedItem().toString();
                String cat2 = editcat2_type_spinner.getSelectedItem().toString();
                String cat3 = editcat3_type_spinner.getSelectedItem().toString();
                if (!pno.equals("") && !pname.equals("") && !pcost.equals("") && !cat1.equals("--Select--") && !cat2.equals("--Select--") && !cat3.equals("--Select--") && !pdesc.equals("")) {

                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                == PackageManager.PERMISSION_GRANTED) {
                            Intent photoCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(photoCaptureIntent, 11);
                        } else {
                            requestWriteExternalPermission();
                        }
                    } else {
                        // Permission is missing and must be requested.
                        requestCameraPermission();
                    }
                } else {
                    if (pno.equals("")) {
                        errorMsg = "Please Enter Product.No";
                    } else if (pname.equals("")) {
                        errorMsg = "Please Enter Product Name";
                    } else if (pcost.equals("")) {
                        errorMsg = "Please Enter Product Cost";
//                    } else if (ptype.equalsIgnoreCase("--Select--")) {
//                        errorMsg = "Please Enter Product Type";
//                    }
                    } else if (cat1.equalsIgnoreCase("--Select--")) {
                        errorMsg = "Please Enter Category-1 Type";
                    } else if (cat2.equalsIgnoreCase("--Select--")) {
                        errorMsg = "Please Enter Category-2 Type";
                    } else if (cat3.equalsIgnoreCase("--Select--")) {
                        errorMsg = "Please Enter Category-3 Type";
                    } else if (pdesc.equals("")) {
                        errorMsg = "Please Enter Product Description";
                    }
                    showDialog(DIALOG_ERROR);
                }
            }
        });

        btnupload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pno = editprodno.getText().toString();
                String pname = editprodname.getText().toString();
                String pcost = editprodcost.getText().toString();
                String pdesc = editproddesc.getText().toString();
//                String ptype = editprod_type_spinner.getSelectedItem().toString();
                String cat1 = editcat1_type_spinner.getSelectedItem().toString();

                String cat2 = editcat2_type_spinner.getSelectedItem().toString();
                String cat3 = editcat3_type_spinner.getSelectedItem().toString();
                if (!pno.equals("") && !pname.equals("") && !pcost.equals("") && !cat1.equals("--Select--") && !cat2.equals("--Select--") && !cat3.equals("--Select--") && !pdesc.equals("")) {
                    uploadFile();
                } else {
                    if (pno.equals("")) {
                        errorMsg = "Please Enter Product.No";
                    } else if (pname.equals("")) {
                        errorMsg = "Please Enter Product Name";
                    } else if (pcost.equals("")) {
                        errorMsg = "Please Enter Product Cost";
//                    } else if (ptype.equalsIgnoreCase("--Select--")) {
//                        errorMsg = "Please Enter Product Type";
//                    }
                    } else if (cat1.equalsIgnoreCase("--Select--")) {
                        errorMsg = "Please Enter Category-1 Type";
                    } else if (cat2.equalsIgnoreCase("--Select--")) {
                        errorMsg = "Please Enter Category-2 Type";
                    } else if (cat3.equalsIgnoreCase("--Select--")) {
                        errorMsg = "Please Enter Category-3 Type";
                    } else if (pdesc.equals("")) {
                        errorMsg = "Please Enter Product Description";
                    }
                    showDialog(DIALOG_ERROR);
                }
            }
        });

        actproduct.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (actproduct.getText().length() == 0) {
                    actproduct.showDropDown();
                    return true;
                }
                return false;
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
                actprodno = splitprodno[1];

                imgView.setVisibility(View.VISIBLE);
                product_table.removeAllViewsInLayout();

                if (hmap.containsKey(actprodno)) {
                    Log.v(TAG, ", actprodno=" + actprodno);
                    ProductList productinfo = hmap.get(actprodno);
                    String pno = productinfo.getProdno();
                    String pname = productinfo.getProdname();
//                    String ptype = productinfo.getProdtypes();

                    //System.out.println("keys="+getKeysFromValue(hcatmap,productinfo.getCat1()));
                    List cat1no = getKeysFromValue(hcatmap, productinfo.getCat1());
                    String cat1 = cat1no.get(0).toString();

                    List cat2no = getKeysFromValue(hcatmap2, productinfo.getCat2());
                    String cat2 = cat2no.get(0).toString();

                    List cat3no = getKeysFromValue(hcatmap3, productinfo.getCat3());
                    String cat3 = cat3no.get(0).toString();

                    String pdesc = productinfo.getProddesc();
                    String pcost = productinfo.getProdcost();
                    productid = productinfo.getProdid();
                    Log.v(TAG, ", productid=" + productid);
                    editprodno.setText(pno);
                    editprodname.setText(pname);
                    editprodcost.setText(pcost);
                    editproddesc.setText(pdesc);
                    //SetSpinnerSelection(editprod_type_spinner, prod_type_List, ptype);
                    SetSpinnerSelection(editcat1_type_spinner, arrcatlist1, cat1);
                    SetSpinnerSelection(editcat2_type_spinner, arrcatlist2, cat2);
                    SetSpinnerSelection(editcat3_type_spinner, arrcatlist3, cat3);
                }

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

    public static List getKeysFromValue(Map<String, CategoryList> hm, Object value) {
        Set ref = hm.keySet();
        Iterator it = ref.iterator();
        List list = new ArrayList();

        while (it.hasNext()) {
            Object o = it.next();
            CategoryList cl = hm.get(o);
            if (cl.getId().equals(value)) {
                list.add(o);
            }
        }
        return list;
    }


    public void addItemsOnSpinner(Spinner dropDownId, String[] opts) {

        if (dropDownId != null) {
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, opts);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
            dropDownId.setAdapter(dataAdapter);
        }
    }

    public void SetSpinnerSelection(Spinner spinner, String[] array, String text) {
        //Spinner name, Array set to the spinner,Value to be set in the spinner
        for (int i = 1; i < array.length; i++) {
            System.out.println(array[i]);
            String[] catnumber = array[i].split("@");
            if (catnumber[1].equals(text)) {
                spinner.setSelection(i);
            }
        }
    }

    private void requestWriteExternalPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            Log.v(TAG, "Access is required to import images");
            Snackbar.make(mLayout, "Storage access is required to import images.",
                    Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(Product_Upload.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_WRITE_STORAGE);
                }
            }).show();

        } else {
            Log.v(TAG, "Permission is not available. Requesting Storage permission");
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        }
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
                    ActivityCompat.requestPermissions(Product_Upload.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_READ_STORAGE);
                }
            }).show();

        } else {
            Log.v(TAG, "Permission is not available. Requesting Storage permission");
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_READ_STORAGE);
        }
    }

    public void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            Log.v(TAG, "Camera Access is required to click images");
            Snackbar.make(mLayout, "Camera access is required to click images.",
                    Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(Product_Upload.this, new String[]{Manifest.permission.CAMERA},
                            REQUEST_CAMERA);
                }
            }).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
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
        File file = null;
        if (mediaPath != null) {
            file = new File(mediaPath);
        }
        String username = "Admin";
        String pno = editprodno.getText().toString();
        String pname = editprodname.getText().toString();
        String pcost = editprodcost.getText().toString();
        String pdesc = editproddesc.getText().toString();
//                String ptype = editprod_type_spinner.getSelectedItem().toString();
        String[] cat1split = editcat1_type_spinner.getSelectedItem().toString().split("@");
        String cat1 = "";
        if (hcatmap.containsKey(cat1split[1])) {
            CategoryList catist = hcatmap.get(cat1split[1]);
            cat1 = catist.getId();
        }

        String[] cat2split = editcat2_type_spinner.getSelectedItem().toString().split("@");
        String cat2 = "";
        if (hcatmap2.containsKey(cat2split[1])) {
            CategoryList catist = hcatmap2.get(cat2split[1]);
            cat2 = catist.getId();
        }

        String[] cat3split = editcat3_type_spinner.getSelectedItem().toString().split("@");
        String cat3 = "";
        if (hcatmap3.containsKey(cat3split[1])) {
            CategoryList catist = hcatmap3.get(cat3split[1]);
            cat3 = catist.getId();
        }


        MultipartBody.Part fileToUpload;
        RequestBody filename = null;
        String ext = "NA";
        if (mediaPath == null) {
            RequestBody attachmentEmpty = RequestBody.create(MediaType.parse("text/plain"), "");
            fileToUpload = MultipartBody.Part.createFormData("attachment", "", attachmentEmpty);
        } else {
            // Parsing any Media type file
            RequestBody requestBody = RequestBody.create(MediaType.parse("*/*"), file);
            fileToUpload = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
            filename = RequestBody.create(MediaType.parse("text/plain"), file.getName());
            Log.v(TAG, "filename=" + file.getName());
            ext = getFileExtension(file);
            Log.v(TAG, "RequestBodyfilename=" + filename);
        }

        RequestBody rbpno = RequestBody.create(MediaType.parse("text/plain"), pno);
        RequestBody rbpname = RequestBody.create(MediaType.parse("text/plain"), pname);
        RequestBody rbpcost = RequestBody.create(MediaType.parse("text/plain"), pcost);
//        RequestBody rbptype = RequestBody.create(MediaType.parse("text/plain"), ptype);
        RequestBody rbcat1 = RequestBody.create(MediaType.parse("text/plain"), cat1);
        RequestBody rbcat2 = RequestBody.create(MediaType.parse("text/plain"), cat2);
        RequestBody rbcat3 = RequestBody.create(MediaType.parse("text/plain"), cat3);
        RequestBody rbpdesc = RequestBody.create(MediaType.parse("text/plain"), pdesc);
        RequestBody rbpid = RequestBody.create(MediaType.parse("text/plain"), "NA");
        RequestBody rbusername = RequestBody.create(MediaType.parse("text/plain"), username);
        if (productid == 0) {
            rbpid = RequestBody.create(MediaType.parse("text/plain"), "NA");
        } else {
            Log.v(TAG, "prodid=" + productid);
            rbpid = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(productid));
            Log.v(TAG, "reqbodyprodid=" + productid);
        }


        HashMap<String, RequestBody> map = new HashMap<>();
        map.put("pno", rbpno);
        map.put("pname", rbpname);
        map.put("pcost", rbpcost);
//        map.put("ptype", rbptype);
        map.put("cat1", rbcat1);
        map.put("cat2", rbcat2);
        map.put("cat3", rbcat3);
        map.put("pdesc", rbpdesc);
        map.put("pid", rbpid);
        map.put("username", rbusername);

        ApiConfig getResponse = AppConfig.getRetrofit(appurl).create(ApiConfig.class);
        Call<ServerResponse> call = getResponse.uploadFilenew(fileToUpload, filename, ext, map);
        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                ServerResponse serverResponse = response.body();
                Log.v(TAG, "response=" + response);
                if (serverResponse != null) {
                    if (serverResponse.getSuccess()) {
                        Toast.makeText(getApplicationContext(), serverResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        String pimgs = serverResponse.getPimgs();
                        product_table.removeAllViewsInLayout();
                        listpimgs.clear();
                        if (pimgs != null && !pimgs.equalsIgnoreCase("")) {

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
                        SendNotfn task = new SendNotfn();
                        String URLNotification = appurl + "/GCMNotification1?message=NewProductLaunched";
                        Log.v(TAG, "URLNotification=" + URLNotification);
                        params.clear();
                        params.put("message", "New Product Launched");
                        task.execute(new String[]{URLNotification});
                        imgView.setImageResource(0);
                        str1.setText("");
                    } else {
                        Toast.makeText(getApplicationContext(), serverResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    assert serverResponse != null;
                    Log.v("Response", serverResponse.toString());
                }
                progressDialog.dismiss();
                resetall();
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                Log.v("Response", call.toString() + "," + t);
                t.printStackTrace();

            }
        });
    }

    public class SendNotfn extends AsyncTask<String, Void, String> {

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
        }
    }

    private void resetall() {
        editprodno.setText("");
        editprodname.setText("");
        editprodcost.setText("");
        editproddesc.setText("");
//        editprod_type_spinner.setSelection(0);
        editcat1_type_spinner.setSelection(0);
        editcat2_type_spinner.setSelection(0);
        editcat3_type_spinner.setSelection(0);
//        actproduct.setText("");
//        product_table.removeAllViewsInLayout();
        btnPickImg.setVisibility(View.VISIBLE);
        btnclickimage.setVisibility(View.VISIBLE);
        imgView.setImageResource(0);
        imgView.setVisibility(View.GONE);
        btnupload.setVisibility(View.VISIBLE);
        str1.setText("");
        str1.setVisibility(View.GONE);
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
            try {
                prodsList.clear();
                hmap.clear();
                if (output != null && output.length() > 0 && !output.substring(0, 1).equalsIgnoreCase("0")) {
                    hmaplist.clear();
                    productLists.clear();
                    productImages.clear();
                    String res[] = output.split("@@");
                    Log.v(TAG, "res=" + Arrays.toString(res));
                    for (int i = 0; i < res.length; i++) {
//                        String pno = "", prodnames = "", prodtypes = "", proddesc = "", prodcost = "";
                        String pno = "", prodnames = "", cat1 = "", cat2 = "", cat3 = "", proddesc = "", prodcost = "";
                        int prodid = 0;
                        productImages = new ArrayList<>();

                        String ressplit[] = res[i].split("@");
                        Log.v(TAG, "ressplit=" + Arrays.toString(ressplit));
                        String res1[] = ressplit[0].split(">");
                        Log.v(TAG, "res1=" + Arrays.toString(res1));

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
                            prodnames = "";
                        } else {
                            productList.setProdname(res1[1]);
                            prodnames = res1[1];
                        }
                        /*if (res1[2] != null && res1[2].equalsIgnoreCase("NA")) {
                            productList.setProdtypes("");
                            prodtypes = "";
                        } else {
                            productList.setProdtypes(res1[2]);
                            prodtypes = res1[2];
                        }*/
                        if (res1[2] != null && res1[2].equalsIgnoreCase("NA")) {
                            productList.setCat1("");
                            cat1 = "";
                        } else {
                            productList.setCat1(res1[2]);
                            cat1 = res1[2];
                        }
                        if (res1[3] != null && res1[3].equalsIgnoreCase("NA")) {
                            productList.setCat2("");
                            cat2 = "";
                        } else {
                            productList.setCat2(res1[3]);
                            cat2 = res1[3];
                        }
                        if (res1[4] != null && res1[4].equalsIgnoreCase("NA")) {
                            productList.setCat3("");
                            cat3 = "";
                        } else {
                            productList.setCat3(res1[4]);
                            cat3 = res1[4];
                        }
                        if (res1[5] != null && res1[5].equalsIgnoreCase("NA")) {
                            productList.setProddesc("");
                            proddesc = "";
                        } else {
                            productList.setProddesc(res1[5]);
                            proddesc = res1[5];
                        }
                        if (res1[6] != null && res1[6].equalsIgnoreCase("NA")) {
                            productList.setProdcost("");
                            prodcost = "";
                        } else {
                            productList.setProdcost(res1[6]);
                            prodcost = res1[6];
                        }
                        if (res1[7] != null && res1[7].equalsIgnoreCase("NA")) {
                            productList.setProdid(0);
                            prodcost = "";
                        } else {
                            prodid = Integer.parseInt(res1[7]);
                            productList.setProdid(prodid);
                            prodid = prodid;
                        }
                        productLists.add(productList);
                        prodsList.add(productList.getProdname() + "-" + productList.getProdno());
//                        hmap.put(pno, new ProductList(pno, prodnames, prodtypes, proddesc, prodcost, prodid));
                        hmap.put(pno, new ProductList(pno, prodnames, cat1, cat2, cat3, proddesc, prodcost, prodid));
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
                    adapter = new ArrayAdapter<String>(Product_Upload.this, android.R.layout.select_dialog_item, arrproductslist);
                    actproduct.setThreshold(0);
                    actproduct.setAdapter(adapter);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            fetchcatnames();
        }
    }

    private void fetchcatnames() {
        String URLcat = appurl + "/CategoryServlet?isFrom=Android&method=CatA_S";
        GetCategoryNames categoryNames = new GetCategoryNames();
        categoryNames.execute(URLcat);
    }

    //Fetch Category Details
    private class GetCategoryNames extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
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
            hcatmap.clear();
            catList.clear();
            catList.add("--Select--");
            if (output != null && output.length() > 0 && !output.substring(0, 1).equalsIgnoreCase("0")) {
                if (!output.equalsIgnoreCase("0@NA")) {
                    String outputsplit[] = output.split("---");
                    if (outputsplit[0] != null && outputsplit[0].length() > 0 && !outputsplit[0].equalsIgnoreCase("0@NA")) {
                        String res[] = outputsplit[0].split("@");
                        Log.v(TAG, "res=" + Arrays.toString(res));

                        String id = "", no = "", name = "";
                        for (int i = 0; i < res.length; i++) {

                            String res1[] = res[i].split(">");
                            if (res1[0] != null && res1[0].equalsIgnoreCase("NA")) {
                                id = "";
                            } else {
                                id = res1[0];
                            }
                            if (res1[1] != null && res1[1].equalsIgnoreCase("NA")) {
                                no = "";
                            } else {
                                no = res1[1];
                            }
                            if (res1[2] != null && res1[2].equalsIgnoreCase("NA")) {
                                name = "";
                            } else {
                                name = res1[2];
                            }
                            catList.add(name + "@" + no);
                            hcatmap.put(no, new CategoryList(id, name, no));
                        }
                        arrcatlist1 = catList.toArray(new String[]{});
                        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(Product_Upload.this,
                                android.R.layout.simple_spinner_item, arrcatlist1);
                        dataAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
                        editcat1_type_spinner.setAdapter(dataAdapter);
                        //adapter = new ArrayAdapter<String>(Category_Master.this, android.R.layout.select_dialog_item, arrcatlist);
                        //autocompcategory.setThreshold(0);
                        //autocompcategory.setAdapter(adapter);
                    } else {
                        editcat1_type_spinner.setAdapter(selectAdapter);
//                        errorMsg = "No Data";
//                        showDialog(DIALOG_ALERT);
                    }

                    if (outputsplit[1] != null && outputsplit[1].length() > 0 && !outputsplit[1].equalsIgnoreCase("0@NA")) {
                        String res[] = outputsplit[1].split("@");
                        Log.v(TAG, "res=" + Arrays.toString(res));
                        hcatmap2.clear();
                        catList.clear();
                        catList.add("--Select--");
                        String id = "", no = "", name = "";
                        for (int i = 0; i < res.length; i++) {

                            String res1[] = res[i].split(">");
                            if (res1[0] != null && res1[0].equalsIgnoreCase("NA")) {
                                id = "";
                            } else {
                                id = res1[0];
                            }
                            if (res1[1] != null && res1[1].equalsIgnoreCase("NA")) {
                                no = "";
                            } else {
                                no = res1[1];
                            }
                            if (res1[2] != null && res1[2].equalsIgnoreCase("NA")) {
                                name = "";
                            } else {
                                name = res1[2];
                            }
                            catList.add(name + "@" + no);
                            hcatmap2.put(no, new CategoryList(id, name, no));
                        }
                        arrcatlist2 = catList.toArray(new String[]{});
                        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(Product_Upload.this,
                                android.R.layout.simple_spinner_item, arrcatlist2);
                        dataAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
                        editcat2_type_spinner.setAdapter(dataAdapter);
                        //adapter = new ArrayAdapter<String>(Category_Master.this, android.R.layout.select_dialog_item, arrcatlist);
                        //autocompcategory.setThreshold(0);
                        //autocompcategory.setAdapter(adapter);
                    } else {
                        editcat2_type_spinner.setAdapter(selectAdapter);
//                        errorMsg = "No Data";
//                        showDialog(DIALOG_ALERT);
                    }

                    if (outputsplit[2] != null && outputsplit[2].length() > 0 && !outputsplit[2].equalsIgnoreCase("0@NA")) {
                        String res[] = outputsplit[2].split("@");
                        Log.v(TAG, "res=" + Arrays.toString(res));
                        hcatmap3.clear();
                        catList.clear();
                        catList.add("--Select--");
                        String id = "", no = "", name = "";
                        for (int i = 0; i < res.length; i++) {

                            String res1[] = res[i].split(">");
                            if (res1[0] != null && res1[0].equalsIgnoreCase("NA")) {
                                id = "";
                            } else {
                                id = res1[0];
                            }
                            if (res1[1] != null && res1[1].equalsIgnoreCase("NA")) {
                                no = "";
                            } else {
                                no = res1[1];
                            }
                            if (res1[2] != null && res1[2].equalsIgnoreCase("NA")) {
                                name = "";
                            } else {
                                name = res1[2];
                            }
                            catList.add(name + "@" + no);
                            hcatmap3.put(no, new CategoryList(id, name, no));
                        }
                        arrcatlist3 = catList.toArray(new String[]{});
                        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(Product_Upload.this,
                                android.R.layout.simple_spinner_item, arrcatlist3);
                        dataAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
                        editcat3_type_spinner.setAdapter(dataAdapter);
                        //adapter = new ArrayAdapter<String>(Category_Master.this, android.R.layout.select_dialog_item, arrcatlist);
                        //autocompcategory.setThreshold(0);
                        //autocompcategory.setAdapter(adapter);
                    } else {
                        editcat3_type_spinner.setAdapter(selectAdapter);
//                        errorMsg = "No Data";
//                        showDialog(DIALOG_ALERT);
                    }

                } else {
                    editcat1_type_spinner.setAdapter(selectAdapter);
                    editcat2_type_spinner.setAdapter(selectAdapter);
                    editcat3_type_spinner.setAdapter(selectAdapter);
                    //autocompcategory.setAdapter(null);
//                    errorMsg = "No Data";
//                    showDialog(DIALOG_ALERT);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "onActivityResult");
        try {
            // When an Image is picked
            if (requestCode == 10 && resultCode == RESULT_OK && null != data) {
                System.out.println(TAG + "data=" + data);
                // Get the Image from data
                Uri selectedImage = data.getData();
                System.out.println(TAG + "selectedImage=" + selectedImage);
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                System.out.println(TAG + "filePathColumn=" + filePathColumn);
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
                mBitmapInsurance=addWatermark(getResources(),mBitmapInsurance);
                imgView.setImageBitmap(mBitmapInsurance);
                imgView.setVisibility(View.VISIBLE);
                btnupload.setVisibility(View.VISIBLE);
                str1.setVisibility(View.VISIBLE);
                cursor.close();

            } else if (requestCode == 11 && resultCode == RESULT_OK && null != data) {
                System.out.println(TAG + "data=" + data);

                onCaptureImageResult(data);

                // Get the Image from data
               /* Uri selectedImage = data.getData();
                System.out.println(TAG + "selectedImage=" + selectedImage);
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                System.out.println(TAG + "filePathColumn=" + filePathColumn);
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
                imgView.setVisibility(View.VISIBLE);
                btnupload.setVisibility(View.VISIBLE);
                str1.setVisibility(View.VISIBLE);
                cursor.close();*/

            } else {
                Toast.makeText(this, "You haven't picked Image/Video", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
        }

    }

    /*private void onCaptureImageResult(Intent data) {

        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        String formattedname = System.currentTimeMillis() + ".jpg";
        String FILE = Environment.getExternalStorageDirectory().toString()
                + "/ORD_IMGS/" + formattedname;


       *//* File destination = new File(Environment.getExternalStorageDirectory(),
                );*//*
        String root = Environment.getExternalStorageDirectory().getAbsolutePath().toString();
        File myDir = new File(root + "/ORD_IMGS");
        myDir.mkdirs();
        FileOutputStream fo;
        try {
            //destination.createNewFile();
            fo = new FileOutputStream(FILE);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        imgView.setImageBitmap(thumbnail);
        imgView.setVisibility(View.VISIBLE);


        Uri uri=Uri.fromFile(new File(myDir,formattedname));

//        Uri uri = Uri.parse("path");
//        Uri uri = Uri.fromFile(new File(FILE));
        Log.v(TAG,uri.toString());
        System.out.println("uri= "+uri);
        mediaPath=uri.toString();
        str1.setText(mediaPath);
        str1.setVisibility(View.VISIBLE);
    }*/

    private void onCaptureImageResult(Intent data) {

        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        String formattedname = System.currentTimeMillis() + ".jpg";
//        String FILE = Environment.getExternalStorageDirectory().toString()
//                + "/ORD_IMGS/" + formattedname;
        System.out.println("data.getdata= " + data.getData());

        Uri tempUri = getImageUri(getApplicationContext(), thumbnail);
        System.out.println("tempUri real " + getRealPathFromURI(tempUri));
        String realuri = getRealPathFromURI(tempUri);
//        String root = Environment.getExternalStorageDirectory().getAbsolutePath().toString();
//        File myDir = new File(root + "/ORD_IMGS");
//        myDir.mkdirs();
//        FileOutputStream fo;
//        try {
//            fo = new FileOutputStream(FILE);
//            fo.write(bytes.toByteArray());
//            fo.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        thumbnail=addWatermark(getResources(),thumbnail);
        imgView.setImageBitmap(thumbnail);
        imgView.setVisibility(View.VISIBLE);

        String filePath = realuri;
        File filecheck = new File(filePath);
        if (filecheck.exists() == true) {

            // Uri uri = Uri.fromFile(new File(myDir, formattedname));

//        Uri uri = Uri.parse("path");
//        Uri uri = Uri.fromFile(new File(FILE));
            // Log.v(TAG, uri.toString());
            System.out.println("filePath= " + filePath);
            mediaPath = realuri;
            str1.setText(mediaPath);
            str1.setVisibility(View.VISIBLE);
        } else {
            Log.v(TAG, "File nt exists");
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
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

        Glide.with(getApplicationContext()).load(appurl + "/DisplayImage?type=prod&picname=" + imgname)
                .placeholder(R.drawable.image)
                .error(R.drawable.image)
                .into(b1);

        b1.setPadding(15, 15, 10, 10);
        b1.setScaleType(ImageView.ScaleType.FIT_XY);
        bdel.setPadding(5, 5, 5, 5);
        bdel.setImageResource(R.drawable.cancel);
        bdel.setScaleType(ImageView.ScaleType.FIT_XY);
        TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 0.3f);
        TableRow.LayoutParams params1 = new TableRow.LayoutParams(0, 250, 1.8f);
        TableRow.LayoutParams params2 = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 1.4f);
        TableRow.LayoutParams paramsgap = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 0.1f);
        TableRow.LayoutParams paramsdel = new TableRow.LayoutParams(0, 64, 0.5f);
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
        // row.addView(tgap);
        row.addView(bdel);
        row.addView(tgap1);

        product_table.addView(row, new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

        bdel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                AlertDialog.Builder alertdelete = new AlertDialog.Builder(
                        Product_Upload.this);

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
            Intent intent = new Intent(Product_Upload.this, Product_Upload.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
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
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, 0);
            } else {
                // Permission request was denied.
                Snackbar.make(mLayout, "Storage permission request was denied.",
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
        }
        // END_INCLUDE(onRequestPermissionsResult)

        if (requestCode == REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(mLayout, "Camera permission was granted",
                        Snackbar.LENGTH_SHORT)
                        .show();
            } else {
                // Permission request was denied.
                Snackbar.make(mLayout, "Camera permission request was denied.",
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
        }
    }

    /**
     * Adds a watermark on the given image.
     */
    public static Bitmap addWatermark(Resources res, Bitmap source) {
        int w, h;
        Canvas c;
        Paint paint;
        Bitmap bmp, watermark;
        Matrix matrix;
        float scale,scalew;
        RectF r;
        w = source.getWidth();
        h = source.getHeight();
        // Create the new bitmap
        bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
        // Copy the original bitmap into the new one
        c = new Canvas(bmp);
        c.drawBitmap(source, 0, 0, paint);
        // Load the watermark
        watermark = BitmapFactory.decodeResource(res, R.drawable.infinito);
        // Scale the watermark to be approximately 40% of the source image height
        scale = (float) (((float) h * 0.20) / (float) watermark.getHeight());
        scalew = (float) (((float) w * 0.40) / (float) watermark.getWidth());
        // Create the matrix
        matrix = new Matrix();
        matrix.postScale(scalew, scale);
        // Determine the post-scaled size of the watermark
        r = new RectF(0, 0, watermark.getWidth(), watermark.getHeight());
        matrix.mapRect(r);
        // Move the watermark to the bottom right corner
        matrix.postTranslate(w - r.width(), h - r.height());
        // Draw the watermark
        c.drawBitmap(watermark, matrix, paint);
        // Free up the bitmap memory
        watermark.recycle();
        return bmp;
    }

}
