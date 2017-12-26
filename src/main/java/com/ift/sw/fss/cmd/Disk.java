package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONArray;
import org.json.JSONObject;

@FSSTag(key = "disk")
public class Disk extends FSSCmd {
    public Disk(FSSAgent fss, String cmd) throws FSSException {
        super(fss, cmd);
    }

    @Override
    public JSONObject parse(String oriResp) throws Exception {
        switch (cmdArr[1]) {
            case "show":
                if (cmdArr.length != 2 && !cmdArr[2].equalsIgnoreCase("-f")) {
                    setShowList(true);
                }
                return FSSCommander.generalGetCmdParser(oriResp, "diskID");
            case "scan":
                return FSSCommander.generalSetOneCmdParser(oriResp, BOTH_SLOT);
            case "expand":
                return FSSCommander.generalSetCmdParser(oriResp, SINGLE_SLOT);
        }
        throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
    }

    @Override
    public String getAssigmentVal() throws Exception {
        switch (cmdArr[1]) {
            case "expand":
                // get vvid
                String diskShowCmd = "disk show " + cmdArr[2];
                String Resp = null;
                Resp = fss.execute(diskShowCmd, FSSCommander.BOTH_SLOT);
                JSONObject obj = FSSCommander.generalGetCmdParser(Resp, "diskID");
                JSONArray diskList = obj.getJSONArray("data");
                obj = diskList.getJSONObject(0); // get first object
                setOptions(FSSCmd.OP_USE_VVID);
                return obj.getString("serial");
            case "scan":
                setOptions(OP_USE_VVID);
        }
        return FSSCommander.BOTH_SLOT;
    }
}
