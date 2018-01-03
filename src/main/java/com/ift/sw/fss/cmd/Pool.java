package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONException;
import org.json.JSONObject;

@FSSTag(key = "pool")
public class Pool extends FSSCmd {
    public Pool(FSSAgent fss, String cmd) throws FSSException {
        super(fss, cmd);
    }

    @Override
    protected JSONObject execSetup() throws FSSException, JSONException {
        String oriResp;
        this.setShowList(true);
        switch (cmdArr[1]) {
            case "create":
            case "destroy":
            case "export":
            case "import":
                oriResp = executeFSSCmdUseVVId(cmd, cmdArr[2]);
                return FSSCommander.generalSetCmdParser(oriResp, SINGLE_SLOT);
            case "status":
                oriResp = executeFSSCmd(cmd);
                return FSSCommander.generalGetCmdParser(oriResp, "name");
        }
        throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
    }

}
