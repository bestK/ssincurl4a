package me.payti.ssincurla;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        MaterialDialog.Builder builder = new MaterialDialog.Builder(MainActivity.this)
                .title(R.string.progress_dialog)
                .content(R.string.please_wait)
                .progress(true, 0);

        final MaterialDialog dialog = builder.build();

        final FloatingActionButton addUrl = (FloatingActionButton) findViewById(R.id.addUrl);
        addUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                EditText urlText = (EditText) findViewById(R.id.newUrl);
                String firsturl = urlText.getText().toString();

                if (!firsturl.contains("api.php"))  firsturl = firsturl + "/api.php";

                final String url = firsturl;

                boolean isMatch = android.util.Patterns.WEB_URL.matcher(url).matches();

                boolean httpHead = url.contains("http");


                if ("".equals(url)) {
                    Snackbar.make(view, R.string.urlNullTip, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    return;
                }

                if (!isMatch) {
                    Snackbar.make(view, R.string.urlErrTip, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                } else if (!httpHead) {
                    Snackbar.make(view, R.string.urlContainsTip, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                } else {

                    dialog.show();
                    new AsyncTask<Void, Void, Object>() {

                        @Override
                        protected Object doInBackground(Void... params) {

                            try {
                                OkHttpClient client = new OkHttpClient();
                                Request request = new Request.Builder()
                                        .url(url)
                                        .build();
                                Response response = client.newCall(request).execute();

                                if (response.code() == 200) {
                                    JSONObject obj = JSON.parseObject(response.body().string());
                                    initConfigList(obj);
                                    dialog.dismiss();
                                } else {
                                    dialog.dismiss();
                                    Snackbar.make(view, R.string.telnetErrTip, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                }
                                response.close();
                            } catch (IOException e) {
                                Snackbar.make(view, R.string.exceptionTip, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                dialog.dismiss();
                                e.printStackTrace();
                            }

                            return null;

                        }
                    }.execute();

                }
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void initConfigList(JSONObject obj) throws IOException {


        ArrayList l = new ArrayList();
        ArrayList<String> hostList = new ArrayList<String>();
        ArrayList<String> ssUrlList = new ArrayList<String>();

        for (int j = 0; j < obj.size(); j++) {
            JSONArray data = JSONArray.parseArray(obj.get("data").toString());


            for (int k = 0; k < data.size(); k++) {
                JSONObject info = JSONObject.parseObject(data.get(k).toString());
                JSONObject mapping = JSONObject.parseObject(info.get("attributes").toString());
                if (!getResources().getString(R.string.imageName).equals(mapping.get("image_name").toString())) {
                    continue;
                }
                String s = mapping.get("port_mappings").toString();
                //Log.d(TAG, s);
                JSONArray end = JSONArray.parseArray(s);
                String cmd = mapping.get("cmd").toString();

                for (int i = 0; i < end.size(); i++) {
                    String pwd, lock, host, port;
                    JSONObject ss = JSONObject.parseObject(end.get(i).toString().replace("[", "").replace("]", ""));
                    pwd = cmd.substring(cmd.indexOf("k ") + 2, cmd.indexOf(" -m"));
                    lock = cmd.substring(cmd.indexOf("m ") + 2);
                    port = ss.get("service_port").toString();
                    host = ss.get("host").toString().replace("seaof-", "").replaceAll(".jp.*", "").replace("-", ".");
                    String ssUrl = lock + ":" + pwd + "@" + host + ":" + port;
                    // ss链
                    ssUrl = "ss://" + Base64.encodeToString(ssUrl.trim().getBytes(), Base64.DEFAULT);

                    Map<String, String> map = new HashMap<>();
                    Map<String, String> hosts = new HashMap<>();
                    Map<String, String> ssUrls = new HashMap<>();
                    map.put("id", info.get("id").toString());
                    map.put("pwd", pwd);
                    map.put("lock", lock);
                    map.put("port", port);
                    map.put("host", host);
                    map.put("ssUrl", ssUrl);
                    hostList.add(host);
                    ssUrlList.add(ssUrl);

                    l.add(map);


                }
            }

        }

        Intent intent = new Intent(MainActivity.this, ListActivity.class);
        intent.putStringArrayListExtra("list", l);
        intent.putStringArrayListExtra("hostlist", hostList);
        intent.putStringArrayListExtra("sslist", ssUrlList);
        startActivity(intent);

    }


}
