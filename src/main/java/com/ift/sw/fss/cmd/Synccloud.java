package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONObject;

@FSSTag(key = "synccloud")
public class Synccloud extends FSSCmd {
    public Synccloud(FSSAgent fss, String cmd) throws FSSException{
        super(fss, cmd);
    }

    @Override
    protected JSONObject execSetup() throws FSSException {
        String oriResp;
        if (cmdArr.length >= 2) {
            switch (cmdArr[1]) {
                case "start":
                case "stop":
                    oriResp = executeFSSCmd(cmd);
                    return FSSCommander.generalSetCmdParser(oriResp, FSSCmd.BOTH_SLOT);
                case "status":
                    oriResp = executeFSSCmd(cmd);
                    return FSSCommander.generalGetCmdParser(oriResp, "cloudDatabase", "cloudStatus");
                case "tasklist":
                    oriResp = executeFSSCmd(cmd);
                    return FSSCommander.generalGetCmdParser(oriResp, "uniqueID");
                case "usr":
                    if (cmdArr.length == 4 && cmdArr[2].equalsIgnoreCase("-u")){
                        oriResp = executeFSSCmd(cmd);
                        return FSSCommander.generalSetCmdParser(oriResp, FSSCmd.BOTH_SLOT);
                    }
                    else if (cmdArr.length == 2){
                        oriResp = executeFSSCmd(cmd);
                        return FSSCommander.generalGetCmdParser(oriResp, "user", "uid");
                    }
                default:
                        new FSSException("Failed to execute command");
            }
        }
        throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
    }

}
