package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONException;
import org.json.JSONObject;

@FSSTag(key = "bwlist")
public class BWList extends FSSCmd {
    public BWList(FSSAgent fss, String cmd) throws FSSException {
        super(fss, cmd);
    }

    @Override
    protected JSONObject execSetup() throws FSSException, JSONException {
        String oriResp;

        switch (cmdArr[1]) {
            case "status":
                oriResp = executeFSSCmd(cmd);
                return FSSCommander.generalGetCmdParser(oriResp, "list");

            case "add":
                switch (cmdArr[2]) {
                    case "host":
                    case "subnet":
                    case "iprange":
                    case "country":
                        this.setShowList(true);
                        oriResp = executeFSSCmd(cmd);
                        return FSSCommander.generalSetCmdParser(oriResp, BOTH_SLOT);
                    default:
                        throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
                }

            case "list":
                this.setOutPutType(DYNAMIC_KEY_OUTPUT);
                oriResp = executeFSSCmd(cmd);
                return FSSCommander.generalGetCmdParser(oriResp, "UID");

            case "delete":
                this.setShowList(true);
                oriResp = executeFSSCmd(cmd);
                return FSSCommander.generalSetCmdParser(oriResp, BOTH_SLOT);

            case "options":
                this.setShowList(true);
                oriResp = executeFSSCmd(cmd);
                return FSSCommander.generalSetCmdParser(oriResp, BOTH_SLOT);
            default:
                throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);

        }
    }

}
