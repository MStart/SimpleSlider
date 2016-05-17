package net.cpacm.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.RelativeLayout;

import net.cpacm.library.indicator.PageIndicator;
import net.cpacm.library.infinite.AnimationViewPager;
import net.cpacm.library.infinite.FixedSpeedScroller;
import net.cpacm.library.infinite.InfinitePagerAdapter;
import net.cpacm.library.slider.BaseSliderView;
import net.cpacm.library.animation.OnAnimationListener;

import java.lang.reflect.Field;

/**
 * simple slider
 * Auther: cpacm
 * Date: 2016/3/8.
 */
public class SimpleSliderLayout extends RelativeLayout {

    private Context mContext;
    private InfinitePagerAdapter infinitePagerAdapter;
    private BaseSliderAdapter baseSliderAdapter;
    private AnimationViewPager simpleViewPager;
    private boolean autoCycling = true, isCycling = true;
    private PageIndicator pageIndicator;

    /**
     * the duration between animation.
     */
    private long sliderDuration = 3000;

    private Handler sliderHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            if (msg.what == 0) {
                moveNextPosition(true);
                sliderHandler.sendEmptyMessageDelayed(0, sliderDuration);
            }
        }
    };

    public SimpleSliderLayout(Context context) {
        super(context);
        init(context, null);
    }

    public SimpleSliderLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SimpleSliderLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {
        mContext = context;
        View parent = LayoutInflater.from(mContext).inflate(R.layout.simple_slider_layout, this, true);
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.SimpleSliderLayout);
        simpleViewPager = (AnimationViewPager) parent.findViewById(R.id.simple_slider_viewpager);
        baseSliderAdapter = new BaseSliderAdapter();
        infinitePagerAdapter = new InfinitePagerAdapter(baseSliderAdapter);
        setCycling(true);
        setAutoCycling(true);
    }

    public void setPageTransformer(ViewPager.PageTransformer transformer) {
        simpleViewPager.setPageTransformer(true, transformer);
    }

    public void setAnimationListener(OnAnimationListener listener) {
        simpleViewPager.setAnimationListener(listener);
    }

    /**
     * 利用反射修改ViewPager中Scroller的滑动速度
     *
     * @param period       时长
     * @param interpolator 渲染器
     */
    public void setSliderTransformDuration(int period, Interpolator interpolator) {
        try {
            Field mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            FixedSpeedScroller scroller = new FixedSpeedScroller(simpleViewPager.getContext(), interpolator, period);
            mScroller.set(simpleViewPager, scroller);
        } catch (Exception e) {

        }
    }

    public void setSliderTransformDuration(int period) {
        try {
            Field mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            FixedSpeedScroller scroller = new FixedSpeedScroller(simpleViewPager.getContext(), period);
            mScroller.set(simpleViewPager, scroller);
        } catch (Exception e) {

        }
    }

    public void setViewPagerIndicator(PageIndicator pageIndicator) {
        this.pageIndicator = pageIndicator;
        pageIndicator.setViewPager(simpleViewPager);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE)
            if (autoCycling) stopAutoCycling();
        if (event.getAction() == MotionEvent.ACTION_UP)
            if (autoCycling) startAutoCycling();
        return super.dispatchTouchEvent(event);
    }

    /**
     * if cycling
     * 是否无限循环
     *
     * @param isCycling
     */
    public void setCycling(boolean isCycling) {
        this.isCycling = isCycling;
        if (baseSliderAdapter.getCount() < 4) return;
        if (isCycling) cycling();
        else stopCycling();
    }

    private void cycling() {
        simpleViewPager.setAdapter(infinitePagerAdapter);
    }

    private void stopCycling() {
        simpleViewPager.setAdapter(baseSliderAdapter);
    }

    /**
     * if auto isCycling
     * 是否自动循环
     *
     * @param autoCycling
     */
    public void setAutoCycling(boolean autoCycling) {
        this.autoCycling = autoCycling;
        if (autoCycling) startAutoCycling();
        else stopAutoCycling();
    }

    private void startAutoCycling() {
        sliderHandler.removeMessages(0);
        sliderHandler.sendEmptyMessageDelayed(0, sliderDuration);
    }

    private void stopAutoCycling() {
        sliderHandler.removeMessages(0);
    }

    public void setSliderDuration(long sliderDuration) {
        this.sliderDuration = sliderDuration;
    }

    public <T extends BaseSliderView> void addSlider(T baseSlider) {
        baseSliderAdapter.addSlider(baseSlider);
        judgeCycing();
    }

    public void removeSlider(int position) {
        baseSliderAdapter.removeSliderAt(position);
        judgeCycing();
    }

    public <T extends BaseSliderView> void removeSlider(T baseSlider) {
        baseSliderAdapter.removeSlider(baseSlider);
        judgeCycing();
    }

    private void judgeCycing() {
        if (baseSliderAdapter.getCount() < 4) stopCycling();
        else if (isCycling) cycling();
        else stopCycling();
    }

    public void addOnPageChangeListener(final ViewPager.OnPageChangeListener listener) {
        if (listener == null) return;
        simpleViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                listener.onPageScrolled(position % infinitePagerAdapter.getRealCount(), positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                listener.onPageSelected(position % infinitePagerAdapter.getRealCount());
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                listener.onPageScrollStateChanged(state);
            }
        });
    }

    /**
     * move to prev slide.
     */
    public void movePrevPosition(boolean smooth) {
        if (getRealAdapter() == null)
            throw new IllegalStateException("You did not set a slider adapter");
        simpleViewPager.setCurrentItem(simpleViewPager.getCurrentItem() - 1, smooth);
    }

    /**
     * move to next slide.
     */
    public void moveNextPosition(boolean smooth) {

        if (getRealAdapter() == null)
            throw new IllegalStateException("You did not set a slider adapter");
        if (baseSliderAdapter.getCount() < 4 && simpleViewPager.getCurrentItem() == baseSliderAdapter.getCount() - 1)
            simpleViewPager.setCurrentItem(0, false);
        else simpleViewPager.setCurrentItem(simpleViewPager.getCurrentItem() + 1, smooth);
    }

    private BaseSliderAdapter getRealAdapter() {
        PagerAdapter adapter = simpleViewPager.getAdapter();
        if (adapter != null && adapter instanceof InfinitePagerAdapter) {
            return ((InfinitePagerAdapter) adapter).getRealAdapter();
        }
        return (BaseSliderAdapter) adapter;
    }

    public void moveNextPosition() {
        moveNextPosition(true);
    }
}