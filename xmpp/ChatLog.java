package com.wannashare.xmpp;

import android.util.Log;

public class ChatLog {

	private final static String TAG = "ChatLog";
	private static boolean isEnableLog = true;

	public static void enableLog(boolean enableLog) {
		isEnableLog = enableLog;
	}

	public static void i(String msg) {
		if (isEnableLog)
			Log.i(TAG, msg);
	}

	public static void e(String msg) {
		if (isEnableLog)
			Log.e(TAG, msg);
	}

	public static void d(String msg) {
		if (isEnableLog)
			Log.d(TAG, msg);
	}

}
