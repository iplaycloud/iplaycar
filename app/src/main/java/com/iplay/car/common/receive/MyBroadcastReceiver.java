package com.iplay.car.common.receive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iplay.car.R;

/**
 * Created by Administrator on 2016/11/1.
 */

public class MyBroadcastReceiver extends BroadcastReceiver {
    private final String ACTION_BOOT = "";

    private TextView tv;
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        String action = intent.getAction();
        if (action.equals(ACTION_BOOT)) {
            dialog(context);
        }
    }
    /**
     * 展示倒计时对话框
     */
    private void dialog(Context context) {
        View view;
        LayoutInflater inflater;
        inflater = LayoutInflater.from(context);
        view = (LinearLayout) inflater.inflate(R.layout.shutdown_layout, null);
        tv = (TextView) view.findViewById(R.id.tv);
        final MyCountDownTimer timer = new MyCountDownTimer(60 * 1000, 1000);
        timer.start();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);
        builder.setTitle("提示");
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                timer.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.setCancelable(false);
        dialog.show();
    }

    class MyCountDownTimer extends CountDownTimer {

        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            tv.setText("关机倒计时:" + millisUntilFinished / 1000);
        }

        @Override
        public void onFinish() {
            Intent intent = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
            intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
    }

}
