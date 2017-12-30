package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONObject;

@FSSTag(key="dladm")
public class Dladm extends FSSCmd{
    public Dladm(FSSAgent fss, String cmd) throws FSSException {
        super(fss, cmd);
    }

    @Override
    protected JSONObject execSetup() throws FSSException {
        String oriResp;
        switch (cmdArr[1]){
            case "show":
                oriResp = executeFSSCmd(cmd);
                return FSSCommander.getCmdParserByInsertingControllerId(oriResp);
            default:
                throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
        }
    }

}
