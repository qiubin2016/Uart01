package com.utils;

import android.util.Log;

public class ErrorCode {
    public static final String TAG = "ErrorCode";
    public static final String ERROR_CODE_CHECK_SUM_FAILED = "check sum failed!";
    public static final String ERROR_CODE_MAX_LEN_LIMITED = "max len limited!";
    public static final String ERROR_CODE_GROUP_UNMATCHED = "group unmatched!";
    public static final String ERROR_CODE_ADDR_UNMATCHED = "addr unmatched!";
    public static final String ERROR_CODE_DEVICE_TYPE_UNMATCHED = "device type unmatched!";


    private String mErrorCode;

    public void setErrorCode(String errorCode) {
        mErrorCode = errorCode;
    }

    public String getErrorCode() {
//        Log.i(tag, "error code: " + mErrorCode);
        return mErrorCode;
    }

}
