package com.news.browser.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

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
//        int options = 80;
//        while ( baos.toByteArray().length / 1024>40) {	//循环判断如果压缩后图片是否大于40kb,大于继续压缩
//            baos.reset();//重置baos即清空baos
//            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
//            options -= 10;//每次都减少10
//        }
        int options = 50;
        baos.reset();//重置baos即清空baos
        image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中

        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        Log.e("compress", "压缩后图片大小"+image.toString()+"--"+baos.toByteArray().length);
        return bitmap;
    }

    /**
     * by yhw 用于反馈界面 用户点击相册 的图片 压缩图片
     */
    public static boolean commpressImage(String srcPath, String destPath) {
        try {
            Bitmap bitmap = getCompressImage(srcPath);
            if (bitmap != null) {

                FileOutputStream out = new FileOutputStream(destPath);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
                return true;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static Bitmap getCompressImage(String srcPath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        float hh = 800f;
        float ww = 480f;
        int be = 1;
        if (w > h && w > ww) {
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return compressImage(bitmap);
    }
}
