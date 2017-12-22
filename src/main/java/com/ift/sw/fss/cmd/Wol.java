package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONObject;

@FSSTag(key = "wol")
public class Wol extends FSSCmd {
    public Wol(FSSAgent fss, String cmd) throws FSSException{
        super(fss, cmd);
    }

    @Override
    public JSONObject parse(String oriResp) throws Exception {
        this.setShowList(true);
        switch (cmdArr[1]) {
            case "get":
                return FSSCommander.generalGetCmdParser(oriResp, "enabled");
            case "set":
                return FSSCommander.generalSetCmdParser(oriResp, BOTH_SLOT);
        }
        throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
    }
}
