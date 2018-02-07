package com.ift.sw.fss.cmd;

import com.ift.sw.fss.*;
import org.json.JSONException;
import org.json.JSONObject;

@FSSTag(key = "cliscript")
public class CliScript extends FSSCmd {
    public CliScript(FSSAgent fss, String cmd) throws FSSException {
        super(fss, cmd);
    }

    @Override
    protected JSONObject execSetup() throws FSSException, JSONException {
        String fullScriptCmd;
        String oriResp;
        String[] appendCmds = parseCommandSet(cmd);
        fullScriptCmd = getFullScriptCmd(appendCmds);
        oriResp = executeFSSCmdDirectly(fullScriptCmd);
        JSONObject obj = FSSCommander.generalSetCmdParser(oriResp, SINGLE_SLOT);
        return obj;
    }

    private String[] parseCommandSet(String strParams) {
        String strToParse = strParams.substring(11, strParams.length() - 1);
        String[] ret = strToParse.split("@::@");
        return ret;
    }


    private String getFullScriptCmd(String[] appendCmds) throws FSSException {
        String ret = "cliscript ";
        String serviceId = fss.getServiceId();
        for (int idx = 0; idx < appendCmds.length; idx++) {
            String appendCmd = appendCmds[idx];
            String[] splitStr = appendCmd.split("#");
            FSSCmd fssCmd = FSSCommander.generateFSSCmd(fss, splitStr[1]);
            String slot = fssCmd.getAssigmentCmdSlot();
            if (idx == 0) {
                ret += "\"" + getFullOneCmd(fssCmd.getCmd(), slot, serviceId);
            } else {
                ret += "@::@" + getFullOneCmd(fssCmd.getCmd(), slot, serviceId);
            }
        }
        ret += "\"";
        return ret;
    }

    private String getFullOneCmd(String cmd, String slot, String serviceId) {
        cmd = FSSCommander.formatDoubleQuote(cmd);
        if (!slot.equalsIgnoreCase(FSSCommander.BOTH_SLOT)) {
            return cmd + " -z " + slot + "@" + serviceId;
        } else {
            return cmd + " -z a@" + serviceId + "@::@" + cmd + " -z b@" + serviceId;
        }
    }

}
