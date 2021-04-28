package co.plook;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;

public class TagLayout extends LinearLayout {

    private int mChildSpacingX;
    private int mChildSpacingY;
    private OnItemClickListener onItemClickListener;

    public TagLayout(Context context) {
        super(context);
    }

    public TagLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TagLayout, 0, 0);

        try
        {
            mChildSpacingX = a.getDimensionPixelSize(R.styleable.TagLayout_childSpacingX, 0);
            mChildSpacingY = a.getDimensionPixelSize(R.styleable.TagLayout_childSpacingY, 0);
        }
        finally {
            a.recycle();
        }
    }

    public TagLayout(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TagLayout, 0, 0);

        try
        {
            mChildSpacingX = a.getDimensionPixelSize(R.styleable.TagLayout_childSpacingX, 0);
            mChildSpacingY = a.getDimensionPixelSize(R.styleable.TagLayout_childSpacingY, 0);
        }
        finally {
            a.recycle();
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int count = getChildCount();
        int currentRowWidth = 0;
        int currentRowHeight = 0;
        int maxItemWidth = 0;
        int maxWidth = 0;
        int maxHeight = 0;

        int availableWidth = this.getMeasuredWidth() - this.getPaddingRight();

        for (int i = 0; i < count; i++)
        {
            View child = getChildAt(i);

            if (child.getVisibility() == GONE)
                continue;

            try
            {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            }
            catch(Exception e)
            {
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
            }

            int childWidth = child.getMeasuredWidth() + child.getPaddingRight() + child.getPaddingLeft() + mChildSpacingX;
            int childHeight = child.getMeasuredHeight() + child.getPaddingTop() + child.getPaddingBottom() + mChildSpacingY;

            maxItemWidth = Math.max(maxItemWidth, childWidth);

            if (currentRowWidth + childWidth < availableWidth)
            {
                currentRowWidth += childWidth;
                maxWidth = Math.max(maxWidth, currentRowWidth);
                currentRowHeight = Math.max(currentRowHeight, childHeight);
            }
            else
            {
                currentRowWidth = childWidth;
                maxHeight += currentRowHeight;
            }
        }

        maxHeight += currentRowHeight + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(widthMeasureSpec, maxHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
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


        for (int i = 0; i < count; i++)
        {
            View child = getChildAt(i);

            if (child.getVisibility() == GONE)
                return;

            //Get the maximum size of the child
            child.measure(MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.AT_MOST));
            curWidth = child.getMeasuredWidth();
            curHeight = child.getMeasuredHeight();

            //wrap is reach to the end
            if (curLeft + curWidth + mChildSpacingX >= childRight)
            {
                curLeft = childLeft;
                curTop += maxHeight;
                maxHeight = 0;
            }
            //do the layout
            child.layout(curLeft, curTop, curLeft + curWidth, curTop + curHeight);
            //store the max height
            if (maxHeight < curHeight)
                maxHeight = curHeight + mChildSpacingY;

            curLeft += curWidth + mChildSpacingX;

        }
    }

    @Override
    public void addView(View child)
    {
        super.addView(child);

        child.findViewById(R.id.tag_delete).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                onItemClickListener.onItemClicked(indexOfChild(child));
            }
        });
    }

    public interface OnItemClickListener
    {
        void onItemClicked(int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener)
    {
        this.onItemClickListener = onItemClickListener;
    }
}

