package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONException;
import org.json.JSONObject;

@FSSTag(key = "route")
public class Route extends FSSCmd {
    public Route(FSSAgent fss, String cmd) throws FSSException {
        super(fss, cmd);
    }

    @Override
    protected JSONObject execSetup() throws FSSException, JSONException {
        String oriResp;
        switch (cmdArr[1]) {
            case "show":
                oriResp = executeFSSCmd(cmd);
                return FSSCommander.getCmdParserByInsertingControllerId(oriResp);
            case "add":
                if ("slotA".equalsIgnoreCase(cmdArr[2]) || "slotB".equalsIgnoreCase(cmdArr[2])) {
                    oriResp = executeFSSCmd(cmd);
                    return FSSCommander.generalSetCmdParser(oriResp, SINGLE_SLOT);
                }
            case "delete":
                oriResp = executeFSSCmd(cmd);
                return FSSCommander.generalSetCmdParser(oriResp, BOTH_SLOT);
        }
        throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
    }

}
