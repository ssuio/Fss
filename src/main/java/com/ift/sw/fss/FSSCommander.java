package com.ift.sw.fss;

import com.ift.sw.fss.cmd.FSSCmd;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.reflections.Reflections;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FSSCommander {
    private static Reflections REF;
    private static final HashMap<String, String> CMD_CLASS_MAP = new HashMap<>();
    public static int BASESIZE = 16;
    public static String CmdPattern = "([^\"&&[^\']]+? )|([a-z\\d]+=?[\"].*?[\"] )|([a-z\\d]+=?[\'].*?[\'] )|([\"].*?[\"])|([\'].*?[\'])|([a-z\\d]+=?[\"]??[^\"\']*?[\"]?? )|([a-z\\d]+=?[\']??[^\"\']*?[\']?? )|([a-z\\d]+-[a-z\\d]+=?[\"].*?[\"] )";
    public static final String BOTH_SLOT = "both";
    public static final String SLOT_A = "a";
    public static final String SLOT_B = "b";

    static {
        try {
            REF = new Reflections("com.ift.sw.fss.cmd");
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (Class<?> cl : REF.getTypesAnnotatedWith(FSSTag.class)) {
            FSSTag fssTag = cl.getAnnotation(FSSTag.class);
            CMD_CLASS_MAP.put(fssTag.key(), cl.getName());
        }
    }

    public static String formatFSSCmd(String cmd, String slot, String serviceId) {
        if (slot.equalsIgnoreCase(FSSCommander.BOTH_SLOT)) {
            return MessageFormat.format("multicmd \"{0} -z a@{1}\" \"{0} -z b@{1}\"", cmd, serviceId);
        } else {
            return MessageFormat.format("{0} -z {1}@{2}", cmd, slot.toLowerCase(), serviceId);
        }
    }

    public static String formatOutPutStr(String result) throws FSSException {
        int startIdx = result.indexOf("\r\n{");
        int endIdx = result.lastIndexOf("}\r\n");
        if (startIdx==-1 || endIdx==-1){
            throw new FSSException("getOutPutStr failed");
        }
        return result.substring(startIdx+2, endIdx+1);
    }

    public static String formatAssignCmd(String cmd, String assignVal) throws FSSException {
        String finalCmd = cmd;
        if (assignVal.equals("slotA")) {
            finalCmd = cmd.replace("slotA", "");
        } else if (assignVal.equals("slotB")) {
            finalCmd = cmd.replace("slotB", "");
        }
        return finalCmd;
    }

    public static String rmCmdSlots(String cmd) {
        return cmd.replaceAll("slotA", "").replaceAll("slotB", "");
    }

    public static String formatDoubleQuote(String cmd) {
        return cmd.replace("\\\"", "\\\\\"").replace("\"", "\\\"");
    }

    public static String formatPathAssignment(String assignment){
        return assignment.split("/")[1];
    }

    public static String[] cmdSplit(String strParams) {
        // Parse pattern to find out the quotations.
        Pattern pat = Pattern.compile(CmdPattern);
        Matcher mat = pat.matcher(strParams + " ");
        int len = 0;
        for (int i = 0; mat.find(); i++) {
            if ("".equals(mat.group().trim())) continue;
            len++;
        }
        String[] arrayParms = new String[len];
        mat.reset();
        mat = pat.matcher(strParams + " ");
        int idx = 0;
        for (int i = 0; mat.find(); i++) {
            if ("".equals(mat.group().trim())) continue;
            arrayParms[idx] = mat.group().trim();
            idx++;
        }
        return arrayParms;
    }

    public static byte[] generateFssPacket(String cmd) {
        byte[] bArrnew = new byte[BASESIZE + cmd.length()];
        setHeader(bArrnew, BASESIZE + cmd.length());
        try {
            Tool.setValue(bArrnew, cmd.getBytes("UTF-8"), 16, cmd.length());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return bArrnew;
    }

    private static void setHeader(byte[] out, int cmdLen) {
        out[0] = (byte) 0xAF;
        out[1] = (byte) 0xFA;
        Tool.setValue(out, 0, 2, 2);
        Tool.setValue(out, cmdLen, 8, 4); //set output size
        Tool.setValue(out, 0, 12, 4);
    }

    public static FSSCmd generateFSSCmd(FSSAgent fss, String cmd) throws FSSException {
        String[] cmdArr = FSSCommander.cmdSplit(cmd);
        try {
            Class c = Class.forName(CMD_CLASS_MAP.get(cmdArr[0]));
            Class[] params = new Class[]{FSSAgent.class, String.class};
            Constructor constructor = c.getConstructor(params);
            return (FSSCmd) constructor.newInstance(fss, cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new FSSException(MessageFormat.format("GenerateFSSCmd instance failed. [{0}]",cmd));
    }

    public static JSONObject generalSetCmdParser(String cmdResp, boolean isBothSlot) throws JSONException {
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
                obj = aResp;
            else if (!"0x0000".equalsIgnoreCase(bReturn))
                obj = bResp;
        } else {
            obj = new JSONObject(new JSONTokener(jsonObjStr[0]));
        }
        return obj;
    }

    public static JSONObject generalSetOneCmdParser(String cmdResp, boolean isBothSlot) throws JSONException {
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

    public static JSONObject getCmdParserByInsertingControllerId(String cmdResp) throws JSONException {
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

    public static void mergeDataObjectsByInsertingControllerId(JSONObject retObj, JSONArray aObjList, JSONArray bObjList) throws JSONException {
        JSONArray tempArr = new JSONArray();
        HashMap<String, JSONObject> aMap = new HashMap<String, JSONObject>();
        HashMap<String, JSONObject> bMap = new HashMap<String, JSONObject>();
        int aObjListLen = aObjList.length();
        int bObjListLen = bObjList.length();

        for (int i = 0; i < aObjListLen; i++) {
            JSONObject dataObj = aObjList.getJSONObject(i);
            dataObj.put("controller", "SlotA");
            aMap.put(i + "_", dataObj);
        }
        for (int i = 0; i < bObjListLen; i++) {
            JSONObject dataObj = bObjList.getJSONObject(i);
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

    public static JSONObject generalGetCmdParser(String cmdResp, String... keyArgs) throws JSONException {
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
            mergeDataObjects(obj, aObjList, bObjList, keyArgs);
        } else {
            /* Controller failover, so one side returns "0x000f".
             * " Invalid status (Source target or destination status incorrect) ".
			 */
            if ("0x000f".equalsIgnoreCase(aReturn))
                obj = bResp;
            else if ("0x000f".equalsIgnoreCase(bReturn))
                obj = aResp;
            else { // At least one side returns fail.
                if (!"0x0000".equalsIgnoreCase(bReturn))
                    obj = aResp;
                else
                    obj = bResp;
            }
        }
        return obj;
    }

    public static void mergeDataObjects(JSONObject retObj, JSONArray aObjList, JSONArray bObjList, String... keyArgs) throws JSONException {
        JSONArray tempArr = new JSONArray();
        Map<String, JSONObject> aMap = new TreeMap<String, JSONObject>();
        Map<String, JSONObject> bMap = new TreeMap<String, JSONObject>();
        int aObjListLen = aObjList.length();
        int bObjListLen = bObjList.length();

        for (int i = 0; i < aObjListLen; i++) {
            String key = "";
            JSONObject dataObj = aObjList.getJSONObject(i);
            for (String idToken : keyArgs) {
                key += dataObj.getString(idToken) + "_";
            }
            aMap.put(key, dataObj);
        }
        for (int i = 0; i < bObjListLen; i++) {
            String key = "";
            JSONObject dataObj = bObjList.getJSONObject(i);
            for (String idToken : keyArgs) {
                key += dataObj.getString(idToken) + "_";
            }
            bMap.put(key, dataObj);
        }
        for (Iterator<String> it = bMap.keySet().iterator(); it.hasNext(); ) {
            String key = it.next();
            JSONObject tempObj = null;
            if ((tempObj = aMap.get(key)) != null) {
                tempArr.put(tempObj);
                aMap.remove(key);
            } else {
                tempArr.put(bMap.get(key));
            }
        }
        for (Iterator<String> it = aMap.keySet().iterator(); it.hasNext(); ) {
            String key = it.next();
            tempArr.put(aMap.get(key));
        }
        retObj.put("data", tempArr);
    }

    public static JSONObject dnsGetCmdParser(String cmdResp, String... keyArgs) throws JSONException {
        JSONObject obj = null;
        JSONObject temp = null;
        JSONObject tmp_obj = null;
        JSONArray tmp_arr = null;
        String jsonObjStr[] = cmdResp.split("\\s*\r\n\\s*");
        JSONObject aResp = new JSONObject(new JSONTokener(jsonObjStr[0]));
        JSONObject bResp = new JSONObject(new JSONTokener(jsonObjStr[1]));

        String aReturn = aResp.getJSONArray("cliCode").getJSONObject(0).getString("Return");
        String bReturn = bResp.getJSONArray("cliCode").getJSONObject(0).getString("Return");

        if ("0x0000".equalsIgnoreCase(aReturn) && "0x0000".equalsIgnoreCase(bReturn)) {
            // merge data objects to one command response
            temp = new JSONObject();
            tmp_obj = new JSONObject();
            tmp_arr = new JSONArray();
            JSONObject tmp_obj_merg = new JSONObject();
            temp.put("cliCode", aResp.getJSONArray("cliCode"));
            temp.put("returnCode", aResp.getJSONArray("returnCode"));
            JSONArray aObjList = aResp.getJSONArray("data");
            JSONArray bObjList = bResp.getJSONArray("data");
            dnsMergeDataObjects(tmp_obj, aObjList, bObjList, "DNS Server");
            tmp_obj_merg.put("dns", tmp_obj.get("data"));
            temp.put("data", tmp_arr.put(0, tmp_obj_merg));
            obj = temp;
        } else if ("0x000f".equalsIgnoreCase(aReturn)) {
            temp = new JSONObject();
            tmp_obj = new JSONObject();
            tmp_arr = new JSONArray();
            tmp_obj.put("dns", bResp.getJSONArray("data"));
            temp.put("returnCode", bResp.get("returnCode"));
            temp.put("cliCode", bResp.getJSONArray("cliCode"));
            temp.put("data", tmp_arr.put(0, tmp_obj));
            obj = temp;
        } else if ("0x000f".equalsIgnoreCase(bReturn)) {
            temp = new JSONObject();
            tmp_obj = new JSONObject();
            tmp_arr = new JSONArray();
            tmp_obj.put("dns", aResp.getJSONArray("data"));
            temp.put("returnCode", aResp.get("returnCode"));
            temp.put("cliCode", aResp.getJSONArray("cliCode"));
            temp.put("data", tmp_arr.put(0, tmp_obj));
            obj = temp;
        } else { // At least one side returns fail.
            if ("0x0000".equalsIgnoreCase(aReturn)) {
                temp = new JSONObject();
                tmp_obj = new JSONObject();
                tmp_arr = new JSONArray();
                tmp_obj.put("dns", aResp.getJSONArray("data"));
                temp.put("returnCode", aResp.get("returnCode"));
                temp.put("cliCode", aResp.getJSONArray("cliCode"));
                temp.put("data", tmp_arr.put(0, tmp_obj));
                obj = temp;
            } else {
                temp = new JSONObject();
                tmp_obj = new JSONObject();
                tmp_arr = new JSONArray();
                tmp_obj.put("dns", bResp.getJSONArray("data"));
                temp.put("returnCode", bResp.get("returnCode"));
                temp.put("cliCode", bResp.getJSONArray("cliCode"));
                temp.put("data", tmp_arr.put(0, tmp_obj));
                obj = temp;
            }
        }
        return obj;
    }

    public static void dnsMergeDataObjects(JSONObject retObj, JSONArray aObjList, JSONArray bObjList, String... keyArgs) throws JSONException {
        JSONArray tempArr = new JSONArray();
        Map<String, JSONObject> aMap = new TreeMap<String, JSONObject>();
        Map<String, JSONObject> bMap = new TreeMap<String, JSONObject>();
        LinkedHashSet<String> akeyList = new LinkedHashSet<String>();//hashMap'sort would got miss so, use list for key
        LinkedHashSet<String> bkeyList = new LinkedHashSet<String>();
        int aObjListLen = aObjList.length();
        int bObjListLen = bObjList.length();

        for (int i = 0; i < aObjListLen; i++) {
            String key = "";
            JSONObject dataObj = aObjList.getJSONObject(i);
            for (String idToken : keyArgs) {
                key += dataObj.getString(idToken) + "_";
            }
            aMap.put(key, dataObj);
            akeyList.add(key);
        }
        for (int i = 0; i < bObjListLen; i++) {
            String key = "";
            JSONObject dataObj = bObjList.getJSONObject(i);
            for (String idToken : keyArgs) {
                key += dataObj.getString(idToken) + "_";
            }
            bMap.put(key, dataObj);
            bkeyList.add(key);
        }

        for (String key : bkeyList) {
            JSONObject tempObj = null;
            if ((tempObj = aMap.get(key)) != null) {
                tempArr.put(tempObj);
                akeyList.remove(key);
                aMap.remove(key);
            } else {
                tempArr.put(bMap.get(key));
            }
        }
        for (String key : akeyList) {
            tempArr.put(aMap.get(key));
        }
        retObj.put("data", tempArr);
    }

    public static JSONObject serviceStatusGetCmdParser(String cmdResp) throws JSONException {
        JSONObject obj = null;
        JSONObject temp = null;
        String jsonObjStr[] = cmdResp.split("\\s*\r\n\\s*");
        JSONObject aResp = new JSONObject(new JSONTokener(jsonObjStr[0]));
        JSONObject bResp = new JSONObject(new JSONTokener(jsonObjStr[1]));

        String aReturn = aResp.getJSONArray("cliCode").getJSONObject(0).getString("Return");
        String bReturn = bResp.getJSONArray("cliCode").getJSONObject(0).getString("Return");

        if ("0x0000".equalsIgnoreCase(aReturn) && "0x0000".equalsIgnoreCase(bReturn)) {
            JSONArray aObjList = aResp.getJSONArray("data");
            JSONArray bObjList = bResp.getJSONArray("data");
            if (aObjList.length() != 0) {
                temp = aResp.getJSONArray("data").getJSONObject(0);
                temp.put("service", temp.remove("A"));
                aResp.getJSONArray("data").put(0, temp);
                obj = aResp;
            } else if (bObjList.length() != 0) {
                temp = bResp.getJSONArray("data").getJSONObject(0);
                temp.put("service", temp.remove("B"));
                bResp.getJSONArray("data").put(0, temp);
                obj = bResp;
            } else {
                temp.put("service", "");
                aResp.getJSONArray("data").put(0, temp);
                obj = aResp;
            }
        } else {
            if ("0x000f".equalsIgnoreCase(aReturn)) {
                temp = bResp.getJSONArray("data").getJSONObject(0);
                temp.put("service", temp.remove("B"));
                bResp.getJSONArray("data").put(0, temp);
                obj = bResp;
            } else if ("0x000f".equalsIgnoreCase(bReturn)) {
                temp = aResp.getJSONArray("data").getJSONObject(0);
                temp.put("service", temp.remove("A"));
                aResp.getJSONArray("data").put(0, temp);
                obj = aResp;
            } else { // At least one side returns fail.
                if (!"0x0000".equalsIgnoreCase(bReturn)) {
                    temp = aResp.getJSONArray("data").getJSONObject(0);
                    temp.put("service", temp.remove("A"));
                    aResp.getJSONArray("data").put(0, temp);
                    obj = aResp;
                } else {
                    temp = bResp.getJSONArray("data").getJSONObject(0);
                    temp.put("service", temp.remove("B"));
                    bResp.getJSONArray("data").put(0, temp);
                    obj = bResp;
                }
            }
        }
		/*if( "0x0000".equalsIgnoreCase(aReturn) && "0x0000".equalsIgnoreCase(bReturn)){
			// merge data objects to one command response
			obj = new JSONObject();
			obj.put("cliCode", aResp.getJSONArray("cliCode"));
			obj.put("returnCode", aResp.getJSONArray("returnCode"));
			//JSONArray aObjList = aResp.getJSONArray("data");
			//JSONArray bObjList = bResp.getJSONArray("data");
			//mergeDataObjects(obj, aObjList, bObjList, keyArgs);
		}else{
			// Controller failover, so one side returns "0x000f".
			// " Invalid status (Source target or destination status incorrect) ".
			//
			if( "0x000f".equalsIgnoreCase(aReturn) ){
				temp = bResp.getJSONArray("data").getJSONObject(0);
				temp.put("service", temp.remove("B"));
				bResp.getJSONArray("data").put(0, temp);
				obj = bResp;
			}else if ( "0x000f".equalsIgnoreCase(bReturn) ){
				temp = aResp.getJSONArray("data").getJSONObject(0);
				temp.put("service", temp.remove("A"));
				aResp.getJSONArray("data").put(0, temp);
				obj = aResp;
			}else{ // At least one side returns fail.
				if( !"0x0000".equalsIgnoreCase(bReturn) ){
					temp = aResp.getJSONArray("data").getJSONObject(0);
					temp.put("service", temp.remove("A"));
					aResp.getJSONArray("data").put(0, temp);
					obj = aResp;
				}else{
					temp = bResp.getJSONArray("data").getJSONObject(0);
					temp.put("service", temp.remove("B"));
					bResp.getJSONArray("data").put(0, temp);
					obj = bResp;
				}
			}
		}*/
        return obj;
    }

    public static JSONObject netNumberGetCmdParser(String cmdResp, String... keyArgs) throws JSONException {
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
            int aObjListLen = aObjList.length();
            int bObjListLen = bObjList.length();
            JSONObject aDataObj = null;
            JSONObject bDataObj = null;
            int a_total = 0;
            int b_total = 0;
            int a_smb_num = 0;
            int b_smb_num = 0;
            int a_ftp_num = 0;
            int b_ftp_num = 0;
            int a_afp_num = 0;
            int b_afp_num = 0;
            int a_nfs_num = 0;
            int b_nfs_num = 0;

            if (aObjListLen > 0) {
                aDataObj = aObjList.getJSONObject(0);
                a_total = aDataObj.optInt("total");
                a_smb_num = aDataObj.optInt("smb_num");
                a_ftp_num = aDataObj.optInt("ftp_num");
                a_afp_num = aDataObj.optInt("afp_num");
                a_nfs_num = aDataObj.optInt("nfs_num");
            }

            if (bObjListLen > 0) {
                bDataObj = bObjList.getJSONObject(0);
                b_total = bDataObj.optInt("total");
                b_smb_num = bDataObj.optInt("smb_num");
                b_ftp_num = bDataObj.optInt("ftp_num");
                b_afp_num = bDataObj.optInt("afp_num");
                b_nfs_num = bDataObj.optInt("nfs_num");
            }

            JSONObject mergedDataObj = new JSONObject();
            mergedDataObj.put("total", (a_total + b_total) + "");
            mergedDataObj.put("smb_num", (a_smb_num + b_smb_num) + "");
            mergedDataObj.put("ftp_num", (a_ftp_num + b_ftp_num) + "");
            mergedDataObj.put("afp_num", (a_afp_num + b_afp_num) + "");
            mergedDataObj.put("nfs_num", (a_nfs_num + b_nfs_num) + "");
            JSONArray tempArr = new JSONArray();
            tempArr.put(mergedDataObj);
            obj.put("data", tempArr);
        } else {
			/* Controller failover, so one side returns "0x000f".
			 * " Invalid status (Source target or destination status incorrect) ".
			 */
            if ("0x000f".equalsIgnoreCase(aReturn))
                obj = bResp;
            else if ("0x000f".equalsIgnoreCase(bReturn))
                obj = aResp;
            else { // At least one side returns fail.
                if (!"0x0000".equalsIgnoreCase(bReturn))
                    obj = aResp;
                else
                    obj = bResp;
            }
        }
        return obj;
    }

    public static boolean isSupportNASCLIVer(String supportVer, String currentVer) {
        // this.getRAIDInfo().getCliVersion()
        if (supportVer.equals(currentVer)) {
            return true;
        } else if (currentVer == null) {
            return false;
        }
        String[] _supportVer = supportVer.split("\\.");
        String[] _currentVer = currentVer.split("\\.");

        if (_currentVer.length > _supportVer.length) {
            int miuns = _currentVer.length - _supportVer.length;
            for (int i = 0; i < miuns; i++) {
                _supportVer = (supportVer + ".0").split("\\.");
            }
        } else if (_supportVer.length > _currentVer.length) {
            int miuns = _supportVer.length - _currentVer.length;
            for (int i = 0; i < miuns; i++) {
                _currentVer = (_currentVer + ".0").split("\\.");
            }
        }

        for (int i = 0; i < _currentVer.length; i++) {
            int curNum = 0;
            int surpNum = 0;

            try {
                curNum = Integer.parseInt(_currentVer[i]);
                surpNum = Integer.parseInt(_supportVer[i]);
                if (curNum != surpNum) {
                    return curNum > surpNum;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

}
