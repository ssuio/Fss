package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONObject;

@FSSTag(key = "syslog")
public class Syslog extends FSSCmd {
    public Syslog(FSSAgent fss, String cmd) throws FSSException{
        super(fss, cmd);
    }

    @Override
    protected JSONObject execSetup() throws FSSException {
        String oriResp;
        this.setShowList(true);
        switch (cmdArr[1]) {
            case "status":
                oriResp = executeFSSCmd(cmd);
                return FSSCommander.generalGetCmdParser(oriResp, "enabled");
            case "view":
                oriResp = executeFSSCmd(cmd);
                return FSSCommander.generalGetCmdParser(oriResp, "totalpages");
            case "act":
            case "options":
                oriResp = executeFSSCmd(cmd);
                return FSSCommander.generalSetCmdParser(oriResp, BOTH_SLOT);

        }
        throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
    }

}
