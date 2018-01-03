package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONException;
import org.json.JSONObject;

@FSSTag(key = "ldapserver")
public class LDAPServer extends FSSCmd {
    public LDAPServer(FSSAgent fss, String cmd) throws FSSException {
        super(fss, cmd);
    }

    @Override
    protected JSONObject execSetup() throws FSSException, JSONException {
        String oriResp;
        if ("host".equalsIgnoreCase(cmdArr[1])) {
            this.setShowList(true);
            switch (cmdArr[2]) {
                case "options":
                    oriResp = executeFSSCmd(cmd);
                    if (cmdArr.length == 3)
                        return FSSCommander.generalGetCmdParser(oriResp, "domain_name");
                    else
                        return FSSCommander.generalSetCmdParser(oriResp, BOTH_SLOT);
                case "start":
                case "stop":
                case "restart":
                case "initialize":
                    oriResp = executeFSSCmd(cmd);
                    return FSSCommander.generalSetCmdParser(oriResp, BOTH_SLOT);
                case "export":
                case "import":
                    cmd = FSSCommander.rmCmdSlots(cmd);
                    oriResp = executeFSSCmd(cmd, cmdArr[3]);
                    return FSSCommander.generalSetCmdParser(oriResp, SINGLE_SLOT);
            }
        } else if ("user".equalsIgnoreCase(cmdArr[1])) {
            switch (cmdArr[2]) {
                case "add":
                case "batch":
                case "edit":
                    cmd = FSSCommander.formatDoubleQuote(cmd);
                case "delete":
                case "options":
                case "listgroup":
                    this.setShowList(true);
                    oriResp = executeFSSCmd(cmd);
                    return FSSCommander.generalSetCmdParser(oriResp, BOTH_SLOT);
                case "list":
                    oriResp = executeFSSCmd(cmd);
                    return FSSCommander.generalGetCmdParser(oriResp, "uid");
                // TODO ldapserver user joingroup  (replaced by "ldapserver user edit <-u user> [-jg group1 [group2 ...] ]")
                // TODO ldapserver user leavegroup (replaced by "ldapserver user edit <-u user> [-lg groupa [groupb ...] ]")
                // Through m209, so no need to implement // TODO ldapserver user import <-f folder_path> <-n filename> [-o {on | off}]
            }
        } else if ("group".equalsIgnoreCase(cmdArr[1])) {
            switch (cmdArr[2]) {
                case "add":
                case "edit":
                    cmd = FSSCommander.formatDoubleQuote(cmd);
                case "delete":
                    this.setShowList(true);
                    oriResp = executeFSSCmd(cmd);
                    return FSSCommander.generalSetCmdParser(oriResp, BOTH_SLOT);
                case "list":
                    oriResp = executeFSSCmd(cmd);
                    return FSSCommander.generalGetCmdParser(oriResp, "uid");
                // TODO ldapserver group adduser    (replaced by ��ldapserver group edit <-g group> [-au user1 [user2 ...]")
                // TODO ldapserver group deleteuser (replaced by ��ldapserver group edit <-g group> [-du usera [userb ...]")
            }
        } else if ("backup".equalsIgnoreCase(cmdArr[1])) {
            this.setShowList(true);
            oriResp = executeFSSCmd(cmd);
            if (cmdArr.length == 2)
                return FSSCommander.generalGetCmdParser(oriResp, "filename");
            else
                return FSSCommander.generalSetOneCmdParser(oriResp, SINGLE_SLOT);
        }
        throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
    }
}