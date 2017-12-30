package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;

@FSSTag(key = "folder")
public class Folder extends FSSCmd {
    public Folder(FSSAgent fss, String cmd) throws FSSException {
        super(fss, cmd);
    }

    @Override
    protected JSONObject execSetup() throws FSSException {
        String oriResp;
        switch (cmdArr[1]) {
            case "options":
                if (cmd.contains("export=")) {
                    cmd = cmd.replace(cmdArr[cmdArr.length - 1], "");
                }
                oriResp = executeFSSCmd(cmd, cmdArr[2]);
                JSONObject obj = FSSCommander.generalSetCmdParser(oriResp, SINGLE_SLOT);
                if (oriResp.contains("exportkey")) {
                    JSONArray dataList = obj.getJSONArray("data");
                    JSONObject dataObj = dataList.getJSONObject(0);
                    String exportkey = dataObj.get("exportkey").toString();//( (JSONObject)obj.get("data") ).get("exportkey").toString();
                    createEncryptionKeyFile("", cmdArr[cmdArr.length - 1], exportkey);
                    dataObj.put("code", "command_save_to_miltiLang");
                    dataObj.put("Params", new String[]{"" + cmdArr[cmdArr.length - 1]});
                } else {
                    this.setShowList(true);
                }
                return obj;
            case "create":
                this.setShowList(true);
                oriResp = executeFSSCmd(cmd, cmdArr[2]);
                return FSSCommander.generalSetCmdParser(oriResp, SINGLE_SLOT);
            case "status":
                oriResp = executeFSSCmd(cmd);
                return FSSCommander.generalGetCmdParser(oriResp, cmdArr.length > 2 ? "path" : "directory");
            case "usedsize":
                if (cmdArr.length == 2) {
                    oriResp = executeFSSCmd(cmd);
                    return FSSCommander.generalSetCmdParser(oriResp, BOTH_SLOT);
                } else {
                    oriResp = executeFSSCmd(cmd, cmdArr[3].split("/")[1]);
                    return FSSCommander.generalSetCmdParser(oriResp, SINGLE_SLOT);
                }
        }
        throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
    }

    private void createEncryptionKeyFile(String path, String fileName, String encryptionKey) throws FSSException {
        File sedDir = new File(path);
        if (!sedDir.exists())
            sedDir.mkdir();
        File sedKeyFile = (new File(path + fileName));
        byte[] keyWrite = encryptionKey.getBytes();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(sedKeyFile, false);
            fos.write(keyWrite);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
        }
    }
}
