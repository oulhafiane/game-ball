package com.oulhafiane.gameball;

import android.content.res.Resources;
import android.graphics.BitmapFactory;

/**
 * Created by root on 3/25/16.
 */
public class NewBall extends MotherObject{

    public NewBall(float x, float y, Resources res, int index) {
        super(x, y, res, index);
        picture = BitmapFactory.decodeResource(res, R.drawable.airball40);
        width = picture.getWidth();
        height = picture.getHeight();
        ySpeed = 7;
    }
}