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
    private int num;    //platform中有效元素个数
    private Platform[] platform;
    private int screenWidth, screenHeight;
    private int maxPlatInterval;    //相邻两个platform之间的最大间隔 单位: px
    private int head, rear;         //指向队头与队尾元素
    private int numWhitePlat;

    Platforms(int screenWidth, int screenHeight, int size, Context context) {
        this.size = size;
        this.num = size;
        platform = new Platform[size];
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        maxPlatInterval = 450;     //每两个platform之间的间隔最多450px
        head = 0;   rear = 0;
        numWhitePlat = 0;
        for(int i = 0; i < num; i++) {
            platform[i] = new normalPlat(screenWidth, screenHeight, randomX(), randomY(PlatType.normal), context);
            rear = i;
        }
    }

    private int randomX() {
        //return screenWidth / 2 - 138 / 2;
        return (int) (Math.random() * (screenWidth - 185));
    }

    private int randomY(int type) {
        int highestY;       //指的是最高platform的y坐标值(最高platform的y坐标值反而最小).
        //如果所有platform都还没有初始化, 则最高platform的y坐标从screenHeight - 55算起
        if(platform[head] == null) {
            highestY = screenHeight - 55;
            highestY -= (int) (Math.random() * maxPlatInterval + 100);
        }
        else {
            highestY = platform[rear].y;
            if(type == PlatType.broken)
                highestY -= (int) ((Math.random() * 200 + 100));
            else if(platform[rear].type == PlatType.broken)
                highestY -= (int) ((Math.random() * 150 + 100));
            else highestY -= (int) (Math.random() * maxPlatInterval + 100);
        }
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

    void drawBitmap(Canvas canvas, Paint paint) {
        for(int i = 0, j = head; i < num; i++, j = (j+1) % size)
            if(platform[j].valid) platform[j].drawBitmap(canvas, paint, 0);
    }

    void refresh(Context context, Title title) {
        boolean flag = false;
        int deltaY = 0;
        int originHead = head;
        for(int i = 0, j = head; i < num; i++, j = (j+1) % size) {
            if(!flag) {
                deltaY = (int) (platform[j].additionVy * platform[j].interval);
                flag = true;
            }
            if (!platform[j].refresh()) {
                // 当有一个platform掉出屏幕时, 就删去它并在在队尾生成一个新的platform
                if(j == originHead) {
                    deleteHead();
                    newRear(context, title);
                }
            }
        }
        title.addScore(deltaY);
    }

    private void deleteHead() {
        if(num <= 0) Log.e(TAG, "wrong: try to delete element from a empty list.");
        platform[head] = null;
        head = (head + 1) % size;
        //lowest = (lowest + 1) % size;
        num--;
    }

    private void newRear(Context context, Title title) {
        if(num == size) {
            Log.e(TAG, "wrong: try to add element to a full list.");
            return;
        }
        int temp = (rear + 1) % size;
        int rv = (int)(Math.random() * 1000);
        if(platform[rear] == null)
            platform[temp] = new normalPlat(screenWidth, screenHeight, randomX(), randomY(PlatType.normal), context);
        else {
            switch (platform[rear].type) {
                case PlatType.broken:
                    if(rv > 200) platform[temp] = new normalPlat(screenWidth, screenHeight, randomX(), randomY(PlatType.normal), context);
                    else if(rv > 10) platform[temp] = new springPlat(screenWidth, screenHeight, randomX(), randomY(PlatType.withspring), context);
                    else  {
                        platform[temp] = new whitePlat(screenWidth, screenHeight, randomX(), randomY(PlatType.onetouch), context);
                        numWhitePlat++;
                    }
                    break;
                case PlatType.onetouch:
                    if(numWhitePlat < 5) {
                        platform[temp] = new whitePlat(screenWidth, screenHeight, randomX(), randomY(PlatType.onetouch), context);
                        numWhitePlat++;
                    }
                    else {
                        platform[temp] = new normalPlat(screenWidth, screenHeight, randomX(), randomY(PlatType.normal), context);
                        numWhitePlat = 0;
                    }
                    break;
                default:
                    if(rv > 300)
                        platform[temp] = new normalPlat(screenWidth, screenHeight, randomX(), randomY(PlatType.normal), context);
                    else if(rv > 200)
                        platform[temp] = new brokenPlat(screenWidth, screenHeight, randomX(), randomY(PlatType.broken), context);
                    else if(rv > 10)
                        platform[temp] = new springPlat(screenWidth, screenHeight, randomX(), randomY(PlatType.withspring), context);
                    else {
                        platform[temp] = new whitePlat(screenWidth, screenHeight, randomX(), randomY(PlatType.onetouch), context);
                        numWhitePlat++;
                    }
            }
        }
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

    public void impactCheck(Doodle doodle, Context context) {
        if(doodle.vy > 0)
            //表示doodle正在下降
            for(int i = 0, j = head; i < num; i++, j = (j+1) % size)
                platform[j].impactCheck(doodle, context);
    }
}
