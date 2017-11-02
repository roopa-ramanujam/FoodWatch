package com.example.roopar.foodwatch;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.api.request.ClarifaiRequest;
import clarifai2.api.request.model.PredictRequest;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.model.Model;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static TextView mTextView;
    private static Button mCameraButton;
    private static ImageView mImageView;
    private static ImageView mImageViewJPG;

    private String tagMostLikely = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initVars();
        setListeners();
//        String APIKEY = "e14859a3ccae40a095a45a2fde6c8d57";
//        final ClarifaiClient client = new ClarifaiBuilder(APIKEY).buildSync();
//
//
//        Model<Concept> generalModel = client.getDefaultModels().generalModel();
//
//        PredictRequest<Concept> request = generalModel.predict().withInputs(
//                ClarifaiInput.forImage("http://juliandance.org/wp-content/uploads/2016/01/RedApple.jpg")
//        );
//        //ClarifaiRequest.OnFailure onFailure = null;
//        request.executeAsync(onSuccess);

        verifyStoragePermissions(MainActivity.this);


//        onFailure = new ClarifaiRequest.OnFailure() {
//            @Override
//            public void onClarifaiResponseUnsuccessful(int errorCode) {
//                mTextView.setText("failed");
//            }
//        };

    }

    private void sendClarifaiRequest() {
        String APIKEY = "e14859a3ccae40a095a45a2fde6c8d57";
        final ClarifaiClient client = new ClarifaiBuilder(APIKEY).buildSync();


        Model<Concept> generalModel = client.getDefaultModels().generalModel();

        PredictRequest<Concept> request = generalModel.predict().withInputs(
                ClarifaiInput.forImage("http://juliandance.org/wp-content/uploads/2016/01/RedApple.jpg")
        );
        //ClarifaiRequest.OnFailure onFailure = null;
        request.executeAsync(onSuccess);
    }
    private void initVars() {
        //mTextView = (TextView) findViewById(R.id.hello_text_view);
        mCameraButton = (Button) findViewById(R.id.camera_button);
        mImageView = (ImageView) findViewById(R.id.photo);
        mImageViewJPG = (ImageView) findViewById(R.id.jpgphoto);

    }

    ClarifaiRequest.OnSuccess<List<ClarifaiOutput<Concept>>> onSuccess = new ClarifaiRequest.OnSuccess<List<ClarifaiOutput<Concept>>>() {
        @Override
        public void onClarifaiResponseSuccess(List<ClarifaiOutput<Concept>> clarifaiOutputs) {
            String response = clarifaiOutputs.toString();
            clarifaiOutputs.get(0);
            ArrayList<String> tags = new ArrayList<String>();

            double maxProb = 0.0;
            for (Concept concept : clarifaiOutputs.get(0).data()) {
                if (concept.value() > maxProb) {
                    maxProb = concept.value();
                    tagMostLikely = concept.name();
                }
            }
            if (tagMostLikely != "") {
                OkHttpClient client = new OkHttpClient();
                String usdaAPI = "R9oOFUARnDJIfMT0g9f5gzhYAtnXQDzkRlzy1sYV";
                String url = "https://api.nal.usda.gov/ndb/search/?format=json&q=" + tagMostLikely + "&sort=n&max=25&offset=0&api_key=" + usdaAPI;
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                Response resp;
                try {
                    resp = client.newCall(request).execute();
                    String Response = resp.body().string();
                    int a = 5;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    // Instantiate the RequestQueue.

    private void setListeners() {
        mCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
    }

    static final int REQUEST_TAKE_PHOTO = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mImageView.setImageBitmap(imageBitmap);

            String extStorageDirectory = Environment.getExternalStorageDirectory().getAbsolutePath().toString();
            OutputStream outStream = null;
            //File file = new File(extStorageDirectory, "temp.jpg");
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File file = new File(imageFileName);
            if (file.exists()) {
                file.delete();
                Log.e("file exist", "" + file + ",Bitmap= ");
            }
            file = new File(extStorageDirectory, imageFileName);
            try {
                // make a new bitmap from your file
                outStream = new FileOutputStream(file);
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                outStream.flush();
                outStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            Uri savedImageURI = Uri.parse(file.getAbsolutePath());

            mImageViewJPG.setImageURI(savedImageURI);

            String APIKEY = "e14859a3ccae40a095a45a2fde6c8d57";
            final ClarifaiClient client = new ClarifaiBuilder(APIKEY).buildSync();


            Model<Concept> generalModel = client.getDefaultModels().foodModel();

            PredictRequest<Concept> request = generalModel.predict().withInputs(
                    ClarifaiInput.forImage(new File(file.getAbsolutePath()))
            );
            //ClarifaiRequest.OnFailure onFailure = null;
            request.executeAsync(onSuccess);
        }
    }

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }


}

