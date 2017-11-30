package com.example.lenovo.doodlejump;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Created by lenovo on 2017/11/29.
 */

public class Platforms {
    private int size;   //platform数组长度
    private int num;    //platform中有效元素个数, 并不是所有元素都在屏幕内
    private Platform[] platform;
    private int screenWidth, screenHeight;
    private int maxPlatInterval;
    private int head, rear;         //指向队头与队尾元素
    //private int lowest, highest;    //指向屏幕内最低(具有最大y坐标)与最高(具有最小的非负y坐标)的platform

    public Platforms(int screenWidth, int screenHeight, int size, Context context) {
        this.size = size;
        this.num = size;
        platform = new Platform[size];
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        maxPlatInterval = 450;     //每两个platform之间的间隔最多450px
        head = 0;   rear = 0;
        //lowest = 0;
        //highest = 0;
        for(int i = 0; i < num; i++) {
            platform[i] = new normalPlat(screenWidth, screenHeight, randomX(), randomY(), context);
            //if(platform[i].y > 0) highest = i;
            rear = i;
        }
    }

    private int randomX() {
        return screenWidth / 2 - 138 / 2;
        //return (int) (Math.random() * (screenWidth - 185));
    }

    private int randomY() {
        int highestY;       //指的是最高platform的y坐标值(最高platform的y坐标值反而最小).
        //如果所有platform都还没有初始化, 则最高platform的y坐标从screenHeight - 55算起
        if(platform[head] == null) highestY = screenHeight - 55;
        else highestY = platform[rear].y;
        highestY -= (int) (Math.random() * maxPlatInterval + 100);
        return highestY;
    }

    public int getSize() { return size; }

    public int getNum() { return num; }

    public Bitmap getBitmap(int i) {
        //返回第i个platform的bitmap 注意这里的第i个指的是从head开始起的第i个
        i = head + i;
        i = i % size;
        return platform[i].getBitmap();
    }

    public int getX(int i) {
        //返回第i个platform的X坐标 注意这里的第i个指的是从head开始起的第i个
        i = head + i;
        i = i % size;
        return platform[i].x;
    }

    public int getY(int i) {
        //返回第i个platform的Y坐标 注意这里的第i个指的是从head开始起的第i个
        i = head + i;
        i = i % size;
        return platform[i].y;
    }

    public void drawBitmap(Canvas canvas, Paint paint) {
        for(int i = 0, j = head; i < num; i++, j = (j+1) % size)
            platform[j].drawBitmap(canvas, paint);
    }

    public void refresh(Context context) {
        //int i, j, y;
        for(int i = 0, j = head; i < num; i++, j = (j+1) % size) {
            //y = platform[j].y;
            if (!platform[j].refresh()) {
                deleteHead();
                newRear(context);
            }
            //if(y < 0 && platform[j].y > 0) highest = j;
        }
    }

    private void deleteHead() {
        if(num <= 0) Log.e(TAG, "wrong: try to delete element from a empty list.");
        platform[head] = null;
        head = (head + 1) % size;
        //lowest = (lowest + 1) % size;
        num--;
    }

    private void newRear(Context context) {
        if(num == size) Log.e(TAG, "wrong: try to add element to a full list.");
        int temp = (rear + 1) % size;
        platform[temp] = new normalPlat(screenWidth, screenHeight, randomX(), randomY(), context);
        rear = temp;
        num++;
    }

    public void inform(boolean still, double doodleVy) {
        //告诉platforms, doodle是否处于静止状态
        if(still)
            //如果doodle处于静止状态, 则为所有的platform添上一个附加的速度
            for(int i = 0, j = head; i < num; i++, j = (j+1) % size)
                platform[j].additionVy = -doodleVy;
        else
            for(int i = 0, j = head; i < num; i++, j = (j+1) % size)
                platform[j].additionVy = 0;
    }

    public void impactCheck(Doodle doodle) {
        if(doodle.vy > 0)
            //表示doodle正在下降
            for(int i = 0, j = head; i < num; i++, j = (j+1) % size)
                platform[j].impactCheck(doodle);
    }
}