package com.Lion.kakao.toolkit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.CookieManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {

    static ArrayList<File> list;
    GridLayout gridLayout;
    LinearLayout relativeLayout;
    boolean load = false;
    DrawerLayout draw;
    static String getPath;
    String[] pathes = {"sdcard/Android/data/com.kakao.talk/cache/", "sdcard/Android/data/com.kakao.talk/contents/Mg==/", "sdcard/Android/data/com.kakao.talk/cache/", "sdcard/Android/data/com.kakao.talk/cache/"};
    int count;
    int act = 0;
    CheckBox checkBox, ca1, ca2;
    int startValue=0;
    public void runLoad(final int v){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageView myImage = new ImageView(MainActivity.this);
                myImage.setPadding(17, 16, 0, 0);
                Glide.with(MainActivity.this)
                        .load(list.get(v))
                        .apply(new RequestOptions().override(dip2px(83), dip2px(83)).transforms(new CenterCrop(), new RoundedCorners(16)))
                        .into(myImage);
                myImage.setId(v);
                myImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), ImageViewThis.class);
                        intent.putExtra("Data", v.getId());
                        startActivity(intent);
                    }
                });
                gridLayout.addView(myImage);
                synchronized(this) {
                    this.notify();
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        act = 0;
        startValue=0;
        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.MANAGE_EXTERNAL_STORAGE}, 1);
//        if (!Environment.isExternalStorageManager()){
//            Log.i("ㄹ워누", "onCreate:  ㄴ먼ㅇ람ㄴㅇㄹㄴ알닝ㄹ");
//            Intent intent = new Intent();
//            intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
//            Uri uri = Uri.fromParts("package", this.getPackageName(), null);
//            intent.setData(uri);
//            startActivity(intent);
//        }
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            TedPermission.with(MainActivity.this)
                    .setPermissionListener(permissionlistener)
                    .setRationaleMessage("사진을 가져오기 위하여 저장공간 권한이 필요합니다")
                    .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .check();
        } else {
            load = false;
            checkBox = new CheckBox(this);
            checkBox.setText("종료시 캐시 제거");
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            param.leftMargin = 30;
            checkBox.setLayoutParams(param);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SharedPreferences sharedPreferences = getSharedPreferences("Cache", MODE_PRIVATE);
                    //저장을 하기위해 editor를 이용하여 값을 저장시켜준다.
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("check", isChecked); // key, value를 이용하여 저장하는 형태
                    editor.apply();
                }
            });
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final SharedPreferences sf = getSharedPreferences("Cache", MODE_PRIVATE);
//                text라는 key에 저장된 값이 있는지 확인. 아무값도 들어있지 않으면 ""를 반환
                    count = sf.getInt("count", 0);
//                text라는 key에 저장된 값이 있는지 확인. 아무값도 들어있지 않으면 ""를 반환
                    runOnUiThread(new Runnable() {
                        public void run() { // 메시지 큐에 저장될 메시지의 내용
                            checkBox.setChecked(sf.getBoolean("check", false));
                        }
                    });
                }
            }).start();

            LinearLayout.LayoutParams param2 = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            param2.leftMargin = 30;
            param2.topMargin = 40;
            ca1 = new CheckBox(this);
            ca1.setText("카카오톡 캐시");
            ca1.setLayoutParams(param2);
            ca1.setChecked(true);
            ca2 = new CheckBox(this);
            ca2.setText("앱 캐시           ");
            ca2.setLayoutParams(param);
            ca2.setChecked(true);

            final SwipeRefreshLayout mSwipeRefreshLayout = new SwipeRefreshLayout(this);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                /*Intent intent = new Intent(MainActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                finish();

                startActivity(intent);*/
                    act = 1;
                    recreate();
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            });
            relativeLayout = new LinearLayout(this);
            relativeLayout.setOrientation(LinearLayout.VERTICAL);
            relativeLayout.addView(mSwipeRefreshLayout);
            gridLayout = new GridLayout(this);
            gridLayout.setOrientation(0);
            gridLayout.setColumnCount(4);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        list = getResult();
                        Collections.sort(list, new Comparator<File>() {
                            public int compare(File f1, File f2) {
                                return Long.compare(f2.lastModified(), f1.lastModified());
                            }
                        });
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                for (; startValue < list.size(); startValue++) {
                                    if (act == 1) break;
                                    runLoad(startValue);
                                    SystemClock.sleep(25);
                                    if ((startValue + 1) % 40 == 0) {
                                        startValue++;
                                        break;
                                    }
                                }
                                load=true;
                            }
                        }).start();
                    }
                }
            }).start();
            draw = new DrawerLayout(this);
            draw.addView(relativeLayout);
            draw.addView(createDrawerLayout());

            final ScrollView scrollView1 = new ScrollView(this);
            HorizontalScrollView scrollView = new HorizontalScrollView(this);
            scrollView1.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                @Override
                public void onScrollChanged() {
                    if (load&&!scrollView1.canScrollVertically(1)) {
//                        loadTask.execute();
                        load=false;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                for (; startValue < list.size(); startValue++) {
                                    if (act == 1) break;
                                    runLoad(startValue);
                                    SystemClock.sleep(25);
                                    if ((startValue + 1) % 40 == 0) {
                                        startValue++;
                                        break;
                                    }
                                }
                                load=true;
                            }
                        }).start();
                    }
                }
            });
            scrollView.addView(gridLayout);
            scrollView1.addView(scrollView);
            mSwipeRefreshLayout.addView(scrollView1);
            setContentView(draw);
        }
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private LinearLayout createDrawerLayout() {
        try {
            final String[] str = {"Cache", "Message", "emoticon", "profile"};

            LinearLayout lay = new LinearLayout(this);
            lay.setOrientation(1);
            LinearLayout lay2 = new LinearLayout(this);
            lay2.setOrientation(1);
            Spinner spin = new Spinner(MainActivity.this);
            spin.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            ArrayAdapter adapters = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, str);
            spin.setAdapter(adapters);
            spin.setSelection(count);
            spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView adapterView, View view, int i, long l) {
                    count = i;
                }

                @Override
                public void onNothingSelected(AdapterView adapterView) {

                }
            });
            final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
            dialog.setTitle("선택");
            lay.addView(spin);
            lay.addView(checkBox);

            dialog.setView(lay);

            dialog.setNegativeButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    act = 1;
                    recreate();
                }
            });
            final AlertDialog.Builder dialog2 = new AlertDialog.Builder(MainActivity.this);
            dialog2.setTitle("선택");
            lay2.addView(ca1);
            lay2.addView(ca2);
            dialog2.setView(lay2);
            dialog2.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (ca1.isChecked() || ca2.isChecked())
                        new BackgroundTask(MainActivity.this).execute();
                }
            });
            dialog2.setNegativeButton("취소", null);

            final AlertDialog dia2 = dialog2.create();
            final AlertDialog dia = dialog.create();
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(1);
            String[] menus = {"카카오톡 캐시 지우기", "리로드", "설정"};
            ListView list = new ListView(this);
            ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, menus);
            list.setAdapter(adapter);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                    if (pos == 0) {
                        //다이얼로그 띄우기
                        //check box: app cache, kakao cache
                        //하나라도 있으면 ㄱ                         new BackgroundTask(MainActivity.this).execute();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                final String kaka = String.format("%.2f", getFolderSize(new File("sdcard/Android/data/com.kakao.talk")) / 1024.0 / 1024.0);
                                final String cas = String.format("%.2f", getFolderSize(getCacheDir()) / 1024.0 / 1024.0);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ca1.setText("카카오톡 캐시  " + " (" + kaka + "MB)");
                                        ca2.setText("앱 캐시             " + " (" + cas + "MB)");
                                    }
                                });
                            }
                        }).start();
                        dia2.setCancelable(true);
                        dia2.setCanceledOnTouchOutside(true);
                        dia2.show();
                    } else if (pos == 1) {
                        act = 1;
                        recreate();
                    } else if (pos == 2) {
                        dia.setCancelable(true);
                        dia.setCanceledOnTouchOutside(true);
                        dia.show();
                    }
                }
            });
            layout.addView(list);
            DrawerLayout.LayoutParams params = new DrawerLayout.LayoutParams(-1, -1);
            params.gravity = Gravity.LEFT;
            layout.setLayoutParams(params);
            layout.setBackgroundColor(Color.WHITE);
            layout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
            return layout;
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
        return null;
    }

    public void deleteDir(String path) {
        File file = new File(path);
        if (!file.exists()) return;
        File[] files = file.listFiles();
        for (File filed : files) {
            if (filed.isDirectory()) deleteDir(filed.getAbsolutePath());
            else filed.delete();
        }
        file.delete();
    }

    private class BackgroundTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog dialog;

        public BackgroundTask(MainActivity activity) {
            dialog = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Please Wait.");
            dialog.show();
        }

        @Override
        protected void onPostExecute(Void result) {
            Toast.makeText(MainActivity.this, "Kakaotalk Cache Deleted", Toast.LENGTH_SHORT).show();
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            act = 1;
            recreate();
        }

        @Override
        protected Void doInBackground(Void... params) {
            //do your work
            if (ca1.isChecked()) deleteDir("sdcard/Android/data/com.kakao.talk/");
            if (ca2.isChecked()) clearApplicationCache(null);
            return null;
        }

    }

    public long getFolderSize(File file) {
        long totalSize = 0;
        File[] childFileList = file.listFiles();
        if (childFileList == null) {
            return 0;
        }
        for (File childFile : childFileList) {
            if (childFile.isDirectory()) {
                totalSize += getFolderSize(childFile);
            } else {
                totalSize += childFile.length();
            }
        }
        return totalSize;
    }

    public void clearApplicationCache(java.io.File dir) {
        if (dir == null) dir = getCacheDir();
        if (dir == null) return;
        java.io.File[] children = dir.listFiles();
        try {
            // 쿠키 삭제
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeSessionCookie();

            for (int i = 0; i < children.length; i++)
                if (children[i].isDirectory())
                    clearApplicationCache(children[i]);
                else children[i].delete();
        } catch (Exception e) {
        }
    }

    public int dip2px(int dips) {
        return (int) Math.ceil(dips * getApplicationContext().getResources().getDisplayMetrics().density);
    }

    public ArrayList<File> getResult() {
        File path = new File(pathes[count]);
        ArrayList<File> result = new ArrayList<>();
        if (!path.exists()) return result;
        File[] files = path.listFiles();

        for (File file : files) {
            if (file.isDirectory()) {
                if (file.getAbsolutePath().matches(count < 2 ? ".*[0-9].*" : (count == 2 ? ".*emoticon_dir" : ".*(file|default|image).*"))) {
                    File[] filesd = file.listFiles();
                    for (File filee : filesd) {
                        if (filee.isDirectory()) {
                            for (File filed : filee.listFiles()) {
                                if (!filed.getAbsolutePath().contains("thumbnailHint")) {
                                    String str = null;
                                    try {
                                        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filed), "UTF-8"));
                                        str = br.readLine();
                                        String TAG = "ddd";
                                        if (str.substring(0, 13).matches("[^a-zA-FH-Z0-9].*(PNG|xif|IF|\u0010).*"))
                                            result.add(filed);
                                        else
                                            Log.i(TAG, "getResult: " + str + ", " + filed.getAbsolutePath());
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } catch (StringIndexOutOfBoundsException e) {
                                        if (str.matches("[^a-zA-FH-Z0-9].*(PNG|xif|IF).*"))
                                            result.add(filed);
                                    } catch(Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }


    PermissionListener permissionlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            if (!load) {
                act = 1;
                recreate();
            }
        }


        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Toast.makeText(MainActivity.this, "사진을 가져오기 위하여 저장공간 권한이 필요합니다" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            MainActivity.this.finish();
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (checkBox.isChecked()) {

                    clearApplicationCache(null);
                }


                // Activity가 종료되기 전에 저장한다.
                //SharedPreferences를 sFile이름, 기본모드로 설정
                SharedPreferences sharedPreferences = getSharedPreferences("Cache", MODE_PRIVATE);
                //저장을 하기위해 editor를 이용하여 값을 저장시켜준다.
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("count", count); // key, value를 이용하여 저장하는 형태
                //다양한 형태의 변수값을 저장할 수 있다.
                //editor.putString();
                //editor.putBoolean();
                //editor.putFloat();
                //editor.putLong();
                //editor.putInt();
                //editor.putStringSet();
                //최종 커밋
                editor.apply();
            }
        }).start();
    }
}