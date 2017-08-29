package com.hd123.imgedit.bean;

import android.graphics.Path;

/**
 * Created by tianyang on 2017/8/7.
 */

public class DrawPath {
    public DrawPath(DrawType type, Path path, int color) {
        this.type = type;
        this.path = path;
        this.color = color;
    }

    public DrawPath(DrawType type, TextBean textBean) {
        this.type = type;
        this.textBean = textBean;
    }


    public DrawType type;

    public Path path;

    public int color;

    public TextBean textBean;

}
