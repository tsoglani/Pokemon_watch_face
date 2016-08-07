/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tsoglani.pokemon.watch_face.pokemonwatchface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.WindowInsets;
import android.view.WindowManager;

import com.github.kleinerhacker.android.gif.Gif;
import com.github.kleinerhacker.android.gif.GifFactory;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class MyWatchFace extends CanvasWatchFaceService {
    protected static boolean isChangingBackgoundByTouch, isBatteryVisible;
    protected static boolean isChangingAnimationByTouch;
    protected static boolean is24HourType = true;
    protected static boolean isDateVisible = false;
    protected String pokemonName = "pikachu";
    Gif myGif;
    float mainScaleX, mainScaleY;
    Bitmap originalBitmap;
    Bitmap resizedBitmap;
    ArrayList<Bitmap> listOfAnimationImages= new ArrayList<Bitmap>();
    ArrayList<String> listOfAnimationImagesLocation= new ArrayList<String>();
    Bitmap []animationBitmaps;
    int animationCounter = 0,animationNumber=0;
    int previousAnimationCounter=animationCounter,previousAnimationNumber;
    protected static boolean isEnableAnimation = true;

    private static final Typeface NORMAL_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

    /**
     * Update rate in milliseconds for interactive mode. We update once a second since seconds are
     * displayed in interactive mode.
     */
//    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
    private static final int MSG_UPDATE_TIME = 0;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private static class EngineHandler extends Handler {
        private final WeakReference<Engine> mWeakReference;

        public EngineHandler(MyWatchFace.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }


        @Override
        public void handleMessage(Message msg) {
            MyWatchFace.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }

    private class Engine extends CanvasWatchFaceService.Engine {
        final Handler mUpdateTimeHandler = new EngineHandler(this);
        boolean mRegisteredTimeZoneReceiver = false;
        //        Paint mBackgroundPaint;
        Paint mTextPaint;
        boolean mAmbient;
        Time mTime;
        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTime.clear(intent.getStringExtra("time-zone"));
                mTime.setToNow();
            }
        };
        int mTapCount;

        float mXOffset;
        float mYOffset;
        private Bitmap one_amb_bitmap;
        private Bitmap zero_amb_bitmap;
        private Bitmap two_amb_bitmap;
        private Bitmap three_amb_bitmap;
        private Bitmap four_amb_bitmap;
        private Bitmap five_amb_bitmap;
        private Bitmap six_amb_bitmap;
        private Bitmap seven_amb_bitmap;
        private Bitmap eight_amb_bitmap;
        private Bitmap nine_amb_bitmap;
        private Bitmap scaledZero_amb_bitmap;
        private Bitmap scaledOne_amb_bitmap;
        private Bitmap scaledTwo_amb_bitmap;
        private Bitmap scaledThree_amb_bitmap;
        private Bitmap scaledFour_amb_bitmap;
        private Bitmap mScaledFive_amb_bitmap;
        private Bitmap scaledEight_amb_bitmap;
        private Bitmap scaledNine_amb_bitmap;
        private Bitmap scaledSix_amb_bitmap;
        private Bitmap scaledSeven_amb_bitmap;
        private Bitmap batteryBitmap, batteryScaledBitmap, batteryBitmap_abc, batteryScaledBitmap_abc;


        ArrayList<Integer> backgroundList = new ArrayList<>();
        ArrayList<Integer> backgroundList_abc = new ArrayList<>();


        private Bitmap zero_bitmap;
        private Bitmap one_bitmap;
        private Bitmap two_bitmap;
        private Bitmap three_bitmap;
        private Bitmap four_bitmap;
        private Bitmap five_bitmap;
        private Bitmap six_bitmap;
        private Bitmap seven_bitmap;
        private Bitmap eight_bitmap;
        private Bitmap nine_bitmap;
        //        private Bitmap seconds_bitmap, seconds_bitmap_abc;
        private Bitmap scaledZero_bitmap;
        private Bitmap scaledOne_bitmap;
        private Bitmap scaledTwo_bitmap;
        private Bitmap scaledThree_bitmap;
        private Bitmap scaledFour_bitmap;
        private Bitmap scaledFive_bitmap;
        private Bitmap scaledSix_bitmap;
        private Bitmap scaledSeven_bitmap;
        private Bitmap scaledEight_bitmap;
        private Bitmap scaledNine_bitmap;
        private Bitmap blockScaledBitmap;
        private Bitmap blockBitmap, blockBitmap_abc, blockBitmap_abc_Scalled;
//        private Bitmap secondsBlockBitmapScalled, secondsBlockBitmapScalled_abc;


//        private Bitmap date_bitmap, date_amb_bitmap, dateBitmap_abc_Scalled,dateBitmap_Scalled;

        float blockStartX = 20;
        float blockStartY = 30;
        float numberStartY = 30;
        private Movie mMovie;
        java.io.InputStream is;
        Paint paint = new Paint();

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        boolean mLowBitAmbient;
        private Bitmap backgroundBitmap, backgroundBitmapScaled,backgroundBitmap_abc,backgroundBitmapScaled_abc;
        private ArrayList<Integer> animationList = new ArrayList<Integer>();
        PowerManager.WakeLock wl;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            initAnimImageList();
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
            isChangingBackgoundByTouch = Settings.getSharedPref(getApplicationContext(), Settings.CHANGE_BACKGROUND_ON_CLICK, true);
            isChangingAnimationByTouch = Settings.getSharedPref(getApplicationContext(), Settings.CHANGE_ANIMATION_ON_CLICK, false);
            is24HourType = Settings.getSharedPref(getApplicationContext(), Settings.HOUR_TYPE, true);
            isBatteryVisible = Settings.getSharedPref(getApplicationContext(), Settings.ENABLE_BATTERY, false);

            isDateVisible = Settings.getSharedPref(getApplicationContext(), Settings.DATE_TYPE, true);
            isEnableAnimation = Settings.getSharedPref(getApplicationContext(), Settings.ENABLE_ANIMATION, true);
            setWatchFaceStyle(new WatchFaceStyle.Builder(MyWatchFace.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .setAcceptsTapEvents(true)
                    .build());
            Resources resources = MyWatchFace.this.getResources();
//            mYOffset = resources.getDimension(R.dimen.digital_y_offset);
//            mBackgroundPaint = new Paint();
//            mBackgroundPaint.setColor(resources.getColor(R.color.background));

            mTextPaint = new Paint();
            mTextPaint = createTextPaint(resources.getColor(R.color.Teal));

            mTime = new Time();
            initBackgroundList();
            initBitmaps();
            initScalledBitmaps();
            initValues();
            initAnimationList();
            if (myGif == null)
                loadAnimation(animationList.get(0));
        }


        private void wakeLock() {
            try{
                wl.acquire();
            }catch (Exception e){
                e.printStackTrace();
            }

        }

        private void wakeUnlock() {
            try{
                wl.release();
            }catch (Exception e){
                e.printStackTrace();
            }

        }


        private void initAnimationList() {
            animationList.add(R.raw.one);
            animationList.add(R.raw.two);
            animationList.add(R.raw.three);
            animationList.add(R.raw.six);
            animationList.add(R.raw.seven);
            animationList.add(R.raw.eight);
            animationList.add(R.raw.thirty);
            animationList.add(R.raw.thirtysix);
            animationList.add(R.raw.forty);
            animationList.add(R.raw.nine);
            animationList.add(R.raw.ten);
            animationList.add(R.raw.eleven);
            animationList.add(R.raw.twelve);
            animationList.add(R.raw.fourteen);
            animationList.add(R.raw.sixteen);
            animationList.add(R.raw.fifteen);
            animationList.add(R.raw.seventeen);
            animationList.add(R.raw.four);
            animationList.add(R.raw.eighteen);
            animationList.add(R.raw.twenty);
            animationList.add(R.raw.twentyone);
            animationList.add(R.raw.twentytwo);
            animationList.add(R.raw.nineteen);
            animationList.add(R.raw.twentythree);
            animationList.add(R.raw.twentyfour);
            animationList.add(R.raw.twentysix);
            animationList.add(R.raw.twentyfive);
            animationList.add(R.raw.twentyseven);
            animationList.add(R.raw.thirtytwo);
            animationList.add(R.raw.thirtythree);
            animationList.add(R.raw.thirtyfour);
            animationList.add(R.raw.thirtyfive);
            animationList.add(R.raw.thirtyseven);
            animationList.add(R.raw.thirtyeight);
            animationList.add(R.raw.fourtythree);
            animationList.add(R.raw.fourtyfive);
            animationList.add(R.raw.fourtysix);
            animationList.add(R.raw.fourtyseven);
            animationList.add(R.raw.fourtyeight);
            animationList.add(R.raw.fourtynine);
            animationList.add(R.raw.fifty);


            animationList.add(R.raw.fiftyone);
            animationList.add(R.raw.fiftytwo);
            animationList.add(R.raw.fiftythree);
            animationList.add(R.raw.fiftyfour);
            animationList.add(R.raw.fiftysix);
            animationList.add(R.raw.fiftyeight);
            animationList.add(R.raw.fiftynine);
            animationList.add(R.raw.sixty);
            animationList.add(R.raw.sixtyone);
            animationList.add(R.raw.sixtytwo);
            animationList.add(R.raw.sixtythree);
            animationList.add(R.raw.five);
            animationList.add(R.raw.fourtyfour);
            animationList.add(R.raw.twentyeight);
            animationList.add(R.raw.twentynine);
            animationList.add(R.raw.thirtyone);
            animationList.add(R.raw.fiftyfive);
            animationList.add(R.raw.fiftyseven);
            animationList.add(R.raw.thirtynine);
            animationList.add(R.raw.thirteen);

        }

        boolean isLoaded = true;

        private void loadAnimation(final int id) {
            isLoaded = false;
            new Thread() {
                @Override
                public void run() {
                    try {
                        if (animationNumber  >= animationList.size()) {
                            animationNumber = 0;
                        }
                        myGif = GifFactory.decodeResource(getResources(), id);

                        myGif = Gif.createScaledGif(myGif, backgroundBitmap.getWidth(), backgroundBitmap.getHeight(), true);
                        animationBitmaps= new Bitmap[myGif.getFrames().length];
                        for (int i=0;i<animationBitmaps.length;i++){
                            animationBitmaps[i]=myGif.getFrames()[i].getImage();
                        }
                        isLoaded = true;
                    } catch (Exception | Error e) {
                        e.printStackTrace();
                        Log.e("error on", "image " + animationNumber);
                        if (animationNumber + 1 >= animationList.size()) {
                            animationNumber = 0;
                        }
                        animationNumber++;
                        loadAnimation(animationList.get(animationNumber));
                    }


                }
            }.start();


        }


        boolean isAnimationActivate = false;

        private void enableAnimation() {
            if (isAnimationActivate || !shouldTimerBeRunning() || !isLoaded) {
                return;
            }
            wakeLock();
            isAnimationActivate = true;
        }

        private void initAnimImageList(){


//   ani
//

            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.one))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.two))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.three))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.six))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.seven))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.eight))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.seven))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.eight))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.thirty))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.thirtysix))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.forty))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.nine))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.ten))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.eleven))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.twelve))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.fourteen))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.sixteen))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.fifteen))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.seventeen))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.four))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.eighteen))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.twenty))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.twentyone))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.twentytwo))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.nineteen))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.twentythree))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.twentyfour))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.twentysix))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.twentyfive))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.twentyseven))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.thirtytwo))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.thirtythree))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.thirtyfour))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.thirtyfive))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.thirtyseven))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.thirtyeight))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.fourtythree))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.fourtyfive))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.fourtysix))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.fourtyseven))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.fourtyeight))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.fourtynine))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.fifty))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.fiftyone))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.fiftytwo))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.fiftythree))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.fiftyfour))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.fiftysix))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.fiftyeight))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.fiftynine))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.sixty))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.sixtyone))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.sixtytwo))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.sixtythree))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.five))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.fourtyfour))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.twentyeight))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.twentynine))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.thirtyone))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.fiftyfive))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.fiftyseven))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.thirtynine))));
            listOfAnimationImages.add( createTrimmedBitmap(getScaledBitmap3(BitmapFactory.decodeResource(getResources(), R.drawable.thirteen))));




        }
        Bitmap getScaledBitmap3(Bitmap bitmap) {

            WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            Display display = window.getDefaultDisplay();
            int width = display.getWidth();
            int height = display.getHeight();

            return Bitmap.createScaledBitmap(bitmap,width,height, true);
        }

        private void playAnim(final Canvas canvas) {

            if (!isEnableAnimation) {
                if (animationCounter != 0) {
                    animationCounter = 0;
                }
                if (isAnimationActivate)
                    isAnimationActivate = false;
                previousAnimationCounter=animationCounter;
                return;
            }
//                    updateBrightness(255);
            if ((animationBitmaps == null) || !isLoaded) {
                return;
            }

            if (isAnimationActivate) {
                animationCounter++;
//                paint = null;
            }
            if (animationCounter >=animationBitmaps.length) {
//                animationCounter = 0;
                animationCounter = animationBitmaps.length - 1;

//                changeAnimation();
                fadeOut();


            }
//            new Thread(){
//                @Override
//                public void run() {
//                    if(previousAnimationCounter!=animationCounter||originalBitmap==null||resizedBitmap==null) {
//                        originalBitmap = new BitmapDrawable(getResources(), myGif.getFrames()[animationCounter].getImage());
//                        resizedBitmap=getScaledBitmap(originalBitmap.getBitmap());
//                        Log.e("eeeeeeeeeeeeeee","goes here");
//                        previousAnimationCounter=animationCounter;
//                    }
//                }
//            }.start();

            if((animationCounter!=0)) {
                if(previousAnimationNumber!=animationNumber||previousAnimationCounter!=animationCounter||originalBitmap ==null||resizedBitmap ==null) {

                    new Thread(){
                        @Override
                        public void run() {


//


//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//      Bitmap b=  Bitmap.createScaledBitmap(MyWatchFace.originalBitmap, newWidth, newHeight, true);
//        b.compress(Bitmap.CompressFormat.WEBP,10,out);

                            originalBitmap = animationBitmaps[animationCounter];
                            resizedBitmap =getScaledBitmap(originalBitmap);




                        }
                    }.start();

//            startService(new Intent(getApplicationContext(),MyService.class));
                }
                drawAnim(canvas,false);
            }else{
                if((animationCounter==0))

                    drawAnim(canvas,true);
            }

//            canvas.drawRect(animationLeft, animationTop,animationLeft+resizedBitmap.getWidth(),animationTop+resizedBitmap.getHeight(),paint);


        }

        private void drawAnim(Canvas canvas,boolean playFirstImage){
            previousAnimationCounter=animationCounter;
            previousAnimationNumber=animationNumber;
            if(!playFirstImage&&resizedBitmap ==null) {
                canvas.drawBitmap(listOfAnimationImages.get(animationNumber),Integer.parseInt(listOfAnimationImagesLocation.get(animationNumber).split(",,")[0]), Integer.parseInt(listOfAnimationImagesLocation.get(animationNumber).split(",,")[1]), null);

                return;
            }
            if(animationCounter!=0){
                animationTop=0;
                animationLeft=0;
            }


            if(playFirstImage){
//                if(previousAnimationNumber!=animationNumber||MyWatchFace.previousAnimationCounter!=MyWatchFace.animationCounter||MyWatchFace.originalBitmap ==null||MyWatchFace.resizedBitmap ==null) {
////                    MyWatchFace.resizedBitmap =listOfAnimationImages.get(animationNumber);
//                Log.e("play ", "true   ");
//                }
                canvas.drawBitmap(listOfAnimationImages.get(animationNumber),Integer.parseInt(listOfAnimationImagesLocation.get(animationNumber).split(",,")[0]), Integer.parseInt(listOfAnimationImagesLocation.get(animationNumber).split(",,")[1]), null);

            }
            else {
//                Log.e("play ", "false   ");
                canvas.drawBitmap(resizedBitmap, animationLeft, animationTop, null);

            }
        }

        int animationTop,animationLeft;
        public  Bitmap createTrimmedBitmap(Bitmap original) {


            float sampleClopSize=50;
            float trimSizeX=original.getWidth()/sampleClopSize;
            float trimSizeY=original.getHeight()/sampleClopSize;


//            int[] pix = new int[bmp.getHeight()*bmp.getWidth()];
//            bmp.getPixels(pix, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
//int [][]pixels= new int[bmp.getWidth()][bmp.getHeight()];
//            for(int i =0; i<bmp.getWidth();i++) {
//               for (int j =0; j<bmp.getHeight();j++){
//                   pixels[i][j]=pix[i*bmp.getHeight()+j];
//                   Log.e("", "pixel"+i*bmp.getHeight()+j+"b   - " +pixels[i][j]);
//               }
//
//            }
            Bitmap bmp=  Bitmap.createScaledBitmap(original, (int)sampleClopSize, (int)sampleClopSize, true);

            int minX = bmp.getWidth();
            int minY = bmp.getHeight();
            int maxX = -1;
            int maxY = -1;



//
//            int[] pix = new int[bmp.getHeight()*bmp.getWidth()];
//            bmp.getPixels(pix, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
//int [][]pixels= new int[bmp.getWidth()][bmp.getHeight()];
//            for(int i =0; i<bmp.getWidth();i++) {
//               for (int j =0; j<bmp.getHeight();j++){
//                   pixels[i][j]=pix[i*bmp.getHeight()+j];
//                   Log.e("", "pixel"+i*bmp.getHeight()+j+"b   - " +pixels[i][j]);
//               }
//
//            }


            for(int y = 0; y < bmp.getHeight(); y++)
            {
                for(int x = 0; x < bmp.getWidth(); x++)
                {
                    int alpha = (bmp.getPixel(x, y) >> 24) & 255;
                    if(alpha > 0)   // pixel is not 100% transparent
                    {
                        if(x < minX)
                            minX = x;
                        if(x > maxX)
                            maxX = x;
                        if(y < minY)
                            minY = y;
                        if(y > maxY)
                            maxY = y;
                    }
                }
            }
            if((maxX < minX) || (maxY < minY))
                return null; // Bitmap is entirely transparent
            animationTop=(int)(minY*trimSizeY);
            animationLeft=(int)(minX*trimSizeX);
            listOfAnimationImagesLocation.add(animationLeft+",,"+animationTop);
            // crop bitmap to non-transparent area and return:
            return Bitmap.createBitmap(original, (int)(minX*trimSizeX), (int)(minY*trimSizeY), (int)((maxX - minX+ 1)*trimSizeX ), (int)((maxY - minY+ 1) *trimSizeY));




        }
        private void updateBrightness(int brightness) {
            try {
                if (brightness < 0)
                    brightness = 0;
                else if (brightness > 255)
                    brightness = 255;
                android.provider.Settings.System.putInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE, android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                if (!mAmbient)
                    android.provider.Settings.System.putInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS, brightness);
                else
                    android.provider.Settings.System.putInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS, brightness);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void fadeOut() {

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
//                    if (isHourChanged) {
//                        int alpha = 250;
//                        while (alpha > 0 && !isInAmbientMode()) {
//                            try {
//                                Thread.sleep(10);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                            alpha -= 10;
//                            paint.setAlpha(alpha);
//
//                        }
//                        alpha = 0;
//                        paint.setAlpha(alpha);
//
//                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {

                    if (shouldTimerBeRunning()) {
                        animationCounter = 0;


                        // set next animation
                        if (isHourChanged) {
//                                    changeAnimation();
                            isHourChanged = false;
                        }


                    }

                    wakeUnlock();
                    if (shouldTimerBeRunning()) {
                        isAnimationActivate = false;
                        fadeIn();
                    }
                    super.onPostExecute(aVoid);
                }
            }.execute();
//            new Thread() {
//                @Override
//                public void run() { }
//            }.start();
        }

        private void fadeIn() {

            new Thread() {
                @Override
                public void run() {
//                    int alpha = 0;
//                    while (alpha < 250 && !isInAmbientMode()) {
//                        try {
//                            sleep(10);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        alpha += 10;
//                        paint.setAlpha(alpha);
//
//                    }
//                    alpha = 255;
//                    paint.setAlpha(alpha);
                    animationCounter = 0;


                }
            }.start();
        }


        private void changeAnimation() {
            if (isAnimationActivate || !isLoaded) {
                return;
            }
            animationNumber++;
            if (animationNumber >= animationList.size()) {
                animationNumber = 0;
            }
            switch (animationNumber) {
                case 0:
                    pokemonName = "Pikachu";
                    break;
                case 1:
                    pokemonName = "Marowak";
                    break;
                case 2:
                    pokemonName = "Blastoise";
                    break;
                case 3:
                    pokemonName = "Jigglypuff";
                    break;
                case 4:
                    pokemonName = "Charizard";
                    break;

                case 5:
                    pokemonName = "Mew";
                    break;
                case 6:
                    pokemonName = "Jynx";
                    break;
                case 7:
                    pokemonName = "Mr. Mime";
                    break;
                case 8:
                    pokemonName = "Scyther";
                    break;

                case 9:
                    pokemonName = "Snorlax";
                    break;
                case 10:
                    pokemonName = "Alkazam";
                    break;
                case 11:
                    pokemonName = "Gengar";
                    break;
                case 12:
                    pokemonName = "Pikachu";
                    break;
                case 13:
                    pokemonName = "Smoke";
                    break;
                case 14:
                    pokemonName = "Haunter";
                    break;
                case 15:
                    pokemonName = "Gengar";
                    break;
                case 16:
                    pokemonName = "Pikachu";
                    break;
                case 17:
                    pokemonName = "Poliwhirl";
                    break;
                case 18:
                    pokemonName = "Aerodactyl";
                    break;
                case 19:
                    pokemonName = "Togepi";
                    break;
                case 20:
                    pokemonName = "Squirtle";
                    break;
                case 21:
                    pokemonName = "Eevee";
                    break;
                case 22:
                    pokemonName = "Bulbasaur";
                    break;
                case 23:
                    pokemonName = "Starmie";
                    break;
                case 24:
                    pokemonName = "Onix";
                    break;
                case 25:
                    pokemonName = "Jigglypuff";
                    break;
                case 26:
                    pokemonName = "Jigglypuff";
                    break;
                case 27:
                    pokemonName = "Pikachu";
                    break;
                case 28:
                    pokemonName = "Pichu";
                    break;
                case 29:
                    pokemonName = "Ash";
                    break;
                case 30:
                    pokemonName = "Psyduck";
                    break;
                case 31:
                    pokemonName = "Slowbro";
                    break;
                case 32:
                    pokemonName = "Growlithe";
                    break;
                case 33:
                    pokemonName = "Dragonite";
                    break;
                case 34:
                    pokemonName = "Lickitung ";
                    break;
                case 35:
                    pokemonName = "Bulbasaur";
                    break;
                case 36:
                    pokemonName = "Dragonair";
                    break;
                case 37:
                    pokemonName = "Blastoise";
                    break;
                case 38:
                    pokemonName = "Gengar";
                    break;
                case 39:
                    pokemonName = "Gyarados";
                    break;
                case 40:
                    pokemonName = "Haunter";
                    break;
                case 41:
                    pokemonName = "Lapras";
                    break;
                case 42:
                    pokemonName = "Charizard";
                    break;
                case 43:
                    pokemonName = "Squirtle";
                    break;
                case 44:
                    pokemonName = "Doduo";
                    break;
                case 45:
                    pokemonName = "Alkazam";
                    break;
                case 46:
                    pokemonName = "Psyduck";
                    break;
                case 47:
                    pokemonName = "Wartortle";
                    break;
                case 48:
                    pokemonName = "Ninetales";
                    break;
                case 49:
                    pokemonName = "Gengar";
                    break;
                case 50:
                    pokemonName = "Abra";
                    break;
                case 51:
                    pokemonName = "Wartortle";
                    break;
                case 52:
                    pokemonName = "Umbreon";
                    break;
                case 53:
                    pokemonName = "Azumarill";
                    break;
                case 54:
                    pokemonName = "Carvanha";
                    break;
                case 55:
                    pokemonName = "Phantump";
                    break;

                case 56:
                    pokemonName = "Uknown";
                    break;
                case 57:
                    pokemonName = "Hitmontop";
                    break;
                case 58:
                    pokemonName = "Uknown";
                    break;

                case 59:
                    pokemonName = "Goodra";
                    break;
                case 60:
                    pokemonName = "Froakie";
                    break;


            }

            loadAnimation(animationList.get(animationNumber));
        }

//        public Bitmap drawableToBitmap(Drawable drawable) {
//            if (drawable instanceof BitmapDrawable) {
//                return ((BitmapDrawable) drawable).getBitmap();
//            }
//
//            int width = drawable.getIntrinsicWidth();
//            width = width > 0 ? width : 1;
//            int height = drawable.getIntrinsicHeight();
//            height = height > 0 ? height : 1;
//
//            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//            Canvas canvas = new Canvas(bitmap);
//            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
//            drawable.draw(canvas);
//
//            return bitmap;
//        }

        private void initBackgroundList() {
            backgroundList.removeAll(backgroundList);
            backgroundList_abc.removeAll(backgroundList_abc);

            backgroundList.add(R.drawable.bg1);
            backgroundList.add(R.drawable.bg2);
            backgroundList.add(R.drawable.bg3);
            backgroundList.add(R.drawable.bg4);
            backgroundList.add(R.drawable.bg5);
            backgroundList.add(R.drawable.bg6);
            backgroundList.add(R.drawable.bg7);
            backgroundList.add(R.drawable.bg8);
            backgroundList.add(R.drawable.bg9);
            backgroundList.add(R.drawable.bg10);
            backgroundList.add(R.drawable.bg11);
            backgroundList.add(R.drawable.bg12);
            backgroundList.add(R.drawable.bg13);

//            backgroundList.add(R.drawable.bg7);
//            backgroundList.add(R.drawable.bg8);
//            backgroundList.add(R.drawable.bg9);
//            backgroundList.add(R.drawable.bg10);
//            backgroundList.add(R.drawable.bg11);


            backgroundList_abc.add(R.drawable.bg1_abc);
            backgroundList_abc.add(R.drawable.bg2_abc);
            backgroundList_abc.add(R.drawable.bg3_abc);
            backgroundList_abc.add(R.drawable.bg4_abc);
            backgroundList_abc.add(R.drawable.bg5_abc);
            backgroundList_abc.add(R.drawable.bg6_abc);


            backgroundList_abc.add(R.drawable.bg7_abc);
            backgroundList_abc.add(R.drawable.bg8_abc);
            backgroundList_abc.add(R.drawable.bg9_abc);
            backgroundList_abc.add(R.drawable.bg10_abc);
            backgroundList_abc.add(R.drawable.bg11_abc);
            backgroundList_abc.add(R.drawable.bg12_abc);
            backgroundList_abc.add(R.drawable.bg13_abc);
//            backgroundList_abc.add(R.drawable.bg7_abc);
//            backgroundList_abc.add(R.drawable.bg8_abc);
//            backgroundList_abc.add(R.drawable.bg9_abc);
//            backgroundList_abc.add(R.drawable.bg10_abc);
//            backgroundList_abc.add(R.drawable.bg11_abc);

        }

        private void initBitmaps() {
            backgroundBitmap_abc = ((BitmapDrawable) getDrawable(backgroundList_abc.get(0))).getBitmap();

            backgroundBitmap = ((BitmapDrawable) getDrawable(backgroundList.get(0))).getBitmap();

            Resources resources = getResources();

            batteryBitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.battery_2, null)).getBitmap();
            batteryBitmap_abc = ((BitmapDrawable) resources.getDrawable(R.drawable.battery_2_abc, null)).getBitmap();

            zero_bitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.number0, null)).getBitmap();
            one_bitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.number1, null)).getBitmap();
            two_bitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.number2, null)).getBitmap();
            three_bitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.number3, null)).getBitmap();
            four_bitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.number4, null)).getBitmap();
            five_bitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.number5, null)).getBitmap();
            six_bitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.number6, null)).getBitmap();
            seven_bitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.number7, null)).getBitmap();
            eight_bitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.number8, null)).getBitmap();
            nine_bitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.number9, null)).getBitmap();
            blockBitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.brick, null)).getBitmap();

//            seconds_bitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.sec, null)).getBitmap();


            zero_amb_bitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.amb_number0, null)).getBitmap();
            one_amb_bitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.amb_number1, null)).getBitmap();
            two_amb_bitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.amb_number2, null)).getBitmap();
            three_amb_bitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.amb_number3, null)).getBitmap();
            four_amb_bitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.amb_number4, null)).getBitmap();
            five_amb_bitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.amb_number5, null)).getBitmap();
            six_amb_bitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.amb_number6, null)).getBitmap();
            seven_amb_bitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.amb_number7, null)).getBitmap();
            eight_amb_bitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.amb_number8, null)).getBitmap();
            nine_amb_bitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.amb_number9, null)).getBitmap();
            blockBitmap_abc = ((BitmapDrawable) resources.getDrawable(R.drawable.brick_abc, null)).getBitmap();
//            seconds_bitmap_abc = ((BitmapDrawable) resources.getDrawable(R.drawable.sec_abc, null)).getBitmap();
//            date_amb_bitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.date_abc, null)).getBitmap();


//            zero_amb_bitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.amb_number0, null)).getBitmap();
//            one_amb_bitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.amb_number1, null)).getBitmap();
//            two_amb_bitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.amb_number2, null)).getBitmap();
//            three_amb_bitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.amb_number3, null)).getBitmap();
//            four_amb_bitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.amb_number4, null)).getBitmap();
//            five_amb_bitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.amb_number5, null)).getBitmap();
//            six_amb_bitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.amb_number6, null)).getBitmap();
//            seven_amb_bitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.amb_number7, null)).getBitmap();
//            eight_amb_bitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.amb_number8, null)).getBitmap();
//            nine_amb_bitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.amb_number9, null)).getBitmap();


        }

        private void initScalledBitmaps() {
            WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            Display display = window.getDefaultDisplay();
            int width = display.getWidth();
            int height = display.getHeight();
            mainScaleX = ((float) width) / ((float) backgroundBitmap.getWidth());
            mainScaleY = ((float) height) / ((float) backgroundBitmap.getHeight());

            backgroundBitmapScaled = Bitmap.createScaledBitmap(backgroundBitmap, (int) (((float) backgroundBitmap.getWidth()) * mainScaleX), (int) (((float) backgroundBitmap.getHeight()) * mainScaleY), true);
            backgroundBitmapScaled_abc= getScaledBitmap(backgroundBitmap_abc);

            scaledZero_amb_bitmap = getScaledBitmap(zero_amb_bitmap);
            scaledOne_amb_bitmap = getScaledBitmap(one_amb_bitmap);
            scaledTwo_amb_bitmap = getScaledBitmap(two_amb_bitmap);
            scaledThree_amb_bitmap = getScaledBitmap(three_amb_bitmap);
            scaledFour_amb_bitmap = getScaledBitmap(four_amb_bitmap);
            mScaledFive_amb_bitmap = getScaledBitmap(five_amb_bitmap);
            scaledSix_amb_bitmap = getScaledBitmap(six_amb_bitmap);
            scaledSeven_amb_bitmap = getScaledBitmap(seven_amb_bitmap);
            scaledEight_amb_bitmap = getScaledBitmap(eight_amb_bitmap);
            scaledNine_amb_bitmap = getScaledBitmap(nine_amb_bitmap);
            blockBitmap_abc_Scalled = getScaledBitmap(blockBitmap_abc);

            batteryScaledBitmap = getScaledBitmap(batteryBitmap);
            batteryScaledBitmap_abc = getScaledBitmap(batteryBitmap_abc);
//            dateBitmap_Scalled = getScaledBitmap(date_bitmap);
//            dateBitmap_abc_Scalled = getScaledBitmap(date_amb_bitmap);
//            secondsBlockBitmapScalled_abc = getScaledBitmap(seconds_bitmap_abc);


            scaledZero_bitmap = getScaledBitmap(zero_bitmap);
            scaledOne_bitmap = getScaledBitmap(one_bitmap);
            scaledTwo_bitmap = getScaledBitmap(two_bitmap);
            scaledThree_bitmap = getScaledBitmap(three_bitmap);
            scaledFour_bitmap = getScaledBitmap(four_bitmap);
            scaledFive_bitmap = getScaledBitmap(five_bitmap);
            scaledSix_bitmap = getScaledBitmap(six_bitmap);
            scaledSeven_bitmap = getScaledBitmap(seven_bitmap);
            scaledEight_bitmap = getScaledBitmap(eight_bitmap);
            scaledNine_bitmap = getScaledBitmap(nine_bitmap);
            blockScaledBitmap = getScaledBitmap(blockBitmap);
//            secondsBlockBitmapScalled = getScaledBitmap(seconds_bitmap);
        }

        private void initValues() {
            WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            Display display = window.getDefaultDisplay();
            int width = display.getWidth();
            int height = display.getHeight();
            blockStartY = 50f * mainScaleX;
            blockStartX = (width / 2) - blockScaledBitmap.getWidth() / 2;
            Bitmap num1Bitmap = getTimeBitmap(0);
            numberStartY = blockStartY + dpToPx(5);
//            numberStartY = blockStartY + blockScaledBitmap.getHeight() /2-num1Bitmap.getHeight()/2;
        }

//        @Override
//        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//            super.onSurfaceChanged(holder, format, width, height);
//
//        }

        private Bitmap getScaledBitmap(Bitmap bitmap) {
            return Bitmap.createScaledBitmap(bitmap, (int) (((float) bitmap.getWidth()) * mainScaleX), (int) (((float) bitmap.getHeight()) * mainScaleY), true);
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        private Paint createTextPaint(int textColor) {
            Paint paint = new Paint();
            paint.setColor(textColor);
            paint.setTypeface(NORMAL_TYPEFACE);
            paint.setAntiAlias(true);
            return paint;
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                mTime.clear(TimeZone.getDefault().getID());
                mTime.setToNow();
            } else {
                unregisterReceiver();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            MyWatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            MyWatchFace.this.unregisterReceiver(mTimeZoneReceiver);
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            // Load resources that have alternate values for round watches.
            Resources resources = MyWatchFace.this.getResources();
            boolean isRound = insets.isRound();
            mXOffset = resources.getDimension(isRound
                    ? R.dimen.digital_x_offset_round : R.dimen.digital_x_offset);
            float textSize = resources.getDimension(isRound
                    ? R.dimen.digital_text_size_round : R.dimen.digital_text_size);

            mTextPaint.setFakeBoldText(true);
            mTextPaint.setTextSize(textSize);
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (mAmbient != inAmbientMode) {
                mAmbient = inAmbientMode;
                if (mLowBitAmbient) {
                    mTextPaint.setAntiAlias(!inAmbientMode);
                }
                invalidate();
            }

            if (inAmbientMode) {
                setAmbienceBackground(backgroundImageCounter);
            } else {
                setNonAmbienceBackground(backgroundImageCounter);
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }


        long touchTime = 0;
        final long maxTouchTime = 150;

        /**
         * Captures tap event (and tap type) and toggles the background color if the user finishes
         * a tap.
         */
        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
//            Resources resources = MyWatchFace.this.getResources();
//            switch (tapType) {
//                case TAP_TYPE_TOUCH:
//                    // The user has started touching the screen.
//                    break;
//                case TAP_TYPE_TOUCH_CANCEL:
//                    // The user has started a different gesture or otherwise cancelled the tap.
//                    break;
//                case TAP_TYPE_TAP:
//                    // The user has completed the tap gesture.
//                    mTapCount++;
////                    mBackgroundPaint.setColor(resources.getColor(mTapCount % 2 == 0 ?
////                            R.color.background : R.color.background2));
////                    changeStage();
//
////                    isUpdateTime = true;
//                    enableAnimation();
////                    timeTolayAnimaton = true;
//                    break;
//            }
//            invalidate();


            if (tapType == TAP_TYPE_TOUCH) {
                touchTime = eventTime;
                Log.e("TAP_TYPE_TOUCH", "TAP_TYPE_TOUCH");
            }
            if (tapType == TAP_TYPE_TAP) {
                touchTime = eventTime - touchTime;
                if (touchTime < maxTouchTime) {
                    if (isChangingBackgoundByTouch) {
                        changeStage();
                    } else if (isChangingAnimationByTouch) {
//                        enableAnimation();
                        changeAnimation();
                        Log.e("changeAnimation", "" + touchTime);

                    }
                }
                Log.e("TAP_TYPE_TAP", "" + touchTime);

                touchTime = 0;
            }

        }


        int backgroundImageCounter = 0;

        private void changeStage() {
            backgroundImageCounter++;
            if (backgroundImageCounter >= backgroundList.size()) {
                backgroundImageCounter = 0;
            }

            if (shouldTimerBeRunning()) {
                setNonAmbienceBackground(backgroundImageCounter);
            } else {
                setAmbienceBackground(backgroundImageCounter);
            }

        }

        private void setNonAmbienceBackground(int id) {

            backgroundBitmap = ((BitmapDrawable) getDrawable(backgroundList.get(backgroundImageCounter))).getBitmap();

            backgroundBitmapScaled = Bitmap.createScaledBitmap(backgroundBitmap, (int) (((float) backgroundBitmap.getWidth()) * mainScaleX), (int) (((float) backgroundBitmap.getHeight()) * mainScaleY), true);

        }

        private void setAmbienceBackground(int id) {

            backgroundBitmap = ((BitmapDrawable) getDrawable(backgroundList_abc.get(id))).getBitmap();

            backgroundBitmapScaled = Bitmap.createScaledBitmap(backgroundBitmap, (int) (((float) backgroundBitmap.getWidth()) * mainScaleX), (int) (((float) backgroundBitmap.getHeight()) * mainScaleY), true);

        }

        boolean isHourChanged = false;
        private int previousHour = -1, previousMinute = -1, previousSecond = -1;
        boolean isFlashing = true;
        boolean previousIs24HourType=is24HourType;

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            // Draw the background.
            Paint paint = new Paint();

            WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            Display display = window.getDefaultDisplay();
            int width = display.getWidth();
            int height = display.getHeight();

//                    if (isInAmbientMode()){
//                        updateBrightness(100);
//                    }else{
//                        updateBrightness(255);
//                    }
//                canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);
            canvas.drawBitmap(shouldTimerBeRunning()?backgroundBitmapScaled:backgroundBitmapScaled_abc, 0, 0, null);


            // Draw H:MM in ambient mode or H:MM:SS in interactive mode.
            mTime.setToNow();
//            String text = mAmbient
//                    ? String.format("%d:%02d", mTime.hour, mTime.minute)
//                    : String.format("%d:%02d:%02d", mTime.hour, mTime.minute, mTime.second);
//            canvas.drawText(text, mXOffset, mYOffset, mTextPaint);
            int tempHour = mTime.hour, tempMinute = mTime.minute, timeTextOneByOne = 0;
            float numberHourX1, numberHourX2, numberMinuteX1, numberMinuteX2;
            if (previousMinute == -1) {
                previousMinute = tempMinute;
            }
            String hourExtra = null;
            Date date = new Date();
            if (!is24HourType) {
                hourExtra = (tempHour < 12) ? "AM" : "PM";
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a");


                String newTimeFormat = sdf.format(date);

                try {
                    String newHourFormat = newTimeFormat.split(":")[0];
                    if (tempHour != 0)
                        tempHour = Integer.parseInt(newHourFormat);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(previousIs24HourType!=is24HourType){
                previousHour=tempHour;
            }

            if (tempHour < 10) {
                timeTextOneByOne = 0;

            } else {
                timeTextOneByOne = tempHour / 10;
            }
            Bitmap num1Bitmap = getTimeBitmap(timeTextOneByOne);

            if (previousHour == -1) {
                previousHour = tempHour;
            } else if (previousHour != tempHour) {
//                        if (!isChangingBackgoundByTouch)
//                            changeStage();

                isHourChanged = true;
                changeAnimation();
            }

            float energyBottomY = blockStartY + blockScaledBitmap.getHeight() - dpToPx(5);
            float distanceY = dpToPx(5);

            canvas.drawBitmap(!shouldTimerBeRunning() ? blockBitmap_abc_Scalled : blockScaledBitmap, blockStartX, blockStartY, null);


            Paint transPaintWhite, transPaintBlack;
            transPaintWhite = new Paint();
            transPaintBlack = new Paint();
//            transPaintWhite.setColor(getColorWithAlpha(getResources().getColor(R.color.blockBackground), 0.6f));
            transPaintWhite.setColor((getResources().getColor(R.color.transparent_black_percent_60)));

            transPaintBlack.setColor(getResources().getColor(R.color.transparent_black_percent_60));


float ends=(shouldTimerBeRunning())? numberStartY + (int) (num1Bitmap.getHeight() ):blockStartY + blockScaledBitmap.getHeight();
            canvas.drawRect(blockStartX, blockStartY, blockStartX + blockScaledBitmap.getWidth(), ends, (isInAmbientMode()) ? transPaintBlack : transPaintWhite);


//            canvas.drawBitmap(isInAmbientMode() ? blockBitmap_abc_Scalled : blockScaledBitmap, width/2+10, blockStartY, null);

//            if (!isInAmbientMode()) {// draw energy
                Paint transPaint = new Paint();
                transPaint.setColor(getResources().getColor(R.color.transparent_black_percent_75));
//                canvas.drawRect(width/2-blockScaledBitmap.getWidth()-10, blockStartY,width/2-blockScaledBitmap.getWidth()-10+ blockScaledBitmap.getWidth(), blockStartY+blockScaledBitmap.getHeight(), transPaint);
//                canvas.drawRect(width/2+10, blockStartY,width/2+10+ blockScaledBitmap.getWidth(), blockStartY+blockScaledBitmap.getHeight(), transPaint);


                Paint grayPaint = new Paint();
                grayPaint.setColor(getResources().getColor((!shouldTimerBeRunning()?R.color.DimGray:R.color.powergray)));
                Paint whitePaint = new Paint();
                whitePaint.setColor(getResources().getColor((!shouldTimerBeRunning()?R.color.transparent_white_percent_55:R.color.white)));
                Paint greenPaint = new Paint();
                greenPaint.setColor(getResources().getColor(!shouldTimerBeRunning()?R.color.LightCyan:R.color.powerGreen));
                float hourX = blockStartX + dpToPx(30);

                float hourTotalEndWidth = hourX + dpToPx(70);
                float hourDistanse = Math.abs(hourTotalEndWidth - hourX);

                int usedTempHour = tempHour;
                if (!is24HourType && hourExtra.equals("PM")) {
                    usedTempHour += 12;
                }

//                float distanseGreen=hourDistanse-hourDistanse*((((usedTempHour+1)*60+(tempMinute))/(24.0f*60+59)));

//                Log.e(Float.toString((usedTempHour)*60*60+(tempMinute)*60+mTime.second),"result "+23.0f*60*60);

//                if(!isInAmbientMode()){
                    Paint transPaintWhite2;
                    transPaintWhite2 = new Paint();
                    transPaintWhite2.setColor(getColorWithAlpha(getResources().getColor(R.color.blockBackground), 0.8f));
                    canvas.drawRect(blockStartX,  numberStartY + (int) (num1Bitmap.getHeight()  )
                            , blockStartX + blockScaledBitmap.getWidth(), blockStartY + blockScaledBitmap.getHeight(),  transPaintWhite2);

//                }


//                RectF energyRect=new RectF();
                RectF AllenergysItemRect = new RectF();
                RectF whitePartenergysItemRect = new RectF();

                AllenergysItemRect.set(hourX - dpToPx(5), energyBottomY - distanceY - dpToPx(3),
                        hourTotalEndWidth + dpToPx(3), energyBottomY + dpToPx(3));
//                energyRect.set(hourX,energyBottomY-distanceY,hourTotalEndWidth,energyBottomY);

                whitePartenergysItemRect.set(hourX + dpToPx(20), energyBottomY - distanceY - dpToPx(1),
                        hourTotalEndWidth + dpToPx(1), energyBottomY + dpToPx(1));
                canvas.drawRect(AllenergysItemRect, grayPaint);
                Paint paint4 = new Paint();
                paint4.setTextSize(dpToPx(10));
                paint4.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD));

                paint4.setColor(getResources().getColor(R.color.textGold));
                canvas.drawText("DT", (hourX - dpToPx(2)), energyBottomY + dpToPx(1), paint4);
                canvas.drawRect(whitePartenergysItemRect, whitePaint);


                RectF grayPartenergysItemRect = new RectF();
                Paint paint3 = new Paint();
                paint3.setColor(getResources().getColor(R.color.black_pressed));
                grayPartenergysItemRect.set(hourX + dpToPx(22), energyBottomY - distanceY,
                        hourTotalEndWidth, energyBottomY);
                float distanseGreen = Math.abs(  (grayPartenergysItemRect.left-grayPartenergysItemRect.right) * (1-(((usedTempHour) * 60 + (tempMinute) + mTime.second) / (24.0f * 60 + 59))));

//                Paint grayPaint=new Paint();
//                paint2.setColor(getResources().getColor(R.color.dark_grey));
                canvas.drawRect(grayPartenergysItemRect, paint3);


//                Log.e("distanseGreen" + usedTempHour, hourExtra + " " + distanseGreen);

                canvas.drawRect(grayPartenergysItemRect.left, energyBottomY - distanceY,grayPartenergysItemRect.left+ distanseGreen, energyBottomY, greenPaint);


//
//
//
// canvas.drawRect(hourX+distanseGreen,  energyBottomY-distanceY, hourTotalEndWidth,energyBottomY, redPaint);
//
//
//                float minX=width/2+10;
//
//                float minTotalEndWidth=width/2+10+ blockScaledBitmap.getWidth();
//                float minDistanse=minTotalEndWidth-minX;
//
//                float distanseMinGreen=minDistanse*(1-tempMinute/59.0f);

//                canvas.drawRect(minX, energyBottomY-distanceY,minX+distanseMinGreen, energyBottomY, greenPaint);
//                canvas.drawRect(minX+distanseMinGreen, energyBottomY-distanceY, minTotalEndWidth, energyBottomY, redPaint);

//            }
            if (shouldTimerBeRunning()) {
                if (previousMinute != tempMinute) {

                    enableAnimation();
                }
//                if (timeTolayAnimaton) {
                if (isEnableAnimation) {
                    playAnim(canvas);
                }

//                }
            } else {
                if (isEnableAnimation&&myGif!=null) {

//                    final BitmapDrawable myDrawable = new BitmapDrawable(getResources(), myGif.getFrames()[animationCounter].getImage());
//                    canvas.drawBitmap(getAmbienceGifImage(getScaledBitmap(myDrawable.getBitmap())), 0, 0, paint);

                    canvas.drawBitmap(getAmbienceGifImage(listOfAnimationImages.get(animationNumber)),Integer.parseInt(listOfAnimationImagesLocation.get(animationNumber).split(",,")[0]), Integer.parseInt(listOfAnimationImagesLocation.get(animationNumber).split(",,")[1]), null);

                }
            }


            numberHourX1 = blockStartX + 10;


            canvas.drawBitmap(num1Bitmap, numberHourX1, numberStartY, null);


            if (tempHour < 10) {
                timeTextOneByOne = tempHour;
            } else {
                timeTextOneByOne = tempHour - ((tempHour / 10) * 10);
            }


            numberHourX2 = numberHourX1 + num1Bitmap.getWidth();
            Bitmap num2Bitmap = getTimeBitmap(timeTextOneByOne);


            canvas.drawBitmap(num2Bitmap, numberHourX2, numberStartY, null);


            if (tempMinute < 10) {
                timeTextOneByOne = 0;
            } else {
                timeTextOneByOne = tempMinute / 10;
            }
            Bitmap num3Bitmap = getTimeBitmap(timeTextOneByOne);

            numberMinuteX1 = numberHourX2 + num1Bitmap.getWidth() + 20;
            canvas.drawBitmap(num3Bitmap, numberMinuteX1, numberStartY, null);


            if (isFlashing) {
                Paint sp = new Paint();
                sp.setColor(getResources().getColor(R.color.transparent_black_percent_70));
                sp.setTextSize(60);
                canvas.drawText(":", numberHourX2 + num1Bitmap.getWidth(), numberStartY + 6 * num2Bitmap.getHeight() / 7.0f, sp);
                if (previousSecond != mTime.second && previousSecond != -1) {

                    isFlashing = false;
                }
            } else {
                if (previousSecond != mTime.second && previousSecond != -1) {
                    isFlashing = true;
                }
            }


            previousSecond = mTime.second;
            if (tempMinute < 10) {
                timeTextOneByOne = tempMinute;
            } else {
                timeTextOneByOne = tempMinute - ((tempMinute / 10) * 10);
            }

            Bitmap num4Bitmap = getTimeBitmap(timeTextOneByOne);
            numberMinuteX2 = numberMinuteX1 + num3Bitmap.getWidth();
            canvas.drawBitmap(num4Bitmap, numberMinuteX2, numberStartY, null);

            Paint paint2 = new Paint();
            paint2.setColor(getResources().getColor(R.color.black));
            paint2.setTextSize((int) ((blockScaledBitmap.getHeight() - (num4Bitmap.getHeight())) / 2.5));
            paint2.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD_ITALIC));




            if ( isDateVisible) {
                Calendar c = Calendar.getInstance();

                String formattedDate = c.get(Calendar.DAY_OF_MONTH) + "/" + c.get(Calendar.MONTH) + "/" + Integer.toString(c.get(Calendar.YEAR)).substring(Integer.toString(c.get(Calendar.YEAR)).length() - 2);


                if (!is24HourType) {
                    canvas.drawText(formattedDate, (int) (numberHourX1 + num1Bitmap.getHeight() / 3),
                            (int) ((energyBottomY - distanceY - dpToPx(5))), paint2);

                } else {
                    canvas.drawText(pokemonName, (int) (numberHourX1 + num1Bitmap.getHeight() / 3),
                            (int) ((energyBottomY - distanceY - dpToPx(5))), paint2);
                    canvas.drawText(formattedDate, (int) (blockStartX + blockScaledBitmap.getWidth() - 2f * num1Bitmap.getWidth()),
                            num1Bitmap.getHeight() + numberStartY + (int) ((blockScaledBitmap.getHeight() - (num4Bitmap.getHeight())) / 2.5), paint2);
                }
            } else if (!isDateVisible) {
                canvas.drawText(pokemonName, (int) (numberHourX1 + num1Bitmap.getHeight() / 3),
                        (int) ((energyBottomY - distanceY - dpToPx(5))), paint2);
            }
            if (!is24HourType) {
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                paint.setTextSize((int) ((blockScaledBitmap.getHeight() - (num4Bitmap.getHeight())) / 2.5));
                canvas.drawText(hourExtra, (int) (blockStartX + blockScaledBitmap.getWidth() - 1.5f * num1Bitmap.getWidth()), num1Bitmap.getHeight() + numberStartY + (int) ((blockScaledBitmap.getHeight() - (num4Bitmap.getHeight())) / 2.5), paint);

            }


            if (isBatteryVisible) {
                Paint bp = new Paint();
                bp.setTextSize(17);
                bp.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD_ITALIC));
                canvas.drawBitmap((!shouldTimerBeRunning()) ? batteryScaledBitmap_abc : batteryScaledBitmap, 20, blockStartY + blockScaledBitmap.getHeight(), null);
                IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                Intent batteryStatus = getApplicationContext().registerReceiver(null, iFilter);
                if (!shouldTimerBeRunning()) {
                    bp.setColor(getResources().getColor(R.color.MilkWhite));
                }

                canvas.drawText(Integer.toString(batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)) + "%", 20 + batteryScaledBitmap.getWidth() / 4, (blockStartY + blockScaledBitmap.getHeight() + 2 * batteryScaledBitmap.getHeight() / 3.0f), bp);
            }

//            canvas.drawBitmap(isInAmbientMode() ? secondsBlockBitmapScalled_abc : secondsBlockBitmapScalled, secX, secY, null);
            if (shouldTimerBeRunning()) {
                Paint secontPaint = new Paint();
                secontPaint.setFakeBoldText(true);
                secontPaint.setColor(getResources().getColor(R.color.mnalClr));//DarkSlateBlue,DarkGreen,black
                float secX = blockStartX + blockScaledBitmap.getWidth() - dpToPx(5),//width/2-30,//
                        secY = blockScaledBitmap.getHeight() + blockStartY + 55;
                Paint backgroundSecWhitePaint = new Paint();
                backgroundSecWhitePaint.setColor(getResources().getColor(R.color.transparent_white_percent_80));
                Paint backgroundSecRedPaint = new Paint();
                backgroundSecRedPaint.setColor(getColorWithAlpha(getResources().getColor(R.color.ballRed), 0.8f));


                Paint backgroundSecBlackPaint = new Paint();
                backgroundSecBlackPaint.setColor(getResources().getColor(R.color.transparent_black_percent_50));
//                backgroundSecRedPaint.setColor((getResources().getColor(R.color.dark_red)));

//backgroundSecWhitePaint.setStyle(Paint.Style.FILL);
                secontPaint.setTextSize(50);

                RectF rectF = new RectF(secX - 10, secY - 55, secX + 70, secY + 15);
                canvas.drawArc(rectF, 180, 180, true, backgroundSecRedPaint);
//                canvas.drawOval(new RectF(secX-10 ,10+secY,secX+70,secY-50), backgroundSecWhitePaint);
                canvas.drawArc(rectF, 0, 180, false, backgroundSecWhitePaint);
                canvas.drawRect(rectF.left, rectF.top + rectF.height() / 2 - 5, rectF.right, rectF.top + rectF.height() / 2 + 5, backgroundSecBlackPaint);

                Paint backgroundSecDarkBlackPaint = new Paint();
                backgroundSecDarkBlackPaint.setColor(getResources().getColor(R.color.transparent_white_percent_70));
//                backgroundSecRedPaint.setColor((getResources().getColor(R.color.dark_red)));

//backgroundSecWhitePaint.setStyle(Paint.Style.FILL);


                Paint backgroundSecLightWhitePaint = new Paint();
                backgroundSecLightWhitePaint.setColor(getResources().getColor(R.color.transparent_white_percent_40));

                canvas.drawOval(rectF.left + rectF.width() / 2 - 15, rectF.top + rectF.height() / 2 - 15, rectF.left + rectF.width() / 2 + 15, rectF.top + rectF.height() / 2 + 15, backgroundSecLightWhitePaint);


                canvas.drawOval(rectF.left + rectF.width() / 2 - 3, rectF.top + rectF.height() / 2 - 3, rectF.left + rectF.width() / 2 + 3, rectF.top + rectF.height() / 2 + 3, backgroundSecDarkBlackPaint);
                secontPaint.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD));


//                Paint backgroundSecWhitePaint=new Paint();
//                backgroundSecWhitePaint.setColor(getResources().getColor(R.color.transparent_black_percent_50));
//                canvas.drawRect( secX-5 ,5+secY,secX+45,secY-28, backgroundSecWhitePaint);

                canvas.drawText(mTime.second >= 10 ? Integer.toString(mTime.second) : "0" + Integer.toString(mTime.second), secX, secY, secontPaint);

            }
//            canvas.drawRect(cardBounds, mainPaint);


//            else {
//                animationCounter = 0;
//            }
            previousIs24HourType=is24HourType;
            previousHour = tempHour;
            previousMinute = tempMinute;
        }


        private Bitmap getAmbienceGifImage(Bitmap src) {
            // constant factors
            final double GS_RED = 0.299;
            final double GS_GREEN = 0.587;
            final double GS_BLUE = 0.114;

            // create output bitmap
            Bitmap bmOut = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
            // pixel information
            int A, R, G, B;
            int pixel;

            // get image size
            int width = src.getWidth();
            int height = src.getHeight();

            // scan through every single pixel
            for(int x = 0; x < width; ++x) {
                for(int y = 0; y < height; ++y) {
                    // get one pixel color
                    pixel = src.getPixel(x, y);
                    // retrieve color of all channels
                    A = Color.alpha(pixel);
                    R = Color.red(pixel);
                    G = Color.green(pixel);
                    B = Color.blue(pixel);
                    // take conversion up to one single value
                    R = G = B = (int)(GS_RED * R + GS_GREEN * G + GS_BLUE * B);
                    // set new pixel color to output bitmap
                    bmOut.setPixel(x, y, Color.argb(A, R, G, B));
                }
            }

            // return final image
            return bmOut;
        }

        public int getColorWithAlpha(int color, float ratio) {
            int newColor = 0;
            int alpha = Math.round(Color.alpha(color) * ratio);
            int r = Color.red(color);
            int g = Color.green(color);
            int b = Color.blue(color);
            newColor = Color.argb(alpha, r, g, b);
            return newColor;
        }

        public int dpToPx(int dp) {
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
            return px;
        }

        public Bitmap getTimeBitmap(int number) {
            switch (number) {
                case 0:
                    return !shouldTimerBeRunning() ? scaledZero_amb_bitmap : scaledZero_bitmap;
                case 1:
                    return !shouldTimerBeRunning() ? scaledOne_amb_bitmap : scaledOne_bitmap;
                case 2:
                    return !shouldTimerBeRunning() ? scaledTwo_amb_bitmap : scaledTwo_bitmap;
                case 3:
                    return !shouldTimerBeRunning() ? scaledThree_amb_bitmap : scaledThree_bitmap;
                case 4:
                    return !shouldTimerBeRunning() ? scaledFour_amb_bitmap : scaledFour_bitmap;
                case 5:
                    return !shouldTimerBeRunning() ? mScaledFive_amb_bitmap : scaledFive_bitmap;
                case 6:
                    return !shouldTimerBeRunning() ? scaledSix_amb_bitmap : scaledSix_bitmap;
                case 7:
                    return !shouldTimerBeRunning() ? scaledSeven_amb_bitmap : scaledSeven_bitmap;
                case 8:
                    return !shouldTimerBeRunning() ? scaledEight_amb_bitmap : scaledEight_bitmap;
                case 9:
                    return !shouldTimerBeRunning() ? scaledNine_amb_bitmap : scaledNine_bitmap;
                default:
                    if (!shouldTimerBeRunning())
                        return scaledZero_amb_bitmap;
                    else
                        return scaledZero_bitmap;
            }
        }

        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = 150;
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }
    }
}