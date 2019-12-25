package com.utils;

public class ParseRcvData<T> {
    private CmdCallback<T> mCmdCallback;

    public ParseRcvData(CmdCallback<T> cmdCallback) {
        this.mCmdCallback = cmdCallback;
    }

    public void put(final byte[] buffer, final int size) {

        for (byte byteData : buffer) {

        }
    }

    //接口  解析到合法数据包后回调onReceive()；解析错误回调onError()
    public interface CmdCallback<T>{
        void onReceive(T cmd);

        void onError(int errCode);
    }
}
