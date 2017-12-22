package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONObject;

@FSSTag(key = "route")
public class Route extends FSSCmd {
    public Route(FSSAgent fss, String cmd) throws FSSException {
        super(fss, cmd);
    }

    @Override
    public JSONObject parse(String oriResp) throws Exception {
        switch (cmdArr[1]) {
            case "show":
                return FSSCommander.getCmdParserByInsertingControllerId(oriResp);
            case "add":
                if ("slotA".equalsIgnoreCase(cmdArr[2]) || "slotB".equalsIgnoreCase(cmdArr[2])) {
                    return FSSCommander.generalSetCmdParser(oriResp, SINGLE_SLOT);
                }
            case "delete":
                return FSSCommander.generalSetCmdParser(oriResp, BOTH_SLOT);
        }
        throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
    }
}
