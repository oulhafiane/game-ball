package com.oulhafiane.gameball;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

/**
 * Created by root on 3/19/16.
 */
public class BlueBall{
    private boolean firstRun = true;
    private boolean isRed = false;
    private int timePassedInRed;
    private Bitmap blueball;
    private int x;
    private int y;
    private int direction = 0;
    private int color = 0;
    private int width;
    private int heitgh;
    private int index;
    private int TIMETOBLUE;

    public BlueBall(float x,float y,Resources res,int index,int time){
        this.x = (int)x;
        this.y = (int)y;
        blueball = BitmapFactory.decodeResource(res,R.drawable.ball);
        width = blueball.getWidth()/3;
        heitgh = blueball.getHeight()/4;
        this.index = index;
        TIMETOBLUE = time;
    }

    //Methods
    public void update(){
        if(isRed){
            timePassedInRed++;
        }
        if(timePassedInRed>30){
            color = 1;
        }
        if(timePassedInRed>TIMETOBLUE){
            isRed=false;
            color = 0;
        }
        direction = ++direction % 4;
    }

    public void drawMe(Canvas c){
        if(firstRun){
            x = c.getWidth()/2;
            y = c.getHeight()-heitgh-(index*heitgh);
            firstRun = false;
        }
        //int srcX = direction*width
        update();
        int srcX = 0;
        if(isRed)srcX = color*width;
        int srcY = direction*heitgh;
        Rect src = new Rect(srcX,srcY,srcX+width,srcY+heitgh);
        Rect dst = new Rect(x-(width/2),y-(heitgh/2),x-(width/2)+width,y-(heitgh/2)+heitgh);
        c.drawBitmap(blueball,src,dst,null);
        //c.drawBitmap(blueball,x-(width/2),y-(heitgh/2),null);
    }

    //Getters and Setters
    public Bitmap getBlueball() {
        return blueball;
    }

    public int getWidth() {
        return width;
    }

    public int getHeitgh() {
        return heitgh;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = (int)x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = (int)y;
    }

    public boolean getIsRed() {
        return isRed;
    }

    public void setIsRed(boolean isRed) {
        this.isRed = isRed;
        color = 2;
        timePassedInRed = 0;
    }

    public int getIndex() {
        return index;
    }

    public int getTIMETOBLUE() {
        return TIMETOBLUE;
    }

    public void setTIMETOBLUE(int TIMETOBLUE) {
        this.TIMETOBLUE = TIMETOBLUE;
    }

    public int getTimePassedInRed() {
        return timePassedInRed;
    }
}
