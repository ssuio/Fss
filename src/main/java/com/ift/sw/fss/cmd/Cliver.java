package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

@FSSTag(key = "cliver")
public class Cliver extends FSSCmd {
    private final static String defaultVer = "2.0.1";

    public Cliver(FSSAgent fss, String cmd) throws FSSException {
        super(fss, cmd);
    }

    @Override
    public JSONObject parse(String oriResp) throws Exception {
        JSONObject obj = FSSCommander.generalGetCmdParser(oriResp);
        int len = obj.optJSONArray("data").length();
        if (len == 0) {
            obj.remove("data");
            Map<String, String> dataMap = new java.util.HashMap<String, String>();
            dataMap.put("version", defaultVer);
            JSONArray tempArr = new JSONArray();
            tempArr.put(dataMap);
            obj.put("data", tempArr);
        } else if (fss != null && fss.getCliver() == null) {
            fss.setCliver(obj.getJSONArray("data").getJSONObject(0).optString("version"));
        }
        return obj;
    }

}
