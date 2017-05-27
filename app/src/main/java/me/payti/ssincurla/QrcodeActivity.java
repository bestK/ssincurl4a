package me.payti.ssincurla;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by Administrator on 2017/5/23.
 */

public class QrcodeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qrcode_main);

        Bundle bundle = this.getIntent().getExtras();
        String img = bundle.getString("path");

        ImageView qrcodeView = (ImageView) findViewById(R.id.qrCodeImageView);
        Bitmap qrcodeImg = getLoacalBitmap(img);
        qrcodeView.setImageBitmap(qrcodeImg);

        TextView text = (TextView) findViewById(R.id.qrText);
        text.setText(img);
        /*File appDir = new File(Environment.getExternalStorageDirectory().getPath(), String.valueOf(R.string.app_name));
        if (appDir.exists()) {
            appDir.delete();
            File[] imgs = appDir.listFiles();
            Arrays.sort(imgs, new Comparator<File>() {
                @Override
                public int compare(File file1, File file2) {
                    return (int)(file2.lastModified()-file1.lastModified());
                }
            });
            ImageView qrcodeView = (ImageView) findViewById(R.id.qrCodeImageView);
            Bitmap qrcodeImg = getLoacalBitmap(imgs[0].getAbsolutePath()); //本地最新取图片
            qrcodeView.setImageBitmap(qrcodeImg);
        }*/
                Log.d("asdasdad", "asdasdasd");
    }


    public static Bitmap getLoacalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
