package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONObject;
import org.json.JSONTokener;

@FSSTag(key = "pagelist")
public class Pagelist extends FSSCmd {
    public Pagelist(FSSAgent fss, String cmd) throws FSSException {
        super(fss, cmd);
    }

    @Override
    protected void beforeExecute() throws FSSException {
        switch (cmdArr[1]) {
            case "user":
                if (cmdArr[2].equalsIgnoreCase("slotA") || cmdArr[2].equalsIgnoreCase("slotB")) {
                    this.setCmdOnly(FSSCommander.rmCmdSlots(cmd));
                } else {
                    this.setCmdOnly(FSSCommander.formatDoubleQuote(cmd));
                }
                break;
        }
    }

    @Override
    public JSONObject parse(String oriResp) throws Exception {
        switch (cmdArr[1]) {
            case "folder":
                if (cmdArr.length == 2) {
                    return FSSCommander.generalGetCmdParser(oriResp, "directory");
                } else {
                    return FSSCommander.generalGetCmdParser(oriResp, "path");
                }
            case "user":
            case "group":
                if (cmdArr[2].equalsIgnoreCase("slotA") || cmdArr[2].equalsIgnoreCase("slotB")) {
                    String jsonObjStr[] = oriResp.split("\\s*\r\n\\s*");
                    return new JSONObject(new JSONTokener(jsonObjStr[0]));
                } else {
                    boolean isUpdateStatus = false;
                    for (int i = 0; i < cmdArr.length; i++) {
                        if (cmdArr[i].equals("-n") && cmdArr[i + 1].equals("0")) {
                            isUpdateStatus = true;
                            break;
                        }
                    }
                    if (isUpdateStatus) {
                        this.setShowList(true);
                        return FSSCommander.generalGetCmdParser(oriResp, "updating");
                    } else {
                        return FSSCommander.generalGetCmdParser(oriResp, "uid");
                    }
                }
            case "ldapuser":
                return FSSCommander.generalGetCmdParser(oriResp, "uid");
            case "ldapgroup":
                return FSSCommander.generalGetCmdParser(oriResp, "cn");
            case "ldapgroupmember":
                this.setOutPutType(DYNAMIC_KEY_OUTPUT);
                return FSSCommander.generalGetCmdParser(oriResp, "name");
            case "share":
                this.setOutPutType(DYNAMIC_KEY_OUTPUT);
                return FSSCommander.generalGetCmdParser(oriResp, "directory");
            case "groupmember":
                return FSSCommander.generalGetCmdParser(oriResp, "name");
        }
        throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
    }

    @Override
    public String getAssigmentVal() throws Exception {
        switch (cmdArr[1]) {
            case "user":
            case "group":
                if (cmdArr[2].equalsIgnoreCase("slotA") || cmdArr[2].equalsIgnoreCase("slotB")) {
                    return cmdArr[2];
                }
        }
        return FSSCommander.BOTH_SLOT;
    }
}
