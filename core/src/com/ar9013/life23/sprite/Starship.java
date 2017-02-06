package com.ar9013.life23.sprite;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by ar9013 on 2017/2/5.
 */
public class Starship extends Sprite {

    Vector2 prePosistion;

    public Starship(Texture texture){
        super(texture);

        prePosistion = new Vector2(getX(),getY());
    }

    public boolean isMoved(){
        if (prePosistion.x != getX() || prePosistion.y != getY()){
            prePosistion.x = getX();
            prePosistion.y =getY();
            return  true;
        }

        return false;
    }
}
