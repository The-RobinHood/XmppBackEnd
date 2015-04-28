package com.wannashare.xmpp;

import java.util.ArrayList;

import org.jivesoftware.smack.XMPPConnection;

public interface XMPPCallbacks {
	public void onRegistrationComplete();

	public void onRegistrationFailed(Exception e);

	public void onLogin(ChatUser ChatnaUser);

	public void onLoginFailed(Exception e);

	public void onLogout();

	public void onMessageRecieved(ChatMessage messageData);

	public void onMessageSent(ChatMessage message);
	
	public void onMessageDeliver(String message);

	public void onMessageSendingFailed(ChatMessage message, Exception e);

	public void onConnected(XMPPConnection connection);

	public void onConnectionFailed(Exception e);
	
	public void onFriendListReceived(ArrayList<String> list);
	
	public void onGetUserPresence(String userId, String status);
	
	public void onUserPresenceChange(String userId, String status);
}
