package me.payti.ssincurla;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static android.R.attr.path;

/**
 * Created by Administrator on 2017/5/21.
 */

public class ListActivity extends AppCompatActivity {

    private final String COM_GITHUB_SHADOWSOCKS = "com.github.shadowsocks";
    private final String IN_ZHAOJ_SHADOWSOCKSR = "in.zhaoj.shadowsocksr";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_main);

        Bundle bundle = this.getIntent().getExtras();

        /*获取Bundle中的数据，注意类型和key*/
        ArrayList<Integer> list = bundle.getIntegerArrayList("list");
        ArrayList<Integer> hostlist = bundle.getIntegerArrayList("hostlist");
        ArrayList<Integer> sslist = bundle.getIntegerArrayList("sslist");

        final ListView lv = (ListView) findViewById(R.id.list_configs);

        lv.setAdapter(new ArrayAdapter<Integer>(this, android.R.layout.simple_list_item_1, sslist));

        showSnackBar(getResources().getString(R.string.successfullyTip));
        // 单击事件复制 ss链 到剪贴板
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                ClipboardManager copy = (ClipboardManager) ListActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                copy.setText(lv.getItemAtPosition(position).toString());


                if (isAppInstalled(ListActivity.this, COM_GITHUB_SHADOWSOCKS)) {
                    startActivity(getPackageManager().getLaunchIntentForPackage(COM_GITHUB_SHADOWSOCKS));
                }
                if (isAppInstalled(ListActivity.this, IN_ZHAOJ_SHADOWSOCKSR)) {
                    startActivity(getPackageManager().getLaunchIntentForPackage(IN_ZHAOJ_SHADOWSOCKSR));
                }

                Toast.makeText(getApplicationContext(), R.string.copyTip, Toast.LENGTH_LONG).show();
            }
        });

        // 长按显示二维码
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                CodeUtils.createImage(lv.getItemAtPosition(position).toString(), 400, 400, null);

                // 生成并保存二维码到图库并返回地址
                String qrCodePath = saveImageToGallery(view.getContext(), CodeUtils.createImage(lv.getItemAtPosition(position).toString(), 400, 400, null));
                // 传递地址到显示页面
                Intent intent = new Intent(ListActivity.this, QrcodeActivity.class);
                intent.putExtra("path", qrCodePath);
                startActivity(intent);

                return true;
            }
        });
    }


    public String saveImageToGallery(Context context, Bitmap bmp) {
        // 首先保存图片
        File appDir = new File(Environment.getExternalStorageDirectory().getPath(), getResources().getString(R.string.app_name));
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            Toast.makeText(context, "FileNotFoundException", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));

        // 返回二维码地址
        return file.getAbsolutePath();
    }


    public boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }


    public void showSnackBar(String message) {
        //去掉虚拟按键
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION //隐藏虚拟按键栏
                | View.SYSTEM_UI_FLAG_IMMERSIVE //防止点击屏幕时,隐藏虚拟按键栏又弹了出来
        );
        final Snackbar snackbar = Snackbar.make(getWindow().getDecorView(), message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("知道了", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
                //隐藏SnackBar时记得恢复隐藏虚拟按键栏,不然屏幕底部会多出一块空白布局出来,和难看
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
        }).show();
    }
}
