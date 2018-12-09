package com.mygdx.game.treasures;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class BaseDefender extends Treasure {

    public BaseDefender(float x, float y, SpriteBatch spriteBatch) {
        super(x, y, spriteBatch);

        setTexture(new Texture(Gdx.files.internal("treasures\\baseDefence.png")));
        setType(TreasureType.BASE_DEFENDER);
    }
}