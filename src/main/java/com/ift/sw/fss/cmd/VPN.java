package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;
import java.util.Iterator;

@FSSTag(key = "vpn")
public class VPN extends FSSCmd {
    public VPN(FSSAgent fss, String cmd) throws FSSException {
        super(fss, cmd);
    }

    @Override
    protected JSONObject execSetup() throws FSSException {
        String oriResp = "";
        switch (this.cmdArr[1]) {
            case "status":
                oriResp = executeFSSCmd(cmd);
                return FSSCommander.getCmdParserByInsertingControllerId(oriResp);
            case "view":
                oriResp = executeFSSCmd(cmd);
                return FSSCommander.generalGetCmdParser(oriResp);
            case "config":
                if (this.cmdArr.length <= 4){
                    oriResp = executeFSSCmd(cmd);
                    return getCmdParserByInsertingControllerId(oriResp);
                }
            case "cut":
            case "act":
            case "mschap":
                oriResp = executeFSSCmd(cmd, cmdArr[2]);
                return FSSCommander.generalSetCmdParser(oriResp, FSSCmd.SINGLE_SLOT);
            default:
                throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
        }
    }

    private static JSONObject getCmdParserByInsertingControllerId(String cmdResp) throws JSONException {
        JSONObject obj = null;
        String jsonObjStr[] = cmdResp.split("\\s*\r\n\\s*");
        JSONObject aResp = new JSONObject(new JSONTokener(jsonObjStr[0]));
        JSONObject bResp = new JSONObject(new JSONTokener(jsonObjStr[1]));

        String aReturn = aResp.getJSONArray("cliCode").getJSONObject(0).getString("Return");
        String bReturn = bResp.getJSONArray("cliCode").getJSONObject(0).getString("Return");
        if ("0x0000".equalsIgnoreCase(aReturn) && "0x0000".equalsIgnoreCase(bReturn)) {
            // merge data objects to one command response
            obj = new JSONObject();
            obj.put("cliCode", aResp.getJSONArray("cliCode"));
            obj.put("returnCode", aResp.getJSONArray("returnCode"));
            JSONArray aObjList = aResp.getJSONArray("data");
            JSONArray bObjList = bResp.getJSONArray("data");
            mergeDataObjectsByInsertingControllerId(obj, aObjList, bObjList);
        } else {
            /* Controller failover, so one side returns "0x000f".
             * " Invalid status (Source target or destination status incorrect) ".
			 */
            if ("0x000f".equalsIgnoreCase(aReturn)) {
                obj = new JSONObject();
                obj.put("cliCode", bResp.getJSONArray("cliCode"));
                obj.put("returnCode", bResp.getJSONArray("returnCode"));
                JSONArray bObjList = bResp.getJSONArray("data");
                mergeDataObjectsByInsertingControllerId(obj, new JSONArray(), bObjList);
            } else if ("0x000f".equalsIgnoreCase(bReturn)) {
                obj = new JSONObject();
                obj.put("cliCode", aResp.getJSONArray("cliCode"));
                obj.put("returnCode", aResp.getJSONArray("returnCode"));
                JSONArray aObjList = aResp.getJSONArray("data");
                mergeDataObjectsByInsertingControllerId(obj, aObjList, new JSONArray());
            } else { // At least one side returns fail.
                if (!"0x0000".equalsIgnoreCase(bReturn)) {
                    obj = new JSONObject();
                    obj.put("cliCode", aResp.getJSONArray("cliCode"));
                    obj.put("returnCode", aResp.getJSONArray("returnCode"));
                    JSONArray aObjList = aResp.getJSONArray("data");
                    mergeDataObjectsByInsertingControllerId(obj, aObjList, new JSONArray());
                } else {
                    obj = new JSONObject();
                    obj.put("cliCode", bResp.getJSONArray("cliCode"));
                    obj.put("returnCode", bResp.getJSONArray("returnCode"));
                    JSONArray bObjList = bResp.getJSONArray("data");
                    mergeDataObjectsByInsertingControllerId(obj, new JSONArray(), bObjList);
                }
            }
        }
        return obj;
    }

    private static void mergeDataObjectsByInsertingControllerId(JSONObject retObj, JSONArray aObjList, JSONArray bObjList) throws JSONException {
        JSONArray tempArr = new JSONArray();
        HashMap<String, JSONObject> aMap = new HashMap<String, JSONObject>();
        HashMap<String, JSONObject> bMap = new HashMap<String, JSONObject>();
        int aObjListLen = aObjList.length();
        int bObjListLen = bObjList.length();

        for (int i = 0; i < aObjListLen; i++) {
            JSONObject dataObj = aObjList.getJSONObject(i);
            dataObj.put("running", dataObj.optString("controller"));
            dataObj.put("controller", "SlotA");
            aMap.put(i + "_", dataObj);
        }
        for (int i = 0; i < bObjListLen; i++) {
            JSONObject dataObj = bObjList.getJSONObject(i);
            dataObj.put("running", dataObj.optString("controller"));
            dataObj.put("controller", "SlotB");
            bMap.put(i + "_", dataObj);
        }
        for (Iterator<String> it = aMap.keySet().iterator(); it.hasNext(); ) {
            String key = it.next();
            tempArr.put(aMap.get(key));
        }
        for (Iterator<String> it = bMap.keySet().iterator(); it.hasNext(); ) {
            String key = it.next();
            tempArr.put(bMap.get(key));
        }
        retObj.put("data", tempArr);
    }

}
