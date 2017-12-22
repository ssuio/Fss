package com.ift.sw.fss;

public class Tool {
    public static int getInt(byte[] b, int offset, int len) {
        int v = 0;
        if (b == null) {
            v = 0;
        }
        else if ((b.length < len) || (b.length < offset + len)) {
            v = 0;
        }
        else {
            int i = 0;
            for (i = 0; (i < 4) && (i < len); i++) {
                v |= ((b[offset + i] & 0xFF) << (8 * i));
            }
        }
        return v;
    }
    public static void setValue(byte[] buf, byte[] value, int offset, int len) {
        try {
            System.arraycopy(value, 0, buf, offset, len);
        }catch(ArrayIndexOutOfBoundsException e) {
            // System.err.println("buf:"+buf.length+" value:"+value.length+" offset:"+offset+" len:"+len);
            throw e;
        }
    }
    public static void setValue(byte[] buf, int value, int offset, int len) {
        for (int i = 0; i < len; i++) {
            buf[offset + i] = (byte) (value & 0xff);
            value >>= 8;
        }
    }
    public static void setValue(byte[] buf, long value, int offset, int len) {
        for (int i = 0; i < len; i++) {
            buf[offset + i] = (byte) (value & 0xff);
            value >>= 8;
        }
    }
    public static String getHexBytesString(byte[] b) {
        StringBuffer sb = new StringBuffer();
        for (int i = b.length - 1; i >= 0; i--) {
            sb.append(Integer.toHexString((int) (b[i] & 0x0F)));
            sb.append(Integer.toHexString((int) ((b[i] & 0xF0) >> 4)));
        }
        // reverse it!
        sb.reverse();
        return sb.toString().toUpperCase();
    }
}
