package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONObject;

@FSSTag(key = "bwlist")
public class BWList extends FSSCmd {
    public BWList(FSSAgent fss, String cmd) throws FSSException {
        super(fss, cmd);
    }

    @Override
    public JSONObject parse(String oriResp) throws Exception {
        switch (cmdArr[1]) {
            case "status":
                return FSSCommander.generalGetCmdParser(oriResp, "list");
            case "add":
                switch (cmdArr[2]) {
                    case "host":
                    case "subnet":
                    case "iprange":
                    case "country":
                        this.setShowList(true);
                        return FSSCommander.generalSetCmdParser(oriResp, BOTH_SLOT);
                }
                break;
            case "list":
                this.setOutPutType(DYNAMIC_KEY_OUTPUT);
                return FSSCommander.generalGetCmdParser(oriResp, "UID");
            case "delete":
                this.setShowList(true);
                return FSSCommander.generalSetCmdParser(oriResp, BOTH_SLOT);
            case "options":
                this.setShowList(true);
                return FSSCommander.generalSetCmdParser(oriResp, BOTH_SLOT);
        }
        throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
    }

}
