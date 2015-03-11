package com.pdstudio.crazyguessmusic.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 换个ID上微博
 * Created by gogo on 2015/1/6.
 */
public class SharedPreferencesUtil {

    public static final String TOTAL_COIN_COUNT = "totalcoin";//金币数量
    public static final String INDEX_GAME_STAGE = "gameindex";//游戏关卡

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    public SharedPreferencesUtil(Context context, String file) {
        sp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        editor = sp.edit();
    }

    public void setTotalCoin(int totalCoin) {
        editor.putInt(TOTAL_COIN_COUNT, totalCoin);
        editor.commit();
    }

    public int getTotalCoin() {

        return sp.getInt(TOTAL_COIN_COUNT, -1);
    }

    public void setStageIndex(int stageIndex) {
        editor.putInt(INDEX_GAME_STAGE, stageIndex);
        editor.commit();
    }

    public int getStageIndex() {
        return sp.getInt(INDEX_GAME_STAGE, -1);
    }
}
