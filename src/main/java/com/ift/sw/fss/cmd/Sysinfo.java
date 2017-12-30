package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONObject;

@FSSTag(key = "sysinfo")
public class Sysinfo extends FSSCmd {
    public Sysinfo(FSSAgent fss, String cmd) throws FSSException{
        super(fss, cmd);
    }

    @Override
    protected JSONObject execSetup() throws FSSException {
        switch (cmdArr[1]) {
            case "network":
            case "cpu":
            case "memory":
                return FSSCommander.getCmdParserByInsertingControllerId(executeFSSCmd(cmd));
        }
        throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
    }

}
