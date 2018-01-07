package study.acodexm.gallery;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;

import acodexm.panorama.R;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GalleryActivity extends Activity {

    public static final String INTENT_EXTRAS_POSITION = "position";
    public static final String INTENT_EXTRAS_FOLDER = "folder";
    private static final String TAG = GalleryActivity.class.getName();
    private ViewFlipper viewFlipper;
    private ImageView currentView;
    private String imagesFolder;
    private List<String> imagesPath;
    private int current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.gallery);
        ButterKnife.bind(this);
        viewFlipper = findViewById(R.id.gallery_view_flipper);
        current = 0;

        loadImages();
        start();

        viewFlipper.setOnTouchListener(new SwipeListener(new Runnable() {
            @Override
            public void run() {
                nextImage();
            }
        }, new Runnable() {
            @Override
            public void run() {
                previousImage();
            }
        }));

    }

    public ImageView createImageView(Activity activity) {

        ImageView imageView = new ImageView(activity);
        imageView.setLayoutParams(new GridView.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        return imageView;
    }


    public void setSlideToRightAnimation(ViewFlipper flipper,
                                         Activity context) {
        flipper.setInAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_in_left));
        flipper.setOutAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_out_right));
    }

    public void setSlideToLeftAnimation(ViewFlipper flipper,
                                        Activity context) {
        flipper.setInAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_in_right));
        flipper.setOutAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_out_left));
    }

    @OnClick(R.id.delete_picture)
    void onTrashClickListener() {
        File picToTrash = new File(imagesPath.get(current));
        if (picToTrash.delete()) {
            imagesPath.remove(current);
            nextImage();
        } else {
            Log.d(TAG, "onTrashClickListener: failed to delete file!");
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (currentView != null) {
            return currentView.onTouchEvent(event);
        } else {
            return super.onTouchEvent(event);
        }
    }

    private void loadImages() {

        Bundle extras = getIntent().getExtras();
        imagesPath = new LinkedList<>();
        int position = 0;

        if (extras != null) {

            String extrasFolder = extras.getString(INTENT_EXTRAS_FOLDER);

            if (extrasFolder != null) {
                imagesFolder = extrasFolder;
            }

            String positionSt = extras.getString(INTENT_EXTRAS_POSITION);

            try {
                position = Integer.parseInt(positionSt);
            } catch (NumberFormatException e) {
                Log.e(TAG, "loadImages " + e);
            }


        }

        Log.d(TAG, "load images from imagesFolder:" + imagesFolder);

        if (imagesFolder != null) {
            File file = new File(imagesFolder);

            Log.d(TAG, "loadImages file exist: " + file.exists());
            Log.d(TAG, "loadImages file id folder: " + file.isDirectory());


            if (file.exists() && file.isDirectory()) {
                File[] listFiles = file.listFiles();

                for (File fileCurrent : listFiles) {
                    if (fileCurrent.isFile()) {
                        Log.d(TAG, "loadImages added file:" + fileCurrent.getPath());
                        imagesPath.add(fileCurrent.getPath());
                    }
                }
            }


            Log.d(TAG, "loadImages images count :" + imagesPath.size());
            if (position < imagesPath.size()) {
                current = position;
            }

        }

    }

    private void start() {

        if (imagesPath.size() > 0) {

            // load firs image
            currentView = createImageView(this);
            viewFlipper.addView(currentView);
            loadImageInView(currentView, imagesPath.get(current));

            viewFlipper.setDisplayedChild(0);
        } else {
            Toast.makeText(this, "No images found", Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
    }

    private void nextImage() {

        current++;
        if (current == imagesPath.size())
            current = 0;
        setSlideToLeftAnimation(viewFlipper, this);

        // load firs image
        currentView = createImageView(this);
        viewFlipper.addView(currentView);
        loadImageInView(currentView, imagesPath.get(current));

        viewFlipper.showNext();
        viewFlipper.removeViewAt(0);

    }

    private void previousImage() {

        current--;
        if (current == -1)
            current = imagesPath.size() - 1;
        setSlideToRightAnimation(viewFlipper, this);

        // load firs image
        currentView = createImageView(this);
        viewFlipper.addView(currentView);
        loadImageInView(currentView, imagesPath.get(current));

        viewFlipper.showNext();
        viewFlipper.removeViewAt(0);

    }

    public void loadImageInView(ImageView imageView, String path) {
        try {
            Bitmap decodeStream;
            FileInputStream fileInputStream = new FileInputStream(path);
            decodeStream = BitmapFactory.decodeStream(fileInputStream);

            imageView.setImageBitmap(decodeStream);

        } catch (FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException " + e);
        }
    }


}