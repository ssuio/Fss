package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import org.json.JSONObject;

public abstract class FSSCmd {
    public static final short SET = 0;
    public static final short GET = 1;
    public static final short EXT = 2;
    public static final short CLI = 3;
    public static final short NONE = 9;
    public static final boolean BOTH_SLOT = true;
    public static final boolean SINGLE_SLOT = false;
    public static int OP_SHOW_LIST = 1 << 2;
    public static int OP_HANDLE_BG = 1 << 3;
    public static final short GENERAL_OUTPUT = 0;
    public static final short DYNAMIC_KEY_OUTPUT = 1;
    public static final short CHKHOST_OUTPUT = 2;
    private short outPutType = GENERAL_OUTPUT;
    private int ops = 0;
    protected FSSAgent fss;
    protected short cmdType = NONE;
    protected String cmd;
    protected String[] cmdArr;
    protected String slot = FSSCommander.BOTH_SLOT;

    public FSSCmd(FSSAgent fss, String cmd) throws FSSException {
        this.fss = fss;
        this.cmd = cmd;
        this.cmdArr = FSSCommander.cmdSplit(cmd);
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
        this.cmdArr = FSSCommander.cmdSplit(cmd);
    }

    public String getSlot() {
        return slot;
    }

    public FSSCmd setSlot(String slot) {
        this.slot = slot;
        return this;
    }

    public FSSCmd setCmdType(short cmdType) {
        this.cmdType = cmdType;
        return this;
    }

    public short getCmdType() {
        return cmdType;
    }

    protected abstract JSONObject execSetup()throws FSSException;

    public JSONObject execute() throws FSSException{
        try{
            JSONObject obj = execSetup();
            return isHandleBgJob() ? this.handleBgJob(obj) : obj;
        }catch (Exception e){
            if(e instanceof FSSException){
                throw e;
            }else{
                throw new FSSException(this.toString() + " unhandle exception when execute fsscmd. "+e.toString());
            }
        }
    }

    protected String executeFSSCmdUseVVId(String cmd, String assignVal) throws FSSException {
        this.slot = this.fss.traveler.getFSSCmdAssignment(assignVal, cmd, true);
        return executeFSSCmd(FSSCommander.formatAssignCmd(cmd, assignVal));
    }

    protected String executeFSSCmd(String cmd, String assignVal) throws FSSException {
        this.slot = this.fss.traveler.getFSSCmdAssignment(assignVal, cmd, false);
        return executeFSSCmd(FSSCommander.formatAssignCmd(cmd, assignVal));
    }

    protected String executeFSSCmd(String cmd) throws FSSException {
        this.setCmd(cmd);
        if(this.fss != null && this.fss.isAlive()){
            return fss.execute(this);
        }else{
            String finalCmd;
            finalCmd = FSSCommander.formatFSSCmd(cmd, this.getSlot(), fss.getServiceId());
            return fss.traveler.executeWhenFSSNotAlive(finalCmd, getCmdType()==NONE ? EXT : getCmdType());
        }
    }

    protected JSONObject handleBgJob(JSONObject msgObj) throws FSSException {
        JSONObject obj = msgObj.getJSONArray("cliCode").getJSONObject(0);
        String code = obj.getString("Return");
        int intCode = Integer.parseInt(code.substring(2), 16);
        if (intCode == 16) {
            //check background status
            JSONObject dataObj = msgObj.optJSONArray("data").getJSONObject(0);
            String jobId = dataObj.getString("jobID");
            String bgjobCommand = "bgjob status -i " + jobId;
            while (true) {
                String oriResp;
                oriResp = executeFSSCmd(bgjobCommand);
                obj = FSSCommander.generalGetCmdParser(oriResp);
                dataObj = obj.getJSONArray("data").getJSONObject(0);
                if (dataObj.getInt("percentage") == 100) {
                    if (dataObj.getInt("result") == 0) {
                        return obj;
                    } else {
                        throw new FSSException("handle bg job failed.");
                    }
                }
            }
        }
        return msgObj;
    }

    public boolean isShowList() {
        return (ops & OP_SHOW_LIST) != 0;
    }

    public void setShowList(boolean showList) {
        ops = showList ? ops|OP_SHOW_LIST : ops^OP_SHOW_LIST;
    }

    public boolean isHandleBgJob() {
        return (ops & OP_HANDLE_BG) != 0;
    }

    public FSSCmd setHandleBgJob(boolean handleBgJob) {
        ops = handleBgJob ? ops|OP_HANDLE_BG : ops^OP_HANDLE_BG;
        return this;
    }

    public short getOutPutType() {
        return outPutType;
    }

    public void setOutPutType(short outPutType) {
        this.outPutType = outPutType;
    }

    public FSSAgent getFss() {
        return fss;
    }

    public void setFss(FSSAgent fss) {
        this.fss = fss;
    }

    @Override
    public String toString() {
        return "FSSCmd{" +
                "fss=" + fss +
                ", cmdType=" + cmdType +
                ", cmd='" + cmd + '\'' +
                ", slot='" + slot + '\'' +
                ", showList=" + isShowList() +
                ", handleBgJob=" + isHandleBgJob() +
                ", outPutType=" + outPutType +
                '}';
    }
}
