package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONObject;

@FSSTag(key = "oss")
public class Oss extends FSSCmd {
    public Oss(FSSAgent fss, String cmd) throws FSSException {
        super(fss, cmd);
    }

    @Override
    protected void beforeExecute() throws FSSException {
        switch (cmdArr[1]) {
            case "keygen":
            case "keydel":
                this.setCmd(FSSCommander.formatDoubleQuote(cmd));
                break;
        }
    }

    @Override
    public JSONObject parse(String oriResp) throws Exception {
        switch (cmdArr[1]) {
            case "keygen":
            case "keydel":
                this.setShowList(true);
                return FSSCommander.generalSetCmdParser(oriResp, BOTH_SLOT);
            case "keylist":
            case "keynum":
                return FSSCommander.generalGetCmdParser(oriResp, "id");
        }
        throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
    }
}
