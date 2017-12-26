package com.ift.sw.fss;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

import static java.nio.channels.SelectionKey.OP_READ;

public class FSSSocketManager implements Runnable {
    public static final int FSSCMD_PORT = 6100;
    public static final int FSSCMD_SSL_PORT = 6101;
    public static final int SET_TIMEOUT = 60 * 3;
    public static final int GET_TIMEOUT = 60;
    public static final int EXT_TIMEOUT = 60;
    public static boolean isSSL;
    public static volatile Selector selector;

    static {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
            Tool.printErrorMsg("Selector open failed.", e);
        }
    }

    public static SelectionKey register(String ip, Object info) throws IOException, FSSException {
        SelectionKey key = null;
        try {
            SocketChannel channel = SocketChannel.open(new InetSocketAddress(ip, isSSL ? FSSCMD_SSL_PORT : FSSCMD_PORT));
            channel.configureBlocking(false);
            key = channel.register(selector, OP_READ, info);
            Tool.printDebugMsg(info.toString() + " register succ.");
        } catch (IOException e) {
            Tool.printErrorMsg(info.toString() + " register failed.", e);
            throw new FSSException("Ext channel register failed.");
        }
        return key;
    }

    public static void close(Object attachment) {
        for (; ; ) {
            try {
                for (SelectionKey key : selector.keys()) {
                    FSSChannelInfo info = (FSSChannelInfo) key.attachment();
                    if (attachment == info.getObject()) {
                        key.cancel();
                    }
                }
            } catch (ConcurrentModificationException e) {
                Tool.printDebugMsg("Close channel ConcurrentModificationException.");
                continue;
            }
            break;
        }
    }

    public static String execute(Object serviceId, short type, String cmd) throws IOException, InterruptedException, FSSException {
        SelectionKey key = getKey(serviceId, type);
        FSSChannelInfo info = (FSSChannelInfo) key.attachment();
        synchronized (info) {
            SocketChannel channel = (SocketChannel) key.channel();
            if (channel != null && channel.isConnected()) {
                try {
                    info.lock();
                    channel.write(ByteBuffer.wrap(FSSCommander.generateFssPacket(cmd)));
                    do {
                        if (!info.await(type == FSSChannelInfo.GET ? GET_TIMEOUT : SET_TIMEOUT)) {
                            throw new FSSException("Cmd timeout", FSSException.CMD_TIMEOUT);
                        }
                    } while (!handleReadableData(key));
                } catch (FSSException e) {
                    throw e;
                } catch (Exception e) {
                    Tool.printErrorMsg(info.toString() + " " + cmd + " handleReadableData failed.", e);
                } finally {
                    info.signalAll();
                    info.unlock();
                }
                return info.getOutPutStr();
            }
        }
        throw new FSSException(info.toString() + " execute channel problem.");
    }

    public static String execute(String ip, Object serviceId, String cmd) throws IOException, InterruptedException, FSSException {
        FSSChannelInfo info = new FSSChannelInfo(serviceId, FSSChannelInfo.EXT);
        SelectionKey key = register(ip, info);
        try {
            info.lock();
            ((SocketChannel) key.channel()).write(ByteBuffer.wrap(FSSCommander.generateFssPacket(cmd)));
            do {
                if (!info.await(EXT_TIMEOUT)) {
                    throw new FSSException("Cmd timeout", FSSException.CMD_TIMEOUT);
                }
            } while (!handleReadableData(key));
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

    public static SelectionKey getKey(Object serviceId, short type) throws FSSException {
        Iterator<SelectionKey> it = selector.keys().iterator();
        while (it.hasNext()) {
            SelectionKey key = it.next();
            FSSChannelInfo info = (FSSChannelInfo) key.attachment();
            if (info instanceof FSSChannelInfo) {
                if (info.getObject() == serviceId && info.getType() == type) {
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
                while (selector.selectNow() > 0) {
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
                                info.signalAll();
                            } finally {
                                info.unlock();
                            }
                        }
                        iterator.remove();
                    }
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

    public static boolean handleReadableData(SelectionKey key) throws IOException, FSSException {
        FSSChannelInfo info = (FSSChannelInfo) key.attachment();
        ByteBuffer readBuff = info.getBuffer();
        SocketChannel channel = (SocketChannel) key.channel();
        readBuff.clear();
        byte[] tmpBuff = new byte[channel.read(readBuff)]; //Might happen remote force disconnect problem
        readBuff.flip();
        readBuff.get(tmpBuff, 0, tmpBuff.length);
        if (tmpBuff.length == 16 && !isSSL && tmpBuff[0] == (byte) 0xAF && tmpBuff[1] == (byte) 0xFA) {
            info.reset();
            info.setDataLength(Tool.getInt(tmpBuff, 8, 4));
        } else {
            int dataTotalLength = info.getDataLength();
            byte[] preData = info.getResult();
            byte[] result;
            if (preData.length == 0) {
                result = tmpBuff;
                System.arraycopy(tmpBuff, 0, result, 0, tmpBuff.length);
            } else {
                result = new byte[preData.length + tmpBuff.length];
                System.arraycopy(preData, 0, result, 0, preData.length);
                System.arraycopy(tmpBuff, 0, result, preData.length, tmpBuff.length);
            }
            info.setResult(result);
            if (dataTotalLength - FSSCommander.BASESIZE <= result.length) {
                return true;
            }
        }
        return false;
    }

}