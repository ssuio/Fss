package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONObject;

@FSSTag(key = "service")
public class Service extends FSSCmd {
    public Service(FSSAgent fss, String cmd) throws FSSException {
        super(fss, cmd);
    }

    @Override
    protected JSONObject execSetup() throws FSSException {
        String oriResp;
        setShowList(true);
        switch (cmdArr[1]) {
            case "options":
                cmd = FSSCommander.formatDoubleQuote(cmd);
            case "start":
            case "stop":
            case "restart":
                oriResp = executeFSSCmd(cmd);
                return FSSCommander.generalSetCmdParser(oriResp, BOTH_SLOT);
            case "status":
                oriResp = executeFSSCmd(cmd);
                return FSSCommander.serviceStatusGetCmdParser(oriResp);
        }
        throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
    }

}
