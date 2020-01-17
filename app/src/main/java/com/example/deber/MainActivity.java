package com.example.deber;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.TextAnnotation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.spec.ECField;
import java.text.SimpleDateFormat;
import java.util.Arrays;
public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    Button btnProgress;

    Bitmap eyePatchBitmap;
    Bitmap flowerLine;
    Canvas canvas;

    Paint rectPaint = new Paint();
    public Vision vision;
    private final String ruta_fotos = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/misfotos/";
    private File file = new File(ruta_fotos);
    private Button boton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Vision.Builder visionBuilder = new Vision.Builder(new NetHttpTransport(),
                new AndroidJsonFactory(), null);
        visionBuilder.setVisionRequestInitializer(new VisionRequestInitializer("AIzaSyCK1TEvOcxfbuXPd6HP2AOMEkQQ5D3bXRQ"));

        vision = visionBuilder.build();
        imageView = (ImageView)findViewById(R.id.imageView);
        btnProgress = (Button)findViewById(R.id.btnProgress);

        final Bitmap myBitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.tom);
        imageView.setImageBitmap(myBitmap);

        eyePatchBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.eye_patch);
        flowerLine = BitmapFactory.decodeResource(getResources(),R.drawable.flower);

        rectPaint.setStrokeWidth(5);
        rectPaint.setColor(Color.WHITE);
        rectPaint.setStyle(Paint.Style.STROKE);

        final Bitmap tempBitmap = Bitmap.createBitmap(myBitmap.getWidth(),myBitmap.getHeight(), Bitmap.Config.RGB_565);
        canvas  = new Canvas(tempBitmap);
        canvas.drawBitmap(myBitmap,0,0,null);

        btnProgress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FaceDetector faceDetector = new FaceDetector.Builder(getApplicationContext())
                        .setTrackingEnabled(false)
                        .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                        .setMode(FaceDetector.FAST_MODE)
                        .build();
                if(!faceDetector.isOperational())
                {
                    Toast.makeText(MainActivity.this, "Face Detector could not be set up on your device", Toast.LENGTH_SHORT).show();
                    return;
                }
                Frame frame = new Frame.Builder().setBitmap(myBitmap).build();
                SparseArray<Face> sparseArray = faceDetector.detect(frame);

                for(int i=0;i<sparseArray.size();i++)
                {
                    Face face = sparseArray.valueAt(i);
                    float x1=face.getPosition().x;
                    float y1 =face.getPosition().y;
                    float x2 = x1+face.getWidth();
                    float y2=y1+face.getHeight();
                    RectF rectF = new RectF(x1,y1,x2,y2);
                    canvas.drawRoundRect(rectF,2,2,rectPaint);



                }

                imageView.setImageDrawable(new BitmapDrawable(getResources(),tempBitmap));

            }
        });


        }



    public void clickboton(View view)
    {
        AsyncTask.execute(new Runnable() {

            @Override

            public void run() {
                ImageView Imagen=findViewById(R.id.imageView2);
                BitmapDrawable drawable = (BitmapDrawable) Imagen.getDrawable();
                Bitmap bitmap = drawable.getBitmap();
// reducimos a 1200*1200
                bitmap = scaleBitmapDown(bitmap, 1200);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                //calidad de imagen
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                byte[] imageInByte = stream.toByteArray();
                //1-importar clase google de visio
                Image inputImage = new Image();
                inputImage.encodeContent(imageInByte);

                //2
                Feature desiredFeature = new Feature();
                desiredFeature.setType("TEXT_DETECTION");
                //3 arma soliciyufes
                AnnotateImageRequest request = new AnnotateImageRequest();
                request.setImage(inputImage);

                request.setFeatures(Arrays.asList(desiredFeature));

                BatchAnnotateImagesRequest batchRequest = new BatchAnnotateImagesRequest();
                batchRequest.setRequests(Arrays.asList(request));

                //asiganmos al conttrol del visio
                try{
                    Vision.Images.Annotate  annotateRequest = vision.images().annotate(batchRequest);
                    annotateRequest.setDisableGZipContent(true);

                    BatchAnnotateImagesResponse batchResponse =  annotateRequest.execute();

                    TextAnnotation text = batchResponse.getResponses().get(0).getFullTextAnnotation();

                    final String result=text.getText();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            TextView imageDetail = (TextView) findViewById(R.id.textView2);
                            imageDetail.setText(result);
                        }
                    });

                }catch (Exception e){

                }

            }

        });

    }




    //comprime la imagen
    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;
        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }





}