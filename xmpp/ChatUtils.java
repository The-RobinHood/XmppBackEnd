package com.wannashare.xmpp;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.support.v4.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

import com.wannashare.R;
import com.wannashare.constants.ChatConstants;
import com.wannashare.constants.WannaShareConstants;
import com.wannashare.main.ChatActivity_old;
import com.wannashare.main.HomeActivity;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class ChatUtils {

	Intent xmppService;
	Context activity;
    //Message notification
    public static int MESSAGE_NOTIFICATION_ID = 1;
    public static Map<String, ArrayList<String>> mapMessages = new HashMap<String, ArrayList<String>>();
    public static Map<String, ArrayList<String>> tempMessages = new HashMap<String, ArrayList<String>>();
    public static ArrayList<String> privateMessages = new ArrayList<String>();



    public ChatUtils(Context context) {
		xmppService = new Intent(context, XMPPService.class);
		this.activity = context;
		this.activity.startService(xmppService);
	}

	public static int getAction(Intent intent) {
		return intent.getIntExtra(XMPPService.ACTION, -1);
	}

	public void login(ChatUser chatUser) {

		UserUtils.getInstance(activity).setCachedUser(chatUser);

		Bundle bnd = new Bundle();
		bnd.putInt(XMPPService.ACTION, XMPPService.ACTION_LOGIN);
		xmppService.putExtras(bnd);

		activity.startService(xmppService);
	}

	public void register(ChatUser chatUser) {

		UserUtils.getInstance(activity).setCachedUser(chatUser);

		Bundle bnd = new Bundle();
		bnd.putInt(XMPPService.ACTION, XMPPService.ACTION_REGISTER);
		xmppService.putExtras(bnd);

		activity.startService(xmppService);
	}

	public void sendMessage(ChatMessage chatMessage, ArrayList<ChatUser> userList) {

		Bundle bnd = new Bundle();
		bnd.putInt(XMPPService.ACTION, XMPPService.ACTION_SEND_MESSAGE);
		bnd.putParcelable(XMPPService.DATA_MESSAGE, chatMessage);
		bnd.putParcelableArrayList(XMPPService.DATA_LIST, userList);

		xmppService.putExtras(bnd);
		activity.startService(xmppService);
	}

	public void sendMessage(ChatMessage chatMessage, String toUser) {

		Bundle bnd = new Bundle();
		bnd.putInt(XMPPService.ACTION, XMPPService.ACTION_SEND_MESSAGE);
		bnd.putParcelable(XMPPService.DATA_MESSAGE, chatMessage);
		bnd.putString(XMPPService.DATA_LIST, toUser);

		xmppService.putExtras(bnd);
		activity.startService(xmppService);
	}

	public void getFriendList() {
		Bundle bnd = new Bundle();
		bnd.putInt(XMPPService.ACTION, XMPPService.ACTION_SEND_MESSAGE);
		xmppService.putExtras(bnd);
		activity.startService(xmppService);
	}

	public void getUserPresence(String userId) {

		Bundle bnd = new Bundle();
		bnd.putInt(XMPPService.ACTION, XMPPService.ACTION_GET_USER_PRESENCE);
		bnd.putString(XMPPService.DATA_USER_ID, userId);

		xmppService.putExtras(bnd);
		activity.startService(xmppService);
	}

	public void sendSubscribePacket(String userId) {
		// TODO Auto-generated method stub
		Bundle bnd = new Bundle();
		bnd.putInt(XMPPService.ACTION, XMPPService.ACTION_SUBSCRIBE_USER);
		bnd.putString(XMPPService.DATA_USER_ID, userId);

		xmppService.putExtras(bnd);
		activity.startService(xmppService);
	}

	public void sendPresencePacket(boolean isAvailable) {
		// TODO Auto-generated method stub
		Bundle bnd = new Bundle();
		bnd.putInt(XMPPService.ACTION, XMPPService.ACTION_SEND_PRESENCE);
		bnd.putBoolean(XMPPService.DATA_USER_STATUS, isAvailable);
		xmppService.putExtras(bnd);
		activity.startService(xmppService);
	}

	public void registerListener(BroadcastReceiver receiver) {
		activity.registerReceiver(receiver, new IntentFilter(XMPPService.ACTION_PERFORMED));
	}

	public void unregisterListener(BroadcastReceiver receiver) {
		try {
			activity.unregisterReceiver(receiver);
		} catch (Exception e) {

		}
	}

	// Utility
	public static ChatMessage getTextMessageWithOrderId(String myId, String myName, String toId, String iOrderId, String message) {

		ChatMessage chatMessage = new ChatMessage();
//		chatMessage.type = "chat";
//		chatMessage.vtype = ChatConstants.vTYPE_TEXT;
		chatMessage.msg_id = generateRandomUUID();
		chatMessage.vname = myName;
		chatMessage.receiver_id = toId;
		chatMessage.sender_id = myId;
//		chatMessage.iorderid = iOrderId;
		chatMessage.msg_date = getCurrentDateFormat();
		chatMessage.msg_text = message;
		chatMessage.status = ChatConstants.STATUS_TYPE_PROCESS;

		// JSONObject jsonObject = chatMessage.getJSONObject(true);
		// if (jsonObject != null)
		// chatMessage.raw_data = jsonObject.toString();
		return chatMessage;
	}

	public static ChatMessage getImageMessageWithOrderId(String myId, String myName, String toId, String iOrderId, String thumbnailBase64String, String destinationUrl, String fileSize) {

		ChatMessage chatMessage = new ChatMessage();
//		chatMessage.type = "chat";
//		chatMessage.vtype = ChatConstants.vTYPE_IMAGE;
        chatMessage.msg_id = generateRandomUUID();
		chatMessage.vname = myName;
//		chatMessage.isuploaded = TextUtils.isEmpty(destinationUrl) ? "No" : "Yes";
//		chatMessage.localPath = "";
//		chatMessage.url = destinationUrl;
		chatMessage.receiver_id = toId;
		chatMessage.sender_id = myId;
//		chatMessage.iorderid = iOrderId;
//		chatMessage.media_size = fileSize;
		chatMessage.msg_date = getCurrentDateFormat();
		chatMessage.msg_text = thumbnailBase64String;
		chatMessage.status = ChatConstants.STATUS_TYPE_PROCESS;
		// Don't calculate this json object because some fields will update after this like url. and json is calculated at time of sending packet.
		// JSONObject jsonObject = chatMessage.getJSONObject(true);
		// if (jsonObject != null)
		// chatMessage.raw_data = jsonObject.toString();
		return chatMessage;
	}

    public static ChatMessage getLocationMessageWithOrderId(String myId, String myName, String toId,String message,String Isdeliver,String isRead, String groupId) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.msg_id = generateRandomUUID();
        chatMessage.vname = myName;
        chatMessage.msg_text = message;
        chatMessage.receiver_id = toId;
        chatMessage.sender_id = myId;
        chatMessage.msg_date = getCurrentDateFormat();
        chatMessage.status = ChatConstants.STATUS_TYPE_PROCESS;
        chatMessage.isdeliver=Isdeliver;
        chatMessage.isread=isRead;
        chatMessage.group_id=groupId;
        return chatMessage;

        // JSONObject jsonObject = new JSONObject();
        // try {
        // jsonObject.put(ChatConstants.PARAMS_TYPE, "chat");
        // jsonObject.put(ChatConstants.PARAMS_VTYPE, ChatConstants.vTYPE_LOCATION);
        // jsonObject.put(ChatConstants.PARAMS_TO, toId);
        // jsonObject.put(ChatConstants.PARAMS_MYJID, myId);
        // jsonObject.put(ChatConstants.PARAMS_LATITUDE, lat);
        // jsonObject.put(ChatConstants.PARAMS_LONGITUDE, lng);
        // jsonObject.put(ChatConstants.PARAMS_ID, generateRandomUUID());
        // jsonObject.put(ChatConstants.PARAMS_IORDERID, iOrderId);
        // jsonObject.put(ChatConstants.PARAMS_DATE, getCurrentDateFormat());
        // jsonObject.put(ChatConstants.PARAMS_VNAME, myName);
        // jsonObject.put(ChatConstants.PARAMS_TEXT, message);
        //
        // return jsonObject.toString();
        // } catch (Exception e) {
        //
        // }
        // return message;
    }

	/*public static ChatMessage getLocationMessageWithOrderId(String myId, String myName, String toId, String iOrderId, String lat, String lng, String message) {
		ChatMessage chatMessage = new ChatMessage();
//		chatMessage.type = "chat";
//		chatMessage.vtype = ChatConstants.vTYPE_LOCATION;
		chatMessage.receiver_id = toId;
		chatMessage.sender_id = myId;
//		chatMessage.latitude = lat;
//		chatMessage.longitude = lng;
        chatMessage.msg_id = generateRandomUUID();
//		chatMessage.iorderid = iOrderId;
		chatMessage.date = getCurrentDateFormat();
		chatMessage.vname = myName;
		chatMessage.text = message;
		chatMessage.status = ChatConstants.STATUS_TYPE_PROCESS;

		return chatMessage;

		// JSONObject jsonObject = new JSONObject();
		// try {
		// jsonObject.put(ChatConstants.PARAMS_TYPE, "chat");
		// jsonObject.put(ChatConstants.PARAMS_VTYPE, ChatConstants.vTYPE_LOCATION);
		// jsonObject.put(ChatConstants.PARAMS_TO, toId);
		// jsonObject.put(ChatConstants.PARAMS_MYJID, myId);
		// jsonObject.put(ChatConstants.PARAMS_LATITUDE, lat);
		// jsonObject.put(ChatConstants.PARAMS_LONGITUDE, lng);
		// jsonObject.put(ChatConstants.PARAMS_ID, generateRandomUUID());
		// jsonObject.put(ChatConstants.PARAMS_IORDERID, iOrderId);
		// jsonObject.put(ChatConstants.PARAMS_DATE, getCurrentDateFormat());
		// jsonObject.put(ChatConstants.PARAMS_VNAME, myName);
		// jsonObject.put(ChatConstants.PARAMS_TEXT, message);
		//
		// return jsonObject.toString();
		// } catch (Exception e) {
		//
		// }
		// return message;
	}*/

	public static ChatMessage getVideoMessageWithOrderId(String myId, String myName, String toId, String iOrderId, String duration, String thumbnailBase64String, String destinationUrl, String fileSize) {
		ChatMessage chatMessage = new ChatMessage();
//		chatMessage.type = "chat";
//		chatMessage.vtype = ChatConstants.vTYPE_VIDEO;
        chatMessage.msg_id = generateRandomUUID();
		chatMessage.vname = myName;
//		chatMessage.isuploaded = TextUtils.isEmpty(destinationUrl) ? "No" : "Yes";
//		chatMessage.localPath = "";
//		chatMessage.url = destinationUrl;
		chatMessage.receiver_id = toId;
		chatMessage.sender_id = myId;
//		chatMessage.iorderid = iOrderId;
//		chatMessage.media_size = fileSize;
//		chatMessage.media_duration = duration;
		chatMessage.msg_date = getCurrentDateFormat();
		chatMessage.msg_text = thumbnailBase64String;
		chatMessage.status = ChatConstants.STATUS_TYPE_PROCESS;

		// Don't calculate this json object because some fields will update after this like url. and json is calculated at time of sending packet.
		// JSONObject jsonObject = chatMessage.getJSONObject(true);
		// if (jsonObject != null)
		// chatMessage.raw_data = jsonObject.toString();
		return chatMessage;
		// ------------
		// JSONObject jsonObject = new JSONObject();
		// try {
		// jsonObject.put(ChatConstants.PARAMS_ID, generateRandomUUID());
		// jsonObject.put(ChatConstants.PARAMS_TYPE, "chat");
		// jsonObject.put(ChatConstants.PARAMS_VTYPE, ChatConstants.vTYPE_VIDEO);
		// jsonObject.put(ChatConstants.PARAMS_TO, toId);
		// jsonObject.put(ChatConstants.PARAMS_MYJID, myId);
		// jsonObject.put(ChatConstants.PARAMS_VNAME, myName);
		// jsonObject.put(ChatConstants.PARAMS_ISUPLOADED, thumbnailBase64String);
		// jsonObject.put(ChatConstants.PARAMS_PATH, destinationUrl);
		// jsonObject.put(ChatConstants.PARAMS_URL, "");
		// jsonObject.put(ChatConstants.PARAMS_IORDERID, iOrderId);
		// jsonObject.put(ChatConstants.PARAMS_DATE, getCurrentDateFormat());
		// jsonObject.put(ChatConstants.PARAMS_MEDIADURATION, duration);
		// jsonObject.put(ChatConstants.PARAMS_MEDIASIZE, fileSize);
		// jsonObject.put(ChatConstants.PARAMS_TEXT, thumbnailBase64String);
		//
		// return jsonObject.toString();
		// } catch (Exception e) {
		//
		// }
		// return "";
	}

	private static String generateRandomUUID() {
		return UUID.randomUUID().toString();
	}

	public static String getCurrentDateFormat() {
		// return DateFormat.format("kk:mm a", System.currentTimeMillis()).toString();
		return ChatConstants.getCurrentUTCDate();
	}

	public static ChatMessage getMessage(Intent intent) {
		// TODO Auto-generated method stub
		return intent.getExtras().getParcelable(XMPPService.DATA_MESSAGE);
	}

	public static String getMessageId(Intent intent) {
		// TODO Auto-generated method stub
		return intent.getExtras().getString(XMPPService.DATA_MESSAGE_ID);
	}

	public static String getStatus(Intent intent) {
		// TODO Auto-generated method stub
		return intent.getExtras().getString(XMPPService.DATA_USER_STATUS);
	}

	public static String getUserId(Intent intent) {
		// TODO Auto-generated method stub
		return intent.getExtras().getString(XMPPService.DATA_USER_ID);
	}

	public static String getBase64StringFromImagePath(String imagePath) {

		return "";
	}

	public static int[] getDeviceWidthHeight(Activity activity) {

		int size[] = new int[2];

		if (isAndroidAPILevelGreaterThenEqual(android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH)) {
			DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
			size[0] = displayMetrics.widthPixels;
			size[1] = displayMetrics.heightPixels;
		} else {
			Display mDisplay = activity.getWindowManager().getDefaultDisplay();
			size[0] = mDisplay.getWidth();
			size[1] = mDisplay.getHeight();
		}
		return size;
	}

	// android.os.Build.VERSION_CODES.FROYO
	public static boolean isAndroidAPILevelGreaterThenEqual(int apiLevel) {
		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentapiVersion >= apiLevel) {
			return true;
		} else {
			return false;
		}
	}

	public static String encodeTobase64(Bitmap image) {
		Bitmap immagex = image;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		immagex.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		byte[] b = baos.toByteArray();
		String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

		Log.e("LOOK", imageEncoded);
		return imageEncoded;
	}

	public static Bitmap decodeBase64(String input) {
		byte[] decodedByte = Base64.decode(input, 0);
		return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
	}

	public static String getProperSize(long value) {
		double doubleValue = value;
		String[] values = new String[] { "bytes", "KB", "MB", "GB", "TB" };

		int multiplyFactor = 0;
		while (doubleValue > 1024) {
			doubleValue /= 1024;
			multiplyFactor++;
		}
		return String.format("%4.2f %s", doubleValue, values[multiplyFactor]);
	}

/*	public static void sendNotification(Context context, ChatMessage chatMessage) {
		int notificationId = 1;
		if (chatMessage.sender_id != null) {
			if (chatMessage.sender_id.contains("@")) {
				notificationId = Integer.parseInt(chatMessage.sender_id.subSequence(0, chatMessage.sender_id.indexOf("@")).toString());
			}
		}
//		ProfileModel currentUser = Utility.getUserFromPref(context);
		// send push notification
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Intent intent = new Intent(context, ChatActivity_old.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//		intent.putExtra(ChatConstants.PARAMS_MYJID, currentUser.getIUserID() + "@" + XMPPHelper.HOST);
//		intent.putExtra(ChatConstants.PARAMS_MY_NAME, currentUser.getVFirstname());
//		intent.putExtra(ChatConstants.PARAMS_MYPASSWORD, ApplicationSession.getSession(context).getString(ConstantCode.PREF_PASSWORD));
		intent.putExtra(WannaShareConstants.EXTRA_FRIEND_NAME, chatMessage.vname);
		intent.putExtra(WannaShareConstants.EXTRA_PROFILE_ID, chatMessage.sender_id);// "353@54.149.19.238");
//		intent.putExtra(ChatConstants.PARAMS_IORDERID, chatMessage.iorderid);
		final PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		if (chatMessage != null) {
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(HomeActivity.class);
			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).setWhen(System.currentTimeMillis()).setAutoCancel(true).setSmallIcon(R.drawable.ic_launcher).setContentTitle("WannaShare").setContentText(chatMessage.vname +" "+ "Send you a chat message");
			mBuilder.setContentIntent(contentIntent);


			mNotificationManager.notify(notificationId, mBuilder.build());
		}
	}*/

	public static void stopXMPPService(Context context) {
		Intent intent = new Intent(context, XMPPService.class);
		context.stopService(intent);
	}


    public static void generateMessageNotification(Context context, String message,
                                             String userID, String userName, String name) {
        // TODO Auto-generated method stub

        Log.i("##################",
                "================Inside generateNotification Method==============================");
        Uri soundUri = null;
//        if (pref_user.getBoolean(Settings.Global.MESSAGE_SOUND, true))
            soundUri = RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        if (mapMessages == null) {
            mapMessages = new HashMap<String, ArrayList<String>>();
        }

        if (privateMessages == null) {
            privateMessages = new ArrayList<String>();
        }
        privateMessages.add(userName+":"+" "+message);
        mapMessages.put(userID, privateMessages);
//        mapMessages = new TreeMap<String, ArrayList<String>>(tempMessages);

        NotificationCompat.Builder builder = createMessageNotificationBuilder(
                context, userID, userName, name);

        //        builder.setTicker(remoteUserName + ": " + message);

        builder.setDefaults(Notification.DEFAULT_LIGHTS
                | Notification.DEFAULT_VIBRATE);
        builder.setSound(soundUri);
        //		builder.setPriority(NotificationCompat.PRIORITY_HIGH);

        //Look up the notification manager service.
        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        //Pass the notification to the NotificationManager.

        nm.notify(MESSAGE_NOTIFICATION_ID, builder.build());

    }

    public static <K extends Comparable,V extends Comparable> Map<K,V> sortByKeys(Map<K,V> map){
        List<K> keys = new LinkedList<K>(map.keySet());
        Collections.sort(keys);

        //LinkedHashMap will keep the keys in the order they are inserted
        //which is currently sorted on natural ordering
        Map<K,V> sortedMap = new LinkedHashMap<K,V>();
        for(K key: keys){
            sortedMap.put(key, map.get(key));
        }

        return sortedMap;
    }

    private static NotificationCompat.Builder createMessageNotificationBuilder(
            Context context, String userID, String userName, String name) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context);
        builder.setPriority(Notification.PRIORITY_MAX);
        builder.setContentTitle(getActiveMessageCount() > 1 ? "Messages"
                : "Message");
        String contentText = buildMessagetContentText(userName);
        builder.setContentText(contentText);
        builder.setSmallIcon(R.drawable.ic_launcher); // stat_notify_chat taken from Android SDK (API level 17)
        //android.R.drawable.stat_notify_chat
        builder.setAutoCancel(true);
        builder.setWhen(System.currentTimeMillis());

        if (mapMessages.size() > 1) {
            if (Build.VERSION.SDK_INT >= 19) // KITKAT
            {
                PendingIntent pi = createNotificationMessageActivityPendingIntent(context);
                if (pi != null) {
                    pi.cancel();
                }
            }

            PendingIntent msgPendingIntent = createNotificationMessageActivityPendingIntent(context);
            builder.setContentIntent(msgPendingIntent);
        } else {
            if (Build.VERSION.SDK_INT >= 19) // KITKAT
            {
                PendingIntent pi = createChatActivityPendingIntent(context,
                        userID, userName, name);
                if (pi != null) {
                    pi.cancel();
                }
            }

            PendingIntent msgPendingIntent = createChatActivityPendingIntent(
                    context, userID, userName, name);

            builder.setContentIntent(msgPendingIntent);
        }

        //		if (getActiveMessageCount() > 1) {

        applyBigViewStyleMessage(builder, contentText);
        //		}

        return builder;
    }
    private static int getActiveMessageCount() {
        int cnt = 0;
        if (privateMessages != null) {
            cnt = privateMessages.size();
        }
        return cnt;
    }


    private static void applyBigViewStyleMessage(
            NotificationCompat.Builder builder, String summaryText) {
        if ((mapMessages != null) && (getActiveMessageCount() >= 1)) {
            NotificationCompat.BigTextStyle inboxStyle = new NotificationCompat.BigTextStyle();

            //			Log.e("message size", "" + privateMessages.size());
            //			StringBuilder bigtext = new StringBuilder();
            //			for (int i = 0; i < privateMessages.size(); i++) {
            //
            //				bigtext.append(privateMessages.get(i) + "\n");
            //			}



            inboxStyle.bigText(summaryText);
            if (getActiveMessageCount() > 1)
                inboxStyle.setSummaryText(privateMessages.size()
                        + " conversations from " + mapMessages.size()
                        + " users");
            //			else
            //				inboxStyle.setSummaryText("");

            builder.setStyle(inboxStyle);
        }
    }
    private static String buildMessagetContentText(String userName) {
        String contentText = "";
        //At this point activeChats should be non-null and have at least one element.
        if ((mapMessages != null) && (mapMessages.size() > 0)) {
            int msgCnt = getActiveMessageCount();
            StringBuilder sb = new StringBuilder();
            for (int i = privateMessages.size() - 1; i >= 0; i--) {
                sb.append( privateMessages.get(i) + "\n");
                contentText = sb.toString();
                Log.e("", "MessageContent:" + contentText);
            }
        }
        return contentText;
    }


    //IT WILL OPEN CHAT ACTIVITY
    private static PendingIntent createChatActivityPendingIntent(
            Context context, String userID, String userName, String name) {
        //Create an intent to start DalChatActivity.

        Intent msgIntent = new Intent(context, ChatActivity_old.class);
        msgIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        msgIntent.putExtra(WannaShareConstants.EXTRA_PROFILE_ID, userID);
        msgIntent.putExtra(WannaShareConstants.EXTRA_FRIEND_NAME, name);
        msgIntent.putExtra(WannaShareConstants.EXTRA_IS_NOTIFICATION, true);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        //Add the back stack to the stack builder. This method also adds flags
        //that start the stack in a fresh task.
        stackBuilder.addParentStack(ChatActivity_old.class);
        //Add the Intent that starts the Activity from the notification.
        stackBuilder.addNextIntent(msgIntent);

        //Get a PendingIntent containing the entire back stack.
        PendingIntent msgPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);


        return msgPendingIntent;
    }
    //IT WILL OPEN FRIENDLIST SCREEN.
    private static PendingIntent createNotificationMessageActivityPendingIntent(
            Context context) {
        //Start DalMainActivity in the message threads position.
        Intent msgIntent = new Intent(context, HomeActivity.class);
        msgIntent.putExtra(WannaShareConstants.EXTRA_MULTIPLE_NOTIFICATION, true);
        msgIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //        msgIntent.putExtra(DalMainActivity.KEY_PAGE_INDEX, DalMainActivity.MESSAGES_POSITION);

        android.support.v4.app.TaskStackBuilder stackBuilder = android.support.v4.app.TaskStackBuilder.create(context);
        //Add the back stack to the stack builder. This method also adds flags
        //that start the stack in a fresh task.
        stackBuilder.addParentStack(HomeActivity.class);
        //Add the Intent that starts the Activity from the notification.
        stackBuilder.addNextIntent(msgIntent);

        //Get a PendingIntent containing the entire back stack.
        PendingIntent msgPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);

        return msgPendingIntent;
    }


}