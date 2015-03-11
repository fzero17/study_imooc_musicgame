package com.pdstudio.crazyguessmusic.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

import com.pdstudio.crazyguessmusic.crazyguessmusic.R;
import com.pdstudio.crazyguessmusic.model.IWordButtonClickListener;
import com.pdstudio.crazyguessmusic.model.WordButton;
import com.pdstudio.crazyguessmusic.util.Util;

import java.util.ArrayList;

/**
 * 自定义GridView
 * Created by 换个ID上微博 on 2014/12/22.
 */
public class WordGridView extends GridView {

    //24个文字的容器
    private ArrayList<WordButton> mArrayList = new ArrayList<WordButton>();

    private WordGridAdapter mWordGridAdapter;

    private Context mContext;

    //待选择文字个数
    public static final int WORD_COUNT = 24;

    //待选文字框加载动画
    private Animation mScaleAnimation;

    private IWordButtonClickListener mWordButtonClickListener;

    public WordGridView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        //关联GridView和Adapter
        mWordGridAdapter = new WordGridAdapter();
        this.setAdapter(mWordGridAdapter);
    }

    public void updateData(ArrayList<WordButton> list) {
        mArrayList = list;

        //重新设置数据源
        setAdapter(mWordGridAdapter);
    }

    /*
    * 适配器
    * */
    class WordGridAdapter extends BaseAdapter {
        //获取文字的数量
        @Override
        public int getCount() {
            return mArrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return mArrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View v, ViewGroup parent) {

            final WordButton holder;

            if (v == null) {
                v = Util.getView(mContext, R.layout.word_gridview_item);
                //从容器中获取一个WordButton
                holder = mArrayList.get(position);

                //加载动画
                mScaleAnimation = AnimationUtils.loadAnimation(mContext, R.anim.scale);
                //设置动画的延迟时间
                mScaleAnimation.setStartOffset(position * 100);

                holder.mIndex = position;
                if (holder.mViewButton == null) {
                    //这里不能直接findViewById(R.id.item_btn)吗
                    holder.mViewButton = (Button) v.findViewById(R.id.item_btn);
                    holder.mViewButton.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mWordButtonClickListener.onWordButtonClick(holder);
                        }
                    });
                }
                v.setTag(holder);

            } else {
                holder = (WordButton) v.getTag();
            }

            holder.mViewButton.setText(holder.mWordString);

            //动画的播放
            v.startAnimation(mScaleAnimation);

            return v;
        }


    }

    /**
     * 注册监听接口
     */
    public void registerOnWordButtonClick(IWordButtonClickListener listener) {
        mWordButtonClickListener = listener;
    }
}

