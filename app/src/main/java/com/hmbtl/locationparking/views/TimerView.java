package com.hmbtl.locationparking.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hmbtl.locationparking.R;

/**
 * Created by anar on 11/24/17.
 */

public class TimerView extends LinearLayout implements SeekBar.OnSeekBarChangeListener {

    private TextView minuteText, secondText;
    private SeekBar seekBar;

    private int maxSecond = 15 * 60;


    public TimerView(Context context) {
        super(context);
    }

    public TimerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_timer, this, true);

        minuteText = (TextView) findViewById(R.id.timer_minute);
        secondText = (TextView) findViewById(R.id.timer_second);

        seekBar = (SeekBar) findViewById(R.id.timer_seekbar);
        seekBar.setMax(63);
        seekBar.setProgress(3);
        seekBar.setOnSeekBarChangeListener(this);

    }

    public TimerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        int progress = 0;
        progress = i - 3;
        if(i < 3){
            seekBar.setProgress(3);
            progress = 0;
        } else if (i > 60){
            seekBar.setProgress(60);
            progress = 60;
        }

        int time = Math.round(maxSecond * progress/ 60);

        Log.e("Progress", i + "");

        int minute = Math.round(time / 60);
        int second = time - minute * 60;

        minuteText.setText(String.format("%02d",minute));
        secondText.setText(String.format("%02d",second));

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }


    public int getCurrentSeconds(){
        int seconds = 0;

        seconds = (seekBar.getProgress() - 3)  * 15;

        return seconds;
    }
}