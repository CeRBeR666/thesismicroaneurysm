package com.agaoglu.tez;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

public class analiz extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private String selectedImagePath;
    private Mat sampledImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analiz);

        //Bug Fix alttaki fonksiyon tamamiyle saçmalık 3_2_0 versiyonunda opencv nin çalışması için eklenmiş bir kod
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
        //Bug Fix


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.analiz, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    private void loadImage(String path) {
        Mat originalImage = Imgcodecs.imread(path);
        Mat rgbImage = new Mat();

        Imgproc.cvtColor(originalImage,rgbImage,Imgproc.COLOR_BGR2RGB);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        sampledImage = new Mat();
        Size ratio = new Size(width,height);

        Imgproc.resize(rgbImage,sampledImage,ratio,0,0,Imgproc.INTER_AREA);
        displayImage(sampledImage);
    }

    private void displayImage(Mat image) {
        Bitmap bitmap = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(image, bitmap);

        ImageView iv = (ImageView) findViewById(R.id.IODarkRoomImageView);
        iv.setImageBitmap(bitmap);
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case LoaderCallbackInterface.SUCCESS:{
                    Log.i("OpencvLoaded ?","Opencv Yüklendi");



                    Uri selectedImageUri = Uri.parse("content://media/external/images/media/222");
                    selectedImagePath = Util.getPath(analiz.this,selectedImageUri);
                    loadImage(selectedImagePath);

                    Mat gray = new Mat();
                    Imgproc.cvtColor(sampledImage,gray,Imgproc.COLOR_BGR2GRAY);
                    Imgproc.blur(gray,gray,new Size(3,3));


                    Mat edges = new Mat();
                    double lovthreshold = 18;
                    double highthreshold = 32;

                    Imgproc.Canny(gray,edges,lovthreshold,highthreshold);

                    Mat lines = new Mat();
                    Imgproc.HoughLinesP(edges, lines, 1, Math.PI / 180 , 50, 50 ,10);

                    for (int i =0; i < lines.cols(); i++){
                        double[] val = lines.get(0,i);
                        Imgproc.line(edges, new org.opencv.core.Point(val[0],val[1]), new org.opencv.core.Point(val[2],val[3]), new Scalar(0,0,255), 2);
                    }

                    Mat nolines = new Mat();
                    Core.subtract(gray,edges,nolines);
                    //displayImage(nolines);

                    Mat circles = new Mat();
                    int minRadius = 8;
                    int maxRadius = 19;
                    Imgproc.HoughCircles(nolines,circles, Imgproc.HOUGH_GRADIENT, 1 , minRadius, 120 , 10 , minRadius, maxRadius);
                    Log.d("deneme",circles.toString());

                    for (int j =0; j < circles.cols(); j++){
                        double circle[] = circles.get(0,j);
                        org.opencv.core.Point pt = new org.opencv.core.Point(Math.round(circle[0]),Math.round(circle[1]));
                        int radius = (int) Math.round(circle[2]);

                        Imgproc.circle(sampledImage, pt, radius, new Scalar(0,255,0), 1);
                        Imgproc.circle(sampledImage, pt, 3, new Scalar(0,0,255), 1);
                    }
                    displayImage(sampledImage);

                }break;
                default:
                {
                    super.onManagerConnected(status);
                }break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0,this,mLoaderCallback);
    }
}
