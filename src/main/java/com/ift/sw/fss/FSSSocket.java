package com.ift.sw.fss;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class FSSSocket implements Runnable{
    public static final int FSSCMD_PORT = 6100;
    public static final int FSSCMD_SSL_PORT = 6101;
    public static final int REQUEST_ID_OFFSET = 4;
    public static int TIMEOUT = 60000 * 3;

    private boolean isSSL = false;
    private String ip;
    private SocketChannel socketChannel;
    ByteBuffer rcvbuf = ByteBuffer.allocate(6144);
    ByteBuffer sendbuf = ByteBuffer.allocate(6144);

    public FSSSocket(String ip, boolean isSSL) throws FSSException {
        try {
            this.socketChannel = SocketChannel.open();
            this.isSSL = isSSL;
            this.ip = ip;
        } catch (IOException e) {
            throw new FSSException("FSS sock connect failed.");
        }
    }

    public void retry(){

    }

    @Override
    public void run() {
        try {
            socketChannel.connect(new InetSocketAddress(ip, isSSL?FSSCMD_SSL_PORT:FSSCMD_PORT));
            String msg = "";

            while (true) {

                // see if any message has been received
                rcvbuf = ByteBuffer.allocate(6144);
                while ((socketChannel.read(rcvbuf)) > 0) {
                    // flip the buffer to start reading
                    rcvbuf.flip();
//                    System.out.println(Tool.getHexBytesString(rcvbuf.array()));
                    System.out.println(Tool.getInt(rcvbuf.array(), 0 + 8, 4));
                    for(int i=0; i<rcvbuf.limit(); i++){
                        System.out.print(rcvbuf.get()+", ");
                    }
                    System.out.println();
                    msg += Charset.defaultCharset().decode(rcvbuf);
                }
                if (msg.length() > 0) {
                    System.out.println(msg);
                    msg = "";
                }
            }
        } catch (IOException e) {
        }
    }

    public void send(String cmd){
        ByteBuffer sendBuffer = ByteBuffer.wrap(FSSCommander.generateFssPacket("vpn config -z a@0"));
        try {
            socketChannel.write(sendBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
