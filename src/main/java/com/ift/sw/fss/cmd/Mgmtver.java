package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

@FSSTag(key="mgmtver")
public class Mgmtver extends FSSCmd{
    private static String defaultVer = "not-defined";
    public Mgmtver(FSSAgent fss, String cmd) throws FSSException{
        super(fss, cmd);
    }

    @Override
    protected JSONObject execSetup() throws FSSException {
        String oriResp = executeFSSCmd(cmd);
        JSONObject obj =  FSSCommander.generalGetCmdParser(oriResp);
        int len = obj.optJSONArray("data").length();
        if(len == 0){
            obj.remove("data");

            Map<String, String> dataMap = new java.util.HashMap<String, String>();
            dataMap.put("mgmtver", defaultVer);

            JSONArray tempArr = new JSONArray();
            tempArr.put(dataMap);

            obj.put("data", tempArr);

        }else{
            JSONArray tmp = new JSONArray();
            tmp = obj.getJSONArray("data");
            JSONObject tmpObj = tmp.getJSONObject(0);

            obj.remove("data");

            Map<String, String> dataMap = new java.util.HashMap<String, String>();
            dataMap.put("mgmtver", tmpObj.getString("version"));

            JSONArray tempArr = new JSONArray();
            tempArr.put(dataMap);

            obj.put("data", tempArr);

        }
        return obj;
    }

}
