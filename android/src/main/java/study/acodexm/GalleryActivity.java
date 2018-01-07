package study.acodexm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import acodexm.panorama.R;

public class GalleryActivity extends Activity {

    public static final String INTENT_EXTRAS_POSITION = "position";
    public static final String INTENT_EXTRAS_FOLDER = "folder";

    private static final String TAG = GalleryActivity.class.getName();

    ViewFlipper viewFlipper;
    ImageView currentView;

    String imagesFolder;
    List<String> imagesPath;

    int current;
    boolean fromAssets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.gallery);
        viewFlipper = (ViewFlipper) findViewById(R.id.gallery_viewflipper);

        current = 0;
        imagesFolder = "file:///android_asset/gallery";

        fromAssets = false;

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
                previusImage();
            }
        }));

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (currentView != null) {
            return currentView.onTouchEvent(event);
        } else {
            return super.onTouchEvent(event);
        }
    }

    float currentScale = 1;

    private void loadImages() {

        Bundle extras = getIntent().getExtras();
        imagesPath = new LinkedList<String>();
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
                Log.e(TAG, "loadImages [" + e + "]");
            }


        }

        Log.d(TAG, "load images from imagesFolder[" + imagesFolder + "]");

        if (imagesFolder != null) {
            File file = new File(imagesFolder);

            Log.d(TAG, "loadImages file exist[" + file.exists() + "]");
            Log.d(TAG, "loadImages file id folder[" + file.isDirectory()
                    + "]");

            if (imagesFolder.contains("file:///android_asset/")) {
                imagesFolder = imagesFolder.replace(
                        "file:///android_asset/", "");

                fromAssets = true;

                try {
                    String[] list = getAssets().list(imagesFolder);
                    for (int i = 0; i < list.length; i++) {
                        imagesPath.add(list[i]);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "doInBackground [" + e + "]");
                }

            } else {
                if (file.exists() && file.isDirectory()) {
                    File[] listFiles = file.listFiles();

                    for (File fileCurrent : listFiles) {
                        Log.d(TAG,
                                "loadImages added file["
                                        + fileCurrent.getPath() + "]");

                        imagesPath.add(fileCurrent.getPath());
                    }
                }
            }

            Log.d(TAG, "loadImages images count [" + imagesPath.size() + "]");
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

            new AlertDialog.Builder(this)
                    .setMessage("Nessuna Immagine Disponibile")
                    .setPositiveButton("Ok", new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).create().show();

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

    private void previusImage() {

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

    public String getImagesFolder() {
        return imagesFolder;
    }

    public void setImagesFolder(String imagesFolder) {
        this.imagesFolder = imagesFolder;
    }

    public static ImageView createImageView(Activity activity) {

        ImageView imageView = new ImageView(activity);
        imageView.setLayoutParams(new GridView.LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        // imageView.setPadding(8, 8, 8, 8);

        return imageView;
    }

    public void loadImageInView(ImageView imageView, String path) {
        try {
            Bitmap decodeStream = null;
            if (!fromAssets) {

                FileInputStream fileInputStream = new FileInputStream(path);
                decodeStream = BitmapFactory.decodeStream(fileInputStream);

            } else {
                try {
                    InputStream inputStream = getAssets().open(
                            imagesFolder + "/" + path);
                    decodeStream = BitmapFactory.decodeStream(inputStream);
                } catch (IOException e) {
                    Log.e(TAG, "doInBackground [" + e + "]");
                }
            }

            imageView.setImageBitmap(decodeStream);
            Log.d(TAG, "getView add image view[" + imageView + "]");

        } catch (FileNotFoundException e) {
            Log.e(TAG, "doInBackground [" + e + "]");
        }
    }

    public static void setFadeAnimation(ViewFlipper flipper, Activity context) {
        flipper.setInAnimation(AnimationUtils.loadAnimation(context,
                android.R.anim.fade_in));
        flipper.setOutAnimation(AnimationUtils.loadAnimation(context,
                android.R.anim.fade_out));
    }

    public static void setSlideToRightAnimation(ViewFlipper flipper,
                                                Activity context) {
        flipper.setInAnimation(AnimationUtils.loadAnimation(context,
                R.anim.slide_in_left));
        flipper.setOutAnimation(AnimationUtils.loadAnimation(context,
                R.anim.slide_out_right));
    }

    public static void setSlideToLeftAnimation(ViewFlipper flipper,
                                               Activity context) {
        flipper.setInAnimation(AnimationUtils.loadAnimation(context,
                R.anim.slide_in_right));
        flipper.setOutAnimation(AnimationUtils.loadAnimation(context,
                R.anim.slide_out_left));
    }

    public static class SwipeListener implements OnTouchListener {

        private static final int SWIPE_LENGHT = 30;

        private boolean isDragged;
        private boolean isPinch;

        float startX;
        float startY;

        public static float pinch;

        private Runnable onSwipeLeft;
        private Runnable onSwipeRight;
        private Runnable onPositivePinch;
        private Runnable onNegativePinch;

        public SwipeListener(Runnable onSwipeRight, Runnable onSwipeLeft) {
            super();
            this.onSwipeLeft = onSwipeLeft;
            this.onSwipeRight = onSwipeRight;
        }

        public SwipeListener(Runnable onSwipeRight, Runnable onSwipeLeft,
                             Runnable onPositivePinch, Runnable onNegativePinch) {
            super();
            this.onSwipeLeft = onSwipeLeft;
            this.onSwipeRight = onSwipeRight;
            this.onPositivePinch = onPositivePinch;
            this.onNegativePinch = onNegativePinch;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {

                Log.d(TAG, "onTouch [ACTION DOWN]");

                isDragged = false;
                isPinch = false;

                startX = event.getX();
                startY = event.getY();

            } else if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN) {

                ((ViewFlipper) v).getChildAt(0).onTouchEvent(event);

            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {

                // isDragged = true;
                //
                // if (isPinch) {
                //
                // float x = event.getX();
                //
                // if (startX < x) {
                // onPositivePinch.run();
                // } else if (startX > x) {
                // onNegativePinch.run();
                // }
                //
                // }

            } else if (event.getAction() == MotionEvent.ACTION_UP) {

                float x = event.getX();

                if (!isPinch) {
                    if (x > startX && Math.abs(x - startX) > SWIPE_LENGHT)
                        onSwipeLeft.run();

                    if (x < startX && Math.abs(x - startX) > SWIPE_LENGHT)
                        onSwipeRight.run();

                }

            } else {

            }

            return true;

        }
    }

    public static class PinchImageView extends android.support.v7.widget.AppCompatImageView implements
            OnTouchListener {

        public static final int GROW = 0;
        public static final int SHRINK = 1;

        public static final int TOUCH_INTERVAL = 0;

        public static final float MIN_SCALE = 1f;
        public static final float MAX_SCALE = 4f;
        public static final float ZOOM = 1.5f;

        ImageView im = null;
        Matrix mBaseMatrix = new Matrix(), mSuppMatrix = new Matrix(),
                mResultMatrix = new Matrix();
        Bitmap mBitmap = null;

        float xCur, yCur, xPre, yPre, xSec, ySec, distDelta, distCur,
                distPre = -1;
        int mWidth, mHeight, mTouchSlop;
        long mLastGestureTime;

        public PinchImageView(Context context, AttributeSet attr) {
            super(context, attr);
            _init();
        }

        public PinchImageView(Context context) {
            super(context);
            _init();
        }

        public PinchImageView(ImageView im) {
            super(im.getContext());
            _init();
            this.im = im;
            this.im.setScaleType(ScaleType.MATRIX);
            this.im.setOnTouchListener(this);
        }

        public float getScale() {
            return getScale(mSuppMatrix);
        }

        public float getScale(Matrix matrix) {
            float[] values = new float[9];
            matrix.getValues(values);
            return values[Matrix.MSCALE_X];
        }

        public void setImageBitmap(Bitmap bm) {
            super.setImageBitmap(bm);
            Drawable d = getDrawable();
            if (d != null) {
                d.setDither(true);
            }
            mBitmap = bm;
            center(true, true);
        }

        protected void onLayout(boolean changed, int left, int top, int right,
                                int bottom) {
            super.onLayout(changed, left, top, right, bottom);
            mWidth = right - left;
            mHeight = bottom - top;

            if (mBitmap != null) {
                getProperBaseMatrix(mBaseMatrix);
                setImageMatrix(getImageViewMatrix());
            }
        }

        public boolean onTouchEvent(MotionEvent event) {
            int action = event.getAction() & MotionEvent.ACTION_MASK, p_count = event
                    .getPointerCount();

            Log.d(TAG,
                    "PinchImageView onTouchEvent action[" + event.getAction()
                            + "]");
            Log.d(TAG,
                    "PinchImageView onTouchEvent pointer count["
                            + event.getPointerCount() + "]");

            switch (action) {
                case MotionEvent.ACTION_MOVE:

                    // point 1 coords
                    xCur = event.getX(0);
                    yCur = event.getY(0);
                    if (p_count > 1) {
                        // point 2 coords
                        xSec = event.getX(1);
                        ySec = event.getY(1);

                        // distance between
                        distCur = (float) Math.sqrt(Math.pow(xSec - xCur, 2)
                                + Math.pow(ySec - yCur, 2));
                        distDelta = distPre > -1 ? distCur - distPre : 0;

                        float scale = getScale();
                        long now = android.os.SystemClock.uptimeMillis();
                        if (Math.abs(distDelta) > mTouchSlop) {
                            mLastGestureTime = 0;

                            // ScaleAnimation scale = null;
                            int mode = distDelta > 0 ? GROW
                                    : (distCur == distPre ? 2 : SHRINK);
                            switch (mode) {
                                case GROW: // grow
                                    zoomIn(scale);
                                    break;
                                case SHRINK: // shrink
                                    zoomOut(scale);
                                    break;
                            }

                            mLastGestureTime = now;
                            xPre = xCur;
                            yPre = yCur;
                            distPre = distCur;
                            return true;
                        }
                    }

                    xPre = xCur;
                    yPre = yCur;
                    distPre = distCur;
                    break;
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_UP:
                    distPre = -1;
                    mLastGestureTime = android.os.SystemClock.uptimeMillis();
                    break;
            }

            return false;
        }

        private void _init() {
            im = this;
            mTouchSlop = ViewConfiguration.getTouchSlop();
            im.setScaleType(ScaleType.MATRIX);
            center(true, true);
        }

        public boolean onTouch(View v, MotionEvent event) {
            return this.onTouchEvent(event);
        }

        public void zoomMax() {
            zoomTo(MAX_SCALE);
        }

        public void zoomMin() {
            zoomTo(MIN_SCALE);
        }

        public synchronized void postTranslate(float dx, float dy) {
            mSuppMatrix.postTranslate(dx, dy);
        }

        public void center(boolean horizontal, boolean vertical) {
            if (mBitmap == null) {
                return;
            }

            Matrix m = getImageViewMatrix();
            RectF rect = new RectF(0, 0, mBitmap.getWidth(),
                    mBitmap.getHeight());

            m.mapRect(rect);

            float height = rect.height(), width = rect.width(), deltaX = 0, deltaY = 0;

            if (vertical) {
                int viewHeight = getHeight();
                if (height < viewHeight) {
                    deltaY = (viewHeight - height) / 2f - rect.top;
                } else if (rect.top > 0) {
                    deltaY = -rect.top;
                } else if (rect.bottom < viewHeight) {
                    deltaY = getHeight() - rect.bottom;
                }

            }

            if (horizontal) {
                int viewWidth = getWidth();
                if (width < viewWidth) {
                    deltaX = (viewWidth - width) / 2 - rect.left;

                } else if (rect.left > 0) {
                    deltaX = -rect.left;
                } else if (rect.right < viewWidth) {
                    deltaX = viewWidth - rect.right;
                }
            }

            postTranslate(deltaX, deltaY);
            setImageMatrix(getImageViewMatrix());
        }

        protected Matrix getImageViewMatrix() {
            mResultMatrix.set(mBaseMatrix);
            mResultMatrix.postConcat(mSuppMatrix);
            return mResultMatrix;
        }

        protected void zoomTo(float scale) {
            mSuppMatrix.setScale(scale, scale, getWidth() / 2f,
                    getHeight() / 2f);
            setImageMatrix(getImageViewMatrix());
            center(true, true);
        }

        protected void zoomIn(float scale) {

            if (scale > MAX_SCALE)
                return;

            Log.d(TAG, "zoomIn scale[" + scale + "]");

            mSuppMatrix
                    .postScale(ZOOM, ZOOM, getWidth() / 2f, getHeight() / 2f);
            setImageMatrix(getImageViewMatrix());
        }

        protected void zoomOut(float scale) {
            if (scale < MIN_SCALE)
                return;

            Log.d(TAG, "zoomOut scale[" + scale + "]");

            float cx = getWidth() / 2f, cy = getHeight() / 2f, diff = 1f / ZOOM;

            Matrix tmp = new Matrix(mSuppMatrix);
            tmp.postScale(diff, diff, cx, cy);

            if (getScale(tmp) < MIN_SCALE) {
                mSuppMatrix.setScale(MIN_SCALE, MIN_SCALE, cx, cy);
            } else {
                mSuppMatrix.postScale(diff, diff, cx, cy);
            }

            setImageMatrix(getImageViewMatrix());
            center(true, true);
        }

        private void getProperBaseMatrix(Matrix matrix) {
            float viewWidth = getWidth(), viewHeight = getHeight();

            float w = mBitmap.getWidth(), h = mBitmap.getHeight();

            matrix.reset();

            float widthScale = Math.min(viewWidth / w, MAX_SCALE), heightScale = Math
                    .min(viewHeight / h, MAX_SCALE), scale = Math.min(
                    widthScale, heightScale);

            Matrix bitmapMatrix = new Matrix();
            bitmapMatrix.preTranslate(-(mBitmap.getWidth() >> 1),
                    -(mBitmap.getHeight() >> 1));
            bitmapMatrix.postTranslate(getWidth() / 2, getHeight() / 2);

            matrix.postConcat(bitmapMatrix);
            matrix.postScale(scale, scale);
            matrix.postTranslate((viewWidth - w * scale) / 2F, (viewHeight - h
                    * scale) / 2F);
        }

    }
}