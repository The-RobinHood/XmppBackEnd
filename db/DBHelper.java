package com.wannashare.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.google.android.gms.internal.id;
import com.wannashare.constants.ChatConstants;
import com.wannashare.xmpp.ChatLog;
import com.wannashare.xmpp.ChatMessage;
import com.wannashare.xmpp.ChatUser;

import org.json.JSONObject;

import java.util.ArrayList;

;

public class DBHelper {

	private static DBHelper dbHelper;
	private SQLiteDatabase db;

	private DBHelper(Context context) {
		db = new SQLHelper(context).getWritableDatabase();
	}

	public static DBHelper getInstance(Context context) {
		if (dbHelper == null)
			dbHelper = new DBHelper(context);

		return dbHelper;
	}

	// ============MESSAGES============
	// Business Logic

	public boolean containsRecord(String iOrderId, String myId) {
		String sql = "SELECT * FROM messages WHERE iOrderID='" + iOrderId + "' COLLATE NOCASE " +

		" AND " + " ( myJid like '" + myId + "%' COLLATE NOCASE OR mto like '" + myId + "%' COLLATE NOCASE )" +
		// " OR "+
		// " myJid='"+toId+"' COLLATE NOCASE OR mto='"+toId+"' COLLATE NOCASE  ) "+
				" ORDER BY " + DBMessages._ID;

		Cursor cur = db.rawQuery(sql, null);

		return cur != null && cur.moveToFirst();
	}

	public ArrayList<ChatMessage> getGroupedChatHistory(String iOrderId, String myId) {

		String sql = "SELECT * FROM messages WHERE iOrderID='" + iOrderId + "' COLLATE NOCASE " + " AND " + " (myJid like '" + myId + "%' COLLATE NOCASE OR mto like '" + myId + "%' COLLATE NOCASE ) " +
		// " AND "+DBMessages.TO+" not like '"+myId+"%' COLLATE NOCASE "+
				" GROUP BY " + DBMessages.SENDER_ID + " ORDER BY " + DBMessages._ID;

		Cursor cur = db.rawQuery(sql, null);

		ArrayList<ChatMessage> listChatMessage = new ArrayList<ChatMessage>();
		if (cur != null && cur.moveToFirst()) {

			do {
				ChatMessage msg = new ChatMessage();
				msg._id = cur.getString(cur.getColumnIndex(DBMessages._ID));
				msg.msg_date = cur.getString(cur.getColumnIndex(DBMessages.MSG_DATE));
				msg.msg_id = cur.getString(cur.getColumnIndex(DBMessages.MSG_ID));
				msg.sender_id = cur.getString(cur.getColumnIndex(DBMessages.SENDER_ID));
//				msg.receive_date = cur.getString(cur.getColumnIndex(DBMessages.RECEIVE_DATE));
				msg.status = cur.getString(cur.getColumnIndex(DBMessages.STATUS));
				msg.msg_text = cur.getString(cur.getColumnIndex(DBMessages.MSG_TEXT));
				msg.receiver_id = cur.getString(cur.getColumnIndex(DBMessages.RECEIVER_ID));
                msg.vname = cur.getString(cur.getColumnIndex(DBMessages.VNAME));
                msg.isdeliver = cur.getString(cur.getColumnIndex(DBMessages.ISDELIVER));
                msg.isread = cur.getString(cur.getColumnIndex(DBMessages.ISREAD));
                msg.group_id = cur.getString(cur.getColumnIndex(DBMessages.GROUPID));

//                msg.localPath = cur.getString(cur.getColumnIndex(DBMessages.PATH));
//                msg.iorderid = cur.getString(cur.getColumnIndex(DBMessages.IORDERID));
//				msg.isuploaded = cur.getString(cur.getColumnIndex(DBMessages.ISUPLOADED));
//				msg.media_duration = cur.getString(cur.getColumnIndex(DBMessages.MEDIA_DURATION));
//				msg.media_size = cur.getString(cur.getColumnIndex(DBMessages.MEDIA_SIZE));
//				msg.type = cur.getString(cur.getColumnIndex(DBMessages.TYPE));
//				msg.url = cur.getString(cur.getColumnIndex(DBMessages.URL));
//				msg.vtype = cur.getString(cur.getColumnIndex(DBMessages.VTYPE));



/*				JSONObject jsonObject = msg.getJSONObject(true);
				if (jsonObject != null)
					msg.raw_data = jsonObject.toString();*/

				listChatMessage.add(msg);
			} while (cur.moveToNext());
		}
		return listChatMessage;

	}

	public ArrayList<ChatMessage> getAllRecords(String senderId,String receiverId) {

		// String sql = "SELECT * FROM " + DBMessages.TABLE_NAME + " WHERE " + DBMessages.TO + "='" + toId + "' COLLATE NOCASE " + " AND " + DBMessages.IORDERID + "='" + iOrderId + "' COLLATE NOCASE " + " AND " + DBMessages.MYJID + "='" + myId + "' COLLATE NOCASE OR "+DBMessages.TO+"='"+myId+"' COLLATE NOCASE " + " ORDER BY " + DBMessages._ID;

		String sql = "SELECT * FROM messages WHERE sender_id='" +senderId+ "' AND receiver_id='" +receiverId+ "' OR sender_id='" +receiverId+ "' AND receiver_id='" +senderId+ "' ";

		Cursor cur = db.rawQuery(sql, null);

		ArrayList<ChatMessage> listChatMessage = new ArrayList<ChatMessage>();
		if (cur != null && cur.moveToFirst()) {
			do {
                ChatMessage msg = new ChatMessage();
                msg._id = cur.getString(cur.getColumnIndex(DBMessages._ID));
                msg.msg_date = cur.getString(cur.getColumnIndex(DBMessages.MSG_DATE));
                msg.msg_id = cur.getString(cur.getColumnIndex(DBMessages.MSG_ID));
                msg.sender_id = cur.getString(cur.getColumnIndex(DBMessages.SENDER_ID));
//				msg.receive_date = cur.getString(cur.getColumnIndex(DBMessages.RECEIVE_DATE));
                msg.status = cur.getString(cur.getColumnIndex(DBMessages.STATUS));
                msg.msg_text = cur.getString(cur.getColumnIndex(DBMessages.MSG_TEXT));
                msg.receiver_id = cur.getString(cur.getColumnIndex(DBMessages.RECEIVER_ID));
                msg.vname = cur.getString(cur.getColumnIndex(DBMessages.VNAME));
                msg.isdeliver = cur.getString(cur.getColumnIndex(DBMessages.ISDELIVER));
                msg.isread = cur.getString(cur.getColumnIndex(DBMessages.ISREAD));
                msg.group_id = cur.getString(cur.getColumnIndex(DBMessages.GROUPID));

				listChatMessage.add(msg);
			} while (cur.moveToNext());

		}
		return listChatMessage;
	}

	public void add(ChatMessage msg) {
		ContentValues values = new ContentValues();
		values.put(DBMessages.MSG_ID, msg.msg_id);
		values.put(DBMessages.MSG_DATE, msg.msg_date);
        values.put(DBMessages.SENDER_ID, msg.sender_id);
        values.put(DBMessages.MSG_TEXT, msg.msg_text);
        values.put(DBMessages.RECEIVER_ID, msg.receiver_id);
        values.put(DBMessages.VNAME, msg.vname);
        values.put(DBMessages.ISDELIVER, msg.isdeliver);
        values.put(DBMessages.ISREAD, msg.isread);
        values.put(DBMessages.GROUPID, msg.group_id);
        values.put(DBMessages.STATUS, msg.status);
//        values.put(DBMessages.RECEIVE_DATE, msg.receive_date);
        long result = db.insert(DBMessages.TABLE_NAME, null, values);
        ChatLog.e("INSERT RESULT " + result);

//		values.put(DBMessages.IORDERID, msg.iorderid);
//		values.put(DBMessages.ISUPLOADED, msg.isuploaded);
//		values.put(DBMessages.MEDIA_DURATION, msg.media_duration);
//		values.put(DBMessages.MEDIA_SIZE, msg.media_size);
//		values.put(DBMessages.PATH, msg.localPath);
//		values.put(DBMessages.LATITUDE, msg.latitude);
//		values.put(DBMessages.LONGITUDE, msg.longitude);
//		values.put(DBMessages.TYPE, msg.type);
//		values.put(DBMessages.URL, msg.url);
//		values.put(DBMessages.VTYPE, msg.vtype);

	}

	public void update(ChatMessage msg) {

		ContentValues values = new ContentValues();
		values.put(DBMessages.MSG_ID, msg.msg_id);
		values.put(DBMessages.MSG_DATE, msg.msg_date);
        values.put(DBMessages.SENDER_ID, msg.sender_id);
        values.put(DBMessages.MSG_TEXT, msg.msg_text);
        values.put(DBMessages.RECEIVER_ID, msg.receiver_id);
        values.put(DBMessages.VNAME, msg.vname);
        values.put(DBMessages.ISDELIVER, msg.isdeliver);
        values.put(DBMessages.ISREAD, msg.isread);
        values.put(DBMessages.GROUPID, msg.group_id);
        values.put(DBMessages.STATUS, msg.status);
//        values.put(DBMessages.RECEIVE_DATE, msg.receive_date);
//		values.put(DBMessages.IORDERID, msg.iorderid);
//		values.put(DBMessages.ISUPLOADED, msg.isuploaded);
//		values.put(DBMessages.MEDIA_DURATION, msg.media_duration);
//		values.put(DBMessages.MEDIA_SIZE, msg.media_size);
//		values.put(DBMessages.PATH, msg.localPath);
//		values.put(DBMessages.LATITUDE, msg.latitude);
//		values.put(DBMessages.LONGITUDE, msg.longitude);
//		values.put(DBMessages.TYPE, msg.type);
//		values.put(DBMessages.URL, msg.url);
//		values.put(DBMessages.VTYPE, msg.vtype);


		if (!TextUtils.isEmpty(msg._id)) {
			db.update(DBMessages.TABLE_NAME, values, DBMessages._ID + "='" + msg._id + "' COLLATE NOCASE", null);
		} else {
			db.update(DBMessages.TABLE_NAME, values, DBMessages.MSG_ID + "='" + msg.msg_id + "' COLLATE NOCASE", null);
		}
	}

	public void updateStatus(String id, String status) {
		ContentValues values = new ContentValues();
		values.put(DBMessages.STATUS, status);
		db.update(DBMessages.TABLE_NAME, values, DBMessages.MSG_ID + "='" + id + "' COLLATE NOCASE", null);
	}

	public ArrayList<ChatMessage> getAllPendingMessage() {

		String sql = "SELECT * FROM " + DBMessages.TABLE_NAME + " WHERE " +
		// DBMessages.TO + "='" + toId + "' COLLATE NOCASE " +
		// " AND " + DBMessages.IORDERID + "='" + iOrderId + "' COLLATE NOCASE " +
		// " AND " + DBMessages.MYJID + "='" + myId + "' COLLATE NOCASE "+
		// " AND "+
				DBMessages.STATUS + " = '" + ChatConstants.STATUS_TYPE_PENDING + "' COLLATE NOCASE " + " OR " + DBMessages.STATUS + " = '" + ChatConstants.STATUS_TYPE_PROCESS + "' COLLATE NOCASE " + " ORDER BY " + DBMessages._ID;

		Cursor cur = db.rawQuery(sql, null);

		ArrayList<ChatMessage> listChatMessage = new ArrayList<ChatMessage>();
		if (cur != null && cur.moveToFirst()) {

			do {

                ChatMessage msg = new ChatMessage();
                msg._id = cur.getString(cur.getColumnIndex(DBMessages._ID));
                msg.msg_date = cur.getString(cur.getColumnIndex(DBMessages.MSG_DATE));
                msg.msg_id = cur.getString(cur.getColumnIndex(DBMessages.MSG_ID));
                msg.sender_id = cur.getString(cur.getColumnIndex(DBMessages.SENDER_ID));
//                msg.receive_date = cur.getString(cur.getColumnIndex(DBMessages.RECEIVE_DATE));
                msg.status = cur.getString(cur.getColumnIndex(DBMessages.STATUS));
                msg.msg_text = cur.getString(cur.getColumnIndex(DBMessages.MSG_TEXT));
                msg.receiver_id = cur.getString(cur.getColumnIndex(DBMessages.RECEIVER_ID));
                msg.vname = cur.getString(cur.getColumnIndex(DBMessages.VNAME));
                msg.isdeliver = cur.getString(cur.getColumnIndex(DBMessages.ISDELIVER));
                msg.isread = cur.getString(cur.getColumnIndex(DBMessages.ISREAD));
                msg.group_id = cur.getString(cur.getColumnIndex(DBMessages.GROUPID));

/*				JSONObject jsonObject = msg.getJSONObject(true);
				if (jsonObject != null)
					msg.raw_data = jsonObject.toString();*/

				listChatMessage.add(msg);
			} while (cur.moveToNext());

		}
		return listChatMessage;
	}

	// ============FRIENDS============

	public ChatUser getUser(String user_id) {
		String sql = "SELECT * FROM " + DBFriends.TABLE_NAME + " WHERE " + DBFriends.FRIEND_JID + "='" + user_id + "' COLLATE NOCASE ";
		Cursor cur = db.rawQuery(sql, null);
		if (cur != null && cur.moveToFirst()) {
			ChatUser chatUser = new ChatUser();
			chatUser.setUserId(cur.getString(cur.getColumnIndex(DBFriends.FRIEND_JID)));
			chatUser.setUserName(cur.getString(cur.getColumnIndex(DBFriends.VNAME)));
			chatUser.setProfilePic(cur.getString(cur.getColumnIndex(DBFriends.PROFILE_PIC_BASE64)));
			return chatUser;
		}
		return null;
	}

	public void updateOrAddUser(ChatUser chatUser) {
		ContentValues values = new ContentValues();
		values.put(DBFriends.FRIEND_JID, chatUser.getUserId());
		values.put(DBFriends.VNAME, chatUser.getUserName());
		values.put(DBFriends.PROFILE_PIC_BASE64, chatUser.getProfilePic());
		// values.put(DBFriends.MYJID, msg.iorderid);

		int affected_rows = db.update(DBFriends.TABLE_NAME, values, DBFriends.FRIEND_JID + "='" + chatUser.getUserId() + "' COLLATE NOCASE", null);
		if (affected_rows <= 0) { // means insert
			db.insert(DBFriends.TABLE_NAME, null, values);
		}
	}

	public void removeAll() {
		// TODO Auto-generated method stub
		String sql = "DELETE FROM " + DBMessages.TABLE_NAME + " WHERE 1=1";
		db.execSQL(sql);
	}


    public void updateISReadStatus(String id, String status) {
        ContentValues values = new ContentValues();
        values.put(DBMessages.ISREAD, status);
        db.update(DBMessages.TABLE_NAME, values,  "sender_id='" + id + "' COLLATE NOCASE", null);
    }


    public String getUnReadCount(String user_id,String status) {

//        select count(*) from messages where sender_id='132' AND isRead='false'

        String sql = "SELECT count(*) FROM " + DBMessages.TABLE_NAME + " WHERE sender_id='" + user_id + "' AND isRead='" + status + "' COLLATE NOCASE ";
        String count;
        Cursor cur = db.rawQuery(sql, null);
        if (cur != null && cur.moveToFirst()) {
            count=cur.getString(0);
            return count;
        }
        return "";
    }


    public boolean IsAlreadyExist(String user_id,String sender_id) {
        String sql = "SELECT * FROM " + DBMessages.TABLE_NAME + " WHERE sender_id='" + user_id + "' AND receiver_id='" + sender_id + "' OR sender_id='" +sender_id+ "' AND receiver_id='" +user_id+ "' COLLATE NOCASE ";

        Cursor cur = db.rawQuery(sql, null);
        if (cur != null && cur.moveToFirst()) {
            if(cur.getCount()>0){
                return true;
            }

        }
        return false;
    }
}
