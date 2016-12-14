package com.example.ecardccnu;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public static final int SHOW_RESPONSE = 0;

    List<Map<String, Object>> lists = new ArrayList<Map<String, Object>>();

    private Button sendRequest;

    private EditText search;

    private List<Ecard> usersList  = new ArrayList<Ecard>();

    private SimpleAdapter simpleAdapter;
    private ListAdapter listAdapter;

    private Handler handler = new Handler(){

        public void handleMessage(Message msg){
            switch (msg.what){
                case SHOW_RESPONSE :

                    String response = (String) msg.obj;
                    parseJSONWithJSONObject(response);
                    setUsersList(usersList);




                    listAdapter = new BaseAdapter() {
                        @Override
                        public int getCount() {
                            return usersList.size();
                        }

                        @Override
                        public Object getItem(int i) {
                            return null;
                        }

                        @Override
                        public long getItemId(int i) {
                            return i;
                        }

                        @Override
                        public View getView(int i, View view, ViewGroup viewGroup) {
                            view = LayoutInflater.from(MainActivity.this).inflate(R.layout.list_item,null);
                            ((TextView)view.findViewById(R.id.dealDateName)).setText(usersList.get(i).getDealDateTime());
                            ((TextView)view.findViewById(R.id.orgName)).setText(usersList.get(i).getOrgName());
                            ((TextView)view.findViewById(R.id.outName)).setText(usersList.get(i).getOutMoney());
                            ((TextView)view.findViewById(R.id.transMoney)).setText(usersList.get(i).getTransMoney());
                            Log.d("dealDateTime",usersList.get(i).getDealDateTime());
                            Log.d("orgName",usersList.get(i).getDealDateTime());
                            Log.d("outMoney",usersList.get(i).getDealDateTime());
                            Log.d("transMoney",usersList.get(i).getDealDateTime());
                            return view;
                        }
                    };

            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sendRequest =(Button)findViewById(R.id.send_request);
        search = (EditText)findViewById(R.id.search);
        sendRequest.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.send_request){
            sendRequestWithHttpURLConnection();
        }

    }

    private void setUsersList(List<Ecard> list){
        simpleAdapter = new SimpleAdapter(MainActivity.this, lists, R.layout.list_item,

                new String[]{"dealDateTime", "orgName", "outMoney", "transMoney"}, new int[]{R.id.dealDateName, R.id.orgName, R.id.outName, R.id.transMoney});

        ListView lv = (ListView) findViewById(R.id.list_view);

        lv.setAdapter(simpleAdapter);
    }
    private void sendRequestWithHttpURLConnection(){
        //在子线程中修改ui
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try{
                    URL url = new URL("http://console.ccnu.edu.cn/ecard/getTrans?&days=90&startNum=0&num=20&userId=" +search.getText().toString() );
                    connection = (HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream in = connection.getInputStream();
//                    BufferedReader reader  = new BufferedReader(new InputStreamReader(in));


                    InputStreamReader reader = new InputStreamReader(in);
                    BufferedReader bufferedReader = new BufferedReader(reader);
                    StringBuilder response = new StringBuilder();
                    String line;
                    while((line = bufferedReader.readLine())!= null){

                        response.append(line);
                    }
                    Message message = new Message();
                    message.what = SHOW_RESPONSE;
                    message.obj = response.toString();
                    handler.sendMessage(message);
                }catch (Exception e){
                    e.printStackTrace();
                }
                finally {
                    if(connection != null ){
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }
    public void parseJSONWithJSONObject(String jsonData) {
        try {
            JSONObject object= new JSONObject(jsonData);
            JSONArray jsonArr = object.getJSONArray("Ecard");
            usersList = new ArrayList<>();
            for (int i = 0; i < jsonArr.length(); i++) {
                Map<String, Object> list = new HashMap<String, Object>();
                list.put("dealDateTime", "时间:" + jsonArr.getJSONObject(i).getString("dealDateTime"));
                list.put("orgName", "商户名:" + jsonArr.getJSONObject(i).getString("orgName"));
                list.put("outMoney", "余额:" + jsonArr.getJSONObject(i).getString("outMoney"));
                list.put("transMoney", "花费:" + jsonArr.getJSONObject(i).getString("transMoney"));
                lists.add(list);
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }



}