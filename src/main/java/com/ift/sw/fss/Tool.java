package com.ift.sw.fss;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Tool {
    private static Logger log = Logger.getLogger("fss_log");
    // Print all msg using logger mechanism.
    public static void printDebugMsg(Object msg){
        log.debug(msg);
    }
    public static void printDebugMsg(Object msg, Throwable e){
        log.debug(msg , e);
    }
    public static void printInfoMsg(Object msg){
        log.info(msg);
    }
    public static void printInfoMsg(Object msg, Throwable e){
        log.info(msg, e);
    }
    public static void printWarnMsg(Object msg){
        log.warn(msg);
    }
    public static void printWarnMsg(Object msg, Throwable e){
        log.warn(msg, e);
    }
    public static void printErrorMsg(Object msg){
        log.error(msg);
    }
    public static void printErrorMsg(Object msg, Throwable e){
        log.error(msg , e);
    }
    public static void printFatalMsg(Object msg){
        log.fatal(msg);
    }
    public static void printFatalMsg(Object msg, Throwable e){
        log.fatal(msg , e);
    }

    static {
        try {
            PropertyConfigurator.configureAndWatch("fss_log.properties");
        } catch (Exception e) {}
    }

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
    public static int getArrIdx(byte[] source, byte[] target){
        String srcStr = new String(source);
        String tarStr = new String(target);
        return srcStr.indexOf(tarStr);
    }
}
