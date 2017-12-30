package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONObject;

@FSSTag(key = "pwrsche")
public class Pwrsche extends FSSCmd {
    public Pwrsche(FSSAgent fss, String cmd) throws FSSException{
        super(fss, cmd);
    }

    @Override
    protected JSONObject execSetup() throws FSSException {
        String oriResp;
        this.setShowList(true);
        switch (cmdArr[1]) {
            case "list":
                oriResp = executeFSSCmd(cmd);
                return FSSCommander.generalGetCmdParser(oriResp, "id");
            case "add":
            case "edit":
            case "del":
                oriResp = executeFSSCmd(cmd);
                return FSSCommander.generalSetCmdParser(oriResp, BOTH_SLOT);
        }
        throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
    }

}
