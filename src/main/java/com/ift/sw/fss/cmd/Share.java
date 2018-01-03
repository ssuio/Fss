package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONException;
import org.json.JSONObject;

@FSSTag(key = "share")
public class Share extends FSSCmd {
    public Share(FSSAgent fss, String cmd) throws FSSException{
        super(fss, cmd);
    }

    @Override
    protected JSONObject execSetup() throws FSSException, JSONException {
        String oriResp;
        this.setShowList(true);
        switch (cmdArr[1]) {
            case "status":
                this.setOutPutType(DYNAMIC_KEY_OUTPUT);
                oriResp = executeFSSCmd(cmd);
                return FSSCommander.generalGetCmdParser(oriResp, "directory");
            case "options":
                oriResp = executeFSSCmd(cmd, cmdArr[2]);
                return FSSCommander.generalSetCmdParser(oriResp, SINGLE_SLOT);
            default:
                oriResp = executeFSSCmd(cmd, cmdArr[1]);
                return FSSCommander.generalSetCmdParser(oriResp, SINGLE_SLOT);
        }
    }

}
