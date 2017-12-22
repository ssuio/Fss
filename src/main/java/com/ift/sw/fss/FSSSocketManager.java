package com.ift.sw.fss;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class FSSSocketManager implements Runnable {
    public static final int FSSCMD_PORT = 6100;
    public static final int FSSCMD_SSL_PORT = 6101;
    public static boolean isSSL;
    public static Selector selector;
    private String ip;

    static{
        try {
            selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
            //Log
        }
    }

    public FSSSocketManager(String ip, Object raidCmd, boolean isSSL) throws IOException {
        this.ip = ip;
        this.isSSL = isSSL;
        register(this.ip, new FSSChannelInfo(raidCmd, FSSChannelInfo.GET));
        register(this.ip, new FSSChannelInfo(raidCmd, FSSChannelInfo.SET));
    }

    public SelectionKey register(String ip, Object attachment) throws IOException {
        SocketChannel channel = SocketChannel.open(new InetSocketAddress(ip, this.isSSL?FSSCMD_SSL_PORT:FSSCMD_PORT));
        channel.configureBlocking(false);
        SelectionKey key = channel.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ, attachment);
        return key;
    }

    public String execute(Object raidCmd, short type, String cmd) throws IOException, InterruptedException, FSSException {
        if (type == FSSChannelInfo.GET || type == FSSChannelInfo.SET) {
            SelectionKey key = getKey(raidCmd, type);
            FSSChannelInfo info = (FSSChannelInfo) key.attachment();
            synchronized (info) {
                SocketChannel channel = (SocketChannel) key.channel();
                if (channel != null && channel.isConnected()) {
                    channel.write(ByteBuffer.wrap(FSSCommander.generateFssPacket(cmd)));
                    do {
                        info.wait();
                    }while(!handleReadableData(key));
                    return info.getOutPutStr();
                }
            }
        } else if (type == FSSChannelInfo.EXT) {
            FSSChannelInfo info = new FSSChannelInfo(raidCmd, FSSChannelInfo.EXT);
            synchronized (info) {
                SelectionKey key = register(this.ip, info);
                SocketChannel channel = (SocketChannel) key.channel();
                try{
                    channel.write(ByteBuffer.wrap(FSSCommander.generateFssPacket(cmd)));
                    do {
                        info.wait();
                    }while(!handleReadableData(key));
                    return info.getOutPutStr();
                }finally {
                    key.channel().close();
                    key.cancel();
                }
            }
        }
        return "";
    }

    public static SelectionKey getKey(Object raidCmd, short type) {
        Iterator<SelectionKey> it = selector.keys().iterator();
        while (it.hasNext()) {
            SelectionKey key = it.next();
            if (key.attachment() instanceof FSSChannelInfo) {
                if (((FSSChannelInfo) key.attachment()).getObject() == raidCmd && ((FSSChannelInfo) key.attachment()).getType() == type) {
                    return key;
                }
            }
        }
        return null;
    }

    @Override
    public void run() {
        try {
            while (true) {
                while (selector.select() > 0) {
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        if (!key.isValid()){
                            continue;
                        }
                        if (key.isReadable()) {
                            FSSChannelInfo info = (FSSChannelInfo) key.attachment();
                            synchronized (info){
                                info.notifyAll();
                            }
                        } else if (key.isConnectable()) {
                            // connect(key);
                        }
                        iterator.remove();
                    }
                }
            }
        } catch (IOException e) {

        }
    }

    public boolean handleReadableData(SelectionKey key) {
        FSSChannelInfo info = (FSSChannelInfo) key.attachment();
        ByteBuffer readBuff = info.getBuffer();
        SocketChannel channel = (SocketChannel) key.channel();
        readBuff.clear();
        try {
            byte[] tmpBuff = new byte[channel.read(readBuff)];
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
