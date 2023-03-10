package com.example.videomeetingapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.videomeetingapp.R;
import com.example.videomeetingapp.network.ApiClient;
import com.example.videomeetingapp.network.ApiService;
import com.example.videomeetingapp.utilities.Constants;
import com.example.videomeetingapp.utilities.PreferenceManager;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IncomingInvitationActivity extends AppCompatActivity {

    private ImageView imageMeetingType, imageRejectInvitation, imageAcceptInvitation;
    private TextView textFirstChar, textName, textEmail;
    private PreferenceManager preferenceManager;
    private String meetingType = null;
    private String meetingRoom = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_invitation);

        init();

        meetingType = getIntent().getStringExtra(Constants.REMOTE_MSG_MEETING_TYPE);
        if (meetingType != null){
            if (meetingType.equals("video")){
                imageMeetingType.setImageResource(R.drawable.ic_videocam);
            }else if (meetingType.equals("audio")){
                imageMeetingType.setImageResource(R.drawable.ic_call);
            }
        }

        String firstName = getIntent().getStringExtra(Constants.KEY_FIRST_NAME);
        String lastName = getIntent().getStringExtra(Constants.KEY_LAST_NAME);
        String email = getIntent().getStringExtra(Constants.KEY_EMAIL);

        if (firstName != null){
            textFirstChar.setText(firstName.substring(0,1));
        }

        textName.setText(firstName + " " + lastName);
        textEmail.setText(email);

//        Toast.makeText(IncomingInvitationActivity.this,"meetingRoom : " + getIntent()
//                .getStringExtra(Constants.REMOTE_MSG_MEETING_ROOM) , Toast.LENGTH_SHORT).show();

        // Click image ch???p nh???n k???t n???i
        imageAcceptInvitation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(IncomingInvitationActivity.this, "Token inviter : " +
//                        getIntent().getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN), Toast.LENGTH_SHORT).show();

                sendInvitationResponse(Constants.REMOTE_MSG_INVITATION_ACCEPTED,
                        getIntent().getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN));
            }
        });

        // click ko k???t n???i
        imageRejectInvitation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendInvitationResponse(Constants.REMOTE_MSG_INVITATION_REJECTED,
                        getIntent().getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN));
            }
        });

    }

    private void init() {
        imageMeetingType        = findViewById(R.id.imageMeetingType);
        textFirstChar           = findViewById(R.id.textFirstChar);
        textName                = findViewById(R.id.textUserName);
        textEmail               = findViewById(R.id.textEmail);
        imageAcceptInvitation   = findViewById(R.id.imageAcceptInvitation);
        imageRejectInvitation   = findViewById(R.id.imageRejectInvitation);

    }

    // g???i tr??? l???i ph???n h???i
    private void sendInvitationResponse(String type, String receiverToken){
        // receiverToken l?? token c???a ng?????i g???i y??u c???u k???t n???i t???i
        // token n??y ???????c nh???n qua intent t??? service c??n service nh???n token n??y th??ng qua g??i tin
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE); // th??ng b??o cho service bi???t ????y l?? ng nh???n tr??? l???i y??u c???u k???t n???i
            // type n??y m???c ????ch th??ng b??o l?? ng nh???n ???? nh???n/h???y ????? response l???i ng g???i, s???n s??ng k???t n???i

            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE, type); // ????y l?? type c???a response  ????nh ?????u l?? accept hay reject

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), type);

        } catch (JSONException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }


    private void sendRemoteMessage(String remoteMessageBody, String type){
        ApiClient.getClient().create(ApiService.class) // getClient() t??? ApiClient class
                .sendRemoteMessage(Constants.getRemoteMessageHeaders(), remoteMessageBody) // method n??y ??? interface ApiService, remoteMeBody l?? tham s??? c???a fun
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful()){
                            if (type.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED)){
                                Toast.makeText(IncomingInvitationActivity.this, "Accept invitation!", Toast.LENGTH_SHORT).show();
                                try {
                                    URL serverURL = new URL("https://meet.jit.si");

                                    // chung c??? audio v?? video
                                    JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                                    builder.setServerURL(serverURL);
                                    builder.setWelcomePageEnabled(false);
                                    builder.setRoom(getIntent().getStringExtra(Constants.REMOTE_MSG_MEETING_ROOM));


                                    // c???a audio
                                    if (meetingType.equals("audio")){
                                        builder.setVideoMuted(true);
                                    }

                                    JitsiMeetActivity.launch(IncomingInvitationActivity.this, builder.build());
                                    finish(); // b???t bu???c ph???i c?? finish ??? ????y, n???u ko l???i sml nh?? :D


                                } catch (MalformedURLException e) {
                                    Toast.makeText(IncomingInvitationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }


                            }else if (type.equals(Constants.REMOTE_MSG_INVITATION_REJECTED)){
                                Toast.makeText(IncomingInvitationActivity.this, "Reject invitation!", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }else{
                            Toast.makeText(IncomingInvitationActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                            finish();
                        }

                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        Toast.makeText(IncomingInvitationActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });

    }

    private BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE);
            if (type != null){
                if (type.equals(Constants.REMOTE_MSG_INVITATION_CANCELLED)){
                    Toast.makeText(context, "Inviter cancel invitation!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    };

    // ????ng k?? broadcast
    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                invitationResponseReceiver,
                new IntentFilter(Constants.REMOTE_MSG_INVITATION_RESPONSE)
        );
    }

    // h???y ????ng k??
    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext())
                .unregisterReceiver(invitationResponseReceiver);
    }
}