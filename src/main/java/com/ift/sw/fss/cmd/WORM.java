package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONObject;

@FSSTag(key = "worm")
public class WORM extends FSSCmd {
    public WORM(FSSAgent fss, String cmd) throws FSSException{
        super(fss, cmd);
    }

    @Override
    public JSONObject parse(String oriResp) throws Exception {
        switch (cmdArr[1]) {
            case "set":
            case "gclk":
                return FSSCommander.generalSetOneCmdParser(oriResp, BOTH_SLOT);
            case "get":
                return FSSCommander.getCmdParserByInsertingControllerId(oriResp);
        }
        throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
    }
}
