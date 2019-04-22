package cn.pedant.SweetAlert;


import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.pnikosis.materialishprogress.ProgressWheel;
import com.view.dialog.MultiChoiceAdapter;
import com.view.dialog.MultiChoiceItem;
import com.view.dialog.SingleChoiceAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 *自定义dialog<br>
 *最好不要定义为全局变量，因为同一个不能dialog.show()两次<br>
 *即便重新new一个也不行，建议写成局部变量，并定义为final类型（由于在匿名内部类中不能调用非final类型的局部变量）
 */
public class SweetAlertDialog extends Dialog implements View.OnClickListener {
    private View mDialogView;
    private AnimationSet mModalInAnim;
    private AnimationSet mModalOutAnim;
    private Animation mOverlayOutAnim;
    private Animation mErrorInAnim;
    private AnimationSet mErrorXInAnim;
    private AnimationSet mSuccessLayoutAnimSet;
    private Animation mSuccessBowAnim;
    private TextView mTitleTextView;
    private TextView mContentTextView;
    private String mTitleText;
    private String mContentText;
    private boolean mShowCancel;
    private boolean mShowContent;
    private String mCancelText;
    private String mConfirmText;
    private int mAlertType;
    private FrameLayout mErrorFrame;
    private FrameLayout mSuccessFrame;
    private FrameLayout mProgressFrame;
    private SuccessTickView mSuccessTick;
    private ImageView mErrorX;
    private View mSuccessLeftMask;
    private View mSuccessRightMask;
    private Drawable mCustomImgDrawable;
    private ImageView mCustomImage;
    private Button mConfirmButton;
    private Button mCancelButton;
    private ProgressHelper mProgressHelper;
    private FrameLayout mWarningFrame;
    private OnSweetClickListener mCancelClickListener;
    private OnSweetClickListener mConfirmClickListener;
    private boolean mCloseFromCancel;

    //DX
    private View contentView=null;
    private LinearLayout contentLayout=null;
    private Context mContext=null;

    private int singleChoiceListPosition=-1;
    private List<MultiChoiceItem> multiChoiceList;

    public static final int NORMAL_TYPE = 0;
    public static final int ERROR_TYPE = 1;
    public static final int SUCCESS_TYPE = 2;
    public static final int WARNING_TYPE = 3;
    public static final int CUSTOM_IMAGE_TYPE = 4;
    public static final int PROGRESS_TYPE = 5;

    public static interface OnSweetClickListener {
        public void onClick (SweetAlertDialog sweetAlertDialog);
    }

    /**
     * 新建一个normal弹窗
     * @param context
     */
    public SweetAlertDialog(Context context) {
        this(context, NORMAL_TYPE);
    }
    /**
     * 获取单选列表被选择的位置
     * @return
     */
    public int getSingleChoiceListPosition()
    {
        return singleChoiceListPosition;
    }
    /**
     * 该方法要在<b>dialog.show()</b>方法调用之后调用<br>
     * 将一个.xml布局加载进来，作为自定义dialog体
     * @param resource
     * @return
     */
    public View setView(int resource)
    {
        View view;
        LayoutInflater inflater=LayoutInflater.from(mContext);
        view=inflater.inflate(resource, null);
        contentLayout.addView(view);
        contentLayout.setVisibility(View.VISIBLE);

        mContentTextView.setVisibility(View.INVISIBLE);

        contentView=view;
        return contentView;
    }
    /**
     * 该方法要在<b>dialog.show()</b>方法调用之后调用<br>
     * 设置dialog的中间自定义提示部分
     * @param view
     * @return
     */
    public View setView(View view)
    {
        contentLayout.addView(view);
        contentLayout.setVisibility(View.VISIBLE);

        mContentTextView.setVisibility(View.INVISIBLE);

        contentView=view;
        return contentView;
    }
    /**
     * 移除dialog中间自定义提示的部分<br>
     * 该方法要在dialog.show()方法调用之后调用
     */
    public void removeView()
    {
        contentLayout.removeAllViews();
        contentLayout.setVisibility(View.GONE);
    }
    /**
     * 获取dialog的View，如果不存在则返回null
     * @return
     */
    public View getView()
    {
        return contentView;
    }
    /**
     * @param context
     * @param alertType
     * @see  alertType
     * @see 0、正常(normal)弹窗；
     * @see 1、错误(error)弹窗；
     * @see 2、成功(success)弹窗；
     * @see 3、警告(warning)弹窗；
     * @see 4、(CUSTOM_IMAGE_TYPE)用户自定义图片弹窗；
     * @see 5、进度条(progress)弹窗
     */
    public SweetAlertDialog(Context context, int alertType) {
        super(context, R.style.alert_dialog);

        //DX
        mContext=context;

        setCancelable(true);
        setCanceledOnTouchOutside(false);
        mProgressHelper = new ProgressHelper(context);
        mAlertType = alertType;
        mErrorInAnim = OptAnimationLoader.loadAnimation(getContext(), R.anim.error_frame_in);
        mErrorXInAnim = (AnimationSet)OptAnimationLoader.loadAnimation(getContext(), R.anim.error_x_in);
        // 2.3.x system don't support alpha-animation on layer-list drawable
        // remove it from animation set
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
            List<Animation> childAnims = mErrorXInAnim.getAnimations();
            int idx = 0;
            for (;idx < childAnims.size();idx++) {
                if (childAnims.get(idx) instanceof AlphaAnimation) {
                    break;
                }
            }
            if (idx < childAnims.size()) {
                childAnims.remove(idx);
            }
        }
        mSuccessBowAnim = OptAnimationLoader.loadAnimation(getContext(), R.anim.success_bow_roate);
        mSuccessLayoutAnimSet = (AnimationSet)OptAnimationLoader.loadAnimation(getContext(), R.anim.success_mask_layout);
        mModalInAnim = (AnimationSet) OptAnimationLoader.loadAnimation(getContext(), R.anim.modal_in);
        mModalOutAnim = (AnimationSet) OptAnimationLoader.loadAnimation(getContext(), R.anim.modal_out);
        mModalOutAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mDialogView.setVisibility(View.GONE);
                mDialogView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mCloseFromCancel) {
                            SweetAlertDialog.super.cancel();
                        } else {
                            SweetAlertDialog.super.dismiss();
                        }
                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        // dialog overlay fade out
        mOverlayOutAnim = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                WindowManager.LayoutParams wlp = getWindow().getAttributes();
                wlp.alpha = 1 - interpolatedTime;
                getWindow().setAttributes(wlp);
            }
        };
        mOverlayOutAnim.setDuration(120);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert_dialog);

        //DX
        contentLayout=(LinearLayout) findViewById(R.id.content_layout);

        mDialogView = getWindow().getDecorView().findViewById(android.R.id.content);
        mTitleTextView = (TextView)findViewById(R.id.title_text);
        mContentTextView = (TextView)findViewById(R.id.content_text);
        mErrorFrame = (FrameLayout)findViewById(R.id.error_frame);
        mErrorX = (ImageView)mErrorFrame.findViewById(R.id.error_x);
        mSuccessFrame = (FrameLayout)findViewById(R.id.success_frame);
        mProgressFrame = (FrameLayout)findViewById(R.id.progress_dialog);
        mSuccessTick = (SuccessTickView)mSuccessFrame.findViewById(R.id.success_tick);
        mSuccessLeftMask = mSuccessFrame.findViewById(R.id.mask_left);
        mSuccessRightMask = mSuccessFrame.findViewById(R.id.mask_right);
        mCustomImage = (ImageView)findViewById(R.id.custom_image);
        mWarningFrame = (FrameLayout)findViewById(R.id.warning_frame);
        mConfirmButton = (Button)findViewById(R.id.confirm_button);
        mCancelButton = (Button)findViewById(R.id.cancel_button);
        mProgressHelper.setProgressWheel((ProgressWheel)findViewById(R.id.progressWheel));
        mConfirmButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);

        setTitleText(mTitleText);
        setContentText(mContentText);
        setCancelText(mCancelText);
        setConfirmText(mConfirmText);
        changeAlertType(mAlertType, true);

    }

    private void restore () {
        mCustomImage.setVisibility(View.GONE);
        mErrorFrame.setVisibility(View.GONE);
        mSuccessFrame.setVisibility(View.GONE);
        mWarningFrame.setVisibility(View.GONE);
        mProgressFrame.setVisibility(View.GONE);
        mConfirmButton.setVisibility(View.VISIBLE);

        mConfirmButton.setBackgroundResource(R.drawable.blue_button_background);
        mErrorFrame.clearAnimation();
        mErrorX.clearAnimation();
        mSuccessTick.clearAnimation();
        mSuccessLeftMask.clearAnimation();
        mSuccessRightMask.clearAnimation();
    }

    private void playAnimation () {
        if (mAlertType == ERROR_TYPE) {
            mErrorFrame.startAnimation(mErrorInAnim);
            mErrorX.startAnimation(mErrorXInAnim);
        } else if (mAlertType == SUCCESS_TYPE) {
            mSuccessTick.startTickAnim();
            mSuccessRightMask.startAnimation(mSuccessBowAnim);
        }
    }

    private void changeAlertType(int alertType, boolean fromCreate) {
        mAlertType = alertType;
        // call after created views
        if (mDialogView != null) {
            if (!fromCreate) {
                // restore all of views state before switching alert type
                restore();
            }
            switch (mAlertType) {
                case ERROR_TYPE:
                    mErrorFrame.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS_TYPE:
                    mSuccessFrame.setVisibility(View.VISIBLE);
                    // initial rotate layout of success mask
                    mSuccessLeftMask.startAnimation(mSuccessLayoutAnimSet.getAnimations().get(0));
                    mSuccessRightMask.startAnimation(mSuccessLayoutAnimSet.getAnimations().get(1));
                    break;
                case WARNING_TYPE:
                    mConfirmButton.setBackgroundResource(R.drawable.red_button_background);
                    mWarningFrame.setVisibility(View.VISIBLE);
                    break;
                case CUSTOM_IMAGE_TYPE:
                    setCustomImage(mCustomImgDrawable);
                    break;
                case PROGRESS_TYPE:
                    mProgressFrame.setVisibility(View.VISIBLE);
                    mConfirmButton.setVisibility(View.GONE);
                    break;
            }
            if (!fromCreate) {
                playAnimation();
            }
        }
    }

    /**
     * 返回弹窗类型<br>
     * 0、正常(normal)弹窗；<br>
     * 1、错误(error)弹窗；<br>
     * 2、成功(success)弹窗；<br>
     * 3、警告(warning)弹窗；<br>
     * 4、(CUSTOM_IMAGE_TYPE)用户自定义图片弹窗；<br>
     * 5、进度条(progress)弹窗<br>
     * @return
     */
    public int getAlerType () {
        return mAlertType;
    }

    /**
     * 更改弹窗类型<br>
     * 0、正常(normal)弹窗；<br>
     * 1、错误(error)弹窗；<br>
     * 2、成功(success)弹窗；<br>
     * 3、警告(warning)弹窗；<br>
     * 4、(CUSTOM_IMAGE_TYPE)用户自定义图片弹窗；<br>
     * 5、进度条(progress)弹窗<br>
     * @param alertType
     */
    public void changeAlertType(int alertType) {
        changeAlertType(alertType, false);
    }


    /**
     * 获取弹窗标题字段
     * @return
     */
    public String getTitleText () {
        return mTitleText;
    }

    /**
     * 设置弹窗标题
     * @param text
     * @return
     */
    public SweetAlertDialog setTitleText (String text) {
        mTitleText = text;
        if (mTitleTextView != null && mTitleText != null) {
            mTitleTextView.setText(mTitleText);
        }
        return this;
    }

    /**
     * 设置弹窗Image，弹窗类型（alertType）必须是4（用户自定义类型）才行
     * @param drawable
     * @return
     */
    public SweetAlertDialog setCustomImage (Drawable drawable) {
        mCustomImgDrawable = drawable;
        if (mCustomImage != null && mCustomImgDrawable != null) {
            mCustomImage.setVisibility(View.VISIBLE);
            mCustomImage.setImageDrawable(mCustomImgDrawable);
        }
        return this;
    }

    /**
     * 设置弹窗Image，弹窗类型（alertType）必须是4（用户自定义类型）才行
     * @param resourceId
     * @return
     */
    public SweetAlertDialog setCustomImage (int resourceId) {
        return setCustomImage(getContext().getResources().getDrawable(resourceId));
    }

    /**
     * 获取dialog的提示内容
     * @return
     */
    public String getContentText () {
        return mContentText;
    }

    /**
     * 设置dialog的提示内容
     * @param text
     * @return
     */
    public SweetAlertDialog setContentText (String text) {
        mContentText = text;
        if (mContentTextView != null && mContentText != null) {
            showContentText(true);
            mContentTextView.setText(mContentText);
        }
        return this;
    }


    public boolean isShowCancelButton () {
        return mShowCancel;
    }

    public SweetAlertDialog showCancelButton (boolean isShow) {
        mShowCancel = isShow;
        if (mCancelButton != null) {
            mCancelButton.setVisibility(mShowCancel ? View.VISIBLE : View.GONE);
        }
        return this;
    }

    public boolean isShowContentText () {
        return mShowContent;
    }

    public SweetAlertDialog showContentText (boolean isShow) {
        mShowContent = isShow;
        if (mContentTextView != null) {
            mContentTextView.setVisibility(mShowContent ? View.VISIBLE : View.GONE);
        }
        return this;
    }

    public String getCancelText () {
        return mCancelText;
    }

    public SweetAlertDialog setCancelText (String text) {
        mCancelText = text;
        if (mCancelButton != null && mCancelText != null) {
            showCancelButton(true);
            mCancelButton.setText(mCancelText);
        }
        return this;
    }

    public String getConfirmText () {
        return mConfirmText;
    }

    public SweetAlertDialog setConfirmText (String text) {
        mConfirmText = text;
        if (mConfirmButton != null && mConfirmText != null) {
            mConfirmButton.setText(mConfirmText);
        }
        return this;
    }

    /**
     * 设置单选列表的最大显示行数
     * @param listView
     * @param maxHeight
     */
    private void setSingleChoiceListViewHeightBasedOnChildren(ListView listView,int maxHeight)
    {
        if(maxHeight<0)
        {
            maxHeight=6;
        }
        SingleChoiceAdapter adapter=(SingleChoiceAdapter) listView.getAdapter();
        if(adapter==null)
        {
            return;
        }
        int totalHeight=0;
        if(adapter.getCount()<maxHeight)
        {
            for(int i=0;i<adapter.getCount();i++)
            {
                View listItem=adapter.getView(i, null, listView);
                listItem.measure(0, 0);
                totalHeight+=listItem.getMeasuredHeight();
            }
        }else {
            for(int i=0;i<maxHeight;i++)
            {
                View listItem=adapter.getView(i, null, listView);
                listItem.measure(0, 0);
                totalHeight+=listItem.getMeasuredHeight();
            }
        }
        ViewGroup.LayoutParams params=listView.getLayoutParams();
        params.height=totalHeight+(listView.getDividerHeight()*adapter.getCount()-1);
        listView.setLayoutParams(params);
    }
    /**
     * 设置多选列表的最大显示行数
     * @param listView
     * @param maxHeight
     */
    private void setMultiChoiceListViewHeightBasedOnChildren(ListView listView,int maxHeight)
    {
        if(maxHeight<0)
        {
            maxHeight=6;
        }
        MultiChoiceAdapter adapter=(MultiChoiceAdapter) listView.getAdapter();
        if(adapter==null)
        {
            return;
        }
        int totalHeight=0;
        if(adapter.getCount()<maxHeight)
        {
            for(int i=0;i<adapter.getCount();i++)
            {
                View listItem=adapter.getView(i, null, listView);
                listItem.measure(0, 0);
                totalHeight+=listItem.getMeasuredHeight();
            }
        }else {
            for(int i=0;i<maxHeight;i++)
            {
                View listItem=adapter.getView(i, null, listView);
                listItem.measure(0, 0);
                totalHeight+=listItem.getMeasuredHeight();
            }
        }
        ViewGroup.LayoutParams params=listView.getLayoutParams();
        params.height=totalHeight+(listView.getDividerHeight()*adapter.getCount()-1);
        listView.setLayoutParams(params);
    }
    /**
     * 获取所有的多选对话框记录
     * @return
     */
    public List<Boolean> getMultiChoiceChecked()
    {
        List<Boolean> multiChoiceBooleans=new ArrayList<Boolean>();
        for(int i=0;i<multiChoiceList.size();i++)
        {
            multiChoiceBooleans.add(multiChoiceList.get(i).getChecked());
        }
        return multiChoiceBooleans;
    }
    /**
     * 设置多选列表,在dialog.show()方法之后调用
     * @param items
     * 填充的数据
     * @param maxHeight
     * 最大显示多少行,当传入-1时，按默认的显示
     * @return
     */
    public SweetAlertDialog setMultiChoiceItems(String[] items,int maxHeight)
    {
        List<String> itemList=new ArrayList<String>();
        for(int i=0;i<items.length;i++)
        {
            itemList.add(items[i]);
        }
        return setMultiChoiceItems(itemList,maxHeight);
    }
    /**
     * 设置多选列表,在dialog.show()方法之后调用
     * @param items
     * 填充的数据
     * @param maxHeight
     * 最大显示多少行,当传入-1时，按默认的显示
     * @return
     */
    public SweetAlertDialog setMultiChoiceItems(final List<String> items,int maxHeight)
    {
        final List<MultiChoiceItem> itemList=new ArrayList<MultiChoiceItem>();
        for(int i=0;i<items.size();i++)
        {
            itemList.add(new MultiChoiceItem(items.get(i), false));
        }
        //保存到本类中
        multiChoiceList=itemList;
        final MultiChoiceAdapter adapter=new MultiChoiceAdapter(mContext, R.layout.multi_choice_item, itemList);
        View view=setView(R.layout.multi_choice_list_layout);
        CheckBox all_check_box=(CheckBox) view.findViewById(R.id.all_check_box);
        TextView inverse_tv=(TextView) view.findViewById(R.id.inverse_tv);
        ListView lv=(ListView) view.findViewById(R.id.multi_choice_list);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                boolean checkedTemp=itemList.get(position).getChecked();
                itemList.get(position).setChecked(!checkedTemp);
                adapter.notifyDataSetChanged();
            }
        });
        all_check_box.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean checked) {
                if(checked)
                {
                    for(int i=0;i<itemList.size();i++)
                    {
                        itemList.get(i).setChecked(true);
                    }
                }else {
                    for(int i=0;i<itemList.size();i++)
                    {
                        itemList.get(i).setChecked(false);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
        inverse_tv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                for(int i=0;i<itemList.size();i++)
                {
                    if(itemList.get(i).getChecked())
                    {
                        itemList.get(i).setChecked(false);
                    }else {
                        itemList.get(i).setChecked(true);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
        contentView=view;
        setMultiChoiceListViewHeightBasedOnChildren(lv,maxHeight);
        return this;
    }
    /**
     * 设置单选列表,在dialog.show()方法之后调用
     * DX改
     * @param items
     * 填充的数据
     * @param maxHeight
     * 最大显示多少行,当传入-1时，按默认的显示
     * @param position
     * 默认选择的位置
     * @return
     */
    public SweetAlertDialog setSingleChoiceItems(String[] items,int maxHeight,int position)
    {
        List<String> itemList=new ArrayList<String>();
        for(int i=0;i<items.length;i++)
        {
            itemList.add(items[i]);
        }
        return setSingleChoiceItems(itemList,maxHeight,position);
    }

    /**
     * 设置单选列表,在dialog.show()方法之后调用
     * DX改
     * @param items
     * 填充的数据
     * @param maxHeight
     * 最大显示多少行,当传入-1时，按默认的显示
     * @param position
     * 默认选择的位置
     * @return
     */
    public SweetAlertDialog setSingleChoiceItems(final List<String> items,int maxHeight,int position){
        singleChoiceListPosition=position;
        final SingleChoiceAdapter adapter=new SingleChoiceAdapter(mContext, R.layout.single_choice_item, items);
        View view=setView(R.layout.single_choice_list_layout);
        ListView lv=(ListView) view.findViewById(R.id.single_choice_list);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                adapter.setSelectedIndex(position);
                singleChoiceListPosition=position;
                adapter.notifyDataSetChanged();
            }
        });
        adapter.setSelectedIndex(position);
        contentView=view;
        setSingleChoiceListViewHeightBasedOnChildren(lv,maxHeight);
        return this;
    }
    /**
     * 设置取消监听器
     * @param listener
     * @return
     */
    public SweetAlertDialog setCancelClickListener (OnSweetClickListener listener) {
        mCancelClickListener = listener;
        return this;
    }

    /**
     * 设置确定监听器
     * @param listener
     * @return
     */
    public SweetAlertDialog setConfirmClickListener (OnSweetClickListener listener) {
        mConfirmClickListener = listener;
        return this;
    }

    protected void onStart() {
        mDialogView.startAnimation(mModalInAnim);
        playAnimation();
    }

    /**
     * The real Dialog.cancel() will be invoked async-ly after the animation finishes.
     * @see Dialog.cacel()方法将会在动画结束后异步唤醒执行
     */
    @Override
    public void cancel() {
        dismissWithAnimation(true);
    }

    /**
     * The real Dialog.dismiss() will be invoked async-ly after the animation finishes.
     * @see Dialog.dismiss()方法将会在动画结束后异步唤醒执行
     */
    public void dismissWithAnimation() {
        dismissWithAnimation(false);
    }

    private void dismissWithAnimation(boolean fromCancel) {
        mCloseFromCancel = fromCancel;
        mConfirmButton.startAnimation(mOverlayOutAnim);
        mDialogView.startAnimation(mModalOutAnim);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.cancel_button) {
            if (mCancelClickListener != null) {
                mCancelClickListener.onClick(SweetAlertDialog.this);
            } else {
                dismissWithAnimation();
            }
        } else if (v.getId() == R.id.confirm_button) {
            if (mConfirmClickListener != null) {
                mConfirmClickListener.onClick(SweetAlertDialog.this);
            } else {
                dismissWithAnimation();
            }
        }
    }

    public ProgressHelper getProgressHelper () {
        return mProgressHelper;
    }
}