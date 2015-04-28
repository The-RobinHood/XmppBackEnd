package com.wannashare.xmpp;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.wannashare.constants.ChatConstants;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.json.JSONObject;

public class ChatMessage implements Parcelable {

	public String _id; //  DEFAULT ID WHICH WILL AUTO GENERATE.
	public String msg_id; // ID OF THE MESSAGE
	public String sender_id;// ID OF THE  PERSON WHO WILL SEND MESSAGE.
	public String vname; // NAME OF PERSON WHO SEND MESSAGE
	public String receiver_id; //ID OF THE PERSON WHO WILL RECEIVE THE MESSAGE.
	public String msg_date; // MESSAGE DATE
	public String msg_text; // MESSAGE TEXT
    public String isdeliver; // BOOLEAN TO TRACE WHETHER MESSAGE DELIVER OR NOT
    public String isread; // BOOLEAN TO CHECK WHETHER SENT MESSAGE IS READ OR NOT
    public String group_id; // ID OF THE GROUP
    public String status;

//  public String raw_data;
//  public String receive_date;
//	public String iorderid;
//  public String type;
//	public String vtype;
//	public String latitude;
//	public String longitude;
//	public String media_duration;
//	public String media_size;
//	public String localPath;
//	public String url;
//	public String isuploaded;


	public ChatMessage(JSONObject json) {
		if (json != null) {
			msg_id = json.optString(ChatConstants.PARAMS_ID);
            msg_date = json.optString(ChatConstants.PARAMS_DATE);
            sender_id = json.optString(ChatConstants.PARAMS_MYJID);
            msg_text = json.optString(ChatConstants.PARAMS_TEXT);
            receiver_id = json.optString(ChatConstants.PARAMS_TO);
            vname = json.optString(ChatConstants.PARAMS_VNAME);

//            receive_date = ChatUtils.getCurrentDateFormat();
            isdeliver= json.optString(ChatConstants.PARAMS_ISRECEIVE);
            isread= json.optString(ChatConstants.PARAMS_ISREAD);
            group_id= json.optString(ChatConstants.PARAMS_GROUPID);
            status = json.optString(ChatConstants.PARAMS_ID);
//            raw_data = json.toString();


			/*iorderid = json.optString(ChatConstants.PARAMS_IORDERID);
			isuploaded = json.optString(ChatConstants.PARAMS_ISUPLOADED);
			media_duration = json.optString(ChatConstants.PARAMS_MEDIADURATION);
			media_size = json.optString(ChatConstants.PARAMS_MEDIASIZE);
			latitude = json.optString(ChatConstants.PARAMS_LATITUDE);
			longitude = json.optString(ChatConstants.PARAMS_LONGITUDE);*/
//			localPath = json.optString(ChatConstants.PARAMS_LOCAL_PATH);
//			type = json.optString(ChatConstants.PARAMS_TYPE);
//			url = json.optString(ChatConstants.PARAMS_URL);
//			vtype = json.optString(ChatConstants.PARAMS_VTYPE);
		}
	}

/*
	public JSONObject getJSONObject(boolean isIncludeLocalPath) {

		JSONObject jsonObject = null;
		try {

			if (TextUtils.isEmpty(raw_data)) {
				jsonObject = new JSONObject();
                jsonObject.put(ChatConstants.PARAMS_TO, receiver_id);
                jsonObject.put(ChatConstants.PARAMS_MYJID, sender_id);
                jsonObject.put(ChatConstants.PARAMS_DATE, msg_date);
                jsonObject.put(ChatConstants.PARAMS_VNAME, vname);
                jsonObject.put(ChatConstants.PARAMS_TEXT, msg_text);
                jsonObject.put(ChatConstants.PARAMS_ISRECEIVE, isdeliver);
                jsonObject.put(ChatConstants.PARAMS_ISREAD, isread);
                jsonObject.put(ChatConstants.PARAMS_GROUPID, group_id);

//				jsonObject.put(ChatConstants.PARAMS_TYPE, type);
//				jsonObject.put(ChatConstants.PARAMS_VTYPE, vtype);

//				jsonObject.put(ChatConstants.PARAMS_URL, url);
//				if(isIncludeLocalPath)
					*/
                /*jsonObject.put(ChatConstants.PARAMS_LOCAL_PATH, localPath);
				jsonObject.put(ChatConstants.PARAMS_MEDIADURATION, media_duration);
				jsonObject.put(ChatConstants.PARAMS_MEDIASIZE, media_size);
				jsonObject.put(ChatConstants.PARAMS_LATITUDE, latitude);
				jsonObject.put(ChatConstants.PARAMS_LONGITUDE, longitude);
				jsonObject.put(ChatConstants.PARAMS_ISUPLOADED, isuploaded);
				jsonObject.put(ChatConstants.PARAMS_ID, msg_id);
				jsonObject.put(ChatConstants.PARAMS_IORDERID, iorderid);
				jsonObject.put(ChatConstants.PARAMS_IORDERID_UNIQUE, iorderid+"_mule");*//*



			} else {
				jsonObject = new JSONObject(raw_data);
			}

		} catch (Exception e) {

		}

		return jsonObject;
	}
*/

	public ChatMessage() {

	}

	ChatMessage(Parcel source) {
		// TODO Auto-generated constructor stub
		readFromParcel(source);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub

		dest.writeString(_id);
		dest.writeString(msg_id);
		dest.writeString(sender_id);
		dest.writeString(vname);
		dest.writeString(receiver_id);
		dest.writeString(msg_date);
		dest.writeString(msg_text);
        dest.writeString(status);
        dest.writeString(isdeliver);
        dest.writeString(isread);
        dest.writeString(group_id);

//       dest.writeString(raw_data);
//       dest.writeString(raw_data);
//		dest.writeString(iorderid);
//		dest.writeString(latitude);
//		dest.writeString(longitude);
//		dest.writeString(media_duration);
//		dest.writeString(media_size);
//		dest.writeString(localPath);
//		dest.writeString(url);
//		dest.writeString(isuploaded);
        //		dest.writeString(type);
//		dest.writeString(vtype);

	}

	private void readFromParcel(Parcel parcel) {
		_id = parcel.readString();
        msg_id = parcel.readString();
        sender_id = parcel.readString();
		vname = parcel.readString();
        receiver_id = parcel.readString();
        msg_date = parcel.readString();
        msg_text = parcel.readString();
        status = parcel.readString();
        isdeliver= parcel.readString();
        isread=parcel.readString();
        group_id= parcel.readString();



//        raw_data = parcel.readString();
//		iorderid = parcel.readString();
//		latitude = parcel.readString();
//		longitude = parcel.readString();
//		media_duration = parcel.readString();
//		media_size = parcel.readString();
//		localPath = parcel.readString();
//		url = parcel.readString();
//		isuploaded = parcel.readString();
        //		type = parcel.readString();
//		vtype = parcel.readString();


	}

	// Method to recreate a Question from a Parcel
	public static Creator<ChatMessage> CREATOR = new Creator<ChatMessage>() {

		@Override
		public ChatMessage createFromParcel(Parcel source) {
			return new ChatMessage(source);
		}

		@Override
		public ChatMessage[] newArray(int size) {
			return new ChatMessage[size];
		}

	};

	public synchronized Packet toPacket(final boolean enableDeliveryReciept, final boolean enableUserPresence) {
		/*return new Packet() {

			@Override
			public synchronized CharSequence toXML() {
				// TODO Auto-generated method stub

				XmlStringBuilder obj = new XmlStringBuilder();
				obj.append("<message ");
				obj.attribute("type", type);
				obj.attribute("vtype", vtype);
				obj.attribute("to", to);
				obj.attribute("from", sender_id);
				obj.attribute("myJid", sender_id);
				obj.attribute("id", id);
				obj.attribute("url", url!=null?url:"");
				obj.attribute("iOrderID", iorderid);
				obj.attribute("date", date);
				obj.attribute("vName", vname);
				obj.attribute("text", text);
				obj.append(">");
				obj.append("<body>");
				try {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("message", getJSONObject(false));
					obj.append(jsonObject.toString());
				} catch (Exception e) {
					obj.append(raw_data);
				}
				obj.append(" </body> ");

				if (enableDeliveryReciept) // for delivery receipt
					obj.append(" <request xmlns='urn:xmpp:receipts'/> ");

				if (enableUserPresence) // for user presence change
					obj.append(" <composing/> ");

				obj.append(" </message>");
				return obj.toString();

				// return "<message " + "type=\"" + type + "\" " +
				// "vtype=\"" + vtype + "\" " +
				// "to=\"" + to + "\" " +
				// "from=\"" + sender_id + "\" " +
				// "myJid=\"" + sender_id + "\" " +
				// "id=\"" + id + "\" " +
				// "iOrderID=\"" + iorderid + "\" " +
				// "date=\"" + date + "\" " +
				// "vName=\"" + vname + "\" " +
				// "text=\"" + text + "\" >" +
				// "<body>" + jsonObject.toString() + "</body> " +
				// " <request xmlns='urn:xmpp:receipts'/> "+
				// "</message>";
			}
		};*/
        return null;
	}

	public Message toMessage() {
//		Utility.showLog(ChatMessage.class, "inside toMessage");
	/*	return new Message() {

			@Override
			public synchronized XmlStringBuilder toXML() {
				// TODO Auto-generated method stub
//				Utility.showLog(ChatMessage.class, "toXML is starts");
				XmlStringBuilder obj = new XmlStringBuilder();
				obj.append("<message ");
				obj.attribute("type", type);
				obj.attribute("vtype", vtype);
				obj.attribute("to", to);
				obj.attribute("from", sender_id);
				obj.attribute("myJid", sender_id);
				obj.attribute("id", id);
				obj.attribute("iOrderID", iorderid);
				obj.attribute("date", date);
				obj.attribute("vName", vname);
				obj.attribute("text", text);
				obj.append(">");
				obj.append("<body>");
				try {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("message", getJSONObject(false));
					obj.append(jsonObject.toString());
				} catch (Exception e) {
					obj.append(raw_data);
				}
				obj.append("</body>");
				obj.append(" </message>");

				Utility.showLog(ChatMessage.class, "toXML is over");
				return obj;
			}
		};*/
        return  null;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

}
