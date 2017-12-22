package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONObject;

@FSSTag(key = "scheduleRR")
public class ScheduleRR extends FSSCmd {
    public ScheduleRR(FSSAgent fss, String cmd) throws FSSException {
        super(fss, cmd);
    }

    @Override
    protected void beforeExecute() throws FSSException {
        this.setCmd(FSSCommander.formatDoubleQuote(cmd));
    }

    @Override
    public JSONObject parse(String oriResp) throws Exception {
        if ("create".equalsIgnoreCase(cmdArr[1])) {
            this.setOptions(OP_SHOW_LIST);
            return FSSCommander.generalSetCmdParser(oriResp, BOTH_SLOT);
        }
        throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
    }
}
