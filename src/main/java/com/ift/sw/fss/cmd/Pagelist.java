package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

@FSSTag(key = "pagelist")
public class Pagelist extends FSSCmd {
    public Pagelist(FSSAgent fss, String cmd) throws FSSException {
        super(fss, cmd);
    }

    @Override
    protected JSONObject execSetup() throws FSSException, JSONException {
        String oriResp;
        switch (cmdArr[1]) {
            case "folder":
                oriResp = executeFSSCmd(cmd);
                if (cmdArr.length == 2) {
                    return FSSCommander.generalGetCmdParser(oriResp, "directory");
                } else {
                    return FSSCommander.generalGetCmdParser(oriResp, "path");
                }
            case "user":
            case "group":
                if (cmdArr[2].equalsIgnoreCase("slotA") || cmdArr[2].equalsIgnoreCase("slotB")) {
                    cmd = FSSCommander.rmCmdSlots(cmd);
                    oriResp = executeFSSCmd(cmd, cmdArr[2]);
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
                        oriResp = executeFSSCmd(cmd);
                        return FSSCommander.generalGetCmdParser(oriResp, "updating");
                    } else {
                        oriResp = executeFSSCmd(cmd);
                        return FSSCommander.generalGetCmdParser(oriResp, cmdArr[1].equalsIgnoreCase("user")?"uid":"gid");
                    }
                }
            case "ldapuser":
                oriResp = executeFSSCmd(cmd);
                return FSSCommander.generalGetCmdParser(oriResp, "uid");
            case "ldapgroup":
                oriResp = executeFSSCmd(cmd);
                return FSSCommander.generalGetCmdParser(oriResp, "cn");
            case "ldapgroupmember":
                oriResp = executeFSSCmd(cmd);
                this.setOutPutType(DYNAMIC_KEY_OUTPUT);
                return FSSCommander.generalGetCmdParser(oriResp, "name");
            case "share":
                oriResp = executeFSSCmd(cmd);
                this.setOutPutType(DYNAMIC_KEY_OUTPUT);
                return FSSCommander.generalGetCmdParser(oriResp, "directory");
            case "groupmember":
                oriResp = executeFSSCmd(cmd);
                return FSSCommander.generalGetCmdParser(oriResp, "name");
        }
        throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
    }

}
