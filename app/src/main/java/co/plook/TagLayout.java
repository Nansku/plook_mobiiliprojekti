package co.plook;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.widget.LinearLayout;

public class TagLayout extends LinearLayout {
    private int mScreenWidth = 0;
    private int mAvailableWidth = -1;
    boolean expanded = false;

    public TagLayout(Context context) {
        super(context);
        init(context);
    }

    public TagLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TagLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        mScreenWidth = displayMetrics.widthPixels;
    }

    public boolean isExpanded()
    {
        return expanded;
    }

    // TAKEN FROM EXPANDABLE GRIDVIEW
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (isExpanded()) {
            int expandSpec = MeasureSpec.makeMeasureSpec(MEASURED_SIZE_MASK, MeasureSpec.AT_MOST);
            super.onMeasure(widthMeasureSpec, expandSpec);

            ViewGroup.LayoutParams params = getLayoutParams();
            params.height = getMeasuredHeight();
        }
        else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }


    /*@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        int currentRowWidth = 0;
        int currentRowHeight = 0;
        int maxItemWidth = 0;
        int maxWidth = 0;
        int maxHeight = 0;

        if(mAvailableWidth == -1)
            calculateAvailableWidth();

        for(int i = 0; i < count; i++) {
            View child = getChildAt(i);

            if(child.getVisibility() == GONE)
                continue;

            try {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            }
            catch(Exception e) {
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
            }

            int childWidth = child.getMeasuredWidth() + child.getPaddingRight() + child.getPaddingLeft();
            int childHeight = child.getMeasuredHeight() + child.getPaddingTop() + child.getPaddingBottom();

            maxItemWidth = Math.max(maxItemWidth, childWidth);

            if(currentRowWidth + childWidth < mAvailableWidth) {
                currentRowWidth += childWidth;
                maxWidth = Math.max(maxWidth, currentRowWidth);
                currentRowHeight = Math.max(currentRowHeight, childHeight);
            }
            else {
                currentRowWidth = childWidth;
                maxHeight += currentRowHeight;
            }
        }

        if(getLayoutParams().width == LayoutParams.WRAP_CONTENT) {
            mAvailableWidth = maxItemWidth;
            maxWidth = maxItemWidth;
        }

        maxHeight += currentRowHeight + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(maxWidth, 50);
    }

    private void calculateAvailableWidth() {

        if(getLayoutParams() != null && getLayoutParams().width > 0) {
            mAvailableWidth = getLayoutParams().width;
            return;
        }

        mAvailableWidth = mScreenWidth;

        ViewGroup parent = this;

        while(parent != null) {

            mAvailableWidth -= parent.getPaddingLeft() + parent.getPaddingRight();

            if(parent.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams)parent.getLayoutParams();
                mAvailableWidth -= layoutParams.leftMargin + layoutParams.rightMargin;
            }

            if(parent.getParent() instanceof ViewGroup)
                parent = (ViewGroup)parent.getParent();
            else
                parent = null;
        }
    }*/

    /*@Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();
        int curWidth, curHeight, curLeft, curTop, maxHeight;

        //get the available size of child view
        final int childLeft = this.getPaddingLeft();
        final int childTop = this.getPaddingTop();

        final int childRight = this.getMeasuredWidth() - this.getPaddingRight();
        final int childBottom = this.getMeasuredHeight() - this.getPaddingBottom();

        final int childWidth = childRight - childLeft;
        final int childHeight = childBottom - childTop;

        maxHeight = 0;
        curLeft = childLeft;
        curTop = childTop;
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE)
                return;

            //Get the maximum size of the child
            child.measure(MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.AT_MOST));
            curWidth = child.getMeasuredWidth();
            curHeight = child.getMeasuredHeight();
            //wrap is reach to the end
            if (curLeft + curWidth >= childRight) {
                curLeft = childLeft;
                curTop += maxHeight;
                maxHeight = 0;
            }
            //do the layout
            child.layout(curLeft, curTop, curLeft + curWidth, curTop + curHeight);
            //store the max height
            if (maxHeight < curHeight)
                maxHeight = curHeight;
            curLeft += curWidth;
        }*/
}

