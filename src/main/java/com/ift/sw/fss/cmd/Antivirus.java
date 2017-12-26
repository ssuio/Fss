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
    public JSONObject parse(String oriResp) throws Exception {
        switch (cmdArr[1].toLowerCase()) {
            case "schedule":
                this.setShowList(true);
                return FSSCommander.generalSetCmdParser(oriResp, cmd.contains("-f") ? SINGLE_SLOT : BOTH_SLOT);
            case "service":
                this.setShowList(true);
                return FSSCommander.generalSetCmdParser(oriResp, BOTH_SLOT);
            case "quarantine":
                if (cmd.contains("-d") || cmd.contains("-r")) {
                    this.setShowList(true);
                    return FSSCommander.generalSetCmdParser(oriResp, SINGLE_SLOT);
                } else {
                    return FSSCommander.getCmdParserByInsertingControllerId(oriResp);
                }
            case "info":
            case "status":
                return FSSCommander.getCmdParserByInsertingControllerId(oriResp);
            case "update":
                if (cmdArr.length == 2) {
                    return FSSCommander.getCmdParserByInsertingControllerId(oriResp);
                } else {
                    this.setShowList(true);
                    return FSSCommander.generalSetCmdParser(oriResp, SINGLE_SLOT);
                }
            case "scanstart":
            case "scanstop":
                this.setShowList(true);
                if (cmd.contains("-f")) {
                    return FSSCommander.generalSetCmdParser(oriResp, SINGLE_SLOT);
                } else {
                    return FSSCommander.generalSetCmdParser(oriResp, BOTH_SLOT);
                }
            case "log":
                if (cmdArr.length == 2 || (cmdArr.length > 4 && !"-d".equalsIgnoreCase(cmdArr[2]))) {
                    return FSSCommander.getCmdParserByInsertingControllerId(oriResp);
                } else {
                    this.setShowList(true);
                    return FSSCommander.generalSetCmdParser(oriResp, cmd.contains("-f") ? SINGLE_SLOT : BOTH_SLOT);
                }
            case "options":
                if (cmdArr.length <= 2) {
                    return FSSCommander.getCmdParserByInsertingControllerId(oriResp);
                }
            case "pua":
                if (cmdArr.length <= 2) {
                    return FSSCommander.generalGetCmdParser(oriResp, "status");
                }
            case "filetype":
                if (cmdArr.length <= 2) {
                    return FSSCommander.generalGetCmdParser(oriResp, "isEnabled");
                }
            case "whitelist":
                if (cmdArr.length <= 2) {
                    return FSSCommander.generalGetCmdParser(oriResp, "status");
                }
                this.setShowList(true);
                return FSSCommander.generalSetCmdParser(oriResp, cmd.contains("-f") ? SINGLE_SLOT : BOTH_SLOT);
            default:
                throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
        }
    }

    @Override
    public String getAssigmentVal() throws Exception {
        switch (cmdArr[1].toLowerCase()) {
            case "schedule":
                if (cmd.contains("-f")) {
                    return cmdArr[3];
                }
                break;
            case "quarantine":
                if (cmd.contains("-d") || cmd.contains("-4")) {
                    return cmdArr[2];
                }
                break;
            case "update":
                if (cmdArr.length != 2) {
                    return cmdArr[2];
                }
                break;
            case "scanstart":
            case "scanstop":
                return cmdArr[Arrays.asList(cmdArr).indexOf("-f") + 1];
            case "log":
                if (cmdArr.length == 2 || (cmdArr.length > 4 && !"-d".equalsIgnoreCase(cmdArr[2]))) {

                } else {
                    return FSSCommander.formatPathAssignment(cmdArr[Arrays.asList(cmdArr).indexOf("-f") + 1]);
                }
                break;
            case "options":
            case "pua":
            case "filetype":
            case "whitelist":
                if (cmdArr.length > 2) {
                    return cmdArr[Arrays.asList(cmdArr).indexOf("-f") + 1];
                }
        }
        return FSSCommander.BOTH_SLOT;
    }
}
