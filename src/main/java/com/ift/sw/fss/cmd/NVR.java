package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONObject;

@FSSTag(key = "nvr")
public class NVR extends FSSCmd {
    public NVR(FSSAgent fss, String cmd) throws FSSException {
        super(fss, cmd);
    }

    @Override
    protected JSONObject execSetup() throws FSSException {
        String oriResp;
        switch (cmdArr[1]) {
            case "config":
                if (cmdArr.length == 2) {
                    oriResp = executeFSSCmd(cmd);
                    return FSSCommander.generalGetCmdParser(oriResp, "container_id");
                }
            case "enable":
            case "disable":
                this.setShowList(true);
                oriResp = executeFSSCmd(cmd);
                return FSSCommander.generalSetCmdParser(oriResp, BOTH_SLOT);
        }
        throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
    }

}
