package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONException;
import org.json.JSONObject;

@FSSTag(key = "scheduleRR")
public class ScheduleRR extends FSSCmd {
    public ScheduleRR(FSSAgent fss, String cmd) throws FSSException {
        super(fss, cmd);
    }

    @Override
    protected JSONObject execSetup() throws FSSException, JSONException {
        cmd = FSSCommander.formatDoubleQuote(cmd);
        if ("create".equalsIgnoreCase(cmdArr[1])) {
            setShowList(true);
            return FSSCommander.generalSetCmdParser(executeFSSCmd(cmd), BOTH_SLOT);
        }
        throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
    }

}
