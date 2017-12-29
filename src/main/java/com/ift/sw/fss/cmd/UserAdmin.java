package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONObject;

@FSSTag(key = "useradmin")
public class UserAdmin extends FSSCmd {
    public UserAdmin(FSSAgent fss, String cmd) throws FSSException{
        super(fss, cmd);
    }

    @Override
    public void beforeExecute() throws FSSException {
        switch (cmdArr[2]) {
            case "add":
            case "modify":
                this.setCmd(FSSCommander.formatDoubleQuote(cmd));
        }
    }

    @Override
    public JSONObject parse(String oriResp) throws Exception {
        if ("user".equalsIgnoreCase(cmdArr[1])) {
            switch (cmdArr[2]) {
                case "add":
                case "delete":
                case "modify":
                    this.setShowList(true);
                    return FSSCommander.generalSetCmdParser(oriResp, BOTH_SLOT);
                case "list":
                    return FSSCommander.generalGetCmdParser(oriResp, "UID");
            }
        } else if ("group".equalsIgnoreCase(cmdArr[1])) {
            switch (cmdArr[2]) {
                case "add":
                case "delete":
                    this.setShowList(true);
                case "rename":
                case "modify":
                case "adduser":
                case "deluser":
                    return FSSCommander.generalSetCmdParser(oriResp, BOTH_SLOT);
                case "list":
                    return FSSCommander.generalGetCmdParser(oriResp, "Name");
            }
        }
        throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
    }
}
