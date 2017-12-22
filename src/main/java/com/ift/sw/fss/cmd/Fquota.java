package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import org.json.JSONObject;

public class Fquota extends FSSCmd {
    public Fquota(FSSAgent fss, String cmd) throws FSSException{
        super(fss, cmd);
    }

    @Override
    protected void beforeExecute() throws FSSException {
        switch (cmdArr[1]) {
            case "status":
                this.setCmd(FSSCommander.formatDoubleQuote(cmd));
        }
    }

    @Override
    public JSONObject parse(String oriResp) throws Exception {
        switch (cmdArr[1]) {
            case "create":
            case "delete":
                this.setShowList(true);
                return FSSCommander.generalSetCmdParser(oriResp, SINGLE_SLOT);
            case "status":
                if (cmd.contains("-f")) {
                    return FSSCommander.generalSetOneCmdParser(oriResp, SINGLE_SLOT);
                } else {
                    return FSSCommander.generalGetCmdParser(oriResp, "id");
                }
        }
        throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
    }

}
