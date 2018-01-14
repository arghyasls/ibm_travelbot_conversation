package com.example.arghya.samplechatbot;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.example.arghya.samplechatbot.ChatMessageAdapter;
import com.example.arghya.samplechatbot.ChatMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import android.view.View;
public class MainActivity extends AppCompatActivity {

    int res;
    private ListView mListView;
    private FloatingActionButton mButtonSend;
    private EditText mEditTextMessage;
    private ImageView mImageView;
    int pos=0,o=0;
    private ChatMessageAdapter mAdapter;
    private static final String TAG = "TOURISM LTD";
    List<Map<String, Object>> lists = new ArrayList<Map<String, Object>>();
    List <String> intents=new ArrayList<>();
    String pintents[]={"darjeeling","digha","dooars","ghatshila","kalimpong","kurseong","mandarmani","mirik","raichak","siliguri","sunderbans"};
    private static final Pattern END_OF_SENTENCE = Pattern.compile("\\.\\s+");
    public static String textCleaner(String text)
    {
        boolean check = true;
        String ans="";
        // System.out.println(p.text().length());

        for (int x = 0; x <text.length(); x++) {
            if (text.charAt(x) == '[') {
                check = false;
                continue;
            }
            if (text.charAt(x) == ']') {
                check = true;
                continue;
            }
            if (check)
                ans += text.charAt(x);
        }
        return ans;
    }
    public static String getTransport(String text) {
        String ans="";
        String [] arr = text.split("\\.");
        String word[]={"aiport","railway","bus","road","Express","rail","trains","buses","airports","roads"};
        for (int i=0;i<word.length;i++)
        word[i]=word[i].toLowerCase();
        for (String sentence : arr) {
            for (String lcword:word)
            if (sentence.toLowerCase().contains(lcword)) {

                ans+=sentence+".";
                break;
            }
        }
        String link ="https://wikitravel.org";
        if(ans.length()<=1)
            return "I dont have the information. Please check online at "+link;
        else
        return summary(ans);
    }
    public static String getHotel(String text) {
        String ans="";
        String [] arr = text.split("\\.");
        String word[]={"resort","hotel","complex"};
        for (int i=0;i<word.length;i++)
            word[i]=word[i].toLowerCase();
        for (String sentence : arr) {
            for (String lcword:word)
                if (sentence.toLowerCase().contains(lcword)) {

                    ans+=sentence+".";
                    break;
                }
        }
        String link = "https://wikitravel.org";
        if(ans.length()<=1)
            return "I dont have the information. Please check Online at "+link;
        else
        return summaryl(ans);
    }
    public static String summaryl(String text) {
        String [] arr = text.split("\\.");
        //Splits words & assign to the arr[]  ex : arr[0] -> Copying ,arr[1] -> first


        int N=5,i=0,k; // NUMBER OF WORDS THAT YOU NEED
        String nWords="";
        if(arr.length<=N)
            N=arr.length;
        // concatenating number of words that you required
        for( i=0; i<N ; i++){
            nWords = nWords +  arr[i]+"." ;
        }

        return textCleaner(nWords);

    }
   public static String summary(String text) {
       String [] arr = text.split("\\.");
       //Splits words & assign to the arr[]  ex : arr[0] -> Copying ,arr[1] -> first


       int N=3,i=0,k; // NUMBER OF WORDS THAT YOU NEED
       String nWords="";
       if(arr.length<=N)
           N=arr.length;
       // concatenating number of words that you required
       for( i=0; i<N ; i++){
           nWords = nWords +  arr[i]+"." ;
       }

     return textCleaner(nWords);

    }
    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String url = urls[0];
            String ans="";

            try {
                Document doc = Jsoup.connect(url).get();
                Elements paragraphs = doc.select("p");

                Element firstParagraph = paragraphs.first();
                Element lastParagraph = paragraphs.last();
                Element p;
                int i=1;
                p=firstParagraph;

               ans+=p.text();
                    while (p != lastParagraph) {
                        p = paragraphs.get(i);
                        ans+=p.text();
                        i++;
                    }



            } catch (Exception e) {
                Log.i("error",e.getMessage());
                ans+="Problem with Internet connectivity";
            }
            if(ans.length()<=1)
                return "I dont have the information";
            else
            return ans;
        }
    }

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
        lists.clear();
        intents.clear();
        pos=0;
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
        intents.add("");
        o++;
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
               // lists.add(request.context());
                sendMessage(message);

                myConversationService
                        .message(getString(R.string.workspace), request)
                        .enqueue(new ServiceCallback<MessageResponse>() {
                            @Override
                            public void onResponse(MessageResponse response) {
                                lists.add(response.getContext());
                                String ver=response.getIntents().get(0).getIntent();
                                for (String chk:pintents)
                                {
                                    if(chk.equalsIgnoreCase(ver)) {
                                        intents.add(ver);
                                        o++;
                                    }
                                }

                                Log.i("intent",ver);
                               // Toast.makeText(getApplicationContext(),ver,Toast.LENGTH_SHORT).show();
                                if (response.getIntents().get(0).getIntent().endsWith("hotel_queries")) {
                                    final  DownloadTask task = new DownloadTask();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {

                                                mimicOtherMessage(getHotel(task.execute("https://wikitravel.org/en/"+intents.get(o-1)).get()));
                                            } catch (InterruptedException e) {

                                                e.printStackTrace();

                                            } catch (ExecutionException e) {

                                                e.printStackTrace();

                                            }
                                        }
                                    });


                                }
                                    if (response.getIntents().get(0).getIntent().endsWith("kalimpong")) {
                                      final  DownloadTask task = new DownloadTask();
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {

                                                    mimicOtherMessage(summary(task.execute("https://wikitravel.org/en/Kalimpong").get()));
                                                } catch (InterruptedException e) {

                                                    e.printStackTrace();

                                                } catch (ExecutionException e) {

                                                    e.printStackTrace();

                                                }
                                            }
                                        });


                                }
                                if (response.getIntents().get(0).getIntent().endsWith("darjeeling")) {
                                    final  DownloadTask task = new DownloadTask();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {

                                                mimicOtherMessage(summary((task.execute("https://wikitravel.org/en/Darjeeling").get())));
                                            } catch (InterruptedException e) {

                                                e.printStackTrace();

                                            } catch (ExecutionException e) {

                                                e.printStackTrace();

                                            }
                                        }
                                    });


                                }
                                if (response.getIntents().get(0).getIntent().endsWith("digha")) {
                                    final  DownloadTask task = new DownloadTask();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {

                                                mimicOtherMessage(summary(task.execute("https://wikitravel.org/en/Digha").get()));
                                            } catch (InterruptedException e) {

                                                e.printStackTrace();

                                            } catch (ExecutionException e) {

                                                e.printStackTrace();

                                            }
                                        }
                                    });


                                }
                                if (response.getIntents().get(0).getIntent().endsWith("ghatshila")) {
                                    final  DownloadTask task = new DownloadTask();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {

                                                mimicOtherMessage(summary(task.execute("https://wikitravel.org/en/Ghatshila").get()));
                                            } catch (InterruptedException e) {

                                                e.printStackTrace();

                                            } catch (ExecutionException e) {

                                                e.printStackTrace();

                                            }
                                        }
                                    });


                                }
                                if (response.getIntents().get(0).getIntent().endsWith("kurseong")) {
                                    final  DownloadTask task = new DownloadTask();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {

                                                mimicOtherMessage(summary(task.execute("https://wikitravel.org/en/Kurseong").get()));
                                            } catch (InterruptedException e) {

                                                e.printStackTrace();

                                            } catch (ExecutionException e) {

                                                e.printStackTrace();

                                            }
                                        }
                                    });


                                }
                                if (response.getIntents().get(0).getIntent().endsWith("dooars")) {
                                    final  DownloadTask task = new DownloadTask();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {

                                                mimicOtherMessage(summary(task.execute("https://wikitravel.org/en/Dooars").get()));
                                            } catch (InterruptedException e) {

                                                e.printStackTrace();

                                            } catch (ExecutionException e) {

                                                e.printStackTrace();

                                            }
                                        }
                                    });


                                }




                                if (response.getIntents().get(0).getIntent().endsWith("mandarmani")) {
                                    final  DownloadTask task = new DownloadTask();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {

                                                mimicOtherMessage(summary(task.execute("https://wikitravel.org/en/Mandarmani").get()));
                                            } catch (InterruptedException e) {

                                                e.printStackTrace();

                                            } catch (ExecutionException e) {

                                                e.printStackTrace();

                                            }
                                        }
                                    });


                                }
                                if (response.getIntents().get(0).getIntent().endsWith("transport")) {
                                    final DownloadTask task = new DownloadTask();

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {

                                                mimicOtherMessage(getTransport(task.execute("https://wikitravel.org/en/"+intents.get(o-1)).get()));
                                            } catch (InterruptedException e) {

                                                e.printStackTrace();

                                            } catch (ExecutionException e) {

                                                e.printStackTrace();

                                            }
                                        }
                                    });
                                }


                                    if (response.getIntents().get(0).getIntent().endsWith("mirik")) {
                                    final  DownloadTask task = new DownloadTask();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {

                                                mimicOtherMessage(summary(task.execute("https://wikitravel.org/en/Mirik").get()));
                                            } catch (InterruptedException e) {

                                                e.printStackTrace();

                                            } catch (ExecutionException e) {

                                                e.printStackTrace();

                                            }
                                        }
                                    });


                                }
                                if (response.getIntents().get(0).getIntent().endsWith("siliguri")) {
                                    final  DownloadTask task = new DownloadTask();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {

                                                mimicOtherMessage(summary(task.execute("https://wikitravel.org/en/Siliguri").get()));
                                            } catch (InterruptedException e) {

                                                e.printStackTrace();

                                            } catch (ExecutionException e) {

                                                e.printStackTrace();

                                            }
                                        }
                                    });


                                }
                                if (response.getIntents().get(0).getIntent().endsWith("raichak")) {
                                    final  DownloadTask task = new DownloadTask();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {

                                                mimicOtherMessage(summary(task.execute("https://wikitravel.org/en/Raichak").get()));
                                            } catch (InterruptedException e) {

                                                e.printStackTrace();

                                            } catch (ExecutionException e) {

                                                e.printStackTrace();

                                            }
                                        }
                                    });


                                }
                                if (response.getIntents().get(0).getIntent().endsWith("sunderbans")) {
                                    final  DownloadTask task = new DownloadTask();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {

                                                mimicOtherMessage(summary(task.execute("https://wikitravel.org/en/Sunderbans").get()));
                                            } catch (InterruptedException e) {

                                                e.printStackTrace();

                                            } catch (ExecutionException e) {

                                                e.printStackTrace();

                                            }
                                        }
                                    });


                                }






                                final String outputText = response.getText().get(0);
                                 if(!outputText.isEmpty()) {
                                     runOnUiThread(new Runnable() {
                                         @Override
                                         public void run() {
                                             mimicOtherMessage(outputText);

                                         }
                                     });
                                 }
                                else {
                                     runOnUiThread(new Runnable() {
                                         @Override
                                         public void run() {
                                             mimicOtherMessage("Sorry I cant understand you :(");

                                         }
                                     });
                                 }


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