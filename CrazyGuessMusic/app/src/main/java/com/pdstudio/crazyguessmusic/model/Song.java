package com.pdstudio.crazyguessmusic.model;

/**
 * 歌曲类
 * Created by 换个ID上微博 on 2014/12/27.
 */
public class Song {

    //歌曲名称
    private String mSongName;

    //歌曲的文件名
    private String mSongFileName;

    //歌曲名称长度
    private int mNameLength;

    //存放单个歌曲文字
    public char[] getNameCharacters(){
        return mSongName.toCharArray();
    }

    public String getSongName() {
        return mSongName;
    }

    public void setSongName(String songName) {
        this.mSongName = songName;
        this.mNameLength = songName.length();
    }

    public String getSongFileName() {
        return mSongFileName;
    }

    public void setSongFileName(String songFileName) {
        this.mSongFileName = songFileName;
    }

    public int getNameLength() {
        return mNameLength;
    }
}
