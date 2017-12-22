package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONObject;

@FSSTag(key = "acl")
public class Acl extends FSSCmd {
    public Acl(FSSAgent fss, String cmd) throws FSSException {
        super(fss, cmd);
    }

    @Override
    protected void beforeExecute() throws FSSException {
        switch (cmdArr[1]) {
            case "set":
            case "delete":
                this.setCmd(cmd.replace("\\\"", "\\\\\""));
        }
    }

    @Override
    public JSONObject parse(String oriResp) throws Exception {
        switch (cmdArr[1]) {
            case "set":
            case "delete":
            case "get":
                return FSSCommander.generalGetCmdParser(oriResp, "id");
            default:
                throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
        }
    }

    @Override
    public String getAssigmentVal() throws Exception {
        switch (cmdArr[1]) {
            case "set":
            case "delete":
                return cmdArr[2];
        }
        return FSSCommander.BOTH_SLOT;
    }

}
