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
import android.graphics.Canvas;
import android.graphics.Movie;
import android.graphics.Paint;
import android.graphics.Rect;
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
    protected static boolean isChangingBackgoundByTouch,isBatteryVisible=true;;
    protected static boolean isChangingAnimationByTouch;
    protected static boolean is24HourType = true;
    protected static boolean isDateVisible = false;

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
        private Bitmap batteryBitmap,batteryScaledBitmap,batteryBitmap_abc,batteryScaledBitmap_abc;


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
        private Bitmap seconds_bitmap, seconds_bitmap_abc;
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
        private Bitmap secondsBlockBitmapScalled, secondsBlockBitmapScalled_abc;


        private Bitmap date_bitmap, date_amb_bitmap, dateBitmap_abc_Scalled,dateBitmap_Scalled;

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
        private Bitmap backgroundBitmap, backgroundBitmapScaled;
        float mainScaleX, mainScaleY;
        private ArrayList<Integer> animationList = new ArrayList<Integer>();
        PowerManager.WakeLock wl;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
            isChangingBackgoundByTouch = Settings.getSharedPref(getApplicationContext(), Settings.CHANGE_BACKGROUND_ON_CLICK, true);
            isChangingAnimationByTouch= Settings.getSharedPref(getApplicationContext(), Settings.CHANGE_ANIMATION_ON_CLICK, false);
            is24HourType = Settings.getSharedPref(getApplicationContext(), Settings.HOUR_TYPE, true);
            isBatteryVisible=Settings.getSharedPref(getApplicationContext(),Settings.ENABLE_BATTERY, false);

            isDateVisible = Settings.getSharedPref(getApplicationContext(), Settings.DATE_TYPE, false);
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

            wl.acquire();


        }

        private void wakeUnlock() {
            wl.release();


        }


        private void initAnimationList() {
            animationList.add(R.raw.one);


//            animationList.add(R.raw.four);

            animationList.add(R.raw.seven);
            animationList.add(R.raw.thirtytwo);

            animationList.add(R.raw.eleven);

            animationList.add(R.raw.nine);

            animationList.add(R.raw.fifteen);
            animationList.add(R.raw.sixteen);
//            animationList.add(R.raw.seventeen);
//            animationList.add(R.raw.eighteen);
            animationList.add(R.raw.twentyfour);

            animationList.add(R.raw.nineteen);
            animationList.add(R.raw.twelve);//mewtwo
            animationList.add(R.raw.twenty);
//            animationList.add(R.raw.twentyone);
            animationList.add(R.raw.twentythree);
            animationList.add(R.raw.twentyfive);
            animationList.add(R.raw.twentysix);
            animationList.add(R.raw.twentyseven);
            animationList.add(R.raw.thirty);
            animationList.add(R.raw.thirtythree);
            animationList.add(R.raw.thirtyfour);
            animationList.add(R.raw.thirtyfive);
//            animationList.add(R.raw.thirtysix);
            animationList.add(R.raw.thirtyseven);
            animationList.add(R.raw.thirtyeight);
            animationList.add(R.raw.fourty);
//            animationList.add(R.raw.fourtyone);



            animationList.add(R.raw.eight);
//            animationList.add(R.raw.fourtytwo);
            animationList.add(R.raw.fourtythree);
            animationList.add(R.raw.fourtyfive);
            animationList.add(R.raw.fourtysix);
            animationList.add(R.raw.fourtyseven);
            animationList.add(R.raw.twentytwo);
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

            animationList.add(R.raw.two);
            animationList.add(R.raw.three);

            animationList.add(R.raw.sixty);
//            animationList.add(R.raw.sixtytwo);
            animationList.add(R.raw.sixtythree);
            animationList.add(R.raw.ten);


            animationList.add(R.raw.five);
            animationList.add(R.raw.six);
            animationList.add(R.raw.fourtyfour);
            animationList.add(R.raw.twentyeight);
            animationList.add(R.raw.fiftyfive);
            animationList.add(R.raw.fiftyseven);

            animationList.add(R.raw.thirtynine);

            animationList.add(R.raw.sixtyone);
            animationList.add(R.raw.thirtyone);
            animationList.add(R.raw.fourteen);

            animationList.add(R.raw.twentynine);

        }

        Gif myGif;
        boolean isLoaded = false;

        private void loadAnimation(final int id) {
            isLoaded = false;
            new Thread() {
                @Override
                public void run() {
                    try {

                        myGif = GifFactory.decodeResource(getResources(), id);

                        myGif = Gif.createScaledGif(myGif, backgroundBitmap.getWidth(), backgroundBitmap.getHeight(), true);
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

        int animationCounter = 0;
        boolean isAnimationActivate = false;

        private void enableAnimation() {
            if (isAnimationActivate || isInAmbientMode() || !isLoaded) {
                return;
            }
            wakeLock();
            isAnimationActivate = true;
        }

        private void playAnim(Canvas canvas) {

            if (!isEnableAnimation) {
                if (animationCounter != 0) {
                    animationCounter = 0;
                }
                if(isAnimationActivate)
                    isAnimationActivate=false;
                return;
            }
//                    updateBrightness(255);
            if ((myGif == null || myGif.getFrames() == null) || !isLoaded) {
                return;
            }
            final BitmapDrawable myDrawable = new BitmapDrawable(getResources(), myGif.getFrames()[animationCounter].getImage());
            canvas.drawBitmap(getScaledBitmap(myDrawable.getBitmap()), 0, 0, paint);

            if (isAnimationActivate) {
                animationCounter++;
//                paint = null;
            }
            if (animationCounter >= myGif.getFrames().length) {
//                animationCounter = 0;
                animationCounter = myGif.getFrames().length - 1;
                isAnimationActivate = false;
//                changeAnimation();
                fadeOut();
            }
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
                    if (isHourChanged) {
                        int alpha = 250;
                        while (alpha > 0 && !isInAmbientMode()) {
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            alpha -= 10;
                            paint.setAlpha(alpha);

                        }
                        alpha = 0;
                        paint.setAlpha(alpha);

                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {

                    if (!isInAmbientMode()) {
                        animationCounter = 0;


                        // set next animation
                        if (isHourChanged) {
//                                    changeAnimation();
                            isHourChanged = false;
                        }


                    }

                    wakeUnlock();
                    if (!isInAmbientMode())
                        fadeIn();

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
                    int alpha = 0;
                    while (alpha < 250 && !isInAmbientMode()) {
                        try {
                            sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        alpha += 10;
                        paint.setAlpha(alpha);

                    }
                    alpha = 255;
                    paint.setAlpha(alpha);
                    animationCounter = 0;


                }
            }.start();
        }

        private int animationNumber = 0;

        private void changeAnimation() {
            if (isAnimationActivate) {
                return;
            }
            animationNumber++;
            if (animationNumber >= animationList.size()) {
                animationNumber = 0;
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
            if (isInAmbientMode()) {
                backgroundBitmap = ((BitmapDrawable) getDrawable(backgroundList_abc.get(0))).getBitmap();

            } else {
                backgroundBitmap = ((BitmapDrawable) getDrawable(backgroundList.get(0))).getBitmap();
            }
            Resources resources = getResources();

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
            date_bitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.date, null)).getBitmap();

            seconds_bitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.sec, null)).getBitmap();
            batteryBitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.battery_2, null)).getBitmap();
            batteryBitmap_abc = ((BitmapDrawable) resources.getDrawable(R.drawable.battery_2_abc, null)).getBitmap();



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
            seconds_bitmap_abc = ((BitmapDrawable) resources.getDrawable(R.drawable.sec_abc, null)).getBitmap();
            date_amb_bitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.date_abc, null)).getBitmap();


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
            dateBitmap_Scalled = getScaledBitmap(date_bitmap);
            dateBitmap_abc_Scalled = getScaledBitmap(date_amb_bitmap);
            secondsBlockBitmapScalled_abc = getScaledBitmap(seconds_bitmap_abc);
            batteryScaledBitmap = getScaledBitmap(batteryBitmap);
            batteryScaledBitmap_abc = getScaledBitmap(batteryBitmap_abc);



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
            secondsBlockBitmapScalled = getScaledBitmap(seconds_bitmap);
        }

        private void initValues() {
            WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            Display display = window.getDefaultDisplay();
            int width = display.getWidth();
            int height = display.getHeight();
            blockStartY = 50f * mainScaleX;
            blockStartX = (width / 2) - blockScaledBitmap.getWidth();
            numberStartY = blockStartY + blockScaledBitmap.getHeight() / 3;

            Bitmap num1Bitmap = getTimeBitmap(0);
            numberStartY = blockStartY + blockScaledBitmap.getHeight() /2-num1Bitmap.getHeight()/2;


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
                    }
                    else   if(isChangingAnimationByTouch) {
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

            if (!isInAmbientMode()) {
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
        private int previousHour = -1, previousMinute = -1;

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            // Draw the background.
            Paint paint= new Paint();

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
            canvas.drawBitmap(backgroundBitmapScaled, 0, 0, null);


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
            String hourExtra=null;
            Date date = new Date();
            if (!is24HourType) {
                hourExtra=(tempHour<12)?"AM":"PM";
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a");


                String newTimeFormat = sdf.format(date);

                try {
                    String newHourFormat = newTimeFormat.split(":")[0];
                    tempHour = Integer.parseInt(newHourFormat);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (tempHour < 10) {
                timeTextOneByOne = 0;

            } else {
                timeTextOneByOne = tempHour / 10;
            }

            if (previousHour == -1) {
                previousHour = tempHour;
            } else if (previousHour != tempHour) {
//                        if (!isChangingBackgoundByTouch)
//                            changeStage();

                isHourChanged = true;
                changeAnimation();
            }


            canvas.drawBitmap(isInAmbientMode() ? blockBitmap_abc_Scalled : blockScaledBitmap, blockStartX, blockStartY, null);
            canvas.drawBitmap(isInAmbientMode() ? blockBitmap_abc_Scalled : blockScaledBitmap, width/2, blockStartY, null);


            if (!isInAmbientMode()) {
                if (previousMinute != tempMinute) {

                    enableAnimation();
                }
//                if (timeTolayAnimaton) {
                playAnim(canvas);
//                }
            }

            Bitmap num1Bitmap = getTimeBitmap(timeTextOneByOne);

            numberHourX1 = blockStartX + blockScaledBitmap.getWidth() / 10;


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

            numberMinuteX1 = blockStartX + blockScaledBitmap.getWidth() + 5 + blockScaledBitmap.getWidth() / 10;

            canvas.drawBitmap(num3Bitmap, numberMinuteX1, numberStartY, null);
            if (tempMinute < 10) {
                timeTextOneByOne = tempMinute;
            } else {
                timeTextOneByOne = tempMinute - ((tempMinute / 10) * 10);
            }

            Bitmap num4Bitmap = getTimeBitmap(timeTextOneByOne);
            numberMinuteX2 = numberMinuteX1 + num3Bitmap.getWidth();
            canvas.drawBitmap(num4Bitmap, numberMinuteX2, numberStartY, null);

            if(!isInAmbientMode()&& isDateVisible) {
                Calendar c = Calendar.getInstance();
                Paint paint2= new Paint();
                paint2.setColor(getResources().getColor(R.color.black));

                String formattedDate =  c.get(Calendar.DAY_OF_MONTH)+"/"+ c.get(Calendar.MONTH)+"/"+ Integer.toString(c.get(Calendar.YEAR)).substring(Integer.toString(c.get(Calendar.YEAR)).length()-2);

                paint2.setTextSize(23);
                paint2.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD_ITALIC));

                canvas.drawText(formattedDate, (int)(numberHourX1+num1Bitmap.getHeight()/3), numberStartY+num4Bitmap.getHeight()+22, paint2);
            }

            if(!is24HourType){
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
                paint.setTextSize(20);
                canvas.drawText(hourExtra, (int)(width/2+3*blockScaledBitmap.getWidth()/7.0), numberStartY+num4Bitmap.getHeight()+22, paint);

            }





            Paint secontPaint = new Paint();
            secontPaint.setFakeBoldText(true);
            secontPaint.setTextSize(num4Bitmap.getWidth() / 2);
            secontPaint.setColor(getResources().getColor(R.color.black));
            secontPaint.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD));




            float secX = width / 2 + 8 * blockScaledBitmap.getWidth() / 9.0f, secY = blockStartY+blockScaledBitmap.getHeight();
            canvas.drawBitmap(isInAmbientMode() ? secondsBlockBitmapScalled_abc : secondsBlockBitmapScalled, secX, secY, null);
            if (!isInAmbientMode()) {


                canvas.drawText(mTime.second >= 10 ? Integer.toString(mTime.second) : "0" + Integer.toString(mTime.second), secX + secondsBlockBitmapScalled.getWidth() / 7, 3 * secondsBlockBitmapScalled.getHeight() / 4 + secY, secontPaint);
            }
//            canvas.drawRect(cardBounds, mainPaint);


//            else {
//                animationCounter = 0;
//            }

            if(isBatteryVisible){
                Paint bp= new Paint();
                bp.setTextSize(17);
                bp.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD_ITALIC));
                canvas.drawBitmap((isInAmbientMode())?batteryScaledBitmap_abc:batteryScaledBitmap,20,blockStartY+blockScaledBitmap.getHeight(),null);
                IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                Intent batteryStatus = getApplicationContext().registerReceiver(null, iFilter);
                if(isInAmbientMode()){
                    bp.setColor(getResources().getColor(R.color.MilkWhite));
                }

                canvas.drawText(Integer.toString( batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1))+"%",20+batteryScaledBitmap.getWidth()/4,(blockStartY+blockScaledBitmap.getHeight()+2*batteryScaledBitmap.getHeight()/3.0f),bp);
            }

            previousHour = tempHour;
            previousMinute = tempMinute;
        }

        public Bitmap getTimeBitmap(int number) {
            switch (number) {
                case 0:
                    return isInAmbientMode() ? scaledZero_amb_bitmap : scaledZero_bitmap;
                case 1:
                    return isInAmbientMode() ? scaledOne_amb_bitmap : scaledOne_bitmap;
                case 2:
                    return isInAmbientMode() ? scaledTwo_amb_bitmap : scaledTwo_bitmap;
                case 3:
                    return isInAmbientMode() ? scaledThree_amb_bitmap : scaledThree_bitmap;
                case 4:
                    return isInAmbientMode() ? scaledFour_amb_bitmap : scaledFour_bitmap;
                case 5:
                    return isInAmbientMode() ? mScaledFive_amb_bitmap : scaledFive_bitmap;
                case 6:
                    return isInAmbientMode() ? scaledSix_amb_bitmap : scaledSix_bitmap;
                case 7:
                    return isInAmbientMode() ? scaledSeven_amb_bitmap : scaledSeven_bitmap;
                case 8:
                    return isInAmbientMode() ? scaledEight_amb_bitmap : scaledEight_bitmap;
                case 9:
                    return isInAmbientMode() ? scaledNine_amb_bitmap : scaledNine_bitmap;
                default:
                    if (isInAmbientMode())
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
