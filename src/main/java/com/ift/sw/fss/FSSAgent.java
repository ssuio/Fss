package com.ift.sw.fss;

import com.ift.sw.fss.cmd.FSSCmd;

import java.io.IOException;

public class FSSAgent {
    private String serviceId;
    private String ip;
    private String cliver;
    private FSSRetryer retryer = new FSSRetryer();
    public FSSTraveler traveler;

    public FSSAgent(String ip, String serviceId, FSSTraveler traveler) throws IOException, FSSException, InterruptedException {
        this.serviceId = serviceId;
        this.ip = ip;
        this.traveler = traveler;
        try {
            //Default connect two channels
            this.connect(FSSChannelInfo.GET);
            this.connect(FSSChannelInfo.SET);

            //Start retryer for this agent
            new Thread(retryer, "FSSRetryer-" + ip.replaceAll("\\.", "_")).start();

            //Get NAS cli version
            FSSCommander.generateFSSCmd(this, "cliver").execute();
            Tool.printDebugMsg("Get cliver finished.");
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
            Tool.printErrorMsg("Connect failed:" + info.toString(), e);
            retryer.addRetryInfo(info);
        }
        Tool.printInfoMsg(info.toString() + " registered.");
    }

    /* Get original response from NAS */
    public String execute(String cmd, String slot) throws FSSException {
        try {
            String finalCmd = FSSCommander.formatFSSCmd(cmd, slot, serviceId);
            return FSSSocketManager.execute(ip, serviceId, finalCmd);
        } catch (Exception e) {
            throw new FSSException("Fss cmd execute failed.");
        }
    }

    /* General flow */
    public String execute(String cmd, short cmdType, int timeout) throws FSSException {
        try {
            return FSSSocketManager.execute(serviceId, cmdType, cmd, timeout, ip);
        } catch (FSSException e) {
            if (e.getErrorCode() == FSSException.REMOTE_FORCE_DISCONNECT) {
                if (e.getInfo().getType() == FSSChannelInfo.GET || e.getInfo().getType() == FSSChannelInfo.SET) {
                    retryer.addRetryInfo(e.getInfo());
                }
            }
            throw e;
        } catch (Exception e) {
            throw new FSSException("Fss cmd execute failed.");
        }
    }

    /* General flow */
    public String execute(FSSCmd fssCmd) throws FSSException {
        try{
            /* Format cmd by slot */
            String cmd = fssCmd.getCmd();
            String slot = fssCmd.getSlot();
            String finalCmd = FSSCommander.formatFSSCmd(cmd, slot, serviceId);
            return execute(
                    finalCmd,
                    fssCmd.getCmdType(),
                    fssCmd.getTimeout()
            );
        }catch (FSSException e){
            if (e.getInfo().getType() == FSSChannelInfo.GET || e.getInfo().getType() == FSSChannelInfo.SET) {
                retryer.addRetryInfo(e.getInfo());
            }
            throw e;
        }catch (Exception e) {
            throw new FSSException("Fss cmd execute failed.");
        }
    }

    /* Without format cmd by slot, directly execute cmdStr */
    public String executeDirectly(FSSCmd fssCmd) throws FSSException {
        try {
            return execute(
                    serviceId,
                    fssCmd.getCmdType(),
                    fssCmd.getTimeout()
            );
        } catch (Exception e) {
            throw new FSSException("Execute cmd directly failed.");
        }
    }

    public void close() {
        FSSSocketManager.close(serviceId);
        retryer.setRetry(false);
    }

    public static void forceClose(Object serviceId) {
        FSSSocketManager.close(serviceId);
    }

    public boolean isAlive() {
        return !retryer.isRetrying() && retryer.isRetry();
    }

    public String getCliver() {
        return cliver;
    }

    public void setCliver(String cliver) {
        this.cliver = cliver;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    @Override
    public String toString() {
        return "FSSAgent{" +
                "serviceId='" + serviceId + '\'' +
                ", ip='" + ip + '\'' +
                ", cliver='" + cliver + '\'' +
                ", retryer=" + retryer +
                ", traveler=" + traveler +
                '}';
    }
}
