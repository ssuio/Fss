package com.ift.sw.fss;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class FSSChannelInfo extends ReentrantLock {
    public static final short GET = 0;
    public static final short SET = 1;
    public static final short EXT = 2;
    public static final short CLI = 3;
    private Object serviceId;
    private String ip;
    private short type;
    private ByteBuffer buffer = ByteBuffer.allocate(6144);
    private byte[] result;
    private Condition con = this.newCondition();
    private long reqId = 1;

    public FSSChannelInfo(Object serviceId, String ip, short type) {
        this.ip = ip;
        this.serviceId = serviceId;
        this.type = type;
    }

    public short getType() {
        return type;
    }

    public Object getServiceIdObj() {
        return serviceId;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public byte[] getResult() {
        return result != null ? result : new byte[]{};
    }

    public String getOutPutStr() throws FSSException {
        return FSSCommander.formatOutPutStr(new String(Arrays.copyOfRange(this.result, 16, this.result.length)));
    }

    public void setResult(byte[] result) {
        this.result = result;
    }

    public void reset() {
        this.result = new byte[]{};
    }

    public boolean await(int sec) throws InterruptedException {
        return con.await(sec, TimeUnit.SECONDS);
    }

    public void signalAll() {
        con.signalAll();
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public long getReqId(){
        return reqId++;
    }

    @Override
    public String toString() {
        return "FSSChannelInfo{" +
                "serviceId=" + serviceId +
                "ip=" + ip +
                ", type=" + type +
                '}';
    }
}
