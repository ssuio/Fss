package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONException;
import org.json.JSONObject;

@FSSTag(key="hostchk")
public class Hostchk extends FSSCmd{
    public Hostchk(FSSAgent fss, String cmd) throws FSSException{
        super(fss, cmd);
    }

    @Override
    protected JSONObject execSetup() throws FSSException, JSONException {
        switch (cmdArr[1]){
            case "name":
                this.setOutPutType(CHKHOST_OUTPUT);
                String oriResp = executeFSSCmd(cmd);
                return FSSCommander.generalSetCmdParser( oriResp, BOTH_SLOT);
        }
        throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
    }

}