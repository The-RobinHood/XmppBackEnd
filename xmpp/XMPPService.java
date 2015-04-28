package com.wannashare.xmpp;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import com.wannashare.constants.WannaShareConstants;
import com.wannashare.db.DBHelper;
import com.wannashare.main.ChatActivity_old;

import org.jivesoftware.smack.XMPPConnection;

import java.util.ArrayList;


public class XMPPService extends Service {

	public static final String ACTION_PERFORMED = "com.example.xmlpp.MessageService.ACTION_PERFORMED";
    public static final String ACTION_UNREADCOUNT = "com.example.xmlpp.MessageService.ACTION_UNREADCOUNT";

	public static final String ACTION = "action";
	public static final String DATA_MESSAGE = "data_message";
	public static final String DATA_MESSAGE_ID = "data_message_id";
	public static final String DATA_USER_ID = "data_user_id";
	public static final String DATA_USER_STATUS = "data_user_status";

	public static final String DATA_LIST = "data_list";
	public static final String DATA = "data";

	// SENDING ACTIONS
	public static final int ACTION_REGISTER = 0;
	public static final int ACTION_LOGIN = 1;
	public static final int ACTION_LOGOUT = 2;
	public static final int ACTION_SEND_MESSAGE = 4;
	public static final int ACTION_FRIEND_LIST = 5;
	public static final int ACTION_GET_USER_PRESENCE = 6;
	public static final int ACTION_SUBSCRIBE_USER= 7;
	public static final int ACTION_SEND_PRESENCE=8;

	// RECEIVING ACTIONS
	public static final int ACTION_FLAG_REGISTER_SUCCESS = 0;
	public static final int ACTION_FLAG_REGISTER_FAILED = 1;
	public static final int ACTION_FLAG_LOGIN_SUCCESS = 2;
	public static final int ACTION_FLAG_LOGIN_FAILED = 3;
	public static final int ACTION_FLAG_LOGOUT = 4;
	public static final int ACTION_FLAG_SEND_MESSAGE_SUCCESS = 5;
	public static final int ACTION_FLAG_SEND_MESSAGE_FAILED = 6;
	public static final int ACTION_FLAG_CONNECT_SUCCESS = 7;
	public static final int ACTION_FLAG_CONNECT_FAILED = 8;
	public static final int ACTION_FLAG_MESSAGE_RECEIVED = 9;
	public static final int ACTION_FLAG_FRIEND_LIST_RECIEVED = 10;
	public static final int ACTION_FLAG_SEND_MESSAGE_DELIVERED = 11;
	public static final int ACTION_FLAG_GET_USER_PRESENCE = 12;

	private XMPPHelper mXmppHelper;
	private UserUtils sPreferenceManager;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		mXmppHelper = new XMPPHelper(this);
		sPreferenceManager = UserUtils.getInstance(this);
		mXmppHelper.setXMPPCallbacks(xmppCallbacks);

		// REGISTERING LISTENER FOR CONNECTIVITY CHANGE
		IntentFilter networkStateFilter = new IntentFilter();
		networkStateFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		registerReceiver(networkStateChangeReciever, networkStateFilter);
	}

	@Override
	public void onDestroy() {
		ChatLog.i("service is destroyed...");
		unregisterReceiver(networkStateChangeReciever);
		mXmppHelper.disconnect();
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			Bundle bundle = intent.getExtras();

			if (bundle != null) {
				int mMode = bundle.getInt(ACTION, -1);

				ChatUser mUser = sPreferenceManager.getCachedUser();

				switch (mMode) {
				case ACTION_REGISTER:
					mXmppHelper.register(mUser);
					break;
				case ACTION_LOGIN:
					mXmppHelper.login(mUser);
					break;
				case ACTION_LOGOUT:
					mXmppHelper.disconnect();
					break;
				case ACTION_SEND_MESSAGE:

					// ChatMessage chatMessage = bundle.getParcelable(DATA_MESSAGE);
					// ArrayList<ChatUser> chatUsersList = bundle.getParcelableArrayList(DATA_LIST);
					// mXmppHelper.sendMessage(chatMessage, chatUsersList);

					ChatMessage chatMessage = bundle.getParcelable(DATA_MESSAGE);
					String toUser = bundle.getString(DATA_LIST);
					mXmppHelper.sendMessage(chatMessage, toUser);

					break;

				case ACTION_FRIEND_LIST:

					mXmppHelper.getFriendList();

					break;
				case ACTION_GET_USER_PRESENCE:
					String userID = bundle.getString(DATA_USER_ID);
					mXmppHelper.getUserPresence(userID);
					break;
				case ACTION_SEND_PRESENCE:
					mXmppHelper.sendPresencePacket(bundle.getBoolean(DATA_USER_STATUS));
					break;
				case ACTION_SUBSCRIBE_USER:
					mXmppHelper.sendSubscribePacket(bundle.getString(DATA_USER_ID));
					break;
				}
			} else {
			}
		}
		return START_STICKY;
	}

	private XMPPCallbacks xmppCallbacks = new XMPPCallbacks() {

		@Override
		public void onRegistrationFailed(Exception e) {
			// LogM.e("XMPP User Registration failed");
			Intent actionIntent = new Intent(ACTION_PERFORMED);
			actionIntent.putExtra(ACTION, ACTION_FLAG_REGISTER_FAILED);
			sendBroadcast(actionIntent);

			ChatLog.i("onRegistrationFailed : " + e.getMessage() + "\n=======");
		}

		@Override
		public void onRegistrationComplete() {

			Intent actionIntent = new Intent(ACTION_PERFORMED);
			actionIntent.putExtra(ACTION, ACTION_FLAG_REGISTER_SUCCESS);
			sendBroadcast(actionIntent);

			ChatLog.i("onRegistrationComplete\n=======");
		}

		@Override
		public void onMessageSent(ChatMessage chatMessage) {
			Intent actionIntent = new Intent(ACTION_PERFORMED);
			actionIntent.putExtra(ACTION, ACTION_FLAG_SEND_MESSAGE_SUCCESS);
			actionIntent.putExtra(DATA_MESSAGE, chatMessage);
			sendBroadcast(actionIntent);

			ChatLog.i("onMessageSent\n=======");
		}

		@Override
		public void onMessageDeliver(String messageId) {
			Intent actionIntent = new Intent(ACTION_PERFORMED);
			actionIntent.putExtra(ACTION, ACTION_FLAG_SEND_MESSAGE_DELIVERED);
			actionIntent.putExtra(DATA_MESSAGE_ID, messageId);
			sendBroadcast(actionIntent);

			ChatLog.i("onMessageSent\n=======");
		}

		@Override
		public void onMessageSendingFailed(ChatMessage chatMessage, Exception e) {
			Intent actionIntent = new Intent(ACTION_PERFORMED);
			actionIntent.putExtra(ACTION, ACTION_FLAG_SEND_MESSAGE_FAILED);
			actionIntent.putExtra(DATA_MESSAGE, chatMessage);
			sendBroadcast(actionIntent);

			ChatLog.i("onMessageSendingFailed: " + e.getMessage() + "\n=======");
		}

		@Override
		public void onMessageRecieved(ChatMessage chatMessage) {

//			if (ApplicationSession.getSession(getApplicationContext()).getBoolean(ConstantCode.PREF_IS_CHAT_ACTIVITY_OPEN)) {
            if (ChatActivity_old.ISVISIBLE) {
				// if application is open then send broadcast so user can show it on listview
				Intent actionIntent = new Intent(ACTION_PERFORMED);
				actionIntent.putExtra(ACTION, ACTION_FLAG_MESSAGE_RECEIVED);
				actionIntent.putExtra(DATA_MESSAGE, chatMessage);
				sendBroadcast(actionIntent);
				
			} else {
                ChatUtils.generateMessageNotification(getApplicationContext(),chatMessage.msg_text,chatMessage.sender_id,chatMessage.vname,chatMessage.vname);
//                ChatUtils.sendNotification(getApplicationContext(), chatMessage);

                   //WHEN CHAT ACTIVITY IS NOT OPENED.
                DBHelper.getInstance(getApplicationContext()).updateISReadStatus(chatMessage.msg_id,"false");
                Intent actionIntent = new Intent(ACTION_UNREADCOUNT);
//                actionIntent.putExtra(ACTION, ACTION_FLAG_MESSAGE_RECEIVED);
                actionIntent.putExtra(WannaShareConstants.EXTRA_RECEIVER_ID, chatMessage.receiver_id);
                actionIntent.putExtra(WannaShareConstants.EXTRA_SENDER_ID, chatMessage.sender_id);
                sendBroadcast(actionIntent);

			}

			ChatLog.i("onMessageRecieved\n=======");
		}

		@Override
		public void onGetUserPresence(String userId, String status) {
			Intent actionIntent = new Intent(ACTION_PERFORMED);
			actionIntent.putExtra(ACTION, ACTION_FLAG_GET_USER_PRESENCE);
			actionIntent.putExtra(DATA_USER_ID, userId);
			actionIntent.putExtra(DATA_USER_STATUS, status);

			sendBroadcast(actionIntent);

			ChatLog.i("onGetUserPresence \n=======");
		}
		@Override
		public void onUserPresenceChange(String userId, String status) {
			Intent actionIntent = new Intent(ACTION_PERFORMED);
			actionIntent.putExtra(ACTION, ACTION_FLAG_GET_USER_PRESENCE);
			actionIntent.putExtra(DATA_USER_ID, userId);
			actionIntent.putExtra(DATA_USER_STATUS, status);

			sendBroadcast(actionIntent);

			ChatLog.i("onGetUserPresence \n=======");
		}

		@Override
		public void onLoginFailed(Exception e) {
			Intent actionIntent = new Intent(ACTION_PERFORMED);
			actionIntent.putExtra(ACTION, ACTION_FLAG_LOGIN_FAILED);
			sendBroadcast(actionIntent);

			ChatLog.i("onLoginFailed " + e.getMessage() + "\n=======");
		}

		@Override
		public void onLogin(ChatUser ChatUser) {
			Intent actionIntent = new Intent(ACTION_PERFORMED);
			actionIntent.putExtra(ACTION, ACTION_FLAG_LOGIN_SUCCESS);
			sendBroadcast(actionIntent);

			resendPendingMessageFromDatabase();
			ChatLog.i("onLogin\n=======");
		}

		@Override
		public void onConnected(XMPPConnection connection) {
			Intent actionIntent = new Intent(ACTION_PERFORMED);
			actionIntent.putExtra(ACTION, ACTION_FLAG_CONNECT_SUCCESS);
			sendBroadcast(actionIntent);

			ChatLog.i("onConncted\n=======");
		}

		@Override
		public void onConnectionFailed(Exception e) {
			Intent actionIntent = new Intent(ACTION_PERFORMED);
			actionIntent.putExtra(ACTION, ACTION_FLAG_CONNECT_FAILED);
			sendBroadcast(actionIntent);

			ChatLog.i("onConnectionFailed :" + e.getMessage() + "\n=======");
		}

		@Override
		public void onLogout() {
			Intent actionIntent = new Intent(ACTION_PERFORMED);
			actionIntent.putExtra(ACTION, ACTION_FLAG_LOGOUT);
			sendBroadcast(actionIntent);

			ChatLog.i("onLogout\n=======");

			XMPPService.this.stopSelf();
		}

		@Override
		public void onFriendListReceived(ArrayList<String> list) {

			final Intent actionIntent = new Intent(ACTION_PERFORMED);
			actionIntent.putExtra(ACTION, ACTION_FLAG_FRIEND_LIST_RECIEVED);
			actionIntent.putStringArrayListExtra(DATA_LIST, list);

			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					sendBroadcast(actionIntent);
					ChatLog.i("send broadcast to friendlist");
				}
			}, 100);

			ChatLog.i("onFriendListReceived\n=======");

		}
	};

	private BroadcastReceiver networkStateChangeReciever = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
            ChatLog.i("Networking connectivity came");
			if (WannaShareConstants.IsInternetConnected(context) && sPreferenceManager.isLoggedIn()) {
                ChatLog.i("Networking already logged-in");
				if (mXmppHelper.getConnection() == null || !mXmppHelper.getConnection().isConnected() || !mXmppHelper.getConnection().isAuthenticated()) {
					ChatLog.i("Networking connectivity came and login");
                    mXmppHelper.login(sPreferenceManager.getCachedUser());
				}else{
                    resendPendingMessageFromDatabase();
                }
			}
		}
	};

	private void resendPendingMessageFromDatabase() {
        ChatLog.i("Networking connectivity Sending pending messages");
		ArrayList<ChatMessage> listChatMessages = DBHelper.getInstance(getApplicationContext()).getAllPendingMessage();
		ChatUtils chatUtils = new ChatUtils(getApplicationContext());
		for (ChatMessage chatMessage : listChatMessages) {
			chatUtils.sendMessage(chatMessage, chatMessage.receiver_id);
		}
	}
}