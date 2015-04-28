package com.wannashare.xmpp;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.wannashare.constants.ChatConstants;
import com.wannashare.constants.WannaShareConstants;
import com.wannashare.db.DBHelper;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.ConnectionCreationListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.SmackException.ConnectionException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;

public class XMPPHelper {

	public static boolean shouldNotify = true;

    //CUSTOM OBJECT
//    public static String MESSAGE_TAG="message";
//    public static String  MESSAGE_ID="message_id";
//    public static String  MESSAGE_TEXT="message_text";
//    public static String  MESSAGE_DATE="message_date";
//    public static String  SENDER_NAME="sender_name";
//    public static String  SENDER_ID="sender_id";
//    public static String  RECEIVER_ID="receiver_id";
//    public static String  GROUP_ID="group_id";

	// CONFIGURATION
    public static String SERVER_NAME ="@54.153.127.219";
    public static int PORT =5222;
    public static String HOST ="54.153.127.219";

	// private static final String HOST = "talk.google.com";
	// private static final int PORT = 5222;

	private Context mContext;

	private ConnectionConfiguration mConfiguration;
	private XMPPConnection mConnection;
	private ChatManager mChatManager;
	private XMPPCallbacks xmppCallBacks;
	private ChatUser mChatUser;
	private Roster mRoster;
	private PENDING_ACTION mPendingAction = PENDING_ACTION.NONE;

	private enum PENDING_ACTION {
		NONE, REGISTER, LOGIN
	}

	public XMPPHelper(Context mContext) {
		this.mContext = mContext;

		SmackAndroid.init(mContext);
		mConfiguration = new ConnectionConfiguration(HOST, PORT);
		// mConfiguration = new ConnectionConfiguration(HOST, PORT, "gmail.com");
		mConfiguration.setReconnectionAllowed(true);
		mConfiguration.setSecurityMode(SecurityMode.disabled);
		mConfiguration.setDebuggerEnabled(true);
        XMPPConnection.addConnectionCreationListener(onConnectionCreationListener);

	}

	public void setXMPPCallbacks(XMPPCallbacks xmppCallBacks) {
		this.xmppCallBacks = xmppCallBacks;
	}

	public XMPPConnection getConnection() {
		return mConnection;
	}

	// ==========CONNECTIONS===========

	public void connect() {
		new ConnectionTask().execute();
	}

	private class ConnectionTask extends AsyncTask<Void, Void, Boolean> {
		private Exception exception;

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				xmppConnect();
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				exception = e;
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub

			if (xmppCallBacks != null && result) {
				if (mPendingAction == PENDING_ACTION.REGISTER) {
					register(mChatUser);
				} else if (mPendingAction == PENDING_ACTION.LOGIN) {
					login(mChatUser);
				} else {
					xmppCallBacks.onConnected(getConnection());
				}
			} else {
				xmppCallBacks.onConnectionFailed(exception);
			}

		}
	}

	private void xmppConnect() throws Exception {

		if (mConnection == null) {
			mConnection = new XMPPTCPConnection(mConfiguration);

			mConnection.connect();
		} else {
			if (!mConnection.isConnected()) {

				mConnection.connect();
			}
		}

	}

	public void disconnect() {
		new DisconnectionTask().execute();
	}

	private class DisconnectionTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			if (mConnection != null) {
				try {
					mConnection.disconnect();
				} catch (NotConnectedException e) {
					e.printStackTrace();
				}
				mConnection = null;
				// LogM.d("Connection disconnected...");
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			if (xmppCallBacks != null)
				xmppCallBacks.onLogout();
            Log.e("##########","Xmpp Disconnect");
		}
	}

	// ==========REGISTER ===========

	public void register(ChatUser chatUser) {
		this.mChatUser = chatUser;
		try {
			if (mConnection != null && mConnection.isConnected()) {
				new RegisterationTask().execute(chatUser);
			} else {
				// //LogM.d("Connection not found while trying to register, re-connecting...");
				mPendingAction = PENDING_ACTION.REGISTER;
				connect();
			}
		} catch (Exception e) {
			if (xmppCallBacks != null)
				xmppCallBacks.onRegistrationFailed(e);
			e.printStackTrace();
		}
	}

	private class RegisterationTask extends AsyncTask<ChatUser, Void, Boolean> {

		private Exception exception;

		@Override
		protected Boolean doInBackground(ChatUser... params) {

			try {
				xmppRegister(params[0]);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				exception = e;
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);

			if (xmppCallBacks != null && result)
				xmppCallBacks.onRegistrationComplete();
			else
				xmppCallBacks.onRegistrationFailed(exception);
		}

	}

	public void xmppRegister(ChatUser chatUser) throws Exception {

		AccountManager mAccountManager = AccountManager.getInstance(mConnection);

		mAccountManager.createAccount(chatUser.getUserId(), chatUser.getPassword(), null);

	}

	// ==========LOGIN===========

	public void login(ChatUser chatUser) {
		this.mChatUser = chatUser;

		try {
			ChatLog.i("Checking connection...");
			if (mConnection != null && mConnection.isConnected()) {
				ChatLog.i("connection is there...");
				ChatLog.i("checking authentication...");
				if (!mConnection.isAuthenticated()) {
					ChatLog.i("not authenticated so logining...");
					new LoginTask().execute();
				} else {
					listenForInCommingPacket();
					if (xmppCallBacks != null) {
						sendPresencePacket(true);
						xmppCallBacks.onLogin(mChatUser);
					}
				}
			} else {
				mPendingAction = PENDING_ACTION.LOGIN;
				connect();
			}
		} catch (Exception e) {
			if (xmppCallBacks != null)
				xmppCallBacks.onLoginFailed(e);
			e.printStackTrace();
		}

	}

	public void sendSubscribePacket(String userId) {
		// SENDING SUBSCRIBE PACKET
		try {
			if (mRoster == null)
				mRoster = mConnection.getRoster();

			if (mRoster != null) {
				Collection<RosterEntry> entries = mRoster.getEntries();
				boolean isFound = false;
				for (RosterEntry entry : entries) {
					Presence presence = mRoster.getPresence(entry.getUser());
					if (presence.getFrom().split("/")[0].equalsIgnoreCase(userId)) {
						isFound = true;
						break;
					}
				}

				if (isFound == false) {
					Presence subscribe = new Presence(Presence.Type.subscribe);
					subscribe.setTo(userId);
					// subscribe.setMode(Presence.Mode.available);
					mConnection.sendPacket(subscribe);
				}
			}
		} catch (Exception e) {
			ChatLog.e("Exception on sending presence packet" + e.toString());
			if (e instanceof ConnectionException) {
				ChatLog.e("Exception on sending presence packet" + ((ConnectionException) e).getFailedAddresses().get(0).getErrorMessage());
			}
		}
	}

	public void sendPresencePacket(boolean isAvailable) {
		// SENDING PRESENCE PACKET
		try {
			ChatLog.e("Sending " + isAvailable + " Packet");
			Presence presence;
			if (isAvailable) {
				presence = new Presence(Presence.Type.available);
				presence.setMode(Mode.chat);
			} else {
				presence = new Presence(Presence.Type.available);
				presence.setMode(Mode.available);
			}

			mConnection.sendPacket(presence);
		} catch (Exception e) {
			ChatLog.e("Exception on sending presence packet");
		}
	}

	private class LoginTask extends AsyncTask<Void, Void, Boolean> {

		private Exception exception;

		@Override
		protected Boolean doInBackground(Void... params) {

			try {
				ChatLog.i("Logintask goining to xmppLogin...");
				xmppLogin(mChatUser);

				listenForInCommingPacket();

				return true;
			} catch (Exception e) {
				ChatLog.i("Error on login...");
				e.printStackTrace();
				exception = e;
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);

			if (xmppCallBacks != null && result) {
				xmppCallBacks.onLogin(mChatUser);
			} else {
				xmppCallBacks.onLoginFailed(exception);
			}
		}
	}

	private void listenForInCommingPacket() {
		PacketFilter chatFilter = new MessageTypeFilter(Message.Type.chat);
		PacketTypeFilter presenceFilter = new PacketTypeFilter(Presence.class);
		mConnection.addPacketListener(mChatListener, chatFilter);
		mConnection.addPacketListener(mPresenceListener, presenceFilter);
	}

	private void xmppLogin(ChatUser chatUser) throws Exception {
        Log.e("#############","UserId="+chatUser.getUserId());
        Log.e("#############","User Password="+chatUser.getPassword());

		// mConnection.login(chatUser.getUserId(), chatUser.getPassword());
		// mConnection.login(chatUser.getUserId(), chatUser.getPassword(), System.currentTimeMillis() + "");
		mConnection.login(chatUser.getUserId(), chatUser.getPassword());
//		ChatLog.i("checking application is open or not" + App.isApplicationOpen());

//		if (App.isApplicationOpen())
//			sendPresencePacket(true);
//		else
//			sendPresencePacket(false);
//
	}

	public void logout1() {
		ChatLog.i("Logout called");
		try {
			mConnection.disconnect();
		} catch (Exception e) {

		}
	}

	// ==========SENDING MESSAGE===========

	public void sendMessage(ChatMessage messageData, ArrayList<ChatUser> userList) {

		if (WannaShareConstants.IsInternetConnected(mContext)) {
			try {
				if (mConnection != null && mConnection.isConnected()) {

					if (mConnection.isAuthenticated()) {
						new SendMessageTaskList(messageData, userList).execute();
					} else {
						mPendingAction = PENDING_ACTION.LOGIN;
						login(mChatUser);
					}

				} else {
					mPendingAction = PENDING_ACTION.NONE;
					connect();
				}

			} catch (Exception e) {
				if (xmppCallBacks != null)
					xmppCallBacks.onMessageSendingFailed(messageData, e);
				e.printStackTrace();
			}
		} else {
			if (xmppCallBacks != null)
				xmppCallBacks.onMessageSendingFailed(messageData, new Exception("Network unavailable"));
		}

	}

	private class SendMessageTask extends AsyncTask<Void, Void, Boolean> {

		private ChatMessage messageData;
		private String toUser;
		private Exception exception;

		public SendMessageTask(ChatMessage messageData, String toUser) {
			this.messageData = messageData;
			this.toUser = toUser;
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			try {

				xmppSendMessage(messageData, toUser);

				return true;
			} catch (Exception e) {
				e.printStackTrace();
				exception = e;
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {

			if (xmppCallBacks != null && result) {
				xmppCallBacks.onMessageSent(messageData);
                messageData.status=ChatConstants.STATUS_TYPE_SENT;
                DBHelper.getInstance(mContext).update(messageData);
			} else {
				xmppCallBacks.onMessageSendingFailed(messageData, exception);
			}
		}
	}

	public void sendMessage(ChatMessage messageData, String toUser) {

		if (WannaShareConstants.IsInternetConnected(mContext)) {
			try {
				if (mConnection != null && mConnection.isConnected()) {

					if (mConnection.isAuthenticated()) {
						new SendMessageTask(messageData, toUser).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
					} else {
						mPendingAction = PENDING_ACTION.LOGIN;
						login(mChatUser);
					}

				} else {
					mPendingAction = PENDING_ACTION.NONE;
					connect();
				}

			} catch (Exception e) {
				if (xmppCallBacks != null)
					xmppCallBacks.onMessageSendingFailed(messageData, e);
				e.printStackTrace();
			}
		} else {
			if (xmppCallBacks != null)
				xmppCallBacks.onMessageSendingFailed(messageData, new Exception("Network unavailable"));
		}

	}

	private class SendMessageTaskList extends AsyncTask<Void, Void, Boolean> {

		private ChatMessage messageData;
		private ArrayList<ChatUser> userList;
		private Exception exception;

		public SendMessageTaskList(ChatMessage messageData, ArrayList<ChatUser> userList) {
			this.messageData = messageData;
			this.userList = userList;
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			try {
				for (ChatUser mChatnaUser : userList) {
					xmppSendMessage(messageData, mChatnaUser.getUserId());
				}
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				exception = e;
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {

			if (xmppCallBacks != null && result) {
				xmppCallBacks.onMessageSent(messageData);
                messageData.status=ChatConstants.STATUS_TYPE_SENT;
                DBHelper.getInstance(mContext).update(messageData);
			} else {
				xmppCallBacks.onMessageSendingFailed(messageData, exception);
			}
		}
	}

	private void xmppSendMessage(ChatMessage messageData, String userId) throws Exception {
		// String message = encodeToBase64(messageData.getMessage());
//		String message = messageData.raw_data;
		// Chat mChat = ChatManager.getInstanceFor(mConnection).createChat(userId, messageListener);
		/*Chat mChat = ChatManager.getInstanceFor(mConnection).createChat(userId, null);
		Packet packet = messageData.toPacket(true, true);
		mConnection.sendPacket(packet);*/

//        Message msg = new Message(userId, Message.Type.chat);
//        msg.setBody(messageData.msg_text);
//        msg.setFrom(messageData.sender_id);
//        msg.setTo(messageData.receiver_id+(messageData.receiver_id.contains("@")?"":("@"+HOST)));
//        Log.e("#########", "Jiga" + msg.toXML());
//        msg.toXML();
//        mConnection.sendPacket(msg);
        try {
            JSONObject obj = new JSONObject();
            obj.put(WannaShareConstants.MESSAGE_ID,messageData.msg_id);
            obj.put(WannaShareConstants.MESSAGE_TEXT,messageData.msg_text);
            obj.put(WannaShareConstants.MESSAGE_DATE,messageData.msg_date);
            obj.put(WannaShareConstants.SENDER_NAME,messageData.vname);
            obj.put(WannaShareConstants.SENDER_ID,messageData.sender_id);
            obj.put(WannaShareConstants.RECEIVER_ID,messageData.receiver_id);
            obj.put(WannaShareConstants.GROUP_ID,messageData.group_id);
            JSONObject jobj= new JSONObject();
            jobj.put(WannaShareConstants.MESSAGE_TAG,obj);

            Message msg= new Message();
            msg.setBody(jobj.toString());
            msg.setPacketID(messageData.msg_id);

            DeliveryReceiptManager.addDeliveryReceiptRequest(msg);
            if(!userId.contains("@"))
                userId=userId+"@"+HOST;
            Chat mChat=ChatManager.getInstanceFor(mConnection).createChat(userId, null);
            mChat.sendMessage(msg);
        }catch (Exception e){
            e.printStackTrace();
        }


		// Utility.showLog(ChatMessage.class, "getting message from ChatMessage class");
		// Message msg = messageData.toMessage();
		// Thread.sleep(1000);
		// Utility.showLog(ChatMessage.class, "retrived message from ChatMessage class");
		// // msg.setBody(messageData.raw_data);
		// if (msg != null) {
		// mChat.sendMessage(msg);
		// Utility.showLog(ChatMessage.class, "message sended to xmpp");
		// }
		// Message msg=(Message) messageData.toPacket();
		// msg.get
		// mChat.sendMessage(message);
	}

	// ==========GET FRIEND LIST===========

	public void getFriendList() {

		if (WannaShareConstants.IsInternetConnected(mContext)) {
			try {
				if (mConnection != null && mConnection.isConnected()) {

					if (mConnection.isAuthenticated()) {
						new GetFriendListTask().execute();
					} else {
						mPendingAction = PENDING_ACTION.LOGIN;
						login(mChatUser);
					}

				} else {
					mPendingAction = PENDING_ACTION.NONE;
					connect();
				}

			} catch (Exception e) {
				if (xmppCallBacks != null)
					// xmppCallBacks.onMessageSendingFailed(messageData, e);
					e.printStackTrace();
			}
		} else {
			if (xmppCallBacks != null) {
				// xmppCallBacks.onMessageSendingFailed(messageData, new Exception("Network unavailable"));
			}
		}

	}

	private class GetFriendListTask extends AsyncTask<Void, Void, Boolean> {

		private Exception exception;
		private ArrayList<String> friendList;

		public GetFriendListTask() {

		}

		@Override
		protected Boolean doInBackground(Void... params) {

			try {
				friendList = xmppgetFriendList();
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				exception = e;
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {

			if (xmppCallBacks != null && result) {
				xmppCallBacks.onFriendListReceived(friendList);
			} else {
				// xmppCallBacks.onMessageSendingFailed(messageData, exception);
			}
		}
	}

	private ArrayList<String> xmppgetFriendList() throws Exception {
		try {
			ArrayList<String> friendList = new ArrayList<String>();
			Roster roster = mConnection.getRoster();
			Collection<RosterEntry> entries = roster.getEntries();
			for (RosterEntry entry : entries) {
				System.out.println(entry);
				ChatLog.i(entry.toString());
				friendList.add(entry.getUser());
			}
			return friendList;
		} catch (Exception e) {
			return null;
		}
	}

	// ==========HELPERS===========

	private ChatMessage parceRecievedPacket(Message message) {

		if (message.getBody() != null && !message.getBody().isEmpty()) {
			try {
                Log.e("##############","Message received="+message.toString());

				// String decodedMessage = decodeFromBase64(message.getBody());

				    String rawData = message.getBody();

                    JSONObject obj=new JSONObject(rawData);
                    JSONObject jobj= obj.getJSONObject(WannaShareConstants.MESSAGE_TAG);
                    ChatMessage  chatMessage= new ChatMessage();
                    chatMessage.msg_id=jobj.getString(WannaShareConstants.MESSAGE_ID);
                    chatMessage.msg_text=jobj.getString(WannaShareConstants.MESSAGE_TEXT);
                    chatMessage.msg_date=jobj.getString(WannaShareConstants.MESSAGE_DATE);
                    chatMessage.vname=jobj.getString(WannaShareConstants.SENDER_NAME);
                    chatMessage.sender_id=jobj.getString(WannaShareConstants.SENDER_ID);
                    chatMessage.receiver_id=jobj.getString(WannaShareConstants.RECEIVER_ID);
                    chatMessage.group_id=jobj.getString(WannaShareConstants.GROUP_ID);
                    chatMessage.isdeliver="false";
                    chatMessage.isread="false";
                    DBHelper.getInstance(mContext).add(chatMessage);

//                ChatMessage chatMessage = ChatUtils.getLocationMessageWithOrderId(_StrUserID, "Roger Steve", _StrRecipientId,newMessage,"false","","");

//				JSONObject jsonObject = new JSONObject(rawData);
//				jsonObject.put(ChatConstants.PARAMS_DATE, ChatConstants.getCurrentUTCDate());
//				ChatMessage chatMessage = new ChatMessage(jsonObject.optJSONObject("message"));

				return chatMessage;

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	/*
	 * private String encodeToBase64(String message) { return Base64.encodeToString(message.getBytes(), Base64.DEFAULT); }
	 * 
	 * private String decodeFromBase64(String encodedMessage) throws UnsupportedEncodingException { if (encodedMessage.matches("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$")) { byte[] decArr = Base64.decode(encodedMessage, Base64.DEFAULT); return new String(decArr, "UTF-8"); } return new String(encodedMessage.getBytes(), "UTF-8"); }
	 */

	private ConnectionCreationListener onConnectionCreationListener = new ConnectionCreationListener() {

		@Override
		public void connectionCreated(XMPPConnection arg0) {
			arg0.setPacketReplyTimeout(5000);
			ChatLog.i("Connection created...");

			switch (mPendingAction) {
			case NONE:
				break;
			case LOGIN:
				ChatLog.i("Pending action is login...");
				login(mChatUser);
				break;
			case REGISTER:
				register(mChatUser);
				break;
			}
			mPendingAction = PENDING_ACTION.NONE;

			// FOR RECEIPTS AND FRIENDS OFFLINE, ONLINE STATUS CHANGE
//            if(mConnection!=null){
                mChatManager = ChatManager.getInstanceFor(mConnection);
                DeliveryReceiptManager.getInstanceFor(mConnection).enableAutoReceipts();
                DeliveryReceiptManager.getInstanceFor(mConnection).addReceiptReceivedListener(mReceiptReceived);
                mRoster = mConnection.getRoster();
                mRoster.addRosterListener(mRosterListener);
//            }else{
//                Log.e("############","Connection null");
//            }


			/*
			 * PacketFilter filter = new MessageTypeFilter(Message.Type.chat); mConnection.addPacketListener(mPacketListener, filter);
			 */

			if (xmppCallBacks != null)
				xmppCallBacks.onConnected(mConnection);
		}
	};

	private PacketListener mChatListener = new PacketListener() {

		@Override
		public void processPacket(Packet packet) {
			Message message = (Message) packet;
			ChatMessage messageData = parceRecievedPacket(message);

			if (xmppCallBacks != null && messageData != null)
				xmppCallBacks.onMessageRecieved(messageData);

		}
	};

	private PacketListener mPresenceListener = new PacketListener() {

		@Override
		public void processPacket(Packet packet) {
			Presence presence = (Presence) packet;
			if (presence.getType() == Presence.Type.subscribe) {
				sendSubscribePacket(presence.getFrom());
			}
		}
	};

	private ReceiptReceivedListener mReceiptReceived = new ReceiptReceivedListener() {

		@Override
		public void onReceiptReceived(String fromJid, String toJid, String receiptId) {
			// TODO Auto-generated method stub
            Log.e("##########","Message onReceiptReceived receiptId="+receiptId);
			// ChatLog.d(fromJid+","+toJid+","+toJid);
			if (xmppCallBacks != null)
				xmppCallBacks.onMessageDeliver(receiptId);
			DBHelper.getInstance(mContext).updateStatus(receiptId, ChatConstants.STATUS_TYPE_DELIVER);
		}

	};

	public void getUserPresence(String userId) {
		if (mRoster != null) {
			mRoster = mConnection.getRoster();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}

			Presence availability = mRoster.getPresence(userId);
			ChatLog.i("====================================");

			ChatLog.i("check presence:");
			ChatLog.i("User:" + userId);
			ChatLog.i("Presence:" + availability);
			ChatLog.i("State:" + availability.toString());

			if (xmppCallBacks != null)
				xmppCallBacks.onGetUserPresence(userId, retrieveState_mode(availability.getMode(), availability.isAvailable()));
		}
	}

	private String retrieveState_mode(Mode userMode, boolean isOnline) {
		if (userMode == Mode.chat) {
			return ChatConstants.USER_PRESENCE_ONLINE;
		}

		// if (userMode == Mode.dnd) {
		// return ChatConstants.USER_PRESENCE_BUSY;
		// } else if (userMode == Mode.away || userMode == Mode.xa) {
		// return ChatConstants.USER_PRESENCE_AWAY;
		// } else if (isOnline) {
		// return ChatConstants.USER_PRESENCE_ONLINE;
		// }
		return ChatConstants.USER_PRESENCE_OFFLINE;
	}

	private RosterListener mRosterListener = new RosterListener() {

		@Override
		public void presenceChanged(Presence presence) {
			// TODO Auto-generated method stub
			if (mRoster != null) {
				String user = presence.getFrom();
				Presence bestPresence = mRoster.getPresence(user);
				ChatLog.i("====================================");
				ChatLog.i("Presence change:");
				ChatLog.i("User:" + user);
				ChatLog.i("Presence:" + bestPresence);
				ChatLog.i("State:" + presence.toString());
				if (xmppCallBacks != null)
					xmppCallBacks.onUserPresenceChange(user.split("/")[0], retrieveState_mode(presence.getMode(), presence.isAvailable()));
			}
		}

		@Override
		public void entriesUpdated(Collection<String> arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void entriesDeleted(Collection<String> arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void entriesAdded(Collection<String> arg0) {
			// TODO Auto-generated method stub

		}
	};

	// private MessageListener messageListener = new MessageListener() {
	//
	// @Override
	// public void processMessage(Chat chat, Message message) {
	// if (xmppCallBacks != null) {
	// xmppCallBacks.onMessageRecieved(parceRecievedPacket(message));
	// }
	// }
	//
	// };

}