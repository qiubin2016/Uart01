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

import com.itlong.java.EnDecrypt;
import com.utils.DataConversion;
import com.utils.ErrorCode;
import com.utils.ParseRcvData;
import com.utils.RandomNumber;
import com.utils.UartReaderData;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Random;

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
		getRandomValue(16);
		byte[] bytesArr = new byte[16];
		RandomNumber.getRandomBytes(bytesArr);
		Log.i(TAG, DataConversion.toHexString(bytesArr, bytesArr.length, true));
		byte[] bytes = null;
		try {
			Log.i(TAG, "len:" + bytes.length);
		} catch (Exception e) {
			Log.i(TAG, "exception...");
			e.printStackTrace();
		}

	}

	@Override
	protected void onDataReceived(final byte[] buffer, final int size) {
		//串口收到数据的回调函数
//		Log.i(TAG, "len: " + size + ",buffer: " + DataConversion.toHexString(buffer, size, true));
        mParseRcvData.put(buffer, size);  //串口接收的数据，送给解析类，解析成功回调onReceive()，失败回调onError()
		runOnUiThread(new Runnable() {
			public void run() {
				if (mReception != null) {
					mReception.append(new String(buffer, 0, size));
				}
			}
		});
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
		byte[] bytesAck;
		byte[] bytes = cmd.getByteBuffer();  //获取协议中的数据内容
		Log.i(TAG, "cmd: " + String.format("%04x", cmd.getCmd()) + ",reader addr:" + cmd.getAddr() + ",ctrl addr:" + cmd.getCtrAddr());
		switch (cmd.getCmd()) {
			case 0x4100:
			{
				bytesAck = new byte[1];
				bytesAck[0] = 0x00;
				mParseRcvData.write(cmd, cmd.STATUS_OK, bytesAck, mOutputStream);  //按照协议格式封装数据包并写入串口
			}
			break;
			case 0x4200:
			{
				//req：随机数RC（LByte）
				//ack：密钥标识（1Byte)+随机数长度（1Byte）+随机数RS（LByte）+随机数RC'（LByte）
				Log.i(TAG, "len:" + cmd.getLen());
				if (RandomNumber.CONST_NUM == cmd.getLen()) {
					Log.i(TAG, "random num:" + DataConversion.toHexString(bytes, bytes.length, true) + ",len:" + bytes.length);
                    byte[] encryptBytes = new byte[RandomNumber.CONST_NUM];
//					Log.i(TAG, "before:" + DataConversion.toHexString(encryptBytes, encryptBytes.length, true));
                    if (RandomNumber.encryptRandomBytes(bytes, encryptBytes)) {  //读头对随机数加密
                        Log.i(TAG, "after:" + DataConversion.toHexString(encryptBytes, encryptBytes.length, true));
						bytesAck = new byte[34];
						int i = 0;
						bytesAck[i++] = 0x10;
						bytesAck[i++] = 0x10;
						//固定random num
						byte[] bytesRandom = new byte[RandomNumber.CONST_NUM];
						RandomNumber.getRandomBytes(bytesRandom);
						Log.i(TAG, "getRandomBytes:" + DataConversion.toHexString(bytesRandom, bytesRandom.length, true));
						System.arraycopy(bytesRandom, 0, bytesAck, i, bytesRandom.length);
						i += bytesRandom.length;
						System.arraycopy(encryptBytes, 0, bytesAck, i, encryptBytes.length);  //encryptBytes-->bytesAck
                        mParseRcvData.write(cmd, cmd.STATUS_OK, bytesAck, mOutputStream);  //按照协议格式封装数据包并写入串口
                    } else {
                        Log.i(TAG, "encypt null");
                    }
				}
			}
			break;
			case 0x4201:
			{
				//req：密钥标识（1Byte)+随机数长度（1Byte）+随机数RS'（LByte）
				//ack：无
				if (bytes.length > 2) {
					Log.i(TAG, "4201");
					if (RandomNumber.CONST_NUM == bytes[1]) {
						byte[] verify = new byte[RandomNumber.CONST_NUM];
						System.arraycopy(bytes, 2, verify, 0, RandomNumber.CONST_NUM);
						Log.i(TAG, "verify:" + DataConversion.toHexString(verify, verify.length, true));
						if (RandomNumber.verifyRandonBytes(verify)) {  //随机数加密校验通过
							Log.i(TAG, "4201--1");
							mParseRcvData.write(cmd, cmd.STATUS_OK, null, mOutputStream);  //按照协议格式封装数据包并写入串口
						}
					}
				}
			}
			break;
			default:
				break;
		}
	}

	public static String getRandomValue(int numSize) {
		String str = "";
		for (int i = 0; i < numSize; i++) {
			char temp = 0;
			int key = (int) (Math.random() * 2);
			switch (key) {
				case 0:
					temp = (char) (Math.random() * 10 + '0');//产生随机数字
					break;
				case 1:
					temp = (char) (Math.random() * 6 + 'a');//产生a-f
					break;
				default:
					break;
			}
			Log.i(TAG, "temp:" + temp);
			str = str + temp;
		}
		Log.i(TAG, "str:" + str);
		return str;
	}
}
