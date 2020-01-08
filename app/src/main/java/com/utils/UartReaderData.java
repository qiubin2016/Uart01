package com.utils;

import android.util.Log;

import java.nio.ByteBuffer;


public class UartReaderData /*extends Object*/{
    private static final String TAG = "UartReaderData";

    public static final int   MAX_LEN = 10000;
    public static final int   MIN_STEP = 8;
    public static final int   CHECK_SUM_OK = 0;
    public static final byte SOI = (byte)0xF2;
    public static final byte EOI = (byte)0xF3;
    public static final byte GROUP_BYTE = (byte)0x01;    //调试 固定
    public static final byte ADDR_BYTE = (byte)0x03;    //读头号 调试 固定
    public static final byte DEVICE_TYPE = (byte)0x24;  //电梯读头设备类型
    public static final byte BROADCAST_PARAM = (byte)0x00;  //广播地址
    public static final byte MODIFY_BYTE_TX = (byte)0x7F;
    public static final byte MODIFY_BYTE_RX = (byte)0x80;
    public static final byte MODIFY_BYTE = (byte)0xF0;

    public static final byte STATUS_OK = (byte)0;


    private byte mGroup;
    private byte mAddr;
    private int  mCtrAddr;
    private byte mCtrAddrH;    //控制器机号高字节，即设备所属组号的高4位
    private byte mCtrlAddrL;   //控制器机号低字节，即设备地址字节
    private byte mSnr;
    private byte mCmdh;
    private byte mCmdl;
    private int  mCmd;
    private byte mCheckSum;
    private int  mLen;
    private int  mStep;
    private boolean mModify;    //遇到≥0xF0的数据需要合并或拆分
    private ByteBuffer mByteBuf;    //保存数据的缓冲区
    private boolean mReady;


    public UartReaderData() {
        mByteBuf = ByteBuffer.allocate(MAX_LEN);
    }

    public byte getGroup() {
        return mGroup;
    }

    public void setGroup(byte group) {
        mGroup = group;
    }

    public byte getAddr() {
        return mAddr;
    }

    public void setAddr(byte addr) {
        mAddr = addr;
    }

    public int getCtrAddr() {
        return mCtrAddr;
    }

    public void setCtrAddr(int mCtrAddr) {
        this.mCtrAddr = mCtrAddr;
    }

    public byte getCtrAddrH() {
        return mCtrAddrH;
    }

    public void setCtrAddrH(byte mCtrAddrH) {
        this.mCtrAddrH = mCtrAddrH;
    }

    public byte getCtrlAddrL() {
        return mCtrlAddrL;
    }

    public void setCtrlAddrL(byte mCtrlAddrL) {
        this.mCtrlAddrL = mCtrlAddrL;
    }

    public byte getSnr() {
        return mSnr;
    }

    public void setSnr(byte snr) {
        mSnr = snr;
    }

    public byte getCmdh() {
        return mCmdh;
    }

    public void setCmdh(byte cmdh) {
        mCmdh = cmdh;
    }

    public byte getCmdl() {
        return mCmdl;
    }

    public void setCmdl(byte cmdl) {
        mCmdl = cmdl;
    }

    public int getCmd() {
        return mCmd;
    }

    public void setCmd(int mCmd) {
        this.mCmd = mCmd;
    }

    public byte getCheckSum() {
        return mCheckSum;
    }

    public void clearCheckSum() {
        mCheckSum = 0;
    }

    public void calcCheckSum(byte checkSum) {
        mCheckSum ^= checkSum;
    }

    public int getLen() {
        return mLen;
    }

    public void setLen(int len) {
        mLen = len;
    }

    public int getStep() {
        return mStep;
    }

    public void setStep(int step) {
        mStep = step;
    }

    public void increaseStep() {
        mStep++;
    }

    public boolean isModify() {
        return mModify;
    }

    public void setModify(boolean modify) {
        mModify = modify;
    }

    public void put(byte byteData) {
        mByteBuf.put(byteData);
    }

    public boolean isReady() {
        return mReady;
    }

    public void setReady(boolean ready) {
        mReady = ready;
    }

    public byte[] getByteBuffer() {
        mByteBuf.flip();    //写完数据，需要开始读的时候，将postion复位到0，并将limit设为当前postion
//        int len = mByteBuf.limit() - mByteBuf.position();  //ByteBuffer的长度包含了校验字节
        int len = mLen;
        Log.i(TAG, "len:" + len);
        byte[] bytes = new byte[len];
        mByteBuf.get(bytes);
        mByteBuf.clear();

        return bytes;
    }

    @Override
    public String toString() {
        return "UartReaderData{" +
                "mGroup=" + mGroup +
                ", mAddr=" + mAddr +
                ", mCtrAddr=" + mCtrAddr +
                ", mCtrAddrH=" + mCtrAddrH +
                ", mCtrlAddrL=" + mCtrlAddrL +
                ", mSnr=" + mSnr +
                ", mCmdh=" + mCmdh +
                ", mCmdl=" + mCmdl +
                ", mCmd=" + mCmd +
                ", mCheckSum=" + mCheckSum +
                ", mLen=" + mLen +
                ", mStep=" + mStep +
                ", mModify=" + mModify +
                ", mByteBuf=" + mByteBuf +
                ", mReady=" + mReady +
                '}';
    }
}
