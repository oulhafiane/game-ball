package com.oulhafiane.gameball;

import android.content.res.Resources;
import android.graphics.BitmapFactory;

/**
 * Created by root on 3/21/16.
 */
public class PaintBall extends MotherObject{

    public PaintBall(float x, float y, Resources res, int index,int ySpeed) {
        super(x, y, res, index);
        picture = BitmapFactory.decodeResource(res, R.drawable.paint);
        width = picture.getWidth();
        height = picture.getHeight();
        this.ySpeed = ySpeed;
    }
}
