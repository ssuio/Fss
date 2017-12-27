package com.ift.sw.fss;

public class FSSException extends Exception {
    public static final int RESULT_UNKNOWN_PARAM = 0x01;
    public static final int CMD_TIMEOUT = 0x02;
    public static final int REMOTE_FORCE_DISCONNECT = 0x03;
    private int errorCode;
    private FSSChannelInfo info;

    public FSSException(String msg) {
        super(msg);
        Tool.printErrorMsg(msg, this);
    }

    public FSSException(String msg, int errorCode) {
        super(msg);
        this.errorCode = errorCode;
    }

    public FSSException(int errorCode) {
        super();
        this.errorCode = errorCode;
    }

    public FSSException(int errorCode, FSSChannelInfo info) {
        super();
        this.errorCode = errorCode;
        this.info = info;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public FSSChannelInfo getInfo() {
        return info;
    }

    @Override
    public String toString() {
        return "FSSException{" +
                "errorCode=" + errorCode +
                '}';
    }
}
