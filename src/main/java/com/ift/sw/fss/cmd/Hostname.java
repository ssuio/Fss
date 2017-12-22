package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONObject;

@FSSTag(key="hostname")
public class Hostname extends FSSCmd{
    public Hostname(FSSAgent fss, String cmd) throws FSSException{
        super(fss, cmd);
    }

    @Override
    public JSONObject parse(String oriResp) throws Exception {
        this.setOutPutType(CHKHOST_OUTPUT);
        this.setOptions(OP_SHOW_LIST);
        if(cmdArr.length > 1){
            return FSSCommander.generalSetCmdParser(oriResp, SINGLE_SLOT);
        }else{
            return FSSCommander.getCmdParserByInsertingControllerId(oriResp);
        }
    }

    @Override
    public String getAssigmentVal() throws Exception {
        if(cmdArr.length > 1){
            return cmdArr[1];
        }
        return FSSCommander.BOTH_SLOT;
    }
}
