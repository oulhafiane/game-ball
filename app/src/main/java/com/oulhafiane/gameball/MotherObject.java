package com.oulhafiane.gameball;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

import java.util.Random;

/**
 * Created by root on 3/21/16.
 */
public class MotherObject {
    protected boolean firstRun = true;
    protected Bitmap picture;
    protected int x;
    protected int y;
    protected int xSpped;
    protected int ySpeed;
    protected int width;
    protected int height;
    protected int index;
    protected int minX;
    protected int maxX;
    protected int minY;
    protected int maxY;

    public MotherObject(float x,float y,Resources res,int index){
        this.x = (int)x;
        this.y = (int)y;
        this.index = index;
    }

    //Methods
    public void update(Canvas c){
        //color = ++color % 2;
        y = (y+ySpeed);
    }

    public void drawMe(Canvas c){
        if(firstRun){
            Random rand = new Random();
            int right = rand.nextInt(2);
            if(right==2){
                x = c.getWidth();
            }
            if(right==1){
                x = c.getWidth() -(index*width);
            }else{
                x = index*width;
            }

            y = 0;
            firstRun = false;
        }
        update(c);
        //int srcY = color*height;
        int srcY = 0;
        Rect src = new Rect(0,srcY,width,srcY+height);
        Rect dst = new Rect(x-(width/2),y-(height/2),x-(width/2)+width,y-(height/2)+height);
        c.drawBitmap(picture,src,dst,null);
    }

    public boolean isInteractedWithBall(BlueBall ball){
        minX = x - width;
        maxX = x + width;
        minY = y - height;
        maxY = y + height;

        if(ball.getY()>minY && ball.getY()<maxY && ball.getX()>minX && ball.getX()<maxX){
            return true;
        }
        return false;
    }

    //Getters And Setters
    public boolean isFirstRun() {
        return firstRun;
    }

    public void setFirstRun(boolean firstRun) {
        this.firstRun = firstRun;
    }

    public void setX(float x) {
        this.x = (int)x;
    }

    public void setY(float y) {
        this.y = (int)y;
    }

    public void setxSpped(int xSpped) {
        this.xSpped = xSpped;
    }

    public void setySpeed(int ySpeed) {
        this.ySpeed = ySpeed;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getxSpped() {
        return xSpped;
    }

    public int getySpeed() {
        return ySpeed;
    }

    public int getIndex() {
        return index;
    }
}
