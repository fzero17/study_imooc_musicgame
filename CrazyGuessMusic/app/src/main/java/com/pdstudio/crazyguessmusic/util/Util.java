package com.pdstudio.crazyguessmusic.util;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.pdstudio.crazyguessmusic.crazyguessmusic.R;
import com.pdstudio.crazyguessmusic.model.IAlertDialogClickListener;

/**
 * 工具类
 * Created by 换个ID上微博 on 2014/12/23.
 */
public class Util {

    private static AlertDialog mAlertDialog;

    /*
    * 根据layoutId获取View
    * */
    public static View getView(Context context, int layoutId) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View layout = inflater.inflate(layoutId, null);
        return layout;
    }

    /**
     * 显示自定义对话框
     */
    public static void showDialog(Context context, String message, final IAlertDialogClickListener listener) {

        View dialogView = null;

        //创建builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        dialogView = getView(context, R.layout.dialog_view);

        //获取View中的控件
        ImageButton btnOkView = (ImageButton) dialogView.findViewById(R.id.buy_ok);
        ImageButton btnCancelView = (ImageButton)dialogView.findViewById(R.id.buy_cancel);
        TextView txtMessageView = (TextView)dialogView.findViewById(R.id.buy_message);

        txtMessageView.setText(message);

        btnOkView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //关闭对话框
                if(mAlertDialog!=null){
                    mAlertDialog.dismiss();
                }

                //回调
                if(listener!=null){
                    listener.onClick();
                }
            }
        });

        btnCancelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //关闭对话框
                if(mAlertDialog!=null){
                    mAlertDialog.dismiss();
                }
            }
        });

        //为dialog设置View
        builder.setView(dialogView);
        mAlertDialog = builder.create();

        //显示对话框
        mAlertDialog.show();
    }

}
