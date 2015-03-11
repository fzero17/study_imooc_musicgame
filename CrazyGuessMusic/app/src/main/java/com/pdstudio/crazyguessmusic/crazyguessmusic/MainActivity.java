package com.pdstudio.crazyguessmusic.crazyguessmusic;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pdstudio.crazyguessmusic.data.Const;
import com.pdstudio.crazyguessmusic.model.IAlertDialogClickListener;
import com.pdstudio.crazyguessmusic.model.IWordButtonClickListener;
import com.pdstudio.crazyguessmusic.model.Song;
import com.pdstudio.crazyguessmusic.model.WordButton;
import com.pdstudio.crazyguessmusic.ui.WordGridView;
import com.pdstudio.crazyguessmusic.util.MyPlayer;
import com.pdstudio.crazyguessmusic.util.SharedPreferencesUtil;
import com.pdstudio.crazyguessmusic.util.Util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity implements IWordButtonClickListener {

    //唱片相关动画
    private Animation mPanAnim;
    private LinearInterpolator mPanLin;

    //拨杆移动到唱片上相关动画
    private Animation mBarInAnim;
    private LinearInterpolator mBarInLin;

    //拨杆离开唱片相关动画
    private Animation mBarOutAnim;
    private LinearInterpolator mBarOutLin;

    //按键
    private ImageButton mBtnPlayStart;

    private ImageView mViewPan;
    private ImageView mViewPanBar;

    //是否播放
    private boolean mIsRunning = false;


    //文字框容器
    private ArrayList<WordButton> mAllWords;

    //已选择文字容器
    private ArrayList<WordButton> mSelectedWords;

    private WordGridView mWordGridView;

    //已选择文字框容器
    private LinearLayout mViewWordsContainer;

    //当前歌曲
    private Song mCurrentSong;

    //当前关卡的索引
    private int mCurrentStageIndex = -1;

    //答案状态：正确
    private final static int STATUS_ANSWER_RIGHT = 1;
    //答案状态：错误
    private final static int STATUS_ANSWER_WRONG = 2;
    //答案状态：不完整
    private final static int STATUS_ANSWER_LACK = 3;

    //金币初始数量
    private final static int COIN_COUNT = 1000000;
    //当前金币数量
    private int mCurrentCoinCount;
    private final static int COIN_DELETE_COUNT = 30;
    private final static int COIN_TIP_COUNT = 90;

    //删除错误答案按钮
    private ImageButton mDeleteButton;
    private ImageButton mTipButton;
    private ImageButton mShareButton;
    private TextView mTotalCoinCount;

    private TextView mCurrentStage;

    private SharedPreferencesUtil mSharedPreferencesUtil;


    //过关界面
    private LinearLayout mPassGameView;

    private TextView mPassPercent;
    private TextView mPassStage;
    private TextView mPassSongName;

    //下一题
    private ImageButton mNextButton;

    public final static int ID_DIALOG_DELTE_WORD = 1;
    public final static int ID_DIALOG_TIP_ANSWER = 2;
    public final static int ID_DIALOG_LACK_COINS = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSharedPreferencesUtil = new SharedPreferencesUtil(getApplicationContext(), "game");

        mViewPan = (ImageView) findViewById(R.id.pan);
        mViewPanBar = (ImageView) findViewById(R.id.pan_bar);

        mWordGridView = (WordGridView) findViewById(R.id.word_select_gridview);
        //注册监听
        mWordGridView.registerOnWordButtonClick(this);

        //待选择文字框容器
        mViewWordsContainer = (LinearLayout) findViewById(R.id.word_select_container);

        //初始动画
        initAnim();

        mBtnPlayStart = (ImageButton) findViewById(R.id.btn_play_start);
        mBtnPlayStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePlayButton();
            }
        });

        mCurrentStage = (TextView) findViewById(R.id.game_level);

        mTotalCoinCount = (TextView) findViewById(R.id.total_coin_count);

        mPassGameView = (LinearLayout) findViewById(R.id.layout_passgame);
        mPassPercent = (TextView) findViewById(R.id.pass_percent);
        mPassStage = (TextView) findViewById(R.id.pass_stage);
        mPassSongName = (TextView) findViewById(R.id.pass_songName);

        mNextButton = (ImageButton) findViewById(R.id.pass_next);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextGame();
            }
        });

        //从数据存储中得到当前关卡
        mCurrentStageIndex = mSharedPreferencesUtil.getStageIndex();

        initCurrentStageData();
        initCoinCount();

        handleDeleteButton();
        handleTipButton();

        mShareButton = (ImageButton)findViewById(R.id.btn_share);
        mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareToOthers();
            }
        });
    }

    /**
     * 初始化唱片播放动画
     */
    private void initAnim() {
        //初始化唱片播放动画
        mPanAnim = AnimationUtils.loadAnimation(this, R.anim.rotate);
        mPanLin = new LinearInterpolator();
        //Animation和LinearInterpolater关联
        mPanAnim.setInterpolator(mPanLin);
        mPanAnim.setAnimationListener(new Animation.AnimationListener() {
            //动画开始时
            @Override
            public void onAnimationStart(Animation animation) {

            }

            //动画结束时
            @Override
            public void onAnimationEnd(Animation animation) {
                if (mIsRunning) {
                    //在唱片播放完成以后，mIsRunning的值又回到false了
                    mIsRunning = false;
                    mViewPanBar.startAnimation(mBarOutAnim);
                    //播放按钮回到可见状态
                    mBtnPlayStart.setVisibility(View.VISIBLE);
                }
            }

            //动画重复时
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        mBarInAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_45);
        mBarInLin = new LinearInterpolator();
        mBarInAnim.setFillAfter(true);
        mBarInAnim.setInterpolator(mBarInLin);
        mBarInAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mViewPan.startAnimation(mPanAnim);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mBarOutAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_d_45);
        mBarOutLin = new LinearInterpolator();
        mBarOutAnim.setInterpolator(mBarOutLin);
        mBarOutAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /**
     * 删除按钮事件
     */
    private void handleDeleteButton() {
        mDeleteButton = (ImageButton) findViewById(R.id.btn_delete_wrong);
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mCurrentCoinCount < COIN_DELETE_COUNT) {
                    //金币不够，提示
                    showConfirmDialog(ID_DIALOG_LACK_COINS);
                } else {

                    //提示确认
                    showConfirmDialog(ID_DIALOG_DELTE_WORD);
                }
            }
        });
    }

    /**
     * 提示按钮事件
     */
    private void handleTipButton() {
        mTipButton = (ImageButton) findViewById(R.id.btn_tip);
        mTipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentCoinCount < COIN_DELETE_COUNT) {
                    //金币不够，提示
                    //Toast.makeText(getApplicationContext(), "啊咧，您的金币不够了~~~", Toast.LENGTH_SHORT).show();
                    showConfirmDialog(ID_DIALOG_LACK_COINS);
                } else {
                    showConfirmDialog(ID_DIALOG_TIP_ANSWER);
                }
            }
        });
    }

    /**
     * 提示一个正确答案
     */
    private void getTipAnswer(int index) {

        WordButton button = mSelectedWords.get(index);

        button.mWordString = String.valueOf(mCurrentSong.getNameCharacters()[index]);
        button.mIsVisiable = true;
        button.mViewButton.setVisibility(View.VISIBLE);
        button.mViewButton.setText(button.mWordString);

        //设置待选择文字框对应的文字为不可见

        for (int i = 0; i < mAllWords.size(); i++) {
            if (mAllWords.get(i).mWordString.equals(button.mWordString)) {
                setButtonVisible(mAllWords.get(i), View.INVISIBLE);
            }
        }
    }

    /*
    * 初始化文字选择框数据
    * */
    private void initCurrentStageData() {

        //获取当前关的歌曲信息
        mCurrentSong = loadStageSongInfo(++mCurrentStageIndex);

        mSharedPreferencesUtil.setStageIndex(mCurrentStageIndex);

        //设置当前关数
        mCurrentStage.setText((mCurrentStageIndex + 1) + "");

        //获取已选择文字框数据
        mSelectedWords = initSelectedWord();

        //已选择文字框加入到mViewWordsContainer
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(140, 140);
        for (int i = 0; i < mSelectedWords.size(); i++) {
            mViewWordsContainer.addView(mSelectedWords.get(i).mViewButton, params);
        }

        //获取数据
        mAllWords = initAllWord();
        //更新WordGridView中的数据
        mWordGridView.updateData(mAllWords);

        handlePlayButton();
    }

    /**
     * 根据索引值从Const中取歌曲
     * stageIndex:当前关卡
     */
    private Song loadStageSongInfo(int stageIndex) {
        Song song = new Song();
        String[] stage = Const.SONG_INFO[stageIndex];
        song.setSongFileName(stage[Const.INDEX_FILE_NAME]);
        song.setSongName(stage[Const.INDEX_SONG_NAME]);

        return song;
    }


    /*
    *  初始待选择文字框
    * */
    private ArrayList<WordButton> initAllWord() {

        ArrayList<WordButton> data = new ArrayList<>();

        //获取待选择文字

        String[] words = generateWords();

        for (int i = 0; i < WordGridView.WORD_COUNT; i++) {
            WordButton button = new WordButton();
            button.mWordString = words[i];

            data.add(button);
        }

        return data;
    }

    /*
    * 初始化已选择文字框
    * */
    private ArrayList<WordButton> initSelectedWord() {

        ArrayList<WordButton> data = new ArrayList<>();
        for (int i = 0; i < mCurrentSong.getNameLength(); i++) {

            View view = Util.getView(MainActivity.this, R.layout.word_gridview_item);

            final WordButton holder = new WordButton();
            holder.mViewButton = (Button) view.findViewById(R.id.item_btn);
            holder.mViewButton.setTextColor(Color.WHITE);
            holder.mViewButton.setText("");
            holder.mIsVisiable = false;
            holder.mViewButton.setBackgroundResource(R.drawable.game_wordblank);
            holder.mViewButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearSelectedWord(holder);
                }
            });

            data.add(holder);
        }
        return data;
    }

    /*
    * 点击播放按钮逻辑处理
    * */
    private void handlePlayButton() {
        if (!mIsRunning) {
            //当点击btn_play_start按钮后，唱片开始播放，那么mIsRunning的值就为true
            mIsRunning = true;
            mViewPanBar.startAnimation(mBarInAnim);
            //播放按钮隐藏
            mBtnPlayStart.setVisibility(View.INVISIBLE);

            //播放音乐
            MyPlayer.playSong(MainActivity.this,mCurrentSong.getSongFileName());
        }
    }

    @Override
    protected void onPause() {
        mViewPan.clearAnimation();

        //暂停音乐
        MyPlayer.stopPlaySong(MainActivity.this);

        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onWordButtonClick(WordButton wordButton) {
        //Toast.makeText(this, "点击了按钮" + wordButton.mIndex, Toast.LENGTH_SHORT).show();
        setSelectWord(wordButton);
        checkAnswer();


    }

    private void checkAnswer() {
        //获得答案状态
        int checkResult = checkTheAnswer();

        //检查答案
        if (checkResult == STATUS_ANSWER_RIGHT) {
            //显示过关界面
            mPassGameView.setVisibility(View.VISIBLE);
            //设置过关界面的信息
            //mPassPercent.setText("");
            mPassStage.setText((mCurrentStageIndex + 1) + "");
            mPassSongName.setText(mCurrentSong.getSongName());

            //停止播放音乐
            MyPlayer.stopPlaySong(MainActivity.this);

            //播放金币音效
            MyPlayer.playTone(MainActivity.this,MyPlayer.INDEX_STONE_COIN);

            //passGame();
        } else if (checkResult == STATUS_ANSWER_WRONG) {
            //错误提示
            sparkTheWords();
        } else if (checkResult == STATUS_ANSWER_LACK) {
            //缺失

        }
    }

    /**
     * 所有的待选文字
     */
    private String[] generateWords() {
        Random random = new Random();

        String[] words = new String[WordGridView.WORD_COUNT];

        //存入歌名
        for (int i = 0; i < mCurrentSong.getNameLength(); i++) {
            words[i] = mCurrentSong.getNameCharacters()[i] + "";
        }

        //获取随机文字并存入数组
        for (int i = mCurrentSong.getNameLength(); i < WordGridView.WORD_COUNT; i++) {
            words[i] = getRandomChar() + "";
        }

        //打乱文字顺序：首先从所有元素中随机选取一个与第一个元素进行交换。
        //然后在第二个之后选择一个元素与第二个交换，直到循环到最后一个元素。
        //这样能够确保每个元素在每个位置的概率都是1/n。
        for (int i = WordGridView.WORD_COUNT - 1; i >= 0; i--) {
            int index = random.nextInt(i + 1);

            String buf = words[index];
            words[index] = words[i];
            words[i] = buf;
        }

        return words;
    }

    /**
     * 生成一个随机汉字
     */
    private char getRandomChar() {
        String str = "";
        int highPos;
        int lowPos;

        Random random = new Random();

        highPos = (176 + Math.abs(random.nextInt(39)));
        lowPos = (161 + Math.abs(random.nextInt(93)));

        byte[] b = new byte[2];
        b[0] = (Integer.valueOf(highPos)).byteValue();
        b[1] = (Integer.valueOf(lowPos)).byteValue();

        try {
            str = new String(b, "GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return str.charAt(0);
    }

    /**
     * 清除答案
     */
    private void clearSelectedWord(WordButton wordButton) {
        for (int i = 0; i < mSelectedWords.size(); i++) {
            //找出点击的文字按钮
            if (mSelectedWords.get(i).mIndex == wordButton.mIndex) {

                //设置待选择文字框的可见性
                WordButton btn = mAllWords.get(wordButton.mIndex);
                setButtonVisible(btn, View.VISIBLE);

                //设置点击的按钮的内容和不可见性
                mSelectedWords.get(i).mViewButton.setText("");
                mSelectedWords.get(i).mIsVisiable = false;
                mSelectedWords.get(i).mWordString = "";

                break;
            }
        }
    }

    /**
     * 设置已选文字框的内容
     */
    private void setSelectWord(WordButton wordButton) {
        for (int i = 0; i < mSelectedWords.size(); i++) {

            if (mSelectedWords.get(i).mWordString.length() == 0) {
                //设置答案文字框内容及可见性
                mSelectedWords.get(i).mViewButton.setText(wordButton.mWordString);
                mSelectedWords.get(i).mIsVisiable = true;
                mSelectedWords.get(i).mWordString = wordButton.mWordString;

                //记录索引
                mSelectedWords.get(i).mIndex = wordButton.mIndex;

/*                final int finalI = i;
                mSelectedWords.get(i).mViewButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clearSelectedWord(mSelectedWords.get(finalI));
                    }
                });*/

                //设置待选择框的可见性
                setButtonVisible(wordButton, View.INVISIBLE);

                break;

            }
        }
    }

    /*
    * 设置文字按钮的可见性
    * */
    private void setButtonVisible(WordButton wordButton, int visibility) {
        wordButton.mViewButton.setVisibility(visibility);
        wordButton.mIsVisiable = (visibility == View.VISIBLE) ? true : false;
    }

    /**
     * 检测答案
     */
    private int checkTheAnswer() {
        //如果有空的说明答案还不完整
        for (int i = 0; i < mSelectedWords.size(); i++) {
            if (mSelectedWords.get(i).mWordString.length() == 0) {
                return STATUS_ANSWER_LACK;
            }
        }

        //答案完整，继续检查正确性
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < mSelectedWords.size(); i++) {
            sb.append(mSelectedWords.get(i).mWordString);
        }

        return (sb.toString().equals(mCurrentSong.getSongName())) ?
                STATUS_ANSWER_RIGHT : STATUS_ANSWER_WRONG;
    }

    /**
     * 文字闪烁
     */
    private void sparkTheWords() {
        TimerTask task = new TimerTask() {
            boolean mChange = false;
            int mSpardTimes = 0;

            @Override
            public void run() {
                //在UI线程中刷新
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mSpardTimes > 6) {
                            return;
                        }

                        //执行闪烁的逻辑：交替显示红色和白色文字
                        for (int i = 0; i < mSelectedWords.size(); i++) {
                            mSelectedWords.get(i).mViewButton.setTextColor(mChange ? Color.RED : Color.WHITE);
                        }

                        mChange = !mChange;
                        mSpardTimes++;
                    }
                });

            }
        };

        Timer timer = new Timer();
        timer.schedule(task, 1, 150);
    }

    /*
    * 下一题
    * */
    private void nextGame() {
        //Toast.makeText(getApplicationContext(), "恭喜您，第 " + (mCurrentStageIndex + 1) + " 关过了！", Toast.LENGTH_SHORT).show();
        //清除已选择文字框
        mSelectedWords.clear();
        mViewWordsContainer.removeAllViews();
        mPassGameView.setVisibility(View.INVISIBLE);
        initCurrentStageData();
    }

    /**
     * 初始化金币数量
     */
    private void initCoinCount() {


        mCurrentCoinCount = mSharedPreferencesUtil.getTotalCoin();
        if (mCurrentCoinCount == -1) {
            //第一次加载时初始化
            mCurrentCoinCount = COIN_COUNT;
        }

        mTotalCoinCount.setText(mCurrentCoinCount + "");
    }

    /**
     * 减少金币数量
     */
    private int reduceCoinCount(int count) {
        mCurrentCoinCount = mCurrentCoinCount - count;
        mSharedPreferencesUtil.setTotalCoin(mCurrentCoinCount);
        return mCurrentCoinCount;
    }

    /**
     * 删除一个错误答案
     */
    private void deleteOneWrongWord() {


        Random random = new Random();

        while (true) {
            int i = random.nextInt(WordGridView.WORD_COUNT);
            WordButton button = mAllWords.get(i);

            if (isWrongWordButton(i) && button.mIsVisiable) {
                setButtonVisible(mAllWords.get(i), View.INVISIBLE);
                break;
            }
        }
    }

    /**
     * 是否错误文字答案
     */
    private boolean isWrongWordButton(int index) {
        boolean result = false;

        //歌名包含该文字
        if (!mCurrentSong.getSongName().contains(mAllWords.get(index).mWordString)) {
            result = true;
        }

        return result;
    }

    //自定义AlertDialog事件响应
    // 删除错误答案
    private IAlertDialogClickListener mBtnOkDeleteWordListener
            = new IAlertDialogClickListener() {
        @Override
        public void onClick() {
            int length = 0;

            for (int i = 0; i < mAllWords.size(); i++) {
                if (mAllWords.get(i).mIsVisiable) {
                    length++;
                }
            }

            if (length > mCurrentSong.getSongName().length()) {
                //删除一个错误答案
                deleteOneWrongWord();
                //金币足够
                mCurrentCoinCount = reduceCoinCount(COIN_DELETE_COUNT);
                mTotalCoinCount.setText(mCurrentCoinCount + "");
            }
        }
    };

    //答案提示
    private IAlertDialogClickListener mBtnOkTipAnswerListener =
            new IAlertDialogClickListener() {
                @Override
                public void onClick() {
                    //遍历空白的已选择问题框，提示一个正确答案文字
                    for (int i = 0; i < mSelectedWords.size(); i++) {

                        if (mSelectedWords.get(i).mWordString.equals("")) {
                            getTipAnswer(i);
                            //金币足够
                            mCurrentCoinCount = reduceCoinCount(COIN_TIP_COUNT);
                            mTotalCoinCount.setText(mCurrentCoinCount + "");
                            break;
                        }
                    }
                    checkAnswer();
                }
            };


    //金币不足
    private IAlertDialogClickListener mBtnOkLackCoinsListener =
            new IAlertDialogClickListener() {
                @Override
                public void onClick() {

                }
            };


    /**
     * 显示对话框
     */
    private void showConfirmDialog(int id) {
        switch (id) {
            case ID_DIALOG_DELTE_WORD:
                Util.showDialog(MainActivity.this,
                        "确认花掉" + COIN_DELETE_COUNT + "个金币去掉一个错误答案",
                        mBtnOkDeleteWordListener);
                break;
            case ID_DIALOG_TIP_ANSWER:
                Util.showDialog(MainActivity.this,
                        "确认花掉" + COIN_TIP_COUNT + "个金币获得一个文字提示",
                        mBtnOkTipAnswerListener
                );
                break;
            case ID_DIALOG_LACK_COINS:
                Util.showDialog(MainActivity.this,
                        "金币不足，去商店补充",
                        mBtnOkLackCoinsListener
                );
                break;
        }
    }


    /**
     * 分享到微信的方法
     * */
    private void shareToOthers(){
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "分享测试文本");
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.send_to)));
    }

}


