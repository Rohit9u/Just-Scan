package com.example.scanner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    final static String apiKey = "AIzaSyCHTD1XbQ7TJ1_SElAhwZEOh5bAljfVScI";
    final static String cx = "25a255609c5db63d5";
    static String overAll="";
    static String result = null;
    Integer responseCode = null;
    String responseMessage = "";
    final static String searchLink="https://www.google.com/search?q=";
    final static  String searchURL = "https://www.googleapis.com/customsearch/v1?";
    private int REQUEST_CODE_PERMISSIONS = 101;
    private String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA",
            "android.permission.WRITE_EXTERNAL_STORAGE"};
    TextureView textureView;
    Bitmap rotatedBitmap;
    ImageView imageView;
    RelativeLayout layout;
    TextView extText;
    String pathFile;
    static String text="";
    ImageView imgcng;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imgcng=findViewById(R.id.change);
        imgcng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setVisibility(View.INVISIBLE);
                textureView.setVisibility(View.VISIBLE);
                overAll="";
                text="";
                extText.setText("");
            }
        });
        extText=findViewById(R.id.textView);
        getSupportActionBar().hide();
        layout=findViewById(R.id.relativeLayout);
        textureView = findViewById(R.id.view_finder);
        imageView=findViewById(R.id.imageView);
        if(allPermissionGranted()){
            startCamera();
        }
        else{

            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    private void startCamera() {

        CameraX.unbindAll();

        Rational aspectRatio = new Rational(layout.getWidth(), layout.getHeight());
        Size screen = new Size(textureView.getWidth(), textureView.getHeight());

        PreviewConfig pConfig = new PreviewConfig.Builder().setTargetAspectRatio(aspectRatio).setTargetResolution(screen).build();
        Preview preview = new Preview(pConfig);

        preview.setOnPreviewOutputUpdateListener(
                new Preview.OnPreviewOutputUpdateListener() {
                    @Override
                    public void onUpdated(Preview.PreviewOutput output) {
                        ViewGroup parent = (ViewGroup) textureView.getParent();
                        parent.removeView(textureView);
                        parent.addView(textureView, 0);
                        textureView.setSurfaceTexture(output.getSurfaceTexture());
                    }
                });

        ImageCaptureConfig imageCaptureConfig = new ImageCaptureConfig.Builder().setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation()).build();
        final ImageCapture imgCap = new ImageCapture(imageCaptureConfig);


        findViewById(R.id.imgCapture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(Environment.getExternalStorageDirectory().toString() + "/Pictures", "CameraX_" + System.currentTimeMillis() + ".jpg");
                imgCap.takePicture(file, new ImageCapture.OnImageSavedListener() {
                    @Override
                    public void onImageSaved(@NonNull File file) {
                        textureView.setVisibility(View.INVISIBLE);
                        imageView.setVisibility(View.VISIBLE);
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 8;
                        pathFile=file.getAbsolutePath();
                        final Bitmap b = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
                        rotateImage(b);
                    }

                    @Override
                    public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {
                        String msg = "Pic capture failed : " + message;
                        Toast.makeText(getBaseContext(), msg,Toast.LENGTH_LONG).show();
                        if(cause != null){
                            cause.printStackTrace();
                        }
                    }
                });
            }
        });

        CameraX.bindToLifecycle((LifecycleOwner)this, preview, imgCap);
    }
    private void displayTextFromImage(FirebaseVisionText firebaseVisionText){
        List<FirebaseVisionText.Block> blockList=firebaseVisionText.getBlocks();
        String stex="";
        if(blockList.size()==0){
            Toast.makeText(this,"No text found in image",Toast.LENGTH_SHORT).show();
        }
        else{
            for(FirebaseVisionText.Block block: firebaseVisionText.getBlocks()){
                text=text+block.getText()+" "+"\n";
            }
            text=text.replace(" ","+");
            String prr[]=text.split("\n");
                for(int i=0;i<prr.length;i++){
                    if(i!=prr.length-2 && i!=prr.length-1){
                        stex+=prr[i]+" ";
                    }
                }
            makeSearchString(stex);
                stex="";
            for(int i=0;i<prr.length;i++){
                if(i!=prr.length-3 && i!=prr.length-1){
                    stex+=prr[i]+" ";
                }
            }
            makeSearchString(stex);
            stex="";
            for(int i=0;i<prr.length;i++){
                if(i!=prr.length-3 && i!=prr.length-2){
                    stex+=prr[i]+" ";
                }
            }
            makeSearchString(stex);

        }
    };
    private void rotateImage(Bitmap bitmap){
        ExifInterface exifInterface=null;
        try{
            exifInterface=new ExifInterface(pathFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation=exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_UNDEFINED);
        Matrix matrix=new Matrix();
        switch (orientation){
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(90);
                break;
            default:
        }
         rotatedBitmap=Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
        imageView.setImageBitmap(rotatedBitmap);
        FirebaseVisionImage firebaseVisionImage=FirebaseVisionImage.fromBitmap(rotatedBitmap);
        FirebaseVisionTextDetector firebaseVisionTextDetector= FirebaseVision.getInstance().getVisionTextDetector();
        firebaseVisionTextDetector.detectInImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                displayTextFromImage(firebaseVisionText);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull  Exception e) {
                Toast.makeText(MainActivity.this,e.getMessage().toLowerCase(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_CODE_PERMISSIONS){
            if(allPermissionGranted()){
                startCamera();
            } else{
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void makeSearchString(String qSearch) {
        String urlString = "https://www.googleapis.com/customsearch/v1?q=" + qSearch + "&key=" + apiKey + "&cx=" + cx + "&alt=json";
        URL url=null;
        try{
            url=new URL(urlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        GoogleSearchAsyncTask task=new GoogleSearchAsyncTask();
        task.execute(url);
    }
    private boolean allPermissionGranted() {

        for(String permission : REQUIRED_PERMISSIONS){

            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){

                return false;
            }
        }
        return true;
    }
    private class GoogleSearchAsyncTask extends AsyncTask<URL,Integer,String>{

        @Override
        protected String doInBackground(URL... urls) {
            URL url=urls[0];
            HttpURLConnection conn=null;
            try{
                conn=(HttpURLConnection)url.openConnection();
            } catch (IOException e) {
                Log.e("tag","Http connection ERROR "+e.toString());
            }
            try{
             responseCode=conn.getResponseCode();
             responseMessage=conn.getResponseMessage();
            } catch (IOException e) {
                Log.e("tag","Http getting response ERROR "+e.toString());
            }
            try{
                if(responseCode!=null && responseCode==200){
                    BufferedReader rd=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb=new StringBuilder();
                    String line;
                    while((line=rd.readLine())!=null ){
                        if(line.indexOf("formattedTotalResults")>=0){
                            sb.append(line+"\n");
                            sb.append(rd.readLine());
                        }

                    }
                    rd.close();
                    conn.disconnect();
                    result=sb.toString();
                    return result;
                }else{
                    String errorMessage="Http Error response "+responseMessage+"\n"+"Are you online ? ";
                    Log.e("tag",errorMessage);
                    result=errorMessage;
                    return result;
                }
            } catch (IOException e) {
                Log.e("tag","Http response error"+e.toString());
            }
            return null;
        }
        protected void onPostExecute(String result){
                   overAll+=result;
                   extText.setText(overAll);

        }
    }
}