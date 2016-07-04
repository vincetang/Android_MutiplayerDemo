package com.vince.sprites;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by Vince on 16-07-04.
 */
public class Cat extends Sprite{

    private Vector2 previousPosition;
    private Texture catTexture;

    public Cat(Texture texture) {
        super(texture);
        previousPosition = new Vector2(getX(), getY());
    }

    public boolean hasMoved() {
        if (previousPosition.x != getX() || previousPosition.y != getY()) {
            previousPosition.x = getX();
            previousPosition.y = getY();
            return true;
        }
        return false;
    }
}
