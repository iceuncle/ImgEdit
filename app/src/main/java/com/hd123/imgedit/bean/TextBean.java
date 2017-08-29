package com.hd123.imgedit.bean;

/**
 * Created by tianyang on 2017/8/7.
 */

public class TextBean {
    public int x;
    public int y;
    public float scale;
    public float rotate;
    public String text;
    public int color;

    public TextBean() {
    }

    public TextBean(int x, int y, float scale, float rotate, String text, int color) {
        this.x = x;
        this.y = y;
        this.scale = scale;
        this.rotate = rotate;
        this.text = text;
        this.color = color;
    }


}
