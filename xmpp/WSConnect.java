/*
package com.wannashare.xmpp;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class WSConnect {
	public static final int TIMEOUT_DURATION = 60000;

	public static boolean isNetworkConnected(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		if (networkInfo == null) {
			return false;
		} else
			return true;
	}

	public static String get(String mUrl, HashMap<String, String> paramMap, HashMap<String, String> headerMap) throws Exception {
		InputStream is;
		String line;
		String result = null;

		StringBuilder url = new StringBuilder(mUrl);

		if (paramMap != null && paramMap.size() > 0) {
			Set<String> keys = paramMap.keySet();
			for (String key : keys) {
				url.append(key + "=" + URLEncoder.encode(paramMap.get(key), "UTF-8") + "&");
			}
			url.deleteCharAt(url.length() - 1);
		}

		// LogM.i("callGetMethod url: " + url.toString());
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url.toString());

		if (headerMap != null) {
			Set<String> keys = headerMap.keySet();
			for (String key : keys) {
				httpGet.addHeader(key, headerMap.get(key));
			}
		}

		HttpResponse response = httpClient.execute(httpGet);
		HttpEntity entity = response.getEntity();
		is = entity.getContent();

		BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
		StringBuilder sb = new StringBuilder();

		while ((line = reader.readLine()) != null) {
			sb.append(line + "\n");
		}

		is.close();
		result = sb.toString();
		// LogM.i("Result: " + result);
		return result;

	}

	public static String postMultiPart(String mUrl, HashMap<String, String> params, HashMap<String, String> fileParams, HashMap<String, String> headerMap) throws Exception {

		String result = null;

		// MultipartEntity reqEntity = new MultipartEntity(
		// HttpMultipartMode.BROWSER_COMPATIBLE);

		MultipartEntityBuilder reqEntity = MultipartEntityBuilder.create();
		reqEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

		if (params != null) {
			Set<String> keys = params.keySet();
			for (String key : keys) {
				String value = "";
				if (params.get(key) != null) {
					value = params.get(key);
				} else {
					Log.d("log_tag", "KEY : " + key + " value: " + value);
				}
				// LogM.i("ADDED==> KEY : " + key + " value: " + value);
				reqEntity.addPart(key, new StringBody(value));
			}
		}

		if (fileParams != null) {
			Set<String> keys = fileParams.keySet();
			for (String key : keys) {
				File fileToUpload = new File(fileParams.get(key));
				if (fileToUpload.exists()) {
					Log.d("log_tag", "UploadFile Size: " + fileToUpload.length());
					// reqEntity.addPart(key,
					// new FileBody(fileToUpload, "image*/
/*"));
					FileInputStream fis = new FileInputStream(fileToUpload);
					Bitmap bi = BitmapFactory.decodeStream(fis);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					bi.compress(Bitmap.CompressFormat.JPEG, 100, baos);
					byte[] data = baos.toByteArray();
					ByteArrayBody bab = new ByteArrayBody(data, "image/jpg", fileParams.get(key));
					reqEntity.addPart(key, bab);
				}

			}
		}

		// LogM.i("callPostMethod url: " + mUrl);

		HttpClient httpClient = new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), TIMEOUT_DURATION);
		HttpConnectionParams.setSoTimeout(httpClient.getParams(), TIMEOUT_DURATION);

		HttpPost httpPost = new HttpPost(mUrl);
		if (headerMap != null) {
			Set<String> headerKeys = headerMap.keySet();
			for (String key : headerKeys) {
				Log.d("log_tag", "Header KEY : " + key + " value: " + headerMap.get(key));
				httpPost.addHeader(key, headerMap.get(key));
			}
		}

		httpPost.setEntity(reqEntity.build());

		HttpResponse response;
		response = httpClient.execute(httpPost);
		HttpEntity entity = response.getEntity();
		result = EntityUtils.toString(entity);
		return result;
	}
}
*/
