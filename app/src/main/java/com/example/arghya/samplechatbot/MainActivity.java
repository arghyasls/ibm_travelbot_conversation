package com.example.arghya.samplechatbot;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.kittinunf.fuel.Fuel;
import com.github.kittinunf.fuel.core.FuelError;
import com.github.kittinunf.fuel.core.Handler;
import com.github.kittinunf.fuel.core.Request;
import com.github.kittinunf.fuel.core.Response;
import com.ibm.watson.developer_cloud.conversation.v1.ConversationService;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageRequest;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;
import com.ibm.watson.developer_cloud.http.ServiceCallback;
import android.widget.ListView;
import android.support.design.widget.FloatingActionButton;
import android.widget.ImageView;
import org.json.JSONArray;
import org.json.JSONObject;
import com.example.arghya.samplechatbot.ChatMessageAdapter;
import com.example.arghya.samplechatbot.ChatMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.view.View;
public class MainActivity extends AppCompatActivity {
    private ListView mListView;
    private FloatingActionButton mButtonSend;
    private EditText mEditTextMessage;
    private ImageView mImageView;
    int pos=0;
    private ChatMessageAdapter mAdapter;
    private static final String TAG = "TOURISM LTD";
    List<Map<String, Object>> lists = new ArrayList<Map<String, Object>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.activity_main);
        getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,R.drawable.icon);

        final ScrollView mScrollView = (ScrollView) findViewById(R.id.scrollview);
        mScrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                //replace this line to scroll up or down
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        }, 1L);
        final ConversationService myConversationService =
                new ConversationService(
                        "2017-05-26",
                        getString(R.string.username),
                        getString(R.string.password)
                );
        mListView = (ListView) findViewById(R.id.listView);
        mButtonSend = (FloatingActionButton) findViewById(R.id.btn_send);
        mEditTextMessage = (EditText) findViewById(R.id.et_message);
        mImageView = (ImageView) findViewById(R.id.iv_image);
        mAdapter = new ChatMessageAdapter(this, new ArrayList<ChatMessage>());
        mListView.setAdapter(mAdapter);
        MessageRequest blank = new MessageRequest.Builder()
                .inputText("")
                .build();
        lists.add(blank.context());
        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ScrollView mScrollView = (ScrollView) findViewById(R.id.scrollview);
                mScrollView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //replace this line to scroll up or down
                        mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                }, 1L);
                String message = mEditTextMessage.getText().toString();
                pos++;
                MessageRequest request = new MessageRequest.Builder()
                        .inputText(message).context(lists.get(pos-1))
                        .build();
                lists.add(request.context());
                sendMessage(message);

                myConversationService
                        .message(getString(R.string.workspace), request)
                        .enqueue(new ServiceCallback<MessageResponse>() {
                            @Override
                            public void onResponse(MessageResponse response) {
                                lists.add(response.getContext());
                                final String outputText = response.getText().get(0);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mimicOtherMessage(outputText);

                                    }
                                });


                                final ScrollView mScrollView = (ScrollView) findViewById(R.id.scrollview);
                                mScrollView.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        //replace this line to scroll up or down
                                        mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                                    }
                                }, 1L);


            }
                            @Override
                            public void onFailure(Exception e) {
                                Log.d(TAG, e.getMessage());
                            }
        });


                mEditTextMessage.setText("");


            }

        });
        mEditTextMessage.setText("");

        mListView.setSelection(mAdapter.getCount());
    }
    private void sendMessage(String message) {
        ChatMessage chatMessage = new ChatMessage(message, true, false);
        mAdapter.add(chatMessage);
        String  msg=mAdapter.getCount()+"";
        Context context = getApplicationContext();
        CharSequence text =msg;
       /* int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();*/
        //mimicOtherMessage(message);
        final ScrollView mScrollView = (ScrollView) findViewById(R.id.scrollview);
        mScrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                //replace this line to scroll up or down
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        }, 1L);

    }

    private void mimicOtherMessage(String message) {
        ChatMessage chatMessage = new ChatMessage(message, false, false);
        mAdapter.add(chatMessage);
        String  msg=mAdapter.getCount()+"";
        Context context = getApplicationContext();
        CharSequence text =msg;
       /* int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();*/
        final ScrollView mScrollView = (ScrollView) findViewById(R.id.scrollview);
        mScrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                //replace this line to scroll up or down
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        }, 1L);

    }

    private void sendMessage() {
        ChatMessage chatMessage = new ChatMessage(null, true, true);
        mAdapter.add(chatMessage);

        mimicOtherMessage();
    }

    private void mimicOtherMessage() {
        ChatMessage chatMessage = new ChatMessage(null, false, true);
        mAdapter.add(chatMessage);
    }



}