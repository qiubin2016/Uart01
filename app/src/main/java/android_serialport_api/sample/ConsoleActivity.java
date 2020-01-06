/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package android_serialport_api.sample;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.utils.DataConversion;
import com.utils.ErrorCode;
import com.utils.ParseRcvData;
import com.utils.UartReaderData;

import java.io.IOException;
import java.util.Arrays;

public class ConsoleActivity extends SerialPortActivity implements ParseRcvData.CmdCallback<UartReaderData>{
	private static final String TAG = "ConsoleActivity";
	EditText mReception;
	ParseRcvData mParseRcvData;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.console);

//		setTitle("Loopback test");
		mReception = (EditText) findViewById(R.id.EditTextReception);

		EditText Emission = (EditText) findViewById(R.id.EditTextEmission);
		Emission.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				int i;
				CharSequence t = v.getText();
				char[] text = new char[t.length()];
				for (i=0; i<t.length(); i++) {
					text[i] = t.charAt(i);
				}
				try {
					mOutputStream.write(new String(text).getBytes());
					mOutputStream.write('\n');
				} catch (IOException e) {
					e.printStackTrace();
				}
				return false;
			}
		});

		//新建串口数据解析类，设置回调接口
		mParseRcvData = new ParseRcvData(this);
	}

	@Override
	protected void onDataReceived(final byte[] buffer, final int size) {
		//串口收到数据的回调函数
//		Log.i(TAG, "len: " + size + ",buffer: " + DataConversion.toHexString(buffer, size, true));

		runOnUiThread(new Runnable() {
			public void run() {
				if (mReception != null) {
					mReception.append(new String(buffer, 0, size));
				}
			}
		});

		mParseRcvData.put(buffer, size);  //串口接收的数据，送给解析类，解析成功回调onReceive()，失败回调onError()
	}


	@Override
	public void onReceive(UartReaderData cmd) {
		//解析到1个合法数据包
		Log.i(TAG, "onReceive: " + cmd.toString());
		cmdProcess(cmd);
	}

	@Override
	public void onError(ErrorCode errCode) {
		Log.i(TAG, "error code: " + errCode.getErrorCode());
	}

	private void cmdProcess(UartReaderData cmd) {
		Log.i(TAG, "cmd: " + String.format("%04x", cmd.getCmd()) + ",reader addr:" + cmd.getAddr() + ",ctrl addr:" + cmd.getCtrAddr());
		switch (cmd.getCmd()) {
			case 0x4100:
			{

			}
			break;
			case 0x4200:
			{
				Log.i(TAG, "len:" + cmd.getLen());
				if (16 == cmd.getLen()) {
					byte[] bytes = cmd.getByteBuffer();
					Log.i(TAG, "random num:" + DataConversion.toHexString(bytes, bytes.length, true));

				}
				//
				//ack：密钥标识（1Byte)+随机数长度（1Byte）+随机数RS（LByte）+随机数RC'（LByte）
			}
			break;
			case 0x4201:
			{

			}
			break;
			default:
				break;
		}
	}
}
