package com.ift.sw.fss;

import com.ift.sw.fss.cmd.FSSCmd;
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
            throw new FSSException(info.toString() + " channel register failed.");
        } finally {
            if (registerLock.isHeldByCurrentThread()) {
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
                        Tool.printInfoMsg("Remove channel:" + info.toString());
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
                        Tool.printInfoMsg("Remove channel:" + info.toString());
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

    /* For ext channel */
    public static String execute(String ip, Object serviceId, String cmd) throws IOException, FSSException {
        return execute(serviceId, FSSChannelInfo.EXT, cmd, EXT_TIMEOUT, ip);
    }

    /* General flow */
    public static String execute(Object serviceId, short type, String cmd, int timeout, String ip) throws FSSException, IOException {
        return execute(
                getKey(serviceId, ip, type),
                cmd,
                getTimeout(type, timeout)
        );
    }

    public static String execute(SelectionKey key, String cmd, int timeout) throws FSSException, IOException {
        FSSChannelInfo info = (FSSChannelInfo) key.attachment();
        Tool.printDebugMsg(info.toString());
        Tool.printDebugMsg("cmd:" + cmd);
        Tool.printDebugMsg("timeout:" + timeout);
        SocketChannel channel = (SocketChannel) key.channel();
        if (channel != null && channel.isConnected()) {
            try {
                info.lock();
                info.reset();
                long reqId = info.getReqId();
                channel.write(ByteBuffer.wrap(FSSCommander.generateFssPacket(cmd, reqId)));
                do {
                    if (!info.await(timeout)) {
                        info.reset();
                        throw new FSSException("Cmd timeout", FSSException.CMD_TIMEOUT);
                    }
                } while (!handleReadableData(key, reqId));
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
                if (info.getType() == FSSChannelInfo.EXT) {
                    key.channel().close();
                    key.cancel();
                }
            }
            Tool.printDebugMsg("output:" + info.getOutPutStr());
            return info.getOutPutStr();
        }
        throw new FSSException(info.toString() + " execute channel problem.");
    }

    private static int getTimeout(short type, int timeout) {
        if (timeout == FSSCmd.UNSET) {
            if (type == FSSChannelInfo.GET) {
                return GET_TIMEOUT;
            } else {
                return SET_TIMEOUT;
            }
        } else {
            return timeout;
        }
    }

    private static SelectionKey getKey(Object serviceId, String ip, short type) throws FSSException {
        type = FSSCommander.getExecuteCmdType(type);
        if (type == FSSChannelInfo.EXT) {
            FSSChannelInfo info = new FSSChannelInfo(serviceId, ip, FSSChannelInfo.EXT);
            try {
                return register(info);
            } catch (IOException e) {
                Tool.printDebugMsg("getExtKey failed." + info.getOutPutStr(), e);
                throw new FSSException("Could not find target socket channel.");
            }
        } else {
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
    }

    @Override
    public void run() {

        for (; ; ) {
            try {
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
                //For register channel
                try {
                    registerLock.lock();
                } finally {
                    registerLock.unlock();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Tool.printErrorMsg("Selector thread shutdown. Going to rebuild selector.", e);
                rebuildSelector();
            }
        }

    }

    public static void rebuildSelector() {
        Selector oldSelector = selector;
        Selector newSelector;
        for (; ; ) {
            try {
                newSelector = Selector.open();
                Tool.printDebugMsg("Open new selector succ.");
                /* Migrate all channel from old to new selector */
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
                        Tool.printDebugMsg("ConcurrentModificationException.");
                        // Probably due to concurrent modification of the key set.
                        continue;
                    }
                    break;
                }
                Tool.printDebugMsg("Migrate channels to new selector finished.");
                selector = newSelector;
                oldSelector.close();
            } catch (Exception e) {
                /* Rebuild failed keep rebuilding */
                try {
                    Tool.printErrorMsg("Rebuild selector failed. Retry after 60 secs.");
                    Thread.sleep(60000);
                } catch (InterruptedException e1) {
                    Tool.printErrorMsg("Rebuild selector failed. Interrupted.");
                    e1.printStackTrace();
                }
                continue;
            }
            break;
        }
    }

    public static boolean handleReadableData(SelectionKey key, long reqId) throws IOException, FSSException {
        FSSChannelInfo info = (FSSChannelInfo) key.attachment();
        ByteBuffer readBuff = info.getBuffer();
        SocketChannel channel = (SocketChannel) key.channel();
        readBuff.clear();

        //Read data and combine with left data
        byte[] tmpBuff = new byte[channel.read(readBuff)]; //Might happen remote force disconnect problem
        if (tmpBuff.length == 0) return false;
        readBuff.flip();
        readBuff.get(tmpBuff, 0, tmpBuff.length);
        int dataTotalLength = 0;
        byte[] preData = info.getResult();
        byte[] result;
        result = new byte[preData.length + tmpBuff.length];
        System.arraycopy(preData, 0, result, 0, preData.length);
        System.arraycopy(tmpBuff, 0, result, preData.length, tmpBuff.length);

        //Check header
        int headerStart;
        while ((result.length) > FSSCommander.BASESIZE) {
            if (!isSSL && result[0] == (byte) 0xAF && result[1] == (byte) 0xFA) {
                dataTotalLength = Tool.getInt(result, 8, 4);
                break;
            } else if ((headerStart = Tool.getArrIdx(result, FSSCommander.MAGIC)) != -1) {
                info.setResult(Arrays.copyOfRange(result, headerStart, result.length));
                Tool.printInfoMsg("Research header magic succ.");
            } else {
                Tool.printErrorMsg("Research header failed.");
                break;
            }
        }

        //Check data
        info.setResult(result);
        if (dataTotalLength != 0 && dataTotalLength <= result.length) {
            if (reqId == Tool.getInt(result, FSSCommander.REQ_ID_OFFSET, 4)) {
                return true;
            } else {
                //Discard previous resp
                info.setResult(Arrays.copyOfRange(result, dataTotalLength, result.length));
                Tool.printInfoMsg("handleReadData discard previous resp.");
                return handleReadableData(key, reqId); // little recursive...
            }
        }

        return false;
    }

    public static JSONObject getDebugInfo() {
        JSONObject obj = new JSONObject();
        JSONArray channels = new JSONArray();
        try {
            for (SelectionKey key : selector.keys()) {
                channels.put(key.attachment().toString());
            }
            obj.put("channels", channels);
            obj.put("registerLock", registerLock.isLocked());
        } catch (Exception e) {
            Tool.printErrorMsg("Debug log failed.", e);
        }
        return obj;
    }

}