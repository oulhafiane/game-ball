package com.oulhafiane.gameball;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GameView extends Activity implements GestureDetector.OnDoubleTapListener,View.OnTouchListener, GestureDetector.OnGestureListener {
    InterstitialAd mInterstitialAd;
    private int RANDOMBOMBS;
    private int YSPEEDBOMB;
    private int YSPEEDOBJECT;
    private int TIMERED=350;
    OurView v;
    List balls = new ArrayList();
    List ballsDeleted = new ArrayList();
    List bombs = new ArrayList();
    List bombarders = new ArrayList();
    List paintBalls = new ArrayList();
    List newBalls = new ArrayList<>();
    List bombarders2 = new ArrayList();
    List paintBalls2 = new ArrayList();
    GestureDetectorCompat gestureDetect;
    int nbrBall = 0;
    int nbrBombs = 0;
    int nbrNewBall;
    long score = 0;
    MediaPlayer eat,die,run,boom;
    boolean doubleBackToExitPressedOnce = false;
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-6472101575134638/2659721708");
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
            }
        });
        requestNewInterstitial();

        v = new OurView(this);
        v.setOnTouchListener(this);
        gestureDetect = new GestureDetectorCompat(this,this);
        gestureDetect.setOnDoubleTapListener(this);

        //initialising Sounds
        eat = MediaPlayer.create(this,R.raw.eat);
        die = MediaPlayer.create(this,R.raw.die);
        boom = MediaPlayer.create(this,R.raw.boom);
        //nbrNewBall = loadNbrBall("NbrBalls");
        nbrNewBall = 2;

        for(int i=0;i<2;i++){
            balls.add(new BlueBall(0, 0, getResources(), i,TIMERED));
            nbrBall++;
        }
        for(int i=0;i<3;i++){
            bombarders2.add(1);
        }
        for(int i = 0; i < 10; i++) {
            paintBalls2.add(1);
        }

        //Get FullScreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(v);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    //Goodle Adsence
    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest);
    }
    /***********************************/

    private void saveNbrBall(String key,int value){
        SecurePreferences preferences = new SecurePreferences(this, "MyPreferences", "723xDBlablaHiho4957", true);
        preferences.put(key, String.valueOf(value));
    }

    private int loadNbrBall(String key){
        SecurePreferences preferences = new SecurePreferences(this, "MyPreferences", "723xDBlablaHiho4957", true);
        String value = preferences.getString(key);
        int nbr;
        if(value!=null){
            nbr = Integer.parseInt(value.toString());
        }else{
            nbr = 0;
        }
        return nbr;
    }

    public void sendToResume(){
        Intent intent = new Intent(this,ResumeGame.class);
        intent.putExtra("count", count);
        startActivityForResult(intent, 99);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case 99:
                if(resultCode == RESULT_OK){
                    Bundle ret = data.getExtras();
                    boolean resume = ret.getBoolean("returnresult");
                    if(resume){
                        count++;
                        Collections.sort(ballsDeleted);
                        int index = (int)ballsDeleted.get(0);
                        balls.add(new BlueBall(0, 0, getResources(), index,TIMERED));
                        ballsDeleted.remove(0);
                        Collections.sort(ballsDeleted);
                        index = (int)ballsDeleted.get(0);
                        balls.add(new BlueBall(0, 0, getResources(), index,TIMERED));
                        ballsDeleted.remove(0);
                        nbrBall++;
                        nbrBall++;
                        nbrNewBall = loadNbrBall("NbrBalls");
                    }else{
                        long count = loadBestScore("Counter");
                        Intent intent = new Intent(getBaseContext(),MainActivity.class);
                        intent.putExtra("score", score);
                        startActivity(intent);
                        if(score<loadBestScore("BESTSCORE")){
                            if (mInterstitialAd.isLoaded()) {
                                if(count == 0){
                                    saveCounter("Counter",1);
                                }else if(count <3){
                                    saveCounter("Counter",++count);
                                }else{
                                    saveCounter("Counter",1);
                                    mInterstitialAd.show();
                                }
                            }
                        }
                        finish();
                    }
                } else {
                    long count = loadBestScore("Counter");
                    Intent intent = new Intent(getBaseContext(),MainActivity.class);
                    intent.putExtra("score",score);
                    startActivity(intent);
                    if(score<loadBestScore("BESTSCORE")){
                        if (mInterstitialAd.isLoaded()) {
                            if(count == 0){
                                saveCounter("Counter",1);
                            }else if(count <3){
                                saveCounter("Counter",++count);
                            }else{
                                saveCounter("Counter",1);
                                mInterstitialAd.show();
                            }
                        }
                    }
                    finish();
                }
                break;
        }
    }


    private void saveCounter(String key,long value){
        SecurePreferences preferences = new SecurePreferences(this, "MyPreferences", "723xDBlablaHiho4957", true);
        preferences.put(key, String.valueOf(value));
    }

    private long loadBestScore(String key){
        SecurePreferences preferences = new SecurePreferences(this, "MyPreferences", "723xDBlablaHiho4957", true);
        String value = preferences.getString(key);
        long score;
        if(value!=null){
            score = Long.parseLong(value);
        }else{
            score = 0;
        }
        return score;
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);

        //Code App
        run.stop();
        v.pause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);

        //Code App
        run = MediaPlayer.create(this,R.raw.run);
        run.setLooping(true);
        run.start();
        v.resume();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        gestureDetect.onTouchEvent(event);
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                for(int i=0;i<balls.size();i++){
                    BlueBall ball = (BlueBall)balls.get(i);
                    if(ball.getY()<=event.getY()+(ball.getHeitgh()/2) && ball.getY()>=event.getY()-(ball.getHeitgh()/2)){
                        ball.setX(event.getX());
                        //ball.setY(event.getY());
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                for(int i=0;i<balls.size();i++){
                    BlueBall ball = (BlueBall)balls.get(i);
                    if(ball.getY()<=event.getY()+(ball.getHeitgh()/2) && ball.getY()>=event.getY()-(ball.getHeitgh()/2)){
                        ball.setX(event.getX());
                        //ball.setY(event.getY());
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                for(int i=0;i<balls.size();i++){
                    BlueBall ball = (BlueBall)balls.get(i);
                    if(ball.getY()<=event.getY()+(ball.getHeitgh()/2) && ball.getY()>=event.getY()-(ball.getHeitgh()/2)){
                        ball.setX(event.getX());
                        //ball.setY(event.getY());
                    }
                }
                break;
        }
        return true;
    }

    //OnDoubleTapListener
    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        if(paintBalls2.size()>0){
            for(int i=0;i<balls.size();i++){
                BlueBall ball = (BlueBall)balls.get(i);
                if(ball.getY()<=e.getY()+(ball.getHeitgh()/2) && ball.getY()>=e.getY()-(ball.getHeitgh()/2)){
                    if(ball.getIsRed()){
                        paintBalls2.remove(0);
                        ball.setIsRed(false);
                        break;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    //OnGestureListener
    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        if(bombarders2.size()>0){
            bombs.clear();
            boom.start();
            bombarders2.remove(0);
        }
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }
    //*****************************************OurView**************************************************
    //OurView Class Definition
    public class OurView extends SurfaceView implements Runnable{
        Thread t;
        boolean isOk = false;
        SurfaceHolder holder;
        int color4;
        boolean reverseColor = false;
        boolean firstRun = true;
        int height;
        int with;
        int unityWidth;
        int unityHeigth;
        int unityBomb;

        //Constructor of OurView
        public OurView(Context context) {
            super(context);
            holder = getHolder();
            color4=255;
        }

        @Override
        public void run() {
            Canvas c;

            while(isOk){
                if(!holder.getSurface().isValid()){
                    continue;
                }
                c = holder.lockCanvas();
                if(firstRun){
                    int screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
                    switch (screenSize) {
                        case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                            unityBomb = 5;
                            break;
                        case Configuration.SCREENLAYOUT_SIZE_LARGE:
                            unityBomb = 10;
                            break;
                        case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                            unityBomb = 20;
                            break;
                        case Configuration.SCREENLAYOUT_SIZE_SMALL:
                            unityBomb = 25;
                            break;
                        default:
                            unityBomb = 20;
                            break;
                    }
                    RANDOMBOMBS = unityBomb * 5;
                    height = c.getHeight();
                    with = c.getWidth();
                    YSPEEDBOMB = height/70;
                    YSPEEDOBJECT = height/100;
                    unityWidth = with/100;
                    unityHeigth = height/100;
                    TIMERED = 350;
                    firstRun = false;
                }
                addBombsAndBalls();
                drawMe(c);
                testBomb(c);
                drawText(c);
                holder.unlockCanvasAndPost(c);
            }
        }

        public void drawText(Canvas c){
            Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
            paintText.setColor(Color.BLUE);
            int sizeText = getResources().getDimensionPixelSize(R.dimen.myFontSize);
            paintText.setTextSize(sizeText);
            paintText.setStyle(Paint.Style.FILL);
            paintText.setShadowLayer(10f, 10f, 10f, Color.BLACK);

            Rect rectText = new Rect();

            String text = "Score : "+score;
            paintText.getTextBounds(text,0,text.length(),rectText);
            c.drawText(text, 0, rectText.height(), paintText);


            Bitmap miniPaintBall = BitmapFactory.decodeResource(getResources(),R.drawable.paintt);
            Bitmap miniBombarder = BitmapFactory.decodeResource(getResources(), R.drawable.newbombb);
            Bitmap miniNewBall = BitmapFactory.decodeResource(getResources(),R.drawable.airball20);
            c.drawBitmap(miniPaintBall,0,c.getHeight()-(4*unityHeigth),null);
            c.drawBitmap(miniBombarder,18*unityWidth,c.getHeight()-(5*unityHeigth),null);
            c.drawBitmap(miniNewBall,c.getWidth()-miniNewBall.getWidth()-(12*unityWidth),c.getHeight()-(4*unityHeigth),null);
            text = " x "+paintBalls2.size();
            c.drawText(text,miniPaintBall.getWidth()+unityWidth,c.getHeight()-unityHeigth,paintText);
            text = " x "+bombarders2.size();
            c.drawText(text,miniBombarder.getWidth()+(19*unityWidth),c.getHeight()-unityHeigth,paintText);
            text = " x "+nbrNewBall;
            c.drawText(text,c.getWidth()-miniNewBall.getWidth()-(5*unityWidth),c.getHeight()-unityHeigth,paintText);
        }

        public void addBombsAndBalls(){
            if(score>4000){
                decreaseTimeToBlue(80);
                TIMERED = 80;
                YSPEEDBOMB = height/15;
                RANDOMBOMBS = (int)(unityBomb * 1.5);
            }else if(score>2000) {
                decreaseTimeToBlue(90);
                TIMERED = 90;
                YSPEEDBOMB = height/20;
                RANDOMBOMBS = unityBomb * 2;
            }else if(score>1000){
                TIMERED = 100;
                decreaseTimeToBlue(100);
                YSPEEDBOMB = height/30;
                RANDOMBOMBS = (int)(unityBomb * 2.5);
            }else if(score>500){
                TIMERED = 200;
                decreaseTimeToBlue(200);
                YSPEEDBOMB = height/40;
                RANDOMBOMBS = unityBomb * 3;
            }else if(score>300){
                TIMERED = 300;
                YSPEEDBOMB = height/50;
                RANDOMBOMBS = (int)(unityBomb * 3.5);
            }else if(score>200){
                YSPEEDBOMB = height/60;
                RANDOMBOMBS = unityBomb * 4;
            }
            Random rand = new Random();
            int condition = rand.nextInt(RANDOMBOMBS);
            if(condition==5){
                condition = rand.nextInt(10);
                bombs.add(new Bomb(0,0,getResources(),condition,YSPEEDBOMB));
                nbrBombs++;
            }
            condition = rand.nextInt(500);
            if(condition==33){
                if(balls.size()<9){
                    if (ballsDeleted.size() != 0) {
                        Collections.sort(ballsDeleted);
                        int index = (int)ballsDeleted.get(0);
                        balls.add(new BlueBall(0, 0, getResources(), index,TIMERED));
                        ballsDeleted.remove(0);
                    } else {
                        balls.add(new BlueBall(0, 0, getResources(), nbrBall,TIMERED));
                    }
                    nbrBall++;
                }
            }
            condition = rand.nextInt(3000);
            if(condition==75){
                bombarders.add(new Bombarder(0,0,getResources(),rand.nextInt(7),YSPEEDOBJECT));
            }
            condition = rand.nextInt(2500);
            if(condition==359){
                paintBalls.add(new PaintBall(0,0,getResources(),rand.nextInt(7),YSPEEDOBJECT));
            }
            condition = rand.nextInt(10000);
            if(condition==567){
                newBalls.add(new NewBall(0,0,getResources(),rand.nextInt(7)));
            }
        }

        public void decreaseTimeToBlue(int time){
            for(int i=0;i<balls.size();i++){
                BlueBall ball = (BlueBall)balls.get(i);
                ball.setTIMETOBLUE(time);
            }
        }

        public void testBomb(Canvas c){
            for(int i=0;i<balls.size();i++){
                BlueBall ball = (BlueBall)balls.get(i);
                for(int j=0;j<bombarders.size();j++){
                    Bombarder bombarder = (Bombarder)bombarders.get(j);
                    if(bombarder.getY()>c.getHeight()){
                        try{
                            bombarders.remove(j);
                        }catch (IndexOutOfBoundsException e){
                            e.printStackTrace();
                        }
                        testBomb(c);
                    }
                    if(bombarder.isInteractedWithBall(ball)){
                        bombs.clear();
                        score+=50;
                        boom.start();
                        bombarders2.add(1);
                        try{
                            bombarders.remove(j);
                        }catch (IndexOutOfBoundsException e){
                            e.printStackTrace();
                        }
                    }
                }
                for(int j=0;j<paintBalls.size();j++){
                    PaintBall paintBall = (PaintBall)paintBalls.get(j);
                    if(paintBall.getY()>c.getHeight()){
                        try{
                            paintBalls.remove(j);
                        }catch (IndexOutOfBoundsException e){
                            e.printStackTrace();
                        }
                        testBomb(c);
                    }
                    if(paintBall.isInteractedWithBall(ball)){
                        ball.setIsRed(false);
                        paintBalls2.add(1);
                        try{
                            paintBalls.remove(j);
                        }catch (IndexOutOfBoundsException e){
                            e.printStackTrace();
                        }
                    }
                }
                for(int j=0;j<newBalls.size();j++){
                    NewBall newBall = (NewBall)newBalls.get(j);
                    if(newBall.getY()>c.getHeight()){
                        try{
                            newBalls.remove(j);
                        }catch (IndexOutOfBoundsException e){
                            e.printStackTrace();
                        }
                        testBomb(c);
                    }
                    if(newBall.isInteractedWithBall(ball)){
                        int nbr = loadNbrBall("NbrBalls");
                        saveNbrBall("NbrBalls",nbr+1);
                        nbrNewBall = nbr+1;
                        try{
                            newBalls.remove(j);
                        }catch (IndexOutOfBoundsException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
            for(int i=0;i<balls.size();i++){
                BlueBall ball = (BlueBall)balls.get(i);
                for(int j=0;j<bombs.size();j++){
                    Bomb bomb = (Bomb)bombs.get(j);
                    if(bomb.isInteractedWithBall(ball)){
                        if (ball.getIsRed() && ball.getTimePassedInRed() > 30) {
                            try{
                                balls.remove(i);
                                ballsDeleted.add(ball.getIndex());
                                die.start();
                                nbrBall--;
                            }catch (IndexOutOfBoundsException e){
                                e.printStackTrace();
                            }
                            if(balls.size()<=0){
                                sendToResume();
                            }
                        } else {
                            try{
                                bombs.remove(j);
                                eat.start();
                                nbrBombs--;
                                score+= 5;
                                ball.setIsRed(true);
                            }catch (IndexOutOfBoundsException e){
                                e.printStackTrace();
                            };
                        }
                    }
                }
            }
        }

        private void drawMe(Canvas c) {
            //c.drawColor(Color.CYAN);
            c.drawARGB(255,0,255,color4);
            if(!reverseColor){
                color4++;
                if(color4>=255)reverseColor=true;
            }else{
                color4--;
                if(color4<=0)reverseColor=false;
            }
            for(int i=0;i<balls.size();i++){
                BlueBall ball = (BlueBall)balls.get(i);
                ball.drawMe(c);
            }
            for(int i=0;i<bombs.size();i++){
                Bomb bomb = (Bomb)bombs.get(i);
                bomb.drawMe(c);
            }
            for(int i=0;i<bombarders.size();i++){
                Bombarder bombarder = (Bombarder)bombarders.get(i);
                bombarder.drawMe(c);
            }
            for(int i=0;i<paintBalls.size();i++){
                PaintBall paintBall = (PaintBall)paintBalls.get(i);
                paintBall.drawMe(c);
            }
            for(int i=0;i<newBalls.size();i++){
                NewBall newBall = (NewBall)newBalls.get(i);
                newBall.drawMe(c);
            }
        }

        public void pause(){
            isOk = false;
            while(true){
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            }
            t = null;
        }

        public void resume(){
            isOk = true;
            t = new Thread(this);
            t.start();
        }
    }
}
