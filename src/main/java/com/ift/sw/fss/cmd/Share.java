package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONObject;

@FSSTag(key = "share")
public class Share extends FSSCmd {
    public Share(FSSAgent fss, String cmd) throws FSSException{
        super(fss, cmd);
    }

    @Override
    public JSONObject parse(String oriResp) throws Exception {
        this.setShowList(true);
        switch (cmdArr[1]) {
            case "status":
                this.setOutPutType(DYNAMIC_KEY_OUTPUT);
                return FSSCommander.generalGetCmdParser(oriResp, "directory");
            case "options":
            default:
                return FSSCommander.generalSetCmdParser(oriResp, SINGLE_SLOT);
        }
    }

    @Override
    public String getAssigmentVal() throws Exception {
        switch (cmdArr[1]) {
            case "status":
                return FSSCommander.BOTH_SLOT;
            case "options":
                return cmdArr[2];
            default:
                return cmdArr[1];
        }
    }
}
