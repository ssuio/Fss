package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONObject;

@FSSTag(key = "system")
public class System extends FSSCmd {
    public System(FSSAgent fss, String cmd) throws FSSException{
        super(fss, cmd);
    }

    @Override
    protected JSONObject execSetup() throws FSSException {
        switch (cmdArr[1]) {
            case "iostat":
                return FSSCommander.generalGetCmdParser(executeFSSCmd(cmd), "name");
        }
        throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
    }

}
