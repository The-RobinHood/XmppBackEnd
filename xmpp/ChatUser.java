package com.wannashare.xmpp;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class ChatUser implements Parcelable {



	private String userId, userName, password, profile_pic;

	public ChatUser() {

	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public String getUserId() {
		return userId;
	}

	public String getPassword() {
		
		// return "wwwchatnaapp";
		return TextUtils.isEmpty(password)?"wwwchatnaapp":password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getProfilePic() {
		
		return profile_pic;
	}

	public void setProfilePic(String profilePic) {
		this.profile_pic = profilePic;
	}
	
	ChatUser(Parcel source) {
		// TODO Auto-generated constructor stub
		readFromParcel(source);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(userId);
		dest.writeString(userName);
		dest.writeString(password);
	}

	private void readFromParcel(Parcel parcel) {
		userId = parcel.readString();
		userName = parcel.readString();
		password = parcel.readString();

	}

	// Method to recreate a Question from a Parcel
	public static Creator<ChatUser> CREATOR = new Creator<ChatUser>() {

		@Override
		public ChatUser createFromParcel(Parcel source) {
			return new ChatUser(source);
		}

		@Override
		public ChatUser[] newArray(int size) {
			return new ChatUser[size];
		}

	};

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
}
