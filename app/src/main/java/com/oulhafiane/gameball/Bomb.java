package com.oulhafiane.gameball;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

import java.util.Random;

/**
 * Created by root on 3/20/16.
 */
public class Bomb {
    private boolean firstRun = true;
    private Bitmap bomb;
    private int x;
    private int y;
    private int xSpped;
    private int ySpeed;
    private int color = 0;
    private int width;
    private int height;
    private int index;
    private int minXbomb;
    private int maxXbomb;
    private int minYbomb;
    private int maxYbomb;

    //Constructor of Bomb
    public Bomb(float x,float y,Resources res,int index,int ySpeed){
        this.x = (int)x;
        this.y = (int)y;
        bomb = BitmapFactory.decodeResource(res,R.drawable.bomb);
        width = bomb.getWidth();
        height = bomb.getHeight()/2;
        this.index = index;
        this.ySpeed = ySpeed;
    }

    public void update(Canvas c){
        color = ++color % 2;
        y = (y+ySpeed)%c.getHeight();
    }

    public void drawMe(Canvas c){
        if(firstRun){
            int unityCanvas = c.getWidth()/10;
            Random rand = new Random();
            int right = rand.nextInt(2);
            if(right==2){
                x = c.getWidth();
            }
            if(right==1){
                x = c.getWidth() -(index*unityCanvas);
            }else{
                x = index*unityCanvas;
            }

            y = 0;
            firstRun = false;
        }
        update(c);
        int srcY = color*height;
        Rect src = new Rect(0,srcY,width,srcY+height);
        Rect dst = new Rect(x-(width/2),y-(height/2),x-(width/2)+width,y-(height/2)+height);
        c.drawBitmap(bomb,src,dst,null);
    }

    public boolean isInteractedWithBall(BlueBall ball){
        minXbomb = x - width;
        maxXbomb = x + width;
        minYbomb = y - height;
        maxYbomb = y + height;

        if(ball.getY()>minYbomb && ball.getY()<maxYbomb && ball.getX()>minXbomb && ball.getX()<maxXbomb){
            return true;
        }
        return false;
    }

    //Getters and Setters
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

    public Bitmap getBomb() {
        return bomb;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
