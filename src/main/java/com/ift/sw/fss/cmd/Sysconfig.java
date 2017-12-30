package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONObject;

@FSSTag(key = "sysconfig")
public class Sysconfig extends FSSCmd {
    public Sysconfig(FSSAgent fss, String cmd) throws FSSException{
        super(fss, cmd);
    }

    @Override
    protected JSONObject execSetup() throws FSSException {
        String oriResp;
        switch (cmdArr[1]) {
            case "pwdpolicy":
            case "tcpkeepalive":
                if (cmdArr.length == 2) {
                    oriResp = executeFSSCmd(cmd);
                    return FSSCommander.generalGetCmdParser(oriResp);
                } else {
                    oriResp = executeFSSCmd(cmd);
                    return FSSCommander.generalSetCmdParser(oriResp, BOTH_SLOT);
                }

            case "rah":
                if (cmdArr.length > 2) {
                    oriResp = executeFSSCmd(cmd);
                    return FSSCommander.generalSetCmdParser(oriResp, BOTH_SLOT);
                } else {
                    oriResp = executeFSSCmd(cmd);
                    return FSSCommander.generalGetCmdParser(oriResp);
                }
        }
        throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
    }

}
