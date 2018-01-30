//package com.ift.sw.fss.cmd;
//
//import com.ift.sw.fss.FSSAgent;
//import com.ift.sw.fss.FSSCommander;
//import com.ift.sw.fss.FSSException;
//import com.ift.sw.fss.Tool;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//public class CliScript extends FSSCmd{
//    public CliScript(FSSAgent fss, String cmd) throws FSSException {
//        super(fss, cmd);
//    }
//
//    @Override
//    protected JSONObject execSetup() throws FSSException, JSONException {
//        String[] appendCmds = parseCommandSet(cmd);
//        executeCliScript(appendCmds);
//        return null;
//    }
//
//
//
//    private int executeCliScript(String[] appendCmds){
//        String fullScriptCmd = null;
//        try {
//            fullScriptCmd = getFullScriptCmd(raidcmd, appendCmds);
//        }catch(Exception e) {
//            return RaidToolLib.Fail(raidcmd, "Failed to parse parameter", null);
//        }
//        try {
//            String oriResp = CommandHandler.executeFSSScriptSetCommand(raidcmd, fullScriptCmd);
//            JSONObject obj = CommandHandler.generalSetCmdParser(raidcmd, oriResp, "fssCliScript");
//            return CommandHandler.generalOutputSetting(raidcmd, obj, false);
//        } catch (AgentException e) {
//            Tool.printErrorMsg("Failed to execute agent command", e);
//            return RaidToolLib.EIException(raidcmd);
//        } catch (NASCLIException e) {
//            Tool.printErrorMsg("Failed to execute cli command", e);
//            return RaidToolLib.EIException(raidcmd);
//        } catch (JSONException e) {
//            Tool.printErrorMsg("Failed to parse parameter", e);
//            return RaidToolLib.Fail(raidcmd, "Failed to parse parameter", null);
//        } catch (EIException e) {
//            Tool.printErrorMsg("Failed to execute ei command", e);
//            return RaidToolLib.EIException(raidcmd);
//        }
//    }
//
//    private String[] parseCommandSet(String strParams) {
//        String strToParse = strParams.substring(11, strParams.length()-1);
//        String[] ret = strToParse.split("@::@");
//        return ret;
//    }
//
//
//    private String getFullScriptCmd(String[] appendCmds) throws Exception{
//        String ret = "cliscript ";
//        String serviceId = fss.getServiceId();
//        for(int idx = 0; idx < appendCmds.length; idx++) {
//            String appendCmd = appendCmds[idx];
//            String[] splitStr = appendCmd.split("#");
//            String executeTypeKey = splitStr[0];
//            String oneCmd = splitStr[1];
//            FSSCmd fssCmd = FSSCommander.generateFSSCmd(fss, oneCmd);
//            this.fss.traveler.getFSSCmdAssignment(assignVal, cmd, false);
//            String[] arrayParms = FSSCommander.cmdSplit(oneCmd);
//            String assignmentValParam = getOneCmdAssignmentVal(arrayParms);
//            if("slotA".equals(assignmentValParam) || "slotB".equals(assignmentValParam))
//                oneCmd = oneCmd.replace("slotA", "").replace("slotB", "");
//            String assignmentVal = CommandHandler.getFSSCmdAssignment(raidcmd, assignmentValParam, oneCmd, CommandHandler.useVvId(executeTypeKey));
//
//            if(idx == 0) {
//                ret += "\"" + getFullOneCmd(oneCmd, assignmentVal, serviceId);
//            }else {
//                ret += "@::@" + getFullOneCmd(oneCmd, assignmentVal, serviceId);
//            }
//        }
//
//        ret += "\"";
//
//        return ret;
//    }
//
//    private String getFullOneCmd(String oneCmd, String assignmentVal, int serviceId) {
//        // replace double quotation contained in parameter.
//        oneCmd = oneCmd.replace("\\\"", "\\\\\"");
//        // Because cliscript need to use double quotation to package the cmd, so add backslash before
//        // any double quotation in the command.
//        oneCmd = oneCmd.replace("\"", "\\\"");
//
//        if(assignmentVal.equalsIgnoreCase("a")) {
//            return oneCmd + " -z a@" + serviceId;
//        }else if(assignmentVal.equalsIgnoreCase("b")) {
//            return oneCmd + " -z b@"+ serviceId;
//        }else {
//            return oneCmd + " -z a@" + serviceId + "@::@" + oneCmd + " -z b@"+ serviceId;
//        }
//
//    }
//
//    private String getOneCmdAssignmentVal(String[] arrayParms) {
//        switch(arrayParms[0]) {
//            case "trunk":
//                return Trunk.getAssigmentVal(arrayParms);
//            case "ifconfig":
//                return Ifconfig.getAssigmentVal(arrayParms);
//        }
//        return null;
//    }
//
//    public static String executeFSSScriptSetCommand(RaidCmd raidcmd, String cmd) throws AgentException, NASCLIException, JSONException, EIException{
//        String ret = "";
//        Device dev = raidcmd.m_device;
//
//        ret = dev.executeFSSCommand(cmd, 180);
//        int startIdx = ret.indexOf("\r\n{");
//        int endIdx = ret.lastIndexOf("}\r\n");
//        if (startIdx==-1 || endIdx==-1){
//            Tool.printErrorMsg("Failed to parse NASCLI return string: "+ret);
//            throw new NASCLIException(NASCLIException.NAS_RET_PARSE_ERROR, cmd);
//        }
//        ret = ret.substring(startIdx+2, endIdx+1);
//        return ret;
//    }
//
//
//
//
//}
