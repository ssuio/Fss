package com.ift.sw.fss;

import java.nio.ByteBuffer;

public class FSSChannelInfo implements Runnable {
    public static final short GET = 0;
    public static final short SET = 1;
    public static final short EXT = 2;
    private Object raidCmd;
    private short type;
    private ByteBuffer buffer = ByteBuffer.allocate(6144);
    private byte[] result;
    private int dataLength;

    public FSSChannelInfo(Object raidCmd, short type) {
        this.raidCmd = raidCmd;
        this.type = type;
    }

    public short getType() {
        return type;
    }

    public Object getObject() {
        return raidCmd;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public byte[] getResult() {
        return result != null ? result : new byte[]{};
    }

    public String getOutPutStr() throws FSSException {
        String resultStr = new String(result);
        int startIdx = resultStr.indexOf("\r\n{");
        int endIdx = resultStr.lastIndexOf("}\r\n");
        if (startIdx==-1 || endIdx==-1){
            throw new FSSException("getOutPutStr failed");
        }
        return resultStr.substring(startIdx+2, endIdx+1);
    }

    public void setResult(byte[] result) {
        this.result = result;
    }

    public void setDataLength(int dataLength) {
        this.dataLength = dataLength;
    }

    public int getDataLength() {
        return this.dataLength;
    }

    public void reset() {
        this.dataLength = 0;
        this.result = new byte[]{};
    }

    @Override
    public void run() {

    }
}
