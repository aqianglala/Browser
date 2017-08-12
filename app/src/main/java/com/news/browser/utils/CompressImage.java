package com.news.browser.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Created by zy1584 on 2017-7-17.
 */

public class CompressImage {
    /**
     * 压缩类，使用Bitmap的compress方法进行图片压缩，压缩后的大小控制在40kb以内
     * @param image
     * @return
     */
    public static Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        image.compress(Bitmap.CompressFormat.JPEG, 80, baos);//质量压缩方法，100表示不压缩，把压缩后的数据存放到baos中
        Log.e("compress", "压缩前图片大小"+image.toString()+"--"+baos.toByteArray().length);
        int options = 80;
        while ( baos.toByteArray().length / 1024>40) {	//循环判断如果压缩后图片是否大于40kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
        }

        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        Log.e("compress", "压缩后图片大小"+image.toString()+"--"+baos.toByteArray().length);
        return bitmap;
    }
}
