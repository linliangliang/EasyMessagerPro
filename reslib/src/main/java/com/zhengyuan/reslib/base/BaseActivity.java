package com.zhengyuan.reslib.base;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * EventBus注册
 */
public abstract class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);

        className = getFiltTag();
    }

    /**
     * 使用EventBus管理消息的传递
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessage(EventBusMessageEntity event) {

        if (!event.isThisClass(this.className))
            return;

        if (handleFailedMsg(event)) {
            return;
        }
        onHandlerEvent(event);
    }

    private String className;

    /**
     * 设置过滤消息的tag，一般通过类名
     */
    protected abstract String getFiltTag();

    /**
     * 处理失败消息。默认情况：return true-已经处理完毕，不再执行handleEventType
     * 可以重写此方法改变返回值，来处理特殊情况
     *
     * @param eventBusMessageEntity
     * @return
     */
    protected boolean handleFailedMsg(EventBusMessageEntity eventBusMessageEntity) {
        if (!eventBusMessageEntity.isSuccess) {
//            Utils.showToast(eventBusMessageEntity.message);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 子类重写，处理事件类型
     *
     * @param entity
     */
    protected abstract void onHandlerEvent(EventBusMessageEntity entity);

    @Override
    protected void onPause() {
        super.onPause();

        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        return super.dispatchTouchEvent(event);
    }
}