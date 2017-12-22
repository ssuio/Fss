package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import org.json.JSONObject;

public abstract class FSSCmd {
    public static final short SET = 0;
    public static final short GET = 1;
    public static final short EXT = 2;
    public static final boolean BOTH_SLOT = true;
    public static final boolean SINGLE_SLOT = false;
    public static int OP_USE_VVID = 1 << 0;
    public static int OP_SHOW_LIST = 1 << 2;
    public static int OP_HANDLE_BG = 1 << 3;
    public static final short GENERAL_OUTPUT = 0;
    public static final short DYNAMIC_KEY_OUTPUT = 1;
    public static final short CHKHOST_OUTPUT = 2;
    private short outPutType = GENERAL_OUTPUT;
    private int ops = 0;
    protected FSSAgent fss;
    protected short cmdType = EXT;
    protected String cmd;
    protected String[] cmdArr;
    protected String slot = FSSCommander.BOTH_SLOT;
    protected String assignVal;

    public FSSCmd(FSSAgent fss, String cmd) throws FSSException {
        this.fss = fss;
        this.cmd = cmd;
        this.cmdArr = FSSCommander.cmdSplit(cmd);
        try {
            this.assignVal = getAssigmentVal();
        } catch (Exception e) {
            e.printStackTrace();
            throw new FSSException("Get assignmentVal failed. " + e.toString());
        }
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
        this.cmdArr = FSSCommander.cmdSplit(cmd);
    }

    public void setCmdOnly(String cmd) {
        this.cmd = cmd;
    }

    public String getSlot() {
        return slot;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }

    public void setCmdType(short cmdType) {
        this.cmdType = cmdType;
    }

    public short getCmdType() {
        return cmdType;
    }

    protected void beforeExecute() throws FSSException {
    }

    public final JSONObject execute() throws FSSException {
        beforeExecute();
        if (assignVal.equals("slotA")) {
            setCmdOnly(cmd.replace("slotA", ""));
        } else if (assignVal.equals("slotB")) {
            setCmdOnly(cmd.replace("slotB", ""));
        }
        String oriResp = fss.execute(this);
        JSONObject obj = null;
        try {
            obj = parse(oriResp);
        } catch (Exception e) {
            e.printStackTrace();
            throw new FSSException("Parse oriResp failed. " + e.toString());
        }
        return isHandleBgJob() ? this.handleBgJob(obj) : obj;
    }

    public abstract JSONObject parse(String oriResp) throws Exception;

    public String getAssigmentVal() throws Exception {
        return FSSCommander.BOTH_SLOT;
    }

    public String getAssignVal() throws FSSException {
        return assignVal;
    }

    public int setOptions(int ops){
        return this.ops |= ops;
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
                oriResp = fss.execute(bgjobCommand, cmdType, FSSCommander.BOTH_SLOT);
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

    public boolean isUseVVId() {
        return (ops & OP_USE_VVID) != 0;
    }

    public void setUseVVId(boolean useVVId) {
        this.ops = useVVId ? this.ops|OP_USE_VVID : this.ops^OP_USE_VVID;
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

    public void setHandleBgJob(boolean handleBgJob) {
        ops = handleBgJob ? ops|OP_HANDLE_BG : ops^OP_HANDLE_BG;
    }

    public short getOutPutType() {
        return outPutType;
    }

    public void setOutPutType(short outPutType) {
        this.outPutType = outPutType;
    }

    @Override
    public String toString() {
        return "FSSCmd{" +
                "fss=" + fss +
                ", cmdType=" + cmdType +
                ", cmd='" + cmd + '\'' +
                ", slot='" + slot + '\'' +
                ", useVVId=" + isUseVVId() +
                ", showList=" + isShowList() +
                ", handleBgJob=" + isHandleBgJob() +
                ", outPutType=" + outPutType +
                '}';
    }
}
