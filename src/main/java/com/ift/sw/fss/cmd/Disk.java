package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@FSSTag(key = "disk")
public class Disk extends FSSCmd {
    public Disk(FSSAgent fss, String cmd) throws FSSException {
        super(fss, cmd);
    }

    @Override
    protected JSONObject execSetup() throws FSSException, JSONException {
        String oriResp;
        switch (cmdArr[1]) {
            case "show":
                if (cmdArr.length != 2 && !cmdArr[2].equalsIgnoreCase("-f")) {
                    setShowList(true);
                }
                oriResp = executeFSSCmd(cmd);
                return FSSCommander.generalGetCmdParser(oriResp, "diskID");

            case "scan":
                oriResp = executeFSSCmd(cmd);
                return FSSCommander.generalSetOneCmdParser(oriResp, BOTH_SLOT);

            case "expand":
                String diskShowCmd = "disk show " + cmdArr[2];
                String Resp = null;
                Resp = executeFSSCmd(diskShowCmd, FSSCommander.BOTH_SLOT);
                JSONObject tmpObj = FSSCommander.generalGetCmdParser(Resp, "diskID");
                JSONArray diskList = tmpObj.getJSONArray("data");
                tmpObj = diskList.getJSONObject(0); // get first object
                oriResp = executeFSSCmdUseVVId(cmd, tmpObj.getString("serial"));
                return FSSCommander.generalSetCmdParser(oriResp, SINGLE_SLOT);
            default:
                throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
        }
    }

}
