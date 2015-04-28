package com.wannashare.db;


public class DBFriends {

	public static final String TABLE_NAME = "friends";

	public static final String _ID = "_id";
	public static final String MYJID = "myJid"; // myid
	public static final String FRIEND_JID = "friend_jid"; // my name
	public static final String VNAME = "vName"; // chat
	public static final String PROFILE_PIC_PATH = "profile_pic_path";
	public static final String PROFILE_PIC_BASE64= "profile_pic_base64";
	
	protected static final String CREATE_TABLE="CREATE TABLE "+TABLE_NAME+ " ( "+
			
			_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
			MYJID+" text, "+
			FRIEND_JID+" text, "+
			VNAME+" text, "+
			PROFILE_PIC_PATH+" text, "+
			PROFILE_PIC_BASE64+" text "+
			
			" ) ";
}
