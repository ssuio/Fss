package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONException;
import org.json.JSONObject;

@FSSTag(key="ha")
public class HA extends FSSCmd{
    public HA(FSSAgent fss, String cmd) throws FSSException{
        super(fss, cmd);
    }

    @Override
    protected JSONObject execSetup() throws FSSException, JSONException {
        switch (cmdArr[1]){
            case "status":
                return FSSCommander.generalGetCmdParser(executeFSSCmd(cmd));
        }
        throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
    }

}
