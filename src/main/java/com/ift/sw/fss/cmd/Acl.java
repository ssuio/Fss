package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONException;
import org.json.JSONObject;

@FSSTag(key = "acl")
public class Acl extends FSSCmd {
    public Acl(FSSAgent fss, String cmd) throws FSSException {
        super(fss, cmd);
    }

    @Override
    protected JSONObject execSetup() throws FSSException, JSONException {
        String oriResp;
        switch (cmdArr[1]) {
            case "set":
                cmd = cmd.replace("\\\"", "\\\\\"");
                oriResp = this.executeFSSCmd(cmd, FSSCommander.formatPathAssignment(cmdArr[2]));
                return FSSCommander.generalSetCmdParser(oriResp, SINGLE_SLOT);
            case "get":
                cmd = cmd.replace("\\\"", "\\\\\"");
            case "delete":
                oriResp = this.executeFSSCmd(cmd, FSSCommander.formatPathAssignment(cmdArr[2]));
                return FSSCommander.generalGetCmdParser(oriResp, "id");
            default:
                throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
        }
    }

}
