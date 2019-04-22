package com.example.mijan.camera;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.photo.CalibrateDebevec;
import org.opencv.photo.MergeDebevec;
import org.opencv.photo.Photo;
import org.opencv.photo.Tonemap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
    int REQUEST_IMAGE_CAPTURE = 1;
    Button HDRButton;
    Button StandardImage;
    Button ultraEN;
    Bitmap photo;
    File path = Environment.getExternalStorageDirectory();
    File image;
    File file;
    String subpath;
    File[] listOfFiles;
    String parentpath = path + "/DCIM/";
    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    String name;
    FileOutputStream fileOutputStream;
    public  static final String TAG = "MainActivity";
    String OriginalName = "";
    Boolean greyscale = false;
    Boolean enchance = false;
    Boolean hdr = false;
    Boolean ultraenhance = false;

    static
    {
        if(!OpenCVLoader.initDebug())
        {
            Log.d(TAG, "opencv not Loaded");
        }
        else
        {
            Log.d(TAG, "opencv loaded");
        }
    }

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AllPermissions();
        checkDirectory();

        StandardImage = findViewById(R.id.enhancedimage);
        StandardImage.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                enchance = true;
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        });

        ultraEN = findViewById(R.id.ultraenhance);
        ultraEN.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                ultraenhance = true;
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        });

        HDRButton = findViewById(R.id.hdrpic);
        HDRButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                hdr = true;
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        });
    }

    public void AllPermissions()
    {
        int AllPermissions = 2;
        int result;
        String[] permissions = new String[]
                {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                };
        List <String> AllPermissionsRequired = new ArrayList<>();
        for (String eachPermission : permissions)
        {
            result = ContextCompat.checkSelfPermission(this, eachPermission);
            if (result != PackageManager.PERMISSION_GRANTED)
            {
                AllPermissionsRequired.add(eachPermission);
            }
        }

        if (!AllPermissionsRequired.isEmpty())
        {
            ActivityCompat.requestPermissions(this, AllPermissionsRequired.toArray(new String[AllPermissionsRequired.size()]),
                    AllPermissions);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
            if (greyscale ==false && enchance == true)
            {
                getimg();
                increaseSaturation(1.13f);
                setUltraEN(-20);
                StoreIMG();
                clear();
            }

            if (greyscale ==false && ultraenhance == true)
            {
                getimg();
                ultraenhace();
                //sharpenBitmap(this, photo, 0.1f);
                StoreIMG();
                clear();
            }

            if (greyscale ==false && hdr == true)
            {
                hdr();
                clear();
            }
    }

    public void clear()
    {
        photo = null;
        image = null;
        file = null;
        greyscale = false;
        enchance = false;
        hdr = false;
        ultraenhance = false;
        listOfFiles = null;
        OriginalName = "";
    }

    public void checkDirectory()
    {
        File createDIR = new File(parentpath + "100ANDRO");
        if(createDIR.exists())
        {
            subpath = parentpath + "100ANDRO/";
        }
        else
        {
            subpath = parentpath + "Camera/";
        }
    }

    public static String getEXT(File file)
    {
        String filename = file.getName();
        if(filename.lastIndexOf(".") != -1 && filename.lastIndexOf(".") != 0 )
        {
            return filename.substring(filename.lastIndexOf(".")+1);
        }
        else
        {
            return "";
        }
    }

    public void getimg()
    {
        File createDIR = new File(parentpath + "VisionX");
        File folder = new File(subpath);
        listOfFiles = folder.listFiles();
        if(!createDIR.exists())
        {
            createDIR.mkdir();
        }

        for(int i = listOfFiles.length-1; i >= 0; i--)
        {
            if(getEXT(listOfFiles[i]).equals("JPG") || getEXT(listOfFiles[i]).equals("jpg")) {
                image = listOfFiles[i];
                name = image.getAbsolutePath();
                photo = BitmapFactory.decodeFile(name);
                break;
            }
        }
    }

    public void StoreIMG() {
        File parentDir = new File(parentpath + "VisionX");
        if (OriginalName.equals("")) {
            OriginalName = image.getName();
        }

        file = new File(parentDir.getAbsoluteFile() + File.separator + OriginalName);
        try {
            file.createNewFile();
        } catch (IOException e) {
        }
        try {
            fileOutputStream = new FileOutputStream(file);
            photo.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void increaseSaturation(float amount)
    {
        getimg();
        Bitmap bmpOriginal = photo.copy(Bitmap.Config.ARGB_8888, true);
        Canvas c = new Canvas(bmpOriginal);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(amount);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        photo = bmpOriginal;
    }

    public void ultraenhace()
    {
        getimg();
        Bitmap img = photo;
        Bitmap newBitmap = RenderScriptImageEdit.histogramEqualization(img, this);
        photo = newBitmap;
    }

    public void setUltraEN(int value)
    {
        int width = photo.getWidth();
        int height = photo.getHeight();
        Bitmap bmOut = Bitmap.createBitmap(width, height, photo.getConfig());
        int A, R, G, B;
        int pixel;
        for (int x = 0; x < width; ++x)
        {
          for (int y = 0; y < height; ++y)
          {
              pixel = photo.getPixel(x, y);
              A = Color.alpha(pixel);
              R = Color.red(pixel);
              G = Color.green(pixel);
              B = Color.blue(pixel);
              R += value;
              if (R > 255) { R = 255; }
              else if (R < 0) { R = 0; }
              G += value;
              if (G > 255) { G = 255; }
              else if (G < 0) { G = 0; }
              B += value;
              if (B > 255) { B = 255; }
              else if (B < 0) { B = 0; }
              bmOut.setPixel(x, y, Color.argb(A, R, G, B));
          }
        }
        photo = bmOut;
    }

    public void hdr()
    {
        getimg();

        Mat m1 = Imgcodecs.imread(image.getAbsolutePath());
        Mat m2 = Imgcodecs.imread(image.getAbsolutePath());
        Mat m3 = Imgcodecs.imread(image.getAbsolutePath());

        List<Mat> images = new ArrayList<>();
        images.add(m1);
        images.add(m2);
        images.add(m3);
        List<Float> times = new ArrayList<>();
        times.add(5.5f);
        times.add(1.4f);
        times.add(0.23f);

        Mat response = new Mat();
        CalibrateDebevec calibrate = Photo.createCalibrateDebevec();
        Mat matTimes = new Mat(times.size(), 1, CvType.CV_32F);
        float[] arrayTimes = new float[(int) (matTimes.total()*matTimes.channels())];
        for (int i = 0; i < times.size(); i++)
        {
            arrayTimes[i] = times.get(i);
        }
        matTimes.put(0, 0, arrayTimes);
        calibrate.process(images, response, matTimes);

        Mat hdr = new Mat();
        MergeDebevec mergeDebevec = Photo.createMergeDebevec();
        mergeDebevec.process(images, hdr, matTimes);

        Mat ldr = new Mat();
        Tonemap tonemap = Photo.createTonemap(1.628f);
        tonemap.process(hdr, ldr);
        OriginalName  = image.getName();
        Core.multiply(ldr, new Scalar(255,255,255), ldr);
        photo = Bitmap.createBitmap(ldr.cols(), ldr.rows(), Bitmap.Config.ARGB_8888);
        Imgcodecs.imwrite( parentpath + "VisionX/" + image.getName(), ldr);
    }
}
