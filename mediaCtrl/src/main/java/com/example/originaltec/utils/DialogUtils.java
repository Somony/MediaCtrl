package com.example.originaltec.utils;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.example.originaltec.R;

/**
 * Created by somon on 2016/8/10.
 */
public class DialogUtils {

    private static AlertDialog dialog;

    public static void showDialog(Context context, View view, int gravity, Boolean canceledOnTouchOutside) {
        dialog = new AlertDialog.Builder(context, R.style.DialogStyle)
                .setTitle("").setMessage("").create();
        dialog.show();
        dialog.setContentView(view);
        Window window = dialog.getWindow();
        //获得window窗口的属性
        WindowManager.LayoutParams lp = window.getAttributes();
        //设置窗口宽度为充满全屏
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        //设置窗口高度为包裹内容
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        //将设置好的属性set回去
        window.setAttributes(lp);
        window.setGravity(gravity);  //此处可以设置dialog显示的位置
        window.setWindowAnimations(R.style.DialogAnim);  //添加动画
        dialog.setCanceledOnTouchOutside(canceledOnTouchOutside); //不能外点击取消
    }

    public static void dialogDismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
