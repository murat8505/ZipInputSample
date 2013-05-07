package jp.classmethod.android.sample.zipinput;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;

public class MainActivity extends FragmentActivity {
    
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Zipファイルを一時ファイルとして保存
        File file = null;
        try {
            file = File.createTempFile("sample", "zip");
            // Webからダウンロードするなど、何らかの形でZipファイルを取得
            InputStream is = getResources().getAssets().open("sample.zip");
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length = 0;
            while ((length = is.read(buffer))>0) {
                fos.write(buffer, 0, length);
            }
            fos.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Zipファイルを展開して保存
        ArrayList<OpendZipEntry> list = new ArrayList<OpendZipEntry>();
        try {
            // Zipファイルを開く
            ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file));
            ZipFile zipFile = new ZipFile(file);
            // 保存するディレクトリを作成
            File dir = new File(Environment.getExternalStorageDirectory() + "/sample");
            if (!dir.exists()) {
                dir.mkdir();
            }
            // 順番に読み込む
            ZipEntry entry = null;
            while((entry = zipInputStream.getNextEntry()) != null) {
                // Listで表示するアイテム
                OpendZipEntry item = new OpendZipEntry();
                item.fileName = entry.getName();
                item.filePath = dir.getPath() + "/" + item.fileName;
                list.add(item);
                // データのコピー
                InputStream is = zipFile.getInputStream(entry);
                FileOutputStream os = new FileOutputStream(new File(item.filePath));
                byte[] buffer = new byte[1024];
                int length = 0;
                while ((length = is.read(buffer))>0) {
                    os.write(buffer, 0, length);
                }
                os.close();
                is.close();
                zipInputStream.closeEntry();
            }
            zipInputStream.close();
        } catch (IOException e) {
            Log.e(TAG, "IOException");
        }
        
        // AdapterをListFragmentにセット
        ZipEntryAdapter adapter = new ZipEntryAdapter(this, list);
        ListFragment frag = new ListFragment();
        frag.setListAdapter(adapter);
        
        // ListFragmentを表示
        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        trans.add(android.R.id.content, frag);
        trans.commit();
    }

}
