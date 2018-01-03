package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONException;
import org.json.JSONObject;

@FSSTag(key="hostname")
public class Hostname extends FSSCmd{
    public Hostname(FSSAgent fss, String cmd) throws FSSException{
        super(fss, cmd);
    }

    @Override
    protected JSONObject execSetup() throws FSSException, JSONException {
        String oriResp;
        this.setOutPutType(CHKHOST_OUTPUT);
        this.setShowList(true);
        if(cmdArr.length > 1){
            oriResp = executeFSSCmd(cmd, cmdArr[1]);
            return FSSCommander.generalSetCmdParser(oriResp, SINGLE_SLOT);
        }else{
            oriResp = executeFSSCmd(cmd);
            return FSSCommander.getCmdParserByInsertingControllerId(oriResp);
        }
    }

}
