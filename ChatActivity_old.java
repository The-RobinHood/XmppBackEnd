package com.wannashare.main;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences.Editor;

import com.google.android.gms.games.internal.constants.NotificationChannel;
import com.wannashare.R;
import com.wannashare.adapter.AdapterFriend;
import com.wannashare.adapter.ChatAdapter;
import com.wannashare.constants.ChatConstants;
import com.wannashare.constants.PostAsynTask;
import com.wannashare.constants.WannaShareConstants;
import com.wannashare.customview.PinnedSectionListView;
import com.wannashare.db.DBHelper;
import com.wannashare.models.FriendModel;
import com.wannashare.xmpp.ChatMessage;
import com.wannashare.xmpp.ChatUtils;
import com.wannashare.xmpp.XMPPService;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity_old extends ActionBarActivity implements OnClickListener, OnItemClickListener {

    public static boolean ISVISIBLE;
    private String URL_CHAT_HISTORY=WannaShareConstants.BASE_URL+"chat/chatHistory";
    TextView tvSend;
    TextView tvError;
    EditText etchat;
    public static String service = "@54.153.127.219";
    private Handler mHandler = new Handler();
    private ArrayList<ChatMessage> messages;
    private ChatAdapter adapter;
    String _StrMsg = "",
            _StrPath = "";
    private String _StrRecipient;
    private String _StrRecipientId;
    private String _StrRecipientPic;
    private String _StrRecipientName;
    private String _StrUserName;
    private String  _StrUserPic;


    String _StrApiKey, _StrUserID, _StrAccessToken;
    private SharedPreferences pref;

    private PinnedSectionListView stickyList;
    private Toolbar toolbar;

    //chat
    private ChatUtils chatUtils;
    private DBHelper dbHelper;
    private boolean IS_NOTIFICATION;


    BroadcastReceiver xmppReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            ChatMessage chat;
            switch (ChatUtils.getAction(intent)) {
                case XMPPService.ACTION_FLAG_SEND_MESSAGE_SUCCESS:
                    Log.e("##########","Message ACTION_FLAG_SEND_MESSAGE_SUCCESS");
                    chat = ChatUtils.getMessage(intent);

                    // checking if activity open for same session on which message sent
                    if (chat.sender_id.equalsIgnoreCase(_StrUserID)) {
                    if (chat != null) {
                        chat.status = ChatConstants.STATUS_TYPE_SENT;
                        adapter.updateItem(chat);
                    }
                    }
                    break;
                case XMPPService.ACTION_FLAG_SEND_MESSAGE_DELIVERED:
                    String id = ChatUtils.getMessageId(intent);
                    Log.e("##########","Message ACTION_FLAG_SEND_MESSAGE_DELIVERED");
                    Log.e("##########","Message delievery report id="+id);

                    // checking if activity open for same session on which message sent
                    if (id != null) {
                        adapter.updateItem(id, ChatConstants.STATUS_TYPE_DELIVER);
                    }

                    break;
                case XMPPService.ACTION_FLAG_SEND_MESSAGE_FAILED:
                    Log.e("##########","Message ACTION_FLAG_SEND_MESSAGE_FAILED");
                    chat = ChatUtils.getMessage(intent);
                    // checking if activity open for same session on which message sent
                    if (chat.sender_id.equalsIgnoreCase(_StrUserID)) {
                        if (chat != null) {
                            chat.status = ChatConstants.STATUS_TYPE_PENDING;
                            adapter.updateItem(chat);
                            dbHelper.update(chat);

                        }
                    }
                    Toast.makeText(ChatActivity_old.this, "Message send failed", Toast.LENGTH_SHORT).show();
                    break;
                case XMPPService.ACTION_FLAG_MESSAGE_RECEIVED:
                    Log.e("##########","Message ACTION_FLAG_MESSAGE_RECEIVED");
                    Log.e("#############","Message Received=");
                    chat = ChatUtils.getMessage(intent);
                    // checking if activity open for same session on which message received
                    if (chat.sender_id.equalsIgnoreCase(_StrRecipientId)) {
                        if (chat != null) {
                            chat.isread = ChatConstants.STATUS_TYPE_READ;
                            adapter.addItems(chat);
                            dbHelper.update(chat);
                        }
                    } else {
                        // send notification
//                        ChatUtils.sendNotification(getApplicationContext(), chat);
                        ChatUtils.generateMessageNotification(getApplicationContext(),chat.msg_text,chat.sender_id,chat.vname,chat.vname);
                    }
                    break;
                case XMPPService.ACTION_FLAG_LOGIN_SUCCESS:
                    Log.e("##########","Message ACTION_FLAG_LOGIN_SUCCESS");
                    break;
                case XMPPService.ACTION_FLAG_GET_USER_PRESENCE:
                    Log.e("##########","Message ACTION_FLAG_GET_USER_PRESENCE");
                    if (ChatUtils.getUserId(intent).equalsIgnoreCase(_StrRecipientId))
                        getSupportActionBar().setSubtitle(ChatUtils.getStatus(intent));
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_singlechat);

        Initialization();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Editor edit = pref.edit();
                edit.putBoolean(WannaShareConstants.PREF_ISCHAT ,true );
                edit.commit();
                finish();
            }
        });


    }

    private void Initialization() {
        chatUtils = new ChatUtils(this);
        dbHelper = DBHelper.getInstance(this);
        pref = getSharedPreferences(WannaShareConstants.PREFERENCE_NAME,
                Context.MODE_PRIVATE);
        _StrApiKey = pref.getString(WannaShareConstants.PREF_APIKEY, "");
        _StrUserID = pref.getString(WannaShareConstants.PREF_USERID, "");
        _StrAccessToken = pref.getString(WannaShareConstants.PREF_ACCESSTOKEN, "");
        _StrUserName = pref.getString(WannaShareConstants.PREF_USERNAME, "");
        _StrUserPic = pref.getString(WannaShareConstants.PREF_USERPROFILE, "");


        Bundle extras= getIntent().getExtras();
        if(extras!=null){
            _StrRecipientId=extras.getString(WannaShareConstants.EXTRA_PROFILE_ID,"");
            _StrRecipientPic=extras.getString(WannaShareConstants.EXTRA_FRIEND_PROFILE_PIC,"");
            _StrRecipientName=extras.getString(WannaShareConstants.EXTRA_FRIEND_NAME,"");
            IS_NOTIFICATION=extras.getBoolean(WannaShareConstants.EXTRA_IS_NOTIFICATION,false);
            _StrRecipient = _StrRecipientId+service;
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(_StrRecipientName);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        messages = new ArrayList<ChatMessage>();
        etchat = (EditText) findViewById(R.id.edit_singlechat);
        tvSend = (TextView) findViewById(R.id.textview_singlechat_send);
        tvError= (TextView) findViewById(R.id.empty);
        tvSend.setOnClickListener(this);

        // hide virtual keyboard
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        stickyList = (PinnedSectionListView) findViewById(R.id.list);
        stickyList.setOnItemClickListener(this);
        stickyList.initShadow(false);


        if(IS_NOTIFICATION){
            Log.e("#######","Fetch from server");
            //FETCH FROM SERVER
            List<NameValuePair> params= new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("iUserID", _StrRecipientId));
            FetchChatHistoryTask(params);

        }else{
            //CHECK IF CHAT HISTORY ALREADY EXIST
            boolean IsExist=DBHelper.getInstance(getApplicationContext()).IsAlreadyExist(_StrUserID,_StrRecipientId);

            if(IsExist){
                //FETCH FROM DATABASE
                new FetchFromDatabaseTask().execute("");

            }else{
                Log.e("#######","Fetch from server");
                //FETCH FROM SERVER
                List<NameValuePair> params= new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("iUserID", _StrRecipientId));
                FetchChatHistoryTask(params);
            }
        }



        //UPDATE UNREAD MESSAGE TO READ
        DBHelper.getInstance(getApplicationContext()).updateISReadStatus(_StrRecipientId,"true");




    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.textview_singlechat_send:
                String newMessage = etchat.getText().toString().trim();
                _StrPath = "";
                if (TextUtils.isEmpty(newMessage)) {
                    Toast.makeText(ChatActivity_old.this, "Please type message.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    // IS READ WILL BE NULL WHILE SENDING MESSAGE.
                    ChatMessage chatMessage = ChatUtils.getLocationMessageWithOrderId(_StrUserID, _StrUserName, _StrRecipientId,newMessage,"false","","");
                    adapter.addItems(chatMessage);
                    chatUtils.sendMessage(chatMessage, _StrRecipientId);
                    dbHelper.add(chatMessage);
                    etchat.setText("");
                    tvError.setVisibility(View.GONE);
                    stickyList.setVisibility(View.VISIBLE);

                    if(ISVISIBLE){
                        //WHEN CHAT ACTIVITY IS OPENED.
                        DBHelper.getInstance(getApplicationContext()).updateISReadStatus(chatMessage.msg_id,"true");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;

            default:
                break;
        }

    }


    // FETCH COMMENT HISTORY AND DISPLAY
    public class FetchFromDatabaseTask extends AsyncTask<String, String, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(ChatActivity_old.this, null,
                    "Please wait...", true);
            progressDialog.setCancelable(true);
        }

        @Override
        protected String doInBackground(String... arg0) {
            try {
                ArrayList<ChatMessage> temp= dbHelper.getAllRecords(_StrUserID,_StrRecipientId);
                messages = makeChatDateAsSection(temp);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        }

        protected void onProgressUpdate(String... str) {

        }

        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            try {
                if(messages.size()>0){
//                    stickyList.setVisibility(View.VISIBLE);
                    tvError.setVisibility(View.GONE);

                }else{
                    stickyList.setVisibility(View.GONE);
                    tvError.setVisibility(View.VISIBLE);
                }

                adapter = new ChatAdapter(ChatActivity_old.this, messages,_StrUserID,_StrUserPic,_StrRecipientPic);
                stickyList.setAdapter(adapter);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    private void FetchChatHistoryTask(List<NameValuePair> params){
        PostAsynTask Task = new PostAsynTask(ChatActivity_old.this,
                URL_CHAT_HISTORY, params, _StrApiKey, _StrUserID, _StrAccessToken, true) {
            private ProgressDialog _progressDialog;

            protected void onPreExecute() {
                _progressDialog = ProgressDialog.show(
                        ChatActivity_old.this, null,
                        "Please wait...", true);
                _progressDialog.setCancelable(true);
            };

            @Override
            protected void onPostExecute(String result) {
                String state=WannaShareConstants.IsSuccess(result,WannaShareConstants.SERVERSTATUS);
                if(state.equalsIgnoreCase("1")){
                    DataParser(result);
                }
                ArrayList<ChatMessage> temp= dbHelper.getAllRecords(_StrUserID,_StrRecipientId);
                messages = makeChatDateAsSection(temp);
                try {
                    if(messages.size()>0){
//                    stickyList.setVisibility(View.VISIBLE);
                        tvError.setVisibility(View.GONE);

                    }else{
                        stickyList.setVisibility(View.GONE);
                        tvError.setVisibility(View.VISIBLE);
                    }

                    adapter = new ChatAdapter(ChatActivity_old.this, messages,_StrUserID,_StrUserPic,_StrRecipientPic);
                    stickyList.setAdapter(adapter);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                _progressDialog.dismiss();
            }
        };
        Task.execute();
    }

    private void DataParser(String output){
        try {
            JSONObject obj = new JSONObject(output);
            JSONObject jobj= obj.getJSONObject(WannaShareConstants.DATA_JSONOBJECT);
            JSONArray arr= jobj.getJSONArray("chatHistory");
             for (int i=0;i<arr.length();i++){
                 JSONObject iobj=arr.getJSONObject(i);
                 String body=iobj.getString("body");
                if(IS_NOTIFICATION){
                    //CHECK IF CHAT HISTORY ALREADY EXIST.
                    boolean IsExist=DBHelper.getInstance(getApplicationContext()).IsAlreadyExist(_StrUserID,_StrRecipientId);
                    if(!IsExist){
                        ParseOrNot(body);
                    }
                    _StrRecipientPic=jobj.getString("friendUserPic");
                    _StrUserPic=jobj.getString("loggedInUserPic");

                }else{
                    ParseOrNot(body);
                }
             }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void ParseOrNot(String body){
        try {
            //FETCH DATA FROM BODY
            JSONObject bobj=new JSONObject(body);
            if(bobj.has(WannaShareConstants.MESSAGE_TAG)) {
                JSONObject mobj = bobj.getJSONObject(WannaShareConstants.MESSAGE_TAG);
                ChatMessage  chatMessage= new ChatMessage();
                chatMessage.msg_id=mobj.getString(WannaShareConstants.MESSAGE_ID);
                chatMessage.msg_text=mobj.getString(WannaShareConstants.MESSAGE_TEXT);
                chatMessage.msg_date=mobj.getString(WannaShareConstants.MESSAGE_DATE);
                chatMessage.vname=mobj.getString(WannaShareConstants.SENDER_NAME);
                chatMessage.sender_id=mobj.getString(WannaShareConstants.SENDER_ID);
                chatMessage.receiver_id=mobj.getString(WannaShareConstants.RECEIVER_ID);
                chatMessage.group_id=mobj.getString(WannaShareConstants.GROUP_ID);
                chatMessage.status = ChatConstants.STATUS_TYPE_DELIVER;
                chatMessage.isdeliver="true";
                chatMessage.isread="true";
                dbHelper.add(chatMessage);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private ArrayList<ChatMessage> makeChatDateAsSection(ArrayList<ChatMessage> list) {
        ArrayList<ChatMessage> mList = new ArrayList<ChatMessage>();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                if (!list.get(i).msg_date.subSequence(0, 10).toString().equalsIgnoreCase(list.get(i - 1).msg_date.subSequence(0, 10).toString())) {
                    ChatMessage chatDate = new ChatMessage();
                    chatDate.msg_date = list.get(i).msg_date;
                    mList.add(chatDate);
                }
            } else {
                ChatMessage chatDate = new ChatMessage();
                chatDate.msg_date = list.get(i).msg_date;
                mList.add(chatDate);
            }
            mList.add(list.get(i));
        }
        return mList;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onResume() {
        super.onResume();
        ISVISIBLE=true;
        chatUtils.registerListener(xmppReceiver);
        chatUtils.sendSubscribePacket(_StrRecipientId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ISVISIBLE=false;
        chatUtils.unregisterListener(xmppReceiver);

    }

    @Override
    public void onBackPressed() {
        Editor edit = pref.edit();
        edit.putBoolean(WannaShareConstants.PREF_ISCHAT ,true );
        edit.commit();
        super.onBackPressed();
    }
}
