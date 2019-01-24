package com.oulhafiane.gameball;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by root on 3/21/16.
 */
public class Bombarder extends MotherObject{

    public Bombarder(float x, float y, Resources res, int index,int ySpeed) {
        super(x, y, res, index);
        picture = BitmapFactory.decodeResource(res, R.drawable.newbomb);
        width = picture.getWidth();
        height = picture.getHeight();
        this.ySpeed = ySpeed;
    }
}
