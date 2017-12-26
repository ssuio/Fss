package com.ift.sw.fss;

import java.lang.annotation.Retention;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class FSSChannelInfo{
    public static final short GET = 0;
    public static final short SET = 1;
    public static final short EXT = 2;
    public static final short CLI = 3;
    private Object serviceId;
    private short type;
    private ByteBuffer buffer = ByteBuffer.allocate(6144);
    private byte[] result;
    private int dataLength;
    private ReentrantLock lock = new ReentrantLock();
    private Condition con = lock.newCondition();

    public FSSChannelInfo(Object serviceId, short type) {
        this.serviceId = serviceId;
        this.type = type;
    }

    public short getType() {
        return type;
    }

    public Object getObject() {
        return serviceId;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public byte[] getResult() {
        return result != null ? result : new byte[]{};
    }

    public String getOutPutStr() throws FSSException {
        return FSSCommander.formatOutPutStr(new String(result));
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

    public boolean await(int sec) throws InterruptedException {
        return con.await(sec, TimeUnit.SECONDS);
    }

    public void signalAll(){
        con.signalAll();
    }

    public void lock(){
        lock.lock();
    }

    public void unlock(){
        lock.unlock();
    }

    @Override
    public String toString() {
        return "FSSChannelInfo{" +
                "serviceId=" + serviceId +
                ", type=" + type +
                '}';
    }
}
