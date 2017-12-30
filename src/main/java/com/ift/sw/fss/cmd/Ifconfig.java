package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONObject;

@FSSTag(key="ifconfig")
public class Ifconfig extends FSSCmd {
    public Ifconfig(FSSAgent fss, String cmd) throws FSSException{
        super(fss, cmd);
    }

    @Override
    protected JSONObject execSetup() throws FSSException {
        String oriResp;
        switch (cmdArr[1]){
            case "inet":
            case "inet6":

                if(cmdArr[2].equalsIgnoreCase("show")){
                    oriResp = executeFSSCmd(cmd);
                    return FSSCommander.getCmdParserByInsertingControllerId(oriResp);
                }else{
                    oriResp = executeFSSCmd(cmd, cmdArr[2]);
                    return FSSCommander.generalSetCmdParser(oriResp, SINGLE_SLOT);
                }
            case "vlan":
                switch (cmdArr[2]){
                    case "status":
                    case "show":
                        cmd = cmd.replace("show", "status");
                        oriResp = executeFSSCmd(cmd);
                        return FSSCommander.getCmdParserByInsertingControllerId(oriResp);
                    case "add":
                    case "edit":
                        if("SlotA".equalsIgnoreCase(cmdArr[3]) || "SlotB".equalsIgnoreCase(cmdArr[3])){
                            oriResp = executeFSSCmd(cmd, cmdArr[3]);
                            return FSSCommander.generalSetCmdParser(oriResp, SINGLE_SLOT);
                        }
                        break;
                    case "del":
                    case "delete":
                        oriResp = executeFSSCmd(cmd);
                        return FSSCommander.generalSetOneCmdParser(oriResp, BOTH_SLOT);
                }
        }
        throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
    }

}
