package com.ift.sw.fss;

import com.ift.sw.fss.cmd.FSSCmd;

import java.io.IOException;

public class FSSAgent {
    private volatile static FSSSocketManager sockMgr = new FSSSocketManager();
    private static Thread thread;
    private String serviceId;
    private String ip;
    private String cliver;
    private boolean isCLIMode;

    static {
        start();
    }

    public static void start(){
        if(sockMgr==null){
            Tool.printInfoMsg("new SocketManager instance.");
            sockMgr = new FSSSocketManager();
        }
        if( thread == null || !thread.isAlive()){
            Tool.printInfoMsg("Start SocketMgr thread.");
            thread = new Thread(sockMgr, "SocketMgr");
            thread.start();
        }
    }

    public FSSAgent(String ip, String serviceId, boolean isCLIMode) throws IOException, FSSException, InterruptedException {
        this.serviceId = serviceId;
        this.ip = ip;
        this.isCLIMode = isCLIMode;
//        if(!isCLIMode){ //CLIMode won't generate get/set socket.
            FSSSocketManager.register(ip, new FSSChannelInfo(serviceId, FSSChannelInfo.GET));
            FSSSocketManager.register(ip, new FSSChannelInfo(serviceId, FSSChannelInfo.SET));
            Tool.printInfoMsg("Socket ("+serviceId+") GET&SET registered.");
//        }else{
//            FSSSocketManager.register(ip, new FSSChannelInfo(serviceId, FSSChannelInfo.CLI));
//        }
        //Get NAS cli version
        FSSCommander.generateFSSCmd(this, "cliver").execute();
    }

    public String execute(String cmd, short cmdType, String slot) throws FSSException {
        try {
            String finalCmd = FSSCommander.formatFSSCmd(cmd, slot, serviceId);
            return cmdType==FSSChannelInfo.EXT?FSSSocketManager.execute(ip, serviceId, finalCmd):FSSSocketManager.execute(serviceId, cmdType ,finalCmd);
        } catch (FSSException e){
            throw e;
        } catch (Exception e) {
            throw new FSSException("Fss cmd execute failed.");
        }
    }

    public String execute(String cmd, String slot) throws FSSException {
        try {
            String finalCmd = FSSCommander.formatFSSCmd(cmd, slot, serviceId);
            return FSSSocketManager.execute(ip, serviceId, finalCmd);
        } catch (Exception e) {
            throw new FSSException("Fss cmd execute failed.");
        }
    }

    public String execute(FSSCmd fssCmd)throws FSSException{
        return execute(fssCmd.getCmd(), fssCmd.getCmdType(), fssCmd.getSlot());
    }

    public void close(){
        FSSSocketManager.close(serviceId);
    }

    public static void forceClose(Object serviceId){
        FSSSocketManager.close(serviceId);
    }

    public String getCliver() {
        return cliver;
    }

    public void setCliver(String cliver) {
        this.cliver = cliver;
    }
}
