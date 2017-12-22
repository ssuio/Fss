package com.ift.sw.fss;

import com.ift.sw.fss.cmd.FSSCmd;

import java.io.IOException;

public class FSSAgent {
    private final FSSSocketManager sockMgr;
    private String serviceId;
    private String cliver;
    public FSSAgent(String ip, String serviceId, boolean isSSL) throws IOException, FSSException {
        this.serviceId = serviceId;
        this.sockMgr = new FSSSocketManager(ip, serviceId, isSSL);
        new Thread(this.sockMgr, "Thread-"+serviceId).start();
//        this.execute(FSSCommander.generateFSSCmd(this, "cliver"));
    }

    public String execute(String cmd, short cmdType, String slot) throws FSSException {
        try {
            String finalCmd = FSSCommander.formatFSSCmd(cmd, slot, serviceId);
            return sockMgr.execute(serviceId, cmdType ,finalCmd);
        } catch (Exception e) {
            throw new FSSException("Fss cmd execute failed.");
        }
    }

    public String execute(String cmd, String slot) throws FSSException {
        try {
            String finalCmd = FSSCommander.formatFSSCmd(cmd, slot, serviceId);
            return sockMgr.execute(serviceId, FSSChannelInfo.SET ,finalCmd);
        } catch (Exception e) {
            throw new FSSException("Fss cmd execute failed.");
        }
    }

    public String execute(FSSCmd fssCmd)throws FSSException{
        return execute(fssCmd.getCmd(), fssCmd.getCmdType(), fssCmd.getSlot());
    }

    public String getCliver() {
        return cliver;
    }

    public void setCliver(String cliver) {
        this.cliver = cliver;
    }
}
