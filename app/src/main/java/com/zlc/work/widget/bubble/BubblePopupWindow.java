package com.zlc.work.widget.bubble;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;


public class BubblePopupWindow extends PopupWindow {

  public static int DEFAULT_MARGIN;
  private BubbleLinearLayout bubbleView;
  private Context context;
  private TextView mTextView;
  private int mWidth;
  private int mHeight;
  // 是否显示虚拟键盘
  private boolean isHideVirtualKey;
  //默认箭头中间，可以自己设定偏移量
  private int mXOffset;
  //默认箭头中间，可以自己设定偏移量
  private int mYOffset;
  private boolean isAlreadyDismiss;
  private int mGravity;

  private AnimatorSet set;

  private Runnable mDismissRunnable = new Runnable() {
    @Override
    public void run() {
      animatorEasyInOut(false, mGravity);
    }
  };

  public BubblePopupWindow(Context context) {
    super(context);
    this.context = context;
    DEFAULT_MARGIN = (int) dip2Px(context, 3);
    setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
    setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
    setFocusable(false);
    setOutsideTouchable(false);
    setClippingEnabled(false);

    ColorDrawable dw = new ColorDrawable(0);
    setBackgroundDrawable(dw);
    setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
    setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    defaultView();
  }

  /**
   * 默认的布局，就一个textview，添加文字
   */
  private void defaultView() {
    mTextView = new TextView(context);
    //mTextView.setTextColor(context.getResources().getColor(R.color.s1));
    mTextView.setTextSize(13);
    mTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    mTextView.setLines(1);
    mTextView.setGravity(Gravity.CENTER);
    setBubbleView(mTextView); // 设置气泡内容
    isHideVirtualKey = true;
    getContentView().measure(View.MeasureSpec.AT_MOST, View.MeasureSpec.AT_MOST);
  }

  /**
   * 设置显示的view
   *
   * @param view 气泡中需要显示的view
   */
  public void setBubbleView(View view) {
    bubbleView = new BubbleLinearLayout(context);
    bubbleView.setBackgroundColor(Color.TRANSPARENT);
    bubbleView.addView(view);
    bubbleView.setGravity(Gravity.CENTER);
    ViewGroup.MarginLayoutParams layoutParams = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    bubbleView.setLayoutParams(layoutParams);
    bubbleView.setVisibility(View.GONE);
    bubbleView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dismiss();
      }
    });
    setContentView(bubbleView);
  }

  /**
   * 隐藏虚拟键盘
   */
  private void hideStatusBar() {
    if (!isHideVirtualKey) {
      return;
    }
    //隐藏虚拟按键，并且全屏
    if (Build.VERSION.SDK_INT < 19) { // lower api
      getContentView().setSystemUiVisibility(View.GONE);
    } else if (Build.VERSION.SDK_INT >= 19) {
      //for new api versions.
      int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
          | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
      getContentView().setSystemUiVisibility(uiOptions);
    }
  }

  /**
   * @param b 是否显示虚拟键盘
   */
  public void setHideVirtualKey(boolean b) {
    isHideVirtualKey = b;
  }


  /**
   * 设置布局的大小
   *
   * @param width  宽度
   * @param height 高度
   */
  public void setParam(int width, int height) {
    mWidth = width;
    mHeight = height;
    setWidth(width);
    setHeight(height);
  }

  public void setBubbleText(String showText) {
    mTextView.setText(showText);
  }

  public void setBubbleText(int showTextResId) {
    mTextView.setText(showTextResId);
  }

  public void show(View parent) {
    show(parent, Gravity.BOTTOM, true, 0);
  }

  public void show(View parent, int gravity) {

    show(parent, gravity, true, 0);
  }


  public void setXOffset(int xOffset) {
    mXOffset = xOffset;
  }

  public void setYOffset(int yOffset) {
    mYOffset = yOffset;
  }


  /**
   * @param parent       展示的view
   * @param gravity      位置
   * @param isMiddle     默认位置是否是中间
   * @param bubbleOffset 偏移量
   */
  public void show(View parent, int gravity, boolean isMiddle, float bubbleOffset) {
    if (set != null) {
      set.cancel();
      set.removeAllListeners();
    }
    getContentView().removeCallbacks(mDismissRunnable);
    mGravity = gravity;
    @BubbleLinearLayout.BubbleOrientation int orientation = BubbleLinearLayout.LEFT;
    if (!this.isShowing()) {
      switch (gravity) {
        case Gravity.BOTTOM:
          orientation = BubbleLinearLayout.TOP;
          break;
        case Gravity.TOP:
          orientation = BubbleLinearLayout.BOTTOM;
          break;
        case Gravity.RIGHT:
          orientation = BubbleLinearLayout.LEFT;
          break;
        case Gravity.LEFT:
          orientation = BubbleLinearLayout.RIGHT;
          break;
        default:
          break;
      }
      if (mWidth != 0 && mHeight != 0) {
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(mWidth, View.MeasureSpec.EXACTLY);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(mHeight, View.MeasureSpec.EXACTLY);
        getContentView().measure(widthMeasureSpec, heightMeasureSpec);
      } else {
        getContentView().measure(View.MeasureSpec.AT_MOST, View.MeasureSpec.AT_MOST);
      }

      //气泡尖角位置偏移量。默认位于中间
      if (isMiddle) {
        if (gravity == Gravity.BOTTOM || gravity == Gravity.TOP) {
          bubbleOffset = getMeasuredWidth() / 2;
        } else {
          bubbleOffset = getMeasureHeight() / 2;
        }
      }
      bubbleView.setBubbleParams(orientation, bubbleOffset); // 设置气泡布局方向及尖角偏移
      int[] location = new int[2];
      parent.getLocationOnScreen(location);
      hideStatusBar();
      int middleSize = 0;
      switch (gravity) {
        case Gravity.BOTTOM:
          //设置进入退出动画
          // 使箭头指向parent的中部
          if (isMiddle) {
            middleSize = (parent.getMeasuredWidth() - getMeasuredWidth()) / 2;
          }
          showAsDropDown(parent, mXOffset + middleSize, DEFAULT_MARGIN + mYOffset);
          animatorEasyInOut(true, gravity);
          break;
        case Gravity.TOP:
          if (isMiddle) {
            middleSize = (parent.getMeasuredWidth() - getMeasuredWidth()) / 2;
          }
          showAtLocation(parent, Gravity.NO_GRAVITY, location[0] + mXOffset + middleSize, location[1] - getMeasureHeight() + mYOffset - DEFAULT_MARGIN);
          animatorEasyInOut(true, gravity);
          break;
        case Gravity.RIGHT:
          if (isMiddle) {
            middleSize = (parent.getMeasuredHeight() - getMeasureHeight()) / 2;
          }
          showAtLocation(parent, Gravity.NO_GRAVITY, location[0] + mXOffset + parent.getWidth() + DEFAULT_MARGIN, location[1] + mYOffset - middleSize);
          animatorEasyInOut(true, gravity);
          break;
        case Gravity.LEFT:
          if (isMiddle) {
            middleSize = (parent.getMeasuredHeight() - getMeasureHeight()) / 2;
          }
          showAtLocation(parent, Gravity.NO_GRAVITY, location[0] + mXOffset - getMeasuredWidth() - DEFAULT_MARGIN, location[1] + mYOffset - middleSize);
          animatorEasyInOut(true, gravity);
          break;
        default:
          break;
      }
      isAlreadyDismiss = false;
      getContentView().postDelayed(mDismissRunnable, 7000);
    } else {
      super.dismiss();
    }
  }

  private void animatorEasyInOut(final boolean isIn, final int gravity) {
    if (!isShowing()) {
      return;
    }
    final View view = bubbleView;
    if (!isIn) {
      isAlreadyDismiss = true;
    }
    if (set == null) {
      set = new AnimatorSet();
    }
    view.post(new Runnable() {
      @Override
      public void run() {
        if (!isShowing() || set == null) {
          return;
        }
        int pivotX = 0;
        int pivotY = 0;
        switch (gravity) {
          case Gravity.BOTTOM:
            pivotX = (int) (view.getX() + bubbleView.getBubbleOffset());
            pivotY = (int) (view.getY());
            break;
          case Gravity.TOP:
            pivotX = (int) (view.getX() + bubbleView.getBubbleOffset());
            pivotY = (int) (view.getY() + view.getMeasuredHeight());
            break;
          case Gravity.RIGHT:
            pivotX = (int) view.getX();
            pivotY = (int) (view.getY() + bubbleView.getBubbleOffset());
            break;
          case Gravity.LEFT:
            pivotX = (int) (view.getX() + view.getMeasuredWidth());
            pivotY = (int) (view.getY() + bubbleView.getBubbleOffset());
            break;
          default:
            break;
        }
        view.setPivotY(pivotY);
        view.setPivotX(pivotX);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", isIn ? 0 : 1f, isIn ? 1.00f : 0, isIn ? 1f : 0);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", isIn ? 0 : 1f, isIn ? 1.00f : 0, isIn ? 1f : 0);

        set.play(scaleX).with(scaleY);
        set.setDuration(isIn ? 800 : 200);
        set.addListener(new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            if (!isIn) {
              view.setVisibility(View.GONE);
              BubblePopupWindow.super.dismiss();
            }
          }

          @Override
          public void onAnimationStart(Animator animation) {
            super.onAnimationStart(animation);
            if (isIn) {
              view.setVisibility(View.VISIBLE);
            }
          }
        });
        set.start();
      }
    });
  }


  public void dismiss() {
    if (!isAlreadyDismiss) {
      animatorEasyInOut(false, mGravity);
      getContentView().removeCallbacks(mDismissRunnable);
      mXOffset = 0;
      mYOffset = 0;
    }
  }


  /**
   * @return 测量高度
   */
  public int getMeasureHeight() {
    return getContentView().getMeasuredHeight();
  }

  /**
   * @return 测量宽度
   */
  public int getMeasuredWidth() {
    return getContentView().getMeasuredWidth();
  }

  /**
   * 在结束时，需要调用此方法，防止内崔泄漏
   */
  public void onDestroy() {
    if (set != null) {
      set.cancel();
      set = null;
    }
    super.dismiss();
  }

  public static float dip2Px(Context context, float dipValue) {
    final float scale = context.getResources().getDisplayMetrics().density;
    return dipValue * scale + 0.5f;
  }
}