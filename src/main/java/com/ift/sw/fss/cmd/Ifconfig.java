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
        if(cmdArr[1].equalsIgnoreCase("vlan") && ("status".equalsIgnoreCase(cmdArr[2]) || "show".equalsIgnoreCase(cmdArr[2]))){
            this.setCmd(cmd.replace("show", "status"));
        }
    }

    @Override
    public JSONObject parse(String oriResp) throws Exception {
        switch (cmdArr[1]){
            case "inet":
            case "inet6":
                if(cmdArr[2].equalsIgnoreCase("show")){
                    return FSSCommander.getCmdParserByInsertingControllerId(oriResp);
                }else{
                    return FSSCommander.generalSetCmdParser(oriResp, SINGLE_SLOT);
                }
            case "vlan":
                switch (cmdArr[2]){
                    case "status":
//                    case "show":
                        return FSSCommander.getCmdParserByInsertingControllerId(oriResp);
                    case "add":
                    case "edit":
                        if("SlotA".equalsIgnoreCase(cmdArr[3]) || "SlotB".equalsIgnoreCase(cmdArr[3])){
                            return FSSCommander.generalSetCmdParser(oriResp, SINGLE_SLOT);
                        }
                        break;
                    case "del":
                    case "delete":
                        return FSSCommander.generalSetOneCmdParser(oriResp, BOTH_SLOT);
                }
        }
        throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
    }

    @Override
    public String getAssigmentVal() throws Exception {
        if(cmdArr[1].equalsIgnoreCase("vlan")){
            switch (cmdArr[2]){
                case "add":
                case "edit":
                    if("SlotA".equalsIgnoreCase(cmdArr[3]) || "SlotB".equalsIgnoreCase(cmdArr[3])){
                        return cmdArr[3];
                    }
            }
        }
        return FSSCommander.BOTH_SLOT;
    }
}
