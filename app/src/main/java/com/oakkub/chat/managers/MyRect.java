package com.oakkub.chat.managers;

/**
 * Created by OaKKuB on 1/19/2016.
 */
public class MyRect {

    private int left;
    private int right;
    private int top;
    private int bottom;

    public MyRect(int left, int right, int top, int bottom) {
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
    }

    public boolean contains(int x, int y) {
        return x >= left && x <= right && y >= top && y <= bottom;
    }
}
