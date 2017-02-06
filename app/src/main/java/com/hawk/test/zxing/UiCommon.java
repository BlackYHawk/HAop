package com.hawk.test.zxing;

import android.content.Context;

/**
 * Created by lan on 2017/1/12.
 */

public class UiCommon {
    public static int widthPixels = 0;

    /**
     * 计算相对应的长度
     * @param dip
     * @return float
     */
    public static int convertDip2Pixel(Context context, int dip) {
        final float scale = context.getApplicationContext().getResources().getDisplayMetrics().density;
        int pixel = (int) (dip * scale + 0.5f);
        return pixel;
    }

}
