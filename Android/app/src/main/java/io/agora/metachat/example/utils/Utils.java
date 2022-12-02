package io.agora.metachat.example.utils;

import android.content.Context;

public class Utils {
    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     */

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    
}
