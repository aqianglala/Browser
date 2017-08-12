package com.news.browser.widget;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by zy1584 on 2017-8-7.
 */

public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
    private int space;
    private int columnNum;

    public SpacesItemDecoration(int space, int columnNum) {
        this.space = space;
        this.columnNum = columnNum;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {

        outRect.left = space;
        outRect.bottom = space;
        if (parent.getChildLayoutPosition(view) % columnNum == 0) {
            outRect.left = 0;
        }
    }
}