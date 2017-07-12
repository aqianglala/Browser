package com.example.zy1584.mybase.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.example.zy1584.mybase.base.BaseApplication;


/**
 * tzqiang
 * 获取资源的工具类
 */
public class UIUtils {
	
	/**
	 * 获取资源string
	 * @param id
	 * @return
	 */
	public static String getString(int id){
		return getContext().getResources().getString(id);
	}
	
	/**
	 * 获取资源stringarray
	 * @param id
	 * @return
	 */
	public static String[] getStringArray(int id){
		return getContext().getResources().getStringArray(id);
	}

	/**
	 * 获取资源文件图片
	 *
	 * @param id
	 * @return
	 */
	public static Drawable getDrawable(int id) {
		return ContextCompat.getDrawable(getContext(), id);
	}

	/**
	 * 获取尺寸
	 *
	 * @param id
	 * @return
	 */
	public static int getDimen(int id) {
		return getResources().getDimensionPixelSize(id); // 返回具体像素值
	}

	/**
	 * 获取资源文件颜色
	 *
	 * @param id
	 * @return
	 */
	public static int getColor(int id) {
		return ContextCompat.getColor(getContext(), id);
	}
	/**
	 * 根据id获取颜色的状态选择器
	 *
	 * @param id
	 * @return
	 */
	public static ColorStateList getColorStateList(int id) {
		return ContextCompat.getColorStateList(getContext(), id);
	}

	/**
	 * 在主线程执行的方法
	 * 
	 */
	public static void runOnUiThread(Runnable nRunnable){
		long currentThreadId=android.os.Process.myTid();
		if(currentThreadId== BaseApplication.getMainThreadId()){
			//相等则当前是主线程。直接运行即可
			nRunnable.run();
		}else{
			//不相等则到handler中运行
			getMainHandler().post(nRunnable);
		}
	}
	
	/**
	 * 执行延时任务
	 * @param nRunnable
	 * @param delayMillis
	 */
	public static void runOnUiDelayed(Runnable nRunnable,long delayMillis){
		getMainHandler().postDelayed(nRunnable, delayMillis);
		
	}
	
	/**
	 * 移除任务
	 * @param nRunnable
	 */
	public static void UiRemoveCallbacks(Runnable nRunnable){
		getMainHandler().removeCallbacks(nRunnable);
	}

	/**
	 * 获取全局handler
	 * @return
	 */
	public static Handler getMainHandler(){
		return BaseApplication.getMainHandler();
	}
	
	/**
	 * 获取上下文
	 * @return
	 */
	public static Context getContext(){
		return BaseApplication.getContext();
	}

	/**
	 * 获取资源对象
	 */
	public static Resources getResources() {
		return getContext().getResources();
	}

	/**
	 * 加载布局文件
	 *
	 * @param id
	 * @return
	 */
	public static View inflate(int id) {
		return View.inflate(getContext(), id, null);
	}

	/**
	 * 把自身从父View中移除
	 * @param view
	 */
	public static void removeSelfFromParent(View view) {
		if (view != null) {
			ViewParent parent = view.getParent();
			if (parent != null && parent instanceof ViewGroup) {
				ViewGroup group = (ViewGroup) parent;
				group.removeView(view);
			}
		}
	}

}
