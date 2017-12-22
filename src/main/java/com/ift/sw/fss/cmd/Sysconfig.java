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
    public JSONObject parse(String oriResp) throws Exception {
        switch (cmdArr[1]) {
            case "pwdpolicy":
            case "tcpkeepalive":
                if (cmdArr.length == 2) {
                    return FSSCommander.generalGetCmdParser(oriResp);
                } else {
                    return FSSCommander.generalSetCmdParser(oriResp, BOTH_SLOT);
                }

            case "rah":
                if (cmdArr.length > 2) {
                    return FSSCommander.generalSetCmdParser(oriResp, BOTH_SLOT);
                } else {
                    return FSSCommander.generalGetCmdParser(oriResp);
                }
        }
        throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
    }
}
