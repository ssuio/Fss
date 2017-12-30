package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONObject;

@FSSTag(key="dns")
public class Dns extends FSSCmd{
    public Dns(FSSAgent fss, String cmd) throws FSSException {
        super(fss, cmd);
    }

    @Override
    protected JSONObject execSetup() throws FSSException {
        String oriResp;
        switch (cmdArr[1]){
            case "add":
            case "delete":
                setShowList(true);
                oriResp = executeFSSCmd(cmd);
                return FSSCommander.generalSetCmdParser(oriResp, BOTH_SLOT);
            case "show":
                setShowList(true);
                oriResp = executeFSSCmd(cmd);
                return FSSCommander.dnsGetCmdParser(oriResp);
            default:
                throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
        }
    }

}
