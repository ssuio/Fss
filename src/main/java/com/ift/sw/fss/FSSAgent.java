package com.ift.sw.fss;

import com.ift.sw.fss.cmd.FSSCmd;

import java.io.IOException;

public class FSSAgent {
    private String serviceId;
    private String ip;
    private String cliver;
    private boolean isCLIMode;
    private FSSRetryer retryer = new FSSRetryer();

    public FSSAgent(String ip, String serviceId, boolean isCLIMode) throws IOException, FSSException, InterruptedException {
        this.serviceId = serviceId;
        this.ip = ip;
        this.isCLIMode = isCLIMode;
        try {
            this.connect(FSSChannelInfo.GET);
            this.connect(FSSChannelInfo.SET);

            new Thread(retryer, "FSSRetryer-" + ip.replaceAll("\\.", "_")).start();

            //Get NAS cli version
            FSSCommander.generateFSSCmd(this, "cliver").execute();
        } catch (Exception e) {
            close();
            throw e;
        }
    }

    public void connect(short type) {
        FSSChannelInfo info = new FSSChannelInfo(serviceId, ip, type);
        try {
            FSSSocketManager.register(info);
        } catch (Exception e) {
            e.printStackTrace();
            retryer.addRetryInfo(info);
        }
        Tool.printInfoMsg(info.toString() + " registered.");
    }

    public String execute(String cmd, short cmdType, String slot) throws FSSException {
        try {
            String finalCmd = FSSCommander.formatFSSCmd(cmd, slot, serviceId);
            return cmdType == FSSChannelInfo.EXT ?
                    FSSSocketManager.execute(ip, serviceId, finalCmd) :
                    FSSSocketManager.execute(serviceId, cmdType, finalCmd);
        } catch (FSSException e) {
            if (e.getErrorCode() == FSSException.REMOTE_FORCE_DISCONNECT) {
                retryer.addRetryInfo(e.getInfo());
            }
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

    public String execute(FSSCmd fssCmd) throws FSSException {
        return execute(fssCmd.getCmd(), fssCmd.getCmdType(), fssCmd.getSlot());
    }

    public void close() {
        FSSSocketManager.close(serviceId);
        retryer.setRetry(false);
    }

    public static void forceClose(Object serviceId) {
        FSSSocketManager.close(serviceId);
    }

    public boolean isAlive() {
        return !retryer.isRetrying();
    }

    public String getCliver() {
        return cliver;
    }

    public void setCliver(String cliver) {
        this.cliver = cliver;
    }
}
