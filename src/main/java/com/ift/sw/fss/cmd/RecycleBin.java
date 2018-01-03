package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONException;
import org.json.JSONObject;

@FSSTag(key = "recycle_bin")
public class RecycleBin extends FSSCmd {
    public RecycleBin(FSSAgent fss, String cmd) throws FSSException{
        super(fss, cmd);
    }

    @Override
    protected JSONObject execSetup() throws FSSException, JSONException {
        String oriResp;
        this.setShowList(true);
        switch (cmdArr[1]) {
            case "config":
            case "clear":
                oriResp = executeFSSCmd(cmd);
                return FSSCommander.generalSetCmdParser(oriResp, BOTH_SLOT);
            case "enable":
            case "disable":
            case "priv_set":
            case "recover":
                oriResp = executeFSSCmd(cmd, cmdArr[cmdArr.length - 1]);
                return FSSCommander.generalSetCmdParser(oriResp, SINGLE_SLOT);
        }
        throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
    }

}
