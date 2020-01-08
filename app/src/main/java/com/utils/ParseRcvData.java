package com.utils;


import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;

public class ParseRcvData<T>{
    private static final String TAG = "ParseRcvData";


    private CmdCallback<T> mCmdCallback;
    private UartReaderData mUartReaderData;
    private ErrorCode mErrorCode;

    public ParseRcvData(CmdCallback<T> cmdCallback) {
        this.mCmdCallback = cmdCallback;
        mUartReaderData = new UartReaderData();
        mErrorCode = new ErrorCode();
    }

    public void put(final byte[] buffer, final int size) {
        for (int i = 0; i < size; i++) {  //循环处理
            byteProcess(buffer[i]);
        }
    }

    private void byteProcess(byte byteData) {
//        Log.i(TAG, String.format("byte:%02x", byteData));
        if (mUartReaderData.SOI == byteData) {  //起始字节
            //初始化参数
            mUartReaderData.setStep(1);
            mUartReaderData.clearCheckSum();
            mUartReaderData.setModify(false);
            mUartReaderData.setReady(false);
        } else {
            if (0 == mUartReaderData.getStep()) {  //未接收过合法的起始字节，丢弃数据
                return;
            }
            if (mUartReaderData.EOI == byteData) {  //结束字节
                if ((mUartReaderData.getStep() >= mUartReaderData.MIN_STEP)
                        && (mUartReaderData.CHECK_SUM_OK == mUartReaderData.getCheckSum())) {  //step合法，校验通过
                    mUartReaderData.setReady(true);    //解析到合法命令
                    mUartReaderData.setLen(mUartReaderData.getStep() - mUartReaderData.MIN_STEP);
                    //回调命令处理函数
                    mCmdCallback.onReceive((T) mUartReaderData);
                } else if (mUartReaderData.CHECK_SUM_OK != mUartReaderData.getCheckSum()) {  //校验失败，丢弃数据
                    mUartReaderData.setStep(0);
                    mErrorCode.setErrorCode(ErrorCode.ERROR_CODE_CHECK_SUM_FAILED);
                    mCmdCallback.onError(mErrorCode);
                } else {  //长度超过限制，丢弃数据
                    mUartReaderData.setStep(0);
                    mErrorCode.setErrorCode(ErrorCode.ERROR_CODE_MAX_LEN_LIMITED);
                    mCmdCallback.onError(mErrorCode);
                }
            } else if (mUartReaderData.MODIFY_BYTE == byteData) {  //遇到拆分字节
                mUartReaderData.setModify(true);
            } else {
                if (mUartReaderData.isModify()) {  //需要结合上个字节做合并处理
                    mUartReaderData.setModify(false);
                    byteData |= mUartReaderData.MODIFY_BYTE_RX;    //合并
                }
                mUartReaderData.calcCheckSum(byteData);
                if (mUartReaderData.getStep() >= 7) {
                    if ((mUartReaderData.getStep() - 7) < mUartReaderData.MAX_LEN) {  //数据长度未超过最大限制
                        //保存数据
                        mUartReaderData.put(byteData);
                        mUartReaderData.increaseStep();
                    } else {  //数据长度超限，丢弃数据
                        mUartReaderData.setStep(0);
                        mErrorCode.setErrorCode(ErrorCode.ERROR_CODE_MAX_LEN_LIMITED);
                        mCmdCallback.onError(mErrorCode);
                    }
                } else {
                    switch (mUartReaderData.getStep()) {
                        case 1:  //设备组号(读头号):
                        {
                            mUartReaderData.setCtrAddrH((byte)((byteData & 0xF0) >> 4));
                            mUartReaderData.setAddr((byte)(byteData & 0x0F));  //保存
                            Log.i(TAG, "ctrl addr h:" + mUartReaderData.getCtrAddrH() + ",addr:" + mUartReaderData.getAddr());
                            if ((mUartReaderData.BROADCAST_PARAM == byteData) || (mUartReaderData.ADDR_BYTE == mUartReaderData.getAddr())) {
                                mUartReaderData.increaseStep();
                            } else {  //不合法，丢弃数据，重新开始接收
                                mUartReaderData.setStep(0);
//                                mErrorCode.setErrorCode(ErrorCode.ERROR_CODE_GROUP_UNMATCHED);
//                                mCmdCallback.onError(mErrorCode);
                            }
                            break;
                        }
                        case 2:  //设备地址(控制器机号)
                        {
                            mUartReaderData.setCtrlAddrL(byteData);
                            mUartReaderData.setCtrAddr(mUartReaderData.getCtrAddrH() * 256 + byteData);
                            mUartReaderData.increaseStep();
                            break;
                        }
                        case 3:  //设备类型
                        {
                            if ((mUartReaderData.BROADCAST_PARAM == byteData) || (mUartReaderData.DEVICE_TYPE == byteData)) {
                                mUartReaderData.increaseStep();
                            } else {  //不合法，丢弃数据，重新开始接收
                                mUartReaderData.setStep(0);
                                mErrorCode.setErrorCode(ErrorCode.ERROR_CODE_DEVICE_TYPE_UNMATCHED);
                                mCmdCallback.onError(mErrorCode);
                            }
                            break;
                        }
                        case 4:  //流水号
                        {
                            mUartReaderData.setSnr(byteData);
                            mUartReaderData.increaseStep();
                            break;
                        }
                        case 5:  //命令高字节:
                        {
                            mUartReaderData.setCmdh(byteData);
                            mUartReaderData.increaseStep();
                            break;
                        }
                        case 6:  //命令低字节:
                        {
                            mUartReaderData.setCmdl(byteData);
                            mUartReaderData.increaseStep();
                            //需要注意优先级
                            mUartReaderData.setCmd((mUartReaderData.getCmdh() << 8) + mUartReaderData.getCmdl());
                            break;
                        }
                        default:
                            break;
                    }
                }
            }
        }
    }

    public void write(UartReaderData cmd, byte status, byte[] output, OutputStream outputStream) {
        byte[] bytes;
        int num = 0;
        byte tmpByte, checkSum = 0;

        bytes = new byte[100];
        bytes[num++] = cmd.SOI;    //起始字节
        tmpByte = (byte)(cmd.getAddr() | (byte) (cmd.getCtrAddrH() << 4));    //读头号
        Log.i(TAG, String.format("reader addr:%02x", tmpByte));
        checkSum ^= tmpByte;    //计算校验
        num = checkModify(bytes, num, tmpByte, cmd);
        tmpByte = cmd.getCtrlAddrL();    //控制器机号
        Log.i(TAG, String.format("ctrl addr:%02x", tmpByte));
        checkSum ^= tmpByte;    //计算校验
        num = checkModify(bytes, num, tmpByte, cmd);
        bytes[num++] = mUartReaderData.DEVICE_TYPE;    //设备类型
        checkSum ^= mUartReaderData.DEVICE_TYPE;    //计算校验
        tmpByte = cmd.getSnr();    //流水号
        checkSum ^= tmpByte;    //计算校验
        num = checkModify(bytes, num, tmpByte, cmd);
        bytes[num++] = 0x00;   //状态高字节
        bytes[num++] = status;   //状态低字节
        try {
            if (null != output) {
                //当output为null，output.length会崩溃
                for (int i = 0; i < output.length; i++) {
                    checkSum ^= output[i];    //计算校验
                    num = checkModify(bytes, num, output[i], cmd);
                }
            }
            num = checkModify(bytes, num, checkSum, cmd);  //校验字节
            bytes[num++] = cmd.EOI;     //结束字节
            Log.i(TAG, "output:" + DataConversion.toHexString(bytes, num, true));  //打印
            outputStream.write(bytes, 0, num);    //往串口写数据
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int checkModify(byte[] bytes, int num, byte tmpByte, UartReaderData cmd) {
//        Log.i(TAG, String.format("tmpByte:%02x, modify:%02x", tmpByte, cmd.MODIFY_BYTE));
//        Log.i(TAG, "tmpByte:" + tmpByte + ",modify:" + cmd.MODIFY_BYTE);
        if ((char)tmpByte >= (char)cmd.MODIFY_BYTE) {
            Log.i(TAG, ">= 0xF0");
            bytes[num++] = cmd.MODIFY_BYTE;
            bytes[num++] = (byte)(tmpByte & cmd.MODIFY_BYTE_TX);
        } else {
//            Log.i(TAG, "< 0xF0");
            bytes[num++] = tmpByte;
        }
        return num;
    }

    //接口  解析到合法数据包后回调onReceive()；解析错误回调onError()
    public interface CmdCallback<T>{
        void onReceive(T cmd);

        void onError(ErrorCode errCode);
    }
}
