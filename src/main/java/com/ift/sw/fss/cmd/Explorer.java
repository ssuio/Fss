package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONException;
import org.json.JSONObject;

@FSSTag(key="explorer")
public class Explorer extends FSSCmd{

    public Explorer(FSSAgent fss, String cmd) throws FSSException{
        super(fss, cmd);
    }

    @Override
    protected JSONObject execSetup() throws FSSException, JSONException {
        String oriResp;
        if( "app".equalsIgnoreCase(cmdArr[1])) {
            switch (cmdArr[2]) {
                case "start":
                case "stop":
                    oriResp = executeFSSCmd(cmd);
                    return FSSCommander.generalSetCmdParser(oriResp, BOTH_SLOT);
                case "status":
                    oriResp = executeFSSCmd(cmd);
                    return FSSCommander.serviceStatusGetCmdParser(oriResp);
            }
        }
        throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
    }

}
