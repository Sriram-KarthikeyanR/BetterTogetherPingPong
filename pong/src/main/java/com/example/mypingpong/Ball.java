package com.example.mypingpong;

import android.graphics.Canvas;
import android.graphics.Paint;

public class Ball {

    public float cx;
    public float cy;
    public float velocity_x;
    public float velocity_y;

    private int radius;
    private Paint paint;

    public Ball(int radius, Paint paint) {
//        this.cx = cx;
//        this.cy = cy;
//        this.velocity_x = velocity_x;
//        this.velocity_y = velocity_y;
        this.paint = paint;
        this.radius = radius;
        this.velocity_x = PongTable.PHY_BALL_SPEED;
        this.velocity_y = PongTable.PHY_BALL_SPEED;
    }

    public void draw(Canvas canvas){
        canvas.drawCircle(cx,cy,radius,paint);
    }

    public void moveBall(Canvas canvas){
        cx += velocity_x;
        cy += velocity_y;

        if(cy<radius){
            cy = radius;
        }
        else if(cy + radius > canvas.getHeight()){
            cy = canvas.getHeight() - radius - 1;
        }
    }

    public int getRadius() {
        return radius;
    }

    @Override
    public String toString() {
        return "Cx = " + cx + "Cy = " + cy + "VelX = " + velocity_x + "VelY = " + velocity_y;
    }

}
