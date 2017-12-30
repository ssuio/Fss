package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONObject;

@FSSTag(key="trunk")
public class Trunk extends FSSCmd{
    public Trunk(FSSAgent fss, String cmd) throws FSSException{
        super(fss, cmd);
    }

    @Override
    protected JSONObject execSetup() throws FSSException {
        String oriResp;
        switch (cmdArr[1]) {
            case "add":
            case "delete":
                oriResp = executeFSSCmd(cmd);
                return FSSCommander.generalSetCmdParser(oriResp, BOTH_SLOT);
            case "show":
                this.setShowList(true);
                oriResp = executeFSSCmd(cmd);
                return FSSCommander.generalGetCmdParser(oriResp);
        }
        throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
    }

}
