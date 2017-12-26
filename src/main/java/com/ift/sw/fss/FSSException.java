package com.ift.sw.fss;

public class FSSException extends Exception{
    public static final int RESULT_UNKNOWN_PARAM = 0x01;
    public static final int CMD_TIMEOUT = 0x02;
    private int errorCode;

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

    public int getErrorCode(){return errorCode;}

    @Override
    public String toString() {
        return "FSSException{" +
                "errorCode=" + errorCode +
                '}';
    }
}
