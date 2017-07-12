package com.example.zy1584.mybase.utils;

import android.content.Context;
import android.content.Intent;

import java.util.Date;

/**
 * Created by zy1584 on 2017-3-30.
 */

public class MyUtils {

    public static boolean checkNull(Object object){
        if (null == object){
            return true;
        }else{
            return false;
        }
    }

    public static String formatDate(String date){
        Date d = DateUtil.str2Date(date, "yyyyMMdd");
        return DateUtil.date2Str(d, "MM月dd日  EEEE");
    }

    public static void shareToOther(Context context, String url) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, url);
        intent.setType("text/plain");
        context.startActivity(Intent.createChooser(intent, "分享到..."));
    }
}
