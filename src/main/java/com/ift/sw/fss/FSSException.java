package com.ift.sw.fss;

public class FSSException extends Exception{
    public static final int RESULT_UNKNOWN_PARAM = 0x01;
    private int errorCode;

    public FSSException(String msg) {
        super(msg);
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
}
