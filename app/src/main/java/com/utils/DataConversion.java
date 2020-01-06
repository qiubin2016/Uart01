package com.utils;

/**
 * Created by qb on 2018/7/8.
 */

public final class DataConversion {
    /**
     * To byte array byte [ ].
     *
     * @param hexString the hex string
     * @return the byte [ ]
     */
    public static byte[] toByteArray(String hexString) {
        if (0 == hexString.length())
        {
            return null;
        }
        hexString = hexString.toLowerCase();
        final byte[] byteArray = new byte[hexString.length() >> 1];
        int index = 0;
        for (int i = 0; i < hexString.length(); i++) {
            if (index  > hexString.length() - 1)
                return byteArray;
            byte highDit = (byte) (Character.digit(hexString.charAt(index), 16) & 0xFF);
            byte lowDit = (byte) (Character.digit(hexString.charAt(index + 1), 16) & 0xFF);
            byteArray[i] = (byte) (highDit << 4 | lowDit);
            index += 2;
        }
        return byteArray;
    }


    /**
     * byte[] to Hex string.
     *
     * @param byteArray the byte array
     * @return the string
     */

    public static String toHexString(byte[] byteArray, boolean withSpaces) {
        return toHexString(byteArray, byteArray.length, withSpaces);
    }

    public static String toHexString(byte[] byteArray, int length, boolean withSpaces) {
        final StringBuilder hexString = new StringBuilder("");
        if (byteArray == null || length <= 0)
            return null;
        for (int i = 0; i < length; i++) {
            int v = byteArray[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                hexString.append(0);
            }
            hexString.append(hv);
            if (withSpaces) {
                hexString.append(" ");
            }
        }
        return hexString.toString().toLowerCase();
    }

    public static String byteArrayToIp(byte[] byteArray) {
        int i = 0;
        StringBuffer strBuffer = new StringBuffer("");
        strBuffer.setLength(0);     //清空
        strBuffer.append(String.valueOf(byteArray[i++]));
        strBuffer.append(".");
        strBuffer.append(String.valueOf(byteArray[i++]));
        strBuffer.append(".");
        strBuffer.append(String.valueOf(byteArray[i++]));
        strBuffer.append(".");
        strBuffer.append(String.valueOf(byteArray[i++]));
        return strBuffer.toString();
    }
}
