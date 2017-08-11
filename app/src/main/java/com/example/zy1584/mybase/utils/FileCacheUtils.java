package com.example.zy1584.mybase.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import com.example.zy1584.mybase.base.BaseApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;

import static com.example.zy1584.mybase.utils.SPUtils.context;

public class FileCacheUtils {

	/*
	 * 图片缓存的相对路径
	 */
	private static final String IMG_CACHE_DIR = "/Browser/imageCache";
	
	/*
	 * app下载目录
	 */
	private static final String APK_DOWNLOAD_DIR = "/Browser/download";
	
	/*
	 * 启动页存放的位置
	 */
	private static final String LAUNCH_CACHE_DIR = "/DoovAppstore/launch";
	private static final String LAUNCH_CACHE_FILE = "launch.cache";
	
	/*
	 * 手机缓存目录
	 */
	private static String DATA_ROOT_PATH = null;
	/*
	 * SDcard根目录
	 */
	private static String SD_ROOT_PATH   = null;
	/*
	 * 缓存扩展名
	 */
	private static final String CACHE_TAIL     = ".cache";
	/*
	 * 最大缓存空间，单位mb
	 */
	private static final int    CACHE_SIZE     = 200;
	/*
	 * SD卡缓存低于此值时清理缓存，单位mb
	 */
	private static final int    NEED_TO_CLEAN  = 200;
	
	private Context mContext;
	private Object mkdirLock = new Object();
	private static final String TAG            = "BitmapFileCacheUtils";
	
	private FileCacheUtils() {
		this.mContext   = BaseApplication.getContext();
		try {
			DATA_ROOT_PATH = context.getCacheDir().getAbsolutePath();
			
			//SD_ROOT_PATH = context.getExternalFilesDir(null).getAbsolutePath();
			SD_ROOT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
		} catch (Exception e) {
		}
	}

	private static final FileCacheUtils instance = new FileCacheUtils();

	public static FileCacheUtils getInstance(){
		return instance;
	}
	
	public Bitmap getBitmapFromFile(String key) {
		if (key == null) {
		    return null;
		}
		String fileName = getCacheDirectory() + File.separator + converKeyToFileName(key);
		File file     = new File(fileName);
		if (file.exists()) {
			Bitmap bitmap =  getOriginalBitmapFromFile(fileName);
			if (bitmap == null) {
				file.delete();
			} else {
				updateFileModifiedTime(fileName);
				//Log.i(TAG, "get file from sdCard cache sucess...");
				return bitmap;
			}
		}
	
		return null;
	}


	public void addBitmapToFile(final String key, final Bitmap bm) {
		if (bm == null || key == null) {
			return;
		}
		//视情况清除部分缓存
		removeCache(getCacheDirectory());
		
		//存取文件的时候发生ui阻塞，导致速度有点慢
		new Thread(){
			@Override
			public void run() {
				String fileName = converKeyToFileName(key);
				String fileName1 = fileName + ".tmp";
				File dir      = new File(getCacheDirectory());
				if (!dir.exists()) {
					synchronized (mkdirLock) {
						dir.mkdirs();						
					}
				}
				File file     = new File(dir, fileName1);
				OutputStream out = null;
				try {
					out = new FileOutputStream(file);
					bm.compress(CompressFormat.PNG, 100, out);
					out.close();
					
					if(!file.renameTo(new File(dir, fileName))) {
						Log.e("NetRequestFactory", "rename file fail, add BitmapToFile");
					}
					//Log.i(TAG, "add file to sdCard cache sucess...");
				} catch (Exception e) {
					e.printStackTrace();
					if(out != null) {
						try {
							out.close();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}		
				}		
			}
		}.start();
	}
	
	/**
	 * 获取apk下载路径
	 * @return
	 */
	public String getDownloadDirectory() {
		String cachePath = null;
		if (isSDcardAvailable()) {
			cachePath = SD_ROOT_PATH + APK_DOWNLOAD_DIR;
		} else {
			cachePath = DATA_ROOT_PATH + APK_DOWNLOAD_DIR;
		}
		return cachePath;	
	}		
	
	
	public String getLaunchDirectory() {
		String cachePath = null;
		if (isSDcardAvailable()) {
			cachePath = SD_ROOT_PATH + LAUNCH_CACHE_DIR;
		} else {
			cachePath = DATA_ROOT_PATH + LAUNCH_CACHE_DIR;
		}
		return cachePath;	
	}	
	
	/*
	 * 清除40%的缓存，根据文件使用时间排序，越久没使用越容易被删除
	 */
	private void removeCache(String cacheDirectory) {
		// TODO Auto-generated method stub
		File dir   = new File(cacheDirectory);
		File[] files = dir.listFiles();
		if (files == null) {
			return;
		}
		
		double total_size = 0;
		for (File file : files) {
			total_size += file.length();    
		}
		total_size = total_size / 1024 /1024;
		
		if (total_size > CACHE_SIZE || (isSDcardAvailable() && getSdCardFreeSpace() <= NEED_TO_CLEAN)) {
			int removeFactor = (int) (files.length * 0.4);
			Arrays.sort(files, new FileLastModifiedComparator());
			for (int i = 0; i < removeFactor; i++) {
				if(files[i].getName().endsWith(CACHE_TAIL)) {
					files[i].delete();						
				}
			}
		}
	}
	
	
//	public static void cleanCache(final Context context, final Handler handler) {
//		new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				File dir = new File(context.getExternalCacheDir().getAbsolutePath()+"/uil-images");
//				File[] files = dir.listFiles();
//				if (files == null) {
//					handler.removeMessages(SettingsFragment.HANDLE_MSG_UPDATE);
//					handler.sendEmptyMessage(SettingsFragment.HANDLE_MSG_COMPLETE);
//					return;
//				}
//				int removeFactor = files.length;
//				for (int i = 0; i < removeFactor; i++) {
//					Message msg = handler.obtainMessage(SettingsFragment.HANDLE_MSG_UPDATE);
//					msg.arg1 = i * SettingsFragment.PROGRESSBAR_MAX / removeFactor;
//					//if(files[i].getName().endsWith(CACHE_TAIL)) {
//						files[i].delete();
//					//}
//					handler.removeMessages(SettingsFragment.HANDLE_MSG_UPDATE);
//					handler.sendMessage(msg);
//					try {
//						Thread.sleep(10);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//
////					for (int i = 0; i < 100; i++) {
////						Message msg = handler.obtainMessage(SettingActivity.HANDLE_MSG_UPDATE);
////						msg.arg1 = i * 10;
////						handler.removeMessages(SettingActivity.HANDLE_MSG_UPDATE);
////						handler.sendMessage(msg);
////
////						try {
////							Thread.sleep(10);
////						} catch (InterruptedException e) {
////							// TODO Auto-generated catch block
////							e.printStackTrace();
////						}
////					}
//				handler.sendEmptyMessage(SettingsFragment.HANDLE_MSG_COMPLETE);
//			}
//		}).start();
//	}
    
    
	private int getSdCardFreeSpace() {
		// TODO Auto-generated method stub
		StatFs stat            = new StatFs(Environment.getExternalStorageDirectory().getPath());
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
			long   bolckSize       = stat.getBlockSizeLong();
			long   availableBalcks = stat.getAvailableBlocksLong();
			double freeSpace       =   bolckSize * availableBalcks;
			
			return (int) (freeSpace / 1024 /1024);
		}else{
			long   bolckSize       = stat.getBlockSize();
			long   availableBalcks = stat.getAvailableBlocks();
			double freeSpace       =   bolckSize * availableBalcks;
			
			return (int) (freeSpace / 1024 /1024);
		}
	}
	
	/*
	public long getCacheFolderSize() {
		long size = 0;
		File cacheFolder = new File(getCacheDirectory());
		if (!cacheFolder.exists()) {
			return 0;
		}
		try {
			File[] fileList = cacheFolder.listFiles();
			for (int i = 0; i < fileList.length; i++) {
				size = size + fileList[i].length();
			}
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		return (int) (size / 1024 / 1024);		
	} 
	*/

	private String converKeyToFileName(String key) {
		// TODO Auto-generated method stub
		if (key == null) {
			return "";
		}
		
		return key.hashCode() + CACHE_TAIL;
	}
	
	private void updateFileModifiedTime(String fileName) {
		// TODO Auto-generated method stub
		File file = new File(fileName);
		file.setLastModified(System.currentTimeMillis());
	}

	private String getCacheDirectory() {
		// TODO Auto-generated method stub
		String cachePath = null;
		if (isSDcardAvailable()) {
			cachePath = SD_ROOT_PATH + IMG_CACHE_DIR;
		} else {
			cachePath = DATA_ROOT_PATH + IMG_CACHE_DIR;
		}
		return cachePath;
	}

	private boolean isSDcardAvailable() {
		// TODO Auto-generated method stub
		return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
	}
	
	private class FileLastModifiedComparator implements Comparator<File> {

		@Override
		public int compare(File lhs, File rhs) {
			// TODO Auto-generated method stub
			if (lhs.lastModified() > rhs.lastModified()) {
				return 1;
			} else if (lhs.lastModified() == rhs.lastModified()) {
				return 0;
			} else {
				return -1;
			}
		}		
	}	
	
	public static Bitmap getOriginalBitmapFromFile(String filePath){
		BitmapFactory.Options opts=new BitmapFactory.Options();
		opts.inPreferredConfig = Config.ARGB_8888;
		opts.inDither = false;  
		opts.inPurgeable = true;  
		opts.inTempStorage = new byte[12 * 1024];   
		opts.inJustDecodeBounds = false;  
		FileInputStream fs = null;
		Bitmap bm = null;
		try {
			fs = new FileInputStream(new File(filePath));
			if(fs != null){
				bm = BitmapFactory.decodeFileDescriptor(fs.getFD(), null, opts);
			}
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
				e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(fs !=null){
    			try {
    				fs.close();  
				} catch (Exception e) {
					e.printStackTrace();
				}
    		}
		} 
		return bm;
	}
	
	/**
	 * 存储启动图片到本地
	 * @param bmp
	 * @return
	 */
	public boolean storeLaunchBmpToLocal(final Bitmap bmp) {
		if(bmp == null) {
			return false;
		}
		
		//存取文件的时候发生ui阻塞，导致速度有点慢
		new Thread(){
			@Override
			public void run() {
				String fileName = LAUNCH_CACHE_FILE;
				String fileName1 = fileName + ".tmp";
				File dir      = new File(getLaunchDirectory());
				if (!dir.exists()) {
					synchronized (mkdirLock) {
						dir.mkdirs();						
					}
				}
				File file     = new File(dir, fileName1);
				OutputStream out = null;
				try {
					out = new FileOutputStream(file);
					bmp.compress(CompressFormat.PNG, 100, out);
					out.close();
					
					/**
					 * 检查文件是否存在，存在的话，删除
					 */
					File newFile = new File(dir, fileName);
					if(newFile.exists()) {
						newFile.delete();
					}
					
					if(!file.renameTo(newFile)) {
						Log.e("NetRequestFactory", "rename file fail, add BitmapToFile");
					}
					//Log.i(TAG, "add file to sdCard cache sucess...");
				} catch (Exception e) {
					e.printStackTrace();
					if(out != null) {
						try {
							out.close();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}		
				}		
			}
		}.start();
		
		return true;
	}
	
	/**
	 * 读取启动图片从本地
	 * @return
	 */
	public Bitmap getLaunchBmpFromLocal() {
		String fileName = getLaunchDirectory() + File.separator + LAUNCH_CACHE_FILE;
		File file     = new File(fileName);
		if (file.exists()) {
			Bitmap bitmap =  getOriginalBitmapFromFile(fileName);
			if (bitmap == null) {
				file.delete();
			} else {
				//updateFileModifiedTime(fileName);
				//Log.i(TAG, "get file from sdCard cache sucess...");
				return bitmap;
			}
		}
	
		return null;		
	}
	
	/**
	 * 判断本地文件是否存在
	 * @return
	 */
	public boolean existLaunchBmpFromLocal() {
		String fileName = getLaunchDirectory() + File.separator + LAUNCH_CACHE_FILE;
		File file     = new File(fileName);
		if (file.exists()) {
			return true;
		}
		return false;
	}	
}
