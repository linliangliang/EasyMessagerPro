package com.zhengyuan.easymessengerpro.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.zhengyuan.easymessengerpro.R;

/**
 * 底部小圆点
 * Created by gpsts on 17-6-16.
 */

public class LittleDotsView extends LinearLayout {

    private int selectedIndex = 0, totalDotsNumber;
    private Button[] dotsBtn;
    private Context context;

    private final String LOG_TAG = "LittleDotsView";

    public LittleDotsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        // 加载布局

//        LayoutInflater.from(context).inflate(R.layout.widget_relative_video_view, this);

        // 获取控件

//        titleTV = (TextView) findViewById(R.id.title);

        // 初始化课程列表


    }

    public void initDots(int dotNumber) {

        Log.d(LOG_TAG, "dotNumber " + dotNumber);
        totalDotsNumber = dotNumber;
        dotsBtn = new Button[dotNumber];
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.dot_selected);
        for (int i = 0; i < dotNumber; i++) {

            Button button = new Button(context);
            button.setLayoutParams(new ViewGroup.LayoutParams(bitmap.getWidth(),
                    bitmap.getHeight()));
            selectBtn(false, button);
            //this.addView(button);//不显示分页的dot
            dotsBtn[i] = button;
        }

        selectBtn(true, dotsBtn[selectedIndex]);
    }

    /**
     * 切换小圆点选中状态
     *
     * @param index
     */
    public void selectDot(int index) {

        if (index == selectedIndex)
            return;

        selectBtn(false, dotsBtn[selectedIndex]);
        selectedIndex = index;

        if (index > totalDotsNumber - 1)
            throw new ArrayIndexOutOfBoundsException("小圆点越界。小圆点数量" + totalDotsNumber + " " +
                    "要切到的页面" + index);

        selectBtn(true, dotsBtn[selectedIndex]);
    }

    /**
     * 切换小圆点背景图片
     *
     * @param isSelect
     * @param button
     */
    private void selectBtn(boolean isSelect, Button button) {

        if (isSelect)
            button.setBackgroundResource(R.drawable.dot_selected);
        else
            button.setBackgroundResource(R.drawable.dot_unselected);
    }
}
