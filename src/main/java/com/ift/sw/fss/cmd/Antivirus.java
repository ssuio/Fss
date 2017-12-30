package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONObject;

import java.util.Arrays;

@FSSTag(key = "antivirus")
public class Antivirus extends FSSCmd {
    public Antivirus(FSSAgent fss, String cmd) throws FSSException {
        super(fss, cmd);
    }

    @Override
    protected JSONObject execSetup() throws FSSException {
        String oriResp = "";
        switch (cmdArr[1].toLowerCase()) {
            case "schedule":
                this.setShowList(true);
                if (cmd.contains("-f")) {
                    oriResp = executeFSSCmd(cmd,cmdArr[3]);
                }else{
                    oriResp = executeFSSCmd(cmd);
                }
                return FSSCommander.generalSetCmdParser(oriResp, cmd.contains("-f") ? SINGLE_SLOT : BOTH_SLOT);

            case "service":
                this.setShowList(true);
                oriResp = executeFSSCmd(cmd);
                return FSSCommander.generalSetCmdParser(oriResp, BOTH_SLOT);

            case "quarantine":
                if (cmd.contains("-d") || cmd.contains("-r")) {
                    this.setShowList(true);
                    oriResp = executeFSSCmd(cmd,cmdArr[2]);
                    return  FSSCommander.generalSetCmdParser(oriResp, SINGLE_SLOT);
                } else {
                    oriResp = executeFSSCmd(cmd);
                    return FSSCommander.getCmdParserByInsertingControllerId(oriResp);
                }

            case "info":
            case "status":
                oriResp = executeFSSCmd(cmd);
                return FSSCommander.getCmdParserByInsertingControllerId(oriResp);

            case "update":
                if (cmdArr.length == 2) {
                    oriResp = executeFSSCmd(cmd);
                    return  FSSCommander.getCmdParserByInsertingControllerId(oriResp);
                } else {
                    this.setShowList(true);
                    oriResp = executeFSSCmd(cmd, cmdArr[2]);
                    return FSSCommander.generalSetCmdParser(oriResp, SINGLE_SLOT);
                }

            case "scanstart":
            case "scanstop":
                this.setShowList(true);
                if (cmd.contains("-f")) {
                    oriResp = executeFSSCmd(cmd, cmdArr[Arrays.asList(cmdArr).indexOf("-f") + 1]);
                    return FSSCommander.generalSetCmdParser(oriResp, SINGLE_SLOT);
                } else {
                    oriResp = executeFSSCmd(cmd);
                    return FSSCommander.generalSetCmdParser(oriResp, BOTH_SLOT);
                }

            case "log":
                if (cmdArr.length == 2 || (cmdArr.length > 4 && !"-d".equalsIgnoreCase(cmdArr[2]))) {
                    oriResp = executeFSSCmd(cmd);
                    return FSSCommander.getCmdParserByInsertingControllerId(oriResp);
                } else {
                    this.setShowList(true);
                    oriResp = executeFSSCmd(cmd, FSSCommander.formatPathAssignment(cmdArr[Arrays.asList(cmdArr).indexOf("-f") + 1]));
                    return FSSCommander.generalSetCmdParser(oriResp, cmd.contains("-f") ? SINGLE_SLOT : BOTH_SLOT);
                }

            case "options":
                if (cmdArr.length <= 2) {
                    oriResp = executeFSSCmd(cmd);
                    return FSSCommander.getCmdParserByInsertingControllerId(oriResp);
                }else{
                    oriResp = executeFSSCmd(cmd, cmdArr[Arrays.asList(cmdArr).indexOf("-f") + 1]);
                    return FSSCommander.generalSetCmdParser(oriResp, SINGLE_SLOT);
                }

            case "pua":
                if (cmdArr.length <= 2) {
                    oriResp = executeFSSCmd(cmd);
                    return FSSCommander.generalGetCmdParser(oriResp, "status");
                }
            case "filetype":
                if (cmdArr.length <= 2) {
                    oriResp = executeFSSCmd(cmd);
                    return FSSCommander.generalGetCmdParser(oriResp, "isEnabled");
                }
            case "whitelist":
                if (cmdArr.length <= 2) {
                    oriResp = executeFSSCmd(cmd);
                    oriResp = executeFSSCmd(cmd);
                    return FSSCommander.generalGetCmdParser(oriResp, "status");
                }
                this.setShowList(true);
                oriResp = executeFSSCmd(cmd, cmdArr[Arrays.asList(cmdArr).indexOf("-f") + 1]);
                return FSSCommander.generalSetCmdParser(oriResp, cmd.contains("-f") ? SINGLE_SLOT : BOTH_SLOT);

            default:
                throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
        }
    }

}
