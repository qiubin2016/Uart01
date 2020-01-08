package com.utils;

import android.util.Log;

import com.itlong.java.EnDecrypt;

import java.util.Arrays;
import java.util.Random;

public class RandomNumber {
    private static final String TAG = "RandomNumber";
    public static final int CONST_NUM = 16;
    private static Random mRandom;
    private static byte[] mBytes;
    private static byte[] mEncryptBytes;

    public static void getRandomBytes(byte[] bytes) {
        //生成随机数
//        mRandom = new Random();
//        mRandom.nextBytes(bytes);
        //调试  TODO
        byte[] bytesRandom = {(byte)0xB6, (byte)0x06, (byte)0xA9, (byte)0xB2, (byte)0x61, (byte)0xB0, (byte)0x57, (byte)0xAA, (byte)0x12,
                (byte)0xB3, (byte)0x7E, (byte)0xC1, (byte)0x61, (byte)0xAC, (byte)0x5A, (byte)0xAC};
        System.arraycopy(bytesRandom, 0, bytes, 0, bytesRandom.length);  //数组拷贝
        mBytes = new byte[bytes.length];
        mBytes = bytes;  //保存生成的随机数
        Log.i(TAG, "byte[]:" + DataConversion.toHexString(mBytes, mBytes.length, true));
    }

    public static boolean encryptRandomBytes(byte[] src, byte[] dst) {
        boolean ret = false;
        byte[] bytesEncrypt;

        bytesEncrypt = EnDecrypt.encrypt(EnDecrypt.Algorithm.ALGORITHM_SM4, EnDecrypt.KeyId.KEY_ID_0, src, src.length);
        if (null != bytesEncrypt) {
            System.arraycopy(bytesEncrypt, 0, dst, 0, bytesEncrypt.length);  //数组拷贝
            ret = true;
        }

        return ret;
    }

    public static boolean verifyRandonBytes(byte[] bytes) {
        if (null == mEncryptBytes) {
            mEncryptBytes = new byte[CONST_NUM];
            encryptRandomBytes(mBytes, mEncryptBytes);
        }
        Log.i(TAG, "verifyRandonBytes");
        return (Arrays.equals(bytes, mEncryptBytes));
    }
}
