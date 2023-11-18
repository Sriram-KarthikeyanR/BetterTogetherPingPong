package com.example.mypingpong;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.view.SurfaceHolder;

import android.os.Handler;
import android.view.View;

public class GameThread extends Thread{

    //Game States
    public static final int STATE_READY = 0;
    public static final int STATE_PAUSED = 1;
    public static final int STATE_RUNNING = 2;
    public static final int STATE_WIN = 3;
    public static final int STATE_LOSE = 4;

    private boolean mSenorsOn;
    private final Context mCtx;
    private final SurfaceHolder mSurfaceHolder;
    private final Handler mGameStatusHandler;
    private final PongTable mPonTable;
    private final Handler mScoreHandler;

    private boolean mRun = false;
    private int mGameState;
    private Object mRunLock;

    private static final int PHYS_FPS = 60;

    public GameThread(Context mCtx, SurfaceHolder mSurfaceHolder, PongTable mPonTable, Handler mGameStatusHandler, Handler mScoreHandler) {
        this.mCtx = mCtx;
        this.mSurfaceHolder = mSurfaceHolder;
        this.mGameStatusHandler = mGameStatusHandler;
        this.mPonTable = mPonTable;
        this.mScoreHandler = mScoreHandler;
        this.mRunLock = new Object();
    }


    @Override
    public void run() {

        long mNextGameTick = SystemClock.uptimeMillis();
        int skipTicks = 1000/PHYS_FPS;

        while (mRun){
            Canvas c = null;
            try{
                c = mSurfaceHolder.lockCanvas(null);
                if(c!=null){
                    synchronized (mSurfaceHolder){
                        if(mGameState == STATE_RUNNING){
                            mPonTable.update(c);
                        }
                        synchronized (mRunLock){
                            if(mRun){
                                mPonTable.draw(c);
                            }
                        }
                    }
                }

            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if(c!=null){
                    mSurfaceHolder.unlockCanvasAndPost(c);
                }
            }
        }

        mNextGameTick += skipTicks;
        long sleepTime = mNextGameTick - SystemClock.uptimeMillis();
        if(sleepTime > 0){
            try{
                Thread.sleep(sleepTime);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }

    }

    public void setState(int state){
        synchronized (mSurfaceHolder){
            mGameState = state;
            Resources res = mCtx.getResources();
            switch (mGameState){
                case STATE_READY:
                    setUpNewRound();
                    break;
                case STATE_RUNNING:
                    //hide the status hideStatus()
                    hideStatusText();
                    break;
                case STATE_WIN:
                    setStatusText(res.getString(R.string.mode_win));
                    mPonTable.getPlayer().score++;
                    setUpNewRound();
                    break;
                case STATE_LOSE:
                    setStatusText(res.getString(R.string.mode_lose));
                    mPonTable.getOpponent().score++;
                    setUpNewRound();
                    break;
                case STATE_PAUSED:
                    setStatusText(res.getString(R.string.mode_pause));
                    break;
            }
        }
    }

    public void setUpNewRound(){
        synchronized (mSurfaceHolder){
            mPonTable.setupTable();
        }
    }

    public void setRunning(boolean running){
        synchronized (mRunLock){
            mRun = running;
        }
    }

    public boolean SensorsOn(){
        return mSenorsOn;
    }

    public boolean isBetweenRounds(){
        return mGameState != STATE_RUNNING;
    }

    private void setStatusText(String text){
        Message msg = mGameStatusHandler.obtainMessage();
        Bundle b = new Bundle();
        b.putString("text",text);
        b.putInt("visibility", View.VISIBLE);
        msg.setData(b);
        mGameStatusHandler.sendMessage(msg);
    }

    private void hideStatusText(){
        Message msg = mGameStatusHandler.obtainMessage();
        Bundle b = new Bundle();
        b.putInt("visibility",View.INVISIBLE);
        msg.setData(b);
        mGameStatusHandler.sendMessage(msg);
    }

    public void setScoreText(String playerScore, String opponentScore){
        Message msg = mScoreHandler.obtainMessage();
        Bundle b = new Bundle();
        b.putString("player",playerScore);
        b.putString("opponent",opponentScore);
        msg.setData(b);
        mScoreHandler.sendMessage(msg);
    }

}
