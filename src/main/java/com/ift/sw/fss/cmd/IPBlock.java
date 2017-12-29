package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONObject;

@FSSTag(key = "ipblock")
public class IPBlock extends FSSCmd {
    public IPBlock(FSSAgent fss, String cmd) throws FSSException{
        super(fss, cmd);
    }

    @Override
    public void beforeExecute() throws FSSException {
        if (cmdArr[1].equalsIgnoreCase("addfail")) {
            this.setCmd(cmd.replace("\\\"", "\\\\\"").replace("\"", "\\\""));
        }
    }

    @Override
    public JSONObject parse(String oriResp) throws Exception {
        switch (cmdArr[1]) {
            case "status":
                return FSSCommander.generalGetCmdParser(oriResp, "period");
            case "num":
                return FSSCommander.generalGetCmdParser(oriResp, "number");
            case "list":
                return FSSCommander.generalGetCmdParser(oriResp, "list");
            case "delete":
            case "options":
            case "addfail":
                this.setShowList(true);
                return FSSCommander.generalSetCmdParser(oriResp, BOTH_SLOT);
        }
        throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
    }

}
