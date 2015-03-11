package com.pdstudio.crazyguessmusic.model;

import android.widget.Button;

/**
 * 文字按钮
 *
 * Created by 换个ID上微博 on 2014/12/22.
 */
public class WordButton {

    public int mIndex;
    public boolean mIsVisiable;
    public String mWordString;
    public Button mViewButton;


    public WordButton(){
        mIsVisiable = true;
        mWordString ="";
    }
}
