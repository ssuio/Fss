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
    public JSONObject parse(String oriResp) throws Exception {
        if (cmdArr.length >= 2) {
            switch (cmdArr[1]) {
                case "start":
                case "stop":
                    return FSSCommander.generalSetCmdParser(oriResp, FSSCmd.BOTH_SLOT);
                case "status":
                    return FSSCommander.generalGetCmdParser(oriResp, "cloudDatabase", "cloudStatus");
                case "tasklist":
                    return FSSCommander.generalGetCmdParser(oriResp, "uniqueID");
                case "usr":
                    if (cmdArr.length == 4 && cmdArr[2].equalsIgnoreCase("-u"))
                        return FSSCommander.generalSetCmdParser(oriResp, FSSCmd.BOTH_SLOT);
                    else if (cmdArr.length == 2)
                        return FSSCommander.generalGetCmdParser(oriResp, "user", "uid");
                    else
                        new FSSException("Failed to execute command");
            }
        }
        throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
    }

}
