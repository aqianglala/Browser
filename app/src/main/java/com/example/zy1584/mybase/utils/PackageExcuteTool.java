package com.example.zy1584.mybase.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/* Add by CFZ for 特殊手机，安装时，可以调用pm instal 命令，但不会默认安装  at 20161206 Begin */
/* Add by CFZ for 特殊手机，安装时，可以调用pm instal 命令，但不会默认安装  at 20161206 End */

public class PackageExcuteTool {

	/**
	 * 静默安装apk
	 * @param apkPath
	 * @return
	 */
	public static boolean installApp(String apkPath) {
		String[] args = { "pm", "install", "-r", apkPath };
		String result = "";
		ProcessBuilder processBuilder = new ProcessBuilder(args);
		Process process = null;
		InputStream errIs = null;
		InputStream inIs = null;

        if (Utils.isInstallSpecialOS())
        {
			SPUtils.put(GlobalParams.IS_YUZHUANG,false);
            return false;
        }

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int read = -1;
			process = processBuilder.start();
			errIs = process.getErrorStream();
			while ((read = errIs.read()) != -1) {
				baos.write(read);
			}

			//长度不为0，出错了
			if(baos.size() > 0) {
				baos.reset();
			}

			inIs = process.getInputStream();
			while ((read = inIs.read()) != -1) {
				baos.write(read);
			}
			byte[] data = baos.toByteArray();
			result = new String(data);
			if(result.length() > 0 && result.startsWith("Success")) {
				SPUtils.put(GlobalParams.IS_YUZHUANG,true);
				return true;
			}
			else{
				SPUtils.put(GlobalParams.IS_YUZHUANG,false);
			}
			/* Modify by TZQ for  at 2017-3-21 End */
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (errIs != null) {
					errIs.close();
				}
				if (inIs != null) {
					inIs.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (process != null) {
				process.destroy();
			}
		}

		return false;
	}

	public static boolean upgradeInstallApp(Context context, String apkPath) {
		String[] args = { "pm", "install", "-r", apkPath };
		String result = "";
		ProcessBuilder processBuilder = new ProcessBuilder(args);
		Process process = null;
		InputStream errIs = null;
		InputStream inIs = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int read = -1;
			process = processBuilder.start();
			errIs = process.getErrorStream();
			while ((read = errIs.read()) != -1) {
				baos.write(read);
			}

			//长度不为0，出错了
			if(baos.size() > 0) {
				baos.reset();
			}

			inIs = process.getInputStream();
			while ((read = inIs.read()) != -1) {
				baos.write(read);
			}
			byte[] data = baos.toByteArray();
			result = new String(data);
			if(result.length() > 0 && result.startsWith("Success")) {
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (errIs != null) {
					errIs.close();
				}
				if (inIs != null) {
					inIs.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (process != null) {
				process.destroy();
			}
		}
		return false;
	}
	/**
	 * 静默卸载apk
	 * @param packageName
	 * @return
	 */
	public static boolean unInstallApp(String packageName) {
		String[] args = { "pm", "uninstall", packageName };
		String result = "";
		ProcessBuilder processBuilder = new ProcessBuilder(args);
		Process process = null;
		InputStream errIs = null;
		InputStream inIs = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int read = -1;
			process = processBuilder.start();
			errIs = process.getErrorStream();
			while ((read = errIs.read()) != -1) {
				baos.write(read);
			}

			//长度不为0，出错了
			if(baos.size() > 0) {
				baos.reset();
			}

			inIs = process.getInputStream();
			while ((read = inIs.read()) != -1) {
				baos.write(read);
			}
			byte[] data = baos.toByteArray();
			result = new String(data);
			if(result.length() > 0 && result.startsWith("Success")) {
				return true;
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (errIs != null) {
					errIs.close();
				}
				if (inIs != null) {
					inIs.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (process != null) {
				process.destroy();
			}
		}

		return false;
	}

	/**
	 * 开启某个app
	 * @param packageName
	 * @param mainActivity
	 */
	public static String startApp(String packageName, String mainActivity) {
		String[] args = { "am", "start", "-n", packageName+"/"+mainActivity};
		String result = "";
		ProcessBuilder processBuilder = new ProcessBuilder(args);
		Process process = null;
		InputStream errIs = null;
		InputStream inIs = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int read = -1;
			process = processBuilder.start();
			errIs = process.getErrorStream();
			while ((read = errIs.read()) != -1) {
				baos.write(read);
			}
			baos.write('\n');
			inIs = process.getInputStream();
			while ((read = inIs.read()) != -1) {
				baos.write(read);
			}
			byte[] data = baos.toByteArray();
			result = new String(data);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (errIs != null) {
					errIs.close();
				}
				if (inIs != null) {
					inIs.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (process != null) {
				process.destroy();
			}
		}

		return result;
	}

	
	/**
	 * 正常安装
	 * @param localPath
	 * @param context
	 * @return
	 */
	public static boolean normalInstall(String localPath, Context context) {
		File file = new File(localPath);
		if(!file.exists())
			return false; 
		Intent intent = new Intent();
		intent.setAction("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(Uri.fromFile(file),"application/vnd.android.package-archive");
		context.startActivity(intent);
		return true;		
	}
	
	
	/**
	 * 正常卸载
	 * @param packageName
	 * @param context
	 * @return
	 */
	public static boolean normalUninstall(String packageName, Context context){
		Uri packageURI = Uri.parse("package:" + packageName);
		Intent uninstallIntent = new Intent(Intent.ACTION_DELETE,packageURI);
		uninstallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(uninstallIntent);
		return true;
	}	
	
	/**
	 * 安装某个app
	 * @param packageName
	 * @param activityName
	 * @return
	 */
	public static void openApp(String packageName, String activityName, Context context){
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

		// 设置ComponentName参数1:packagename参数2:MainActivity路径
		ComponentName cn = new ComponentName(packageName, activityName);

		intent.setComponent(cn);
		context.startActivity(intent);
	}
	
	
}
