package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONException;
import org.json.JSONObject;

@FSSTag(key = "useradmin")
public class UserAdmin extends FSSCmd {
    public UserAdmin(FSSAgent fss, String cmd) throws FSSException{
        super(fss, cmd);
    }

    @Override
    protected JSONObject execSetup() throws FSSException, JSONException {
        String oriResp;
        if ("user".equalsIgnoreCase(cmdArr[1])) {
            switch (cmdArr[2]) {
                case "add":
                    cmd = FSSCommander.formatDoubleQuote(cmd);
                case "delete":
                case "modify":
                    this.setShowList(true);
                    oriResp = executeFSSCmd(cmd);
                    return FSSCommander.generalSetCmdParser(oriResp, BOTH_SLOT);
                case "list":
                    oriResp = executeFSSCmd(cmd);
                    return FSSCommander.generalGetCmdParser(oriResp, "UID");
            }
        } else if ("group".equalsIgnoreCase(cmdArr[1])) {
            switch (cmdArr[2]) {
                case "add":
                    cmd = FSSCommander.formatDoubleQuote(cmd);
                case "delete":
                    this.setShowList(true);
                case "rename":
                case "modify":
                case "adduser":
                case "deluser":
                    oriResp = executeFSSCmd(cmd);
                    return FSSCommander.generalSetCmdParser(oriResp, BOTH_SLOT);
                case "list":
                    oriResp = executeFSSCmd(cmd);
                    return FSSCommander.generalGetCmdParser(oriResp, "Name");
            }
        }
        throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
    }

}
