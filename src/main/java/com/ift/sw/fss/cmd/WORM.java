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
    protected JSONObject execSetup() throws FSSException {
        String oriResp;
        switch (cmdArr[1]) {
            case "set":
            case "gclk":
                oriResp = executeFSSCmd(cmd);
                return FSSCommander.generalSetOneCmdParser(oriResp, BOTH_SLOT);
            case "get":
                oriResp = executeFSSCmd(cmd);
                return FSSCommander.getCmdParserByInsertingControllerId(oriResp);
        }
        throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
    }

}
