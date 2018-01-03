package com.ift.sw.fss;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

import static java.nio.channels.SelectionKey.OP_READ;

public class FSSSocketManager implements Runnable {
    public static final int FSSCMD_PORT = 6100;
    public static final int FSSCMD_SSL_PORT = 6101;
    public static final int SET_TIMEOUT = 60 * 3;
    public static final int GET_TIMEOUT = 60;
    public static final int EXT_TIMEOUT = 60;
    public static boolean isSSL;
    public static volatile Selector selector;
    private static ReentrantLock registerLock = new ReentrantLock();
    private static FSSSocketManager socketMgr = new FSSSocketManager();

    static {
        try {
            selector = Selector.open();
            new Thread(socketMgr, "FssSocketMgr").start();
        } catch (IOException e) {
            e.printStackTrace();
            Tool.printErrorMsg("Selector open failed.", e);
        }
    }

    public static SelectionKey register(Object info) throws IOException, FSSException {
        SelectionKey key = null;
        try {
            SocketChannel channel = SocketChannel.open(new InetSocketAddress(((FSSChannelInfo) info).getIp(), isSSL ? FSSCMD_SSL_PORT : FSSCMD_PORT));
            channel.configureBlocking(false);
            registerLock.lock();
            selector.wakeup();
            key = channel.register(selector, OP_READ, info);
            Tool.printDebugMsg(info.toString() + " register succ.");
        } catch (IOException e) {
            throw new FSSException(info.toString()+" channel register failed.");
        } finally {
            if(registerLock.isHeldByCurrentThread()){
                registerLock.unlock();
            }
        }
        return key;
    }

    public static void close(Object serviceId) {
        for (; ; ) {
            try {
                for (SelectionKey key : selector.keys()) {
                    FSSChannelInfo info = (FSSChannelInfo) key.attachment();
                    if (serviceId == info.getServiceIdObj()) {
                        key.channel().close();
                    }
                }
            } catch (ConcurrentModificationException e) {
                Tool.printDebugMsg("Close channel ConcurrentModificationException.", e);
                continue;
            } catch (IOException e) {
                e.printStackTrace();
                Tool.printDebugMsg("Close channel IOException.", e);
                continue;
            }
            break;
        }
    }

    public static void closeSingle(Object attachment) {
        for (; ; ) {
            try {
                for (SelectionKey key : selector.keys()) {
                    FSSChannelInfo info = (FSSChannelInfo) key.attachment();
                    if (attachment == info) {
                        key.channel().close();
                    }
                }
            } catch (ConcurrentModificationException e) {
                Tool.printDebugMsg("Close channel ConcurrentModificationException.", e);
                continue;
            } catch (IOException e) {
                e.printStackTrace();
                Tool.printDebugMsg("Close channel IOException.", e);
                continue;
            }
            break;
        }
    }

    public static String execute(Object serviceId, short type, String cmd) throws FSSException {
        SelectionKey key = getKey(serviceId, type);
        FSSChannelInfo info = (FSSChannelInfo) key.attachment();
//        Tool.printErrorMsg(info.toString());
            SocketChannel channel = (SocketChannel) key.channel();
            if (channel != null && channel.isConnected()) {
                try {
                    info.lock();
                    info.reset();
//                    Tool.printErrorMsg(" write: "+ cmd);
                    long reqId = info.getReqId();
                    channel.write(ByteBuffer.wrap(FSSCommander.generateFssPacket(cmd, reqId)));
                    do {
                        if (!info.await(type == FSSChannelInfo.GET ? GET_TIMEOUT : SET_TIMEOUT)) {
                            info.reset();
                            throw new FSSException("Cmd timeout", FSSException.CMD_TIMEOUT);
                        }
                    } while (!handleReadableData(key, reqId));
//                    Tool.printErrorMsg(" read end.");
                } catch (FSSException e) {
                    throw e;
                } catch (IOException e) { //Maybe is remote side force disconnect.
                    if (info.getType() == FSSChannelInfo.GET || info.getType() == FSSChannelInfo.SET) {
                        throw new FSSException(FSSException.REMOTE_FORCE_DISCONNECT, info);
                    }
                } catch (InterruptedException e) {
                    throw new FSSException("handleReadData failed." + e.toString());
                } finally {
                    info.signalAll();
                    info.unlock();
                }
                return info.getOutPutStr();
            }
        throw new FSSException(info.toString() + " execute channel problem.");
    }

    public static String execute(String ip, Object serviceId, String cmd) throws IOException, InterruptedException, FSSException {
        FSSChannelInfo info = new FSSChannelInfo(serviceId, ip, FSSChannelInfo.EXT);
        SelectionKey key = register(info);
        try {
            info.lock();
            info.reset();
            long reqId = info.getReqId();
            ((SocketChannel) key.channel()).write(ByteBuffer.wrap(FSSCommander.generateFssPacket(cmd, reqId)));
            do {
                if (!info.await(EXT_TIMEOUT)) {
                    throw new FSSException("Cmd timeout", FSSException.CMD_TIMEOUT);
                }
            } while (!handleReadableData(key, reqId));
        } catch (FSSException e) {
            throw e;
        } catch (Exception e) {
            Tool.printErrorMsg(info.toString() + " " + cmd + " handleReadableData failed.", e);
        } finally {
            info.signalAll();
            info.unlock();
            key.channel().close();
            key.cancel();
        }
        return info.getOutPutStr();
    }

    private static SelectionKey getKey(Object serviceId, short type) throws FSSException {
        Iterator<SelectionKey> it = selector.keys().iterator();
        while (it.hasNext()) {
            SelectionKey key = it.next();
            FSSChannelInfo info = (FSSChannelInfo) key.attachment();
            if (info instanceof FSSChannelInfo) {
                if (info.getServiceIdObj() == serviceId && info.getType() == type) {
                    return key;
                }
            }
        }
        throw new FSSException("Could not find target socket channel.");
    }

    @Override
    public void run() {
        try {
            for (; ; ) {
                while (selector.select() > 0) {
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        if (!key.isValid()) {
                            continue;
                        }
                        if (key.isReadable()) {
                            FSSChannelInfo info = (FSSChannelInfo) key.attachment();
                            try {
                                info.lock();
                            } finally {
                                info.signalAll();
                                info.unlock();
                            }
                        }
                        iterator.remove();
                    }
                }
                try {
                    registerLock.lock();
                } finally {
                    registerLock.unlock();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Tool.printErrorMsg("Selector thread shutdown. ", e);
        }
    }

    public static void rebuildSelector() throws IOException {
        final Selector oldSelector = selector;
        final Selector newSelector;
        newSelector = Selector.open();

        for (; ; ) {
            try {
                for (SelectionKey key : oldSelector.keys()) {
                    Object info = key.attachment();
                    if (!key.isValid() || key.channel().keyFor(newSelector) != null) {
                        continue;
                    }
                    int interestOps = key.interestOps();
                    key.cancel();
                    key.channel().register(newSelector, interestOps, info);
                }
            } catch (ConcurrentModificationException e) {
                // Probably due to concurrent modification of the key set.
                continue;
            }
            break;
        }
        selector = newSelector;
        oldSelector.close();
    }

    public static boolean handleReadableData(SelectionKey key, long reqId) throws IOException, FSSException {
        FSSChannelInfo info = (FSSChannelInfo) key.attachment();
        ByteBuffer readBuff = info.getBuffer();
        SocketChannel channel = (SocketChannel) key.channel();
        readBuff.clear();

        //Read data and combine with left data
        byte[] tmpBuff = new byte[ channel.read(readBuff)]; //Might happen remote force disconnect problem
//        Tool.printErrorMsg("length:"+tmpBuff.length);
        if(tmpBuff.length == 0) return false;
        readBuff.flip();
        readBuff.get(tmpBuff, 0, tmpBuff.length);
//        Tool.printErrorMsg(Tool.getHexBytesString(tmpBuff));
        int dataTotalLength = 0;
        byte[] preData = info.getResult();
        byte[] result;
        result = new byte[preData.length + tmpBuff.length];
        System.arraycopy(preData, 0, result, 0, preData.length);
        System.arraycopy(tmpBuff, 0, result, preData.length, tmpBuff.length);

        //Check header
        int headerStart;
        while((result.length) > FSSCommander.BASESIZE){
            if(!isSSL && result[0] == (byte) 0xAF && result[1] == (byte) 0xFA){
                dataTotalLength = Tool.getInt(result, 8, 4);
                break;
            }else if((headerStart = Tool.getArrIdx(result, FSSCommander.MAGIC)) != -1){
                info.setResult(Arrays.copyOfRange(result, headerStart, result.length));
                Tool.printInfoMsg("Research header magic succ.");
            }else{
                Tool.printErrorMsg("Research header failed.");
                break;
            }
        }

        //Check data
        info.setResult(result);
        if(dataTotalLength != 0 && dataTotalLength <= result.length){
            if(reqId == Tool.getInt(result, FSSCommander.REQ_ID_OFFSET, 4)){
//                Tool.printErrorMsg("Finish dataTotalLength:"+ dataTotalLength + " resultLength:" + result.length);
//                Tool.printErrorMsg(info.getOutPutStr());
                return true;
            }else{
                //Discard previous resp
                info.setResult(Arrays.copyOfRange(result, dataTotalLength, result.length));
                Tool.printInfoMsg("handleReadData discard previous resp.");
                return handleReadableData(key, reqId);
            }
        }

        return false;
    }

    public static JSONObject getDebugInfo() {
        JSONObject obj = new JSONObject();
        JSONArray channels = new JSONArray();
        try{
            for (SelectionKey key : selector.keys()) {
                channels.put(key.attachment().toString());
            }
            obj.put("channels", channels);
            obj.put("registerLock", registerLock.isLocked());
        }catch (Exception e){
            Tool.printErrorMsg("Debug log failed.", e);
        }
        return obj;
    }

}