package com.pdstudio.crazyguessmusic.util;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;

import java.io.IOException;

/**
 * 音乐播放类
 * <p/>
 * Created by 换个ID上微博 on 2015/1/24.
 */
public class MyPlayer {

    //索引
    public final static int INDEX_STONE_ENTER = 0;
    public final static int INDEX_STONE_CANCEL = 1;
    public final static int INDEX_STONE_COIN = 2;

    //音效文件名
    private final static String[] SONG_NAMES = {"enter.mp3", "cancel.mp3", "coin.mp3"};

    //音效
    private static MediaPlayer[] mediaPlayers = new MediaPlayer[SONG_NAMES.length];

    //歌曲播放
    private static MediaPlayer mMusicMediaPlayer;

    /**
     * 播放歌曲
     */
    public static void playSong(Context context, String fileName) {
        if (mMusicMediaPlayer == null) {
            mMusicMediaPlayer = new MediaPlayer();
        }

        //强制重置
        mMusicMediaPlayer.reset();

        //加载声音文件
        AssetManager assetManager = context.getAssets();
        try {
            AssetFileDescriptor fileDescriptor = assetManager.openFd(fileName);
            //加载数据源
            mMusicMediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(),
                    fileDescriptor.getStartOffset(),
                    fileDescriptor.getLength());

            mMusicMediaPlayer.prepare();

            //声音播放
            mMusicMediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止播放
     */
    public static void stopPlaySong(Context context) {
        if (mMusicMediaPlayer != null) {
            mMusicMediaPlayer.stop();
        }
    }

    /**
     * 播放音效
     */
    public static void playTone(Context context, int index) {
        //加载声音
        AssetManager assetManager = context.getAssets();

        if (mediaPlayers[index] == null) {
            mediaPlayers[index] = new MediaPlayer();
        }

        try {
            AssetFileDescriptor fileDescriptor = assetManager.openFd(SONG_NAMES[index]);

            mediaPlayers[index].setDataSource(fileDescriptor.getFileDescriptor(),
                    fileDescriptor.getStartOffset(),
                    fileDescriptor.getLength());

            mediaPlayers[index].prepare();

            //播放声音
            mediaPlayers[index].start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
