package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

@FSSTag(key = "proxy")
public class Proxy extends FSSCmd {
    public Proxy(FSSAgent fss, String cmd) throws FSSException{
        super(fss, cmd);
    }

    @Override
    public JSONObject parse(String oriResp) throws Exception {
        this.setShowList(true);
        switch (cmdArr[1]) {
            case "switch":
                return FSSCommander.generalSetCmdParser(oriResp, SINGLE_SLOT);
            case "diskcache":
            case "memcache":
            case "acladd":
            case "acledit":
            case "acldel":
            case "aclmov":
                return FSSCommander.generalSetOneCmdParser(oriResp, BOTH_SLOT);
            case "config":
                return proxyConfigGetCmdParser(oriResp, BOTH_SLOT);
            case "status":
                return FSSCommander.getCmdParserByInsertingControllerId(oriResp);
        }
        throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
    }

    @Override
    public String getAssigmentVal() throws Exception {
        if ("switch".equalsIgnoreCase(cmdArr[1])) {
            return cmdArr[2];
        }
        return FSSCommander.BOTH_SLOT;
    }

    private static JSONObject proxyConfigGetCmdParser(String cmdResp, boolean isBothSlot) throws JSONException {
        JSONObject obj = null;
        String jsonObjStr[] = cmdResp.split("\\s*\r\n\\s*");
        if (isBothSlot) {
            JSONObject aResp = new JSONObject(new JSONTokener(jsonObjStr[0]));
            JSONObject bResp = new JSONObject(new JSONTokener(jsonObjStr[1]));
            String aReturn = aResp.getJSONArray("cliCode").getJSONObject(0).getString("Return");
            String bReturn = bResp.getJSONArray("cliCode").getJSONObject(0).getString("Return");
            if ("0x0000".equalsIgnoreCase(aReturn) && "0x0000".equalsIgnoreCase(bReturn))
                obj = aResp;
            else if (!"0x0000".equalsIgnoreCase(aReturn))
                obj = bResp;
            else if (!"0x0000".equalsIgnoreCase(bReturn))
                obj = aResp;
        } else {
            obj = new JSONObject(new JSONTokener(jsonObjStr[0]));
        }
        return obj;
    }
}
