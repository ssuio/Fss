package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONObject;

@FSSTag(key = "pool")
public class Pool extends FSSCmd {
    public Pool(FSSAgent fss, String cmd) throws FSSException {
        super(fss, cmd);
    }

    @Override
    public JSONObject parse(String oriResp) throws Exception {
        this.setShowList(true);
        switch (cmdArr[1]) {
            case "create":
            case "destroy":
            case "export":
            case "import":
                return FSSCommander.generalSetCmdParser(oriResp, SINGLE_SLOT);
            case "status":
                return FSSCommander.generalGetCmdParser(oriResp, "name");
        }
        throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
    }

    @Override
    public String getAssigmentVal() throws Exception {
        switch (cmdArr[1]) {
            case "create":
            case "destroy":
            case "export":
            case "import":
                this.setOptions(OP_USE_VVID);
                return cmdArr[2];
        }
        return FSSCommander.BOTH_SLOT;
    }
}
