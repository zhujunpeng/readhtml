package com.example.htmldemo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener{

    private static final String TAG = "MainActivity";
	protected static final int SCUESS = 0;
	protected static final int ERROR = 1;
	private EditText etHtml;
	private TextView tvHtml;
	private Button btnHtml;
	
	private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case SCUESS:
				tvHtml.setText((String) msg.obj);
				break;
			case ERROR:
				Toast.makeText(MainActivity.this, "抓取失败", 0).show();
			default:
				break;
			}
		}
		
	};

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        etHtml = (EditText) findViewById(R.id.et_html);
        tvHtml = (TextView) findViewById(R.id.tv_html);
        btnHtml = (Button) findViewById(R.id.btn_html);
        btnHtml.setOnClickListener(this);
    }

	@Override
	public void onClick(View v) {
		final String url = etHtml.getText().toString();
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// 请求网络
				String html = getHtmlFromInternet(url);
				if (!TextUtils.isEmpty(html)) {
					Message msg = new Message();
					msg.what = SCUESS;
					msg.obj = html;
					handler.sendMessage(msg);
				}else {
					Message msg = new Message();
					msg.what = ERROR;
					handler.sendMessage(msg);
				}
			}
		}).start();
	}

	protected String getHtmlFromInternet(String url) {
		try {
			URL mURL = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) mURL.openConnection();
			
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(5000);
			
			int responseCode = conn.getResponseCode();
			if (responseCode == 200) {
				InputStream is = conn.getInputStream();
				String html = getStringFromInputStream(is);
				return html;
			}else {
				Log.i(TAG, "访问失败: " + responseCode);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private String getStringFromInputStream(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = -1;
		while ((len = is.read(buffer)) != -1) {
			baos.write(buffer, 0, len);
		}
		is.close();
		// 把流中的数据转换成字符串，采用的编码是utf-8
		String html = baos.toString();
		String charset = "utf-8";
		// 采用的是gbk或者gb2312编码时
		if (html.contains("gbk") || html.contains("gb2312") || html.contains("GBK") || html.contains("GB2312")) {
			charset = "gbk";
		}
		html = new String(baos.toByteArray(), charset);
		return html;
	}
    
}
