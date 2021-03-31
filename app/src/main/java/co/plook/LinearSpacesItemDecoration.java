package co.plook;

import android.content.Context;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class LinearSpacesItemDecoration extends RecyclerView.ItemDecoration
{
    private final int spacing;

    public LinearSpacesItemDecoration(Context context, int spacing)
    {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        this.spacing = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, spacing, metrics);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state)
    {
        super.getItemOffsets(outRect, view, parent, state);

        outRect.top = spacing;

        if(parent.getAdapter() != null && parent.getChildLayoutPosition(view) == parent.getAdapter().getItemCount() - 1)
            outRect.bottom = spacing;
    }
}
