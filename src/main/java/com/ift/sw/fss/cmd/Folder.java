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
    public void beforeExecute() throws FSSException {
        switch (cmdArr[1]) {
            case "options":
                if (cmd.contains("export=")) {
                    this.setCmdOnly(cmd.replace(cmdArr[cmdArr.length - 1], ""));
                }
        }
    }

    @Override
    public JSONObject parse(String oriResp) throws Exception {
        switch (cmdArr[1]) {
            case "options":
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
                return FSSCommander.generalSetCmdParser(oriResp, SINGLE_SLOT);
            case "status":
                return FSSCommander.generalGetCmdParser(oriResp, cmdArr.length > 2 ? "path" : "directory");
            case "usedsize":
                if (cmdArr.length == 2) {
                    return FSSCommander.generalSetCmdParser(oriResp, BOTH_SLOT);
                } else {
                    return FSSCommander.generalSetCmdParser(oriResp, SINGLE_SLOT);
                }
        }
        throw new FSSException(FSSException.RESULT_UNKNOWN_PARAM);
    }

    @Override
    public String getAssigmentVal() throws Exception {
        switch (cmdArr[1]) {
            case "options":
                this.setOptions(OP_USE_VVID);
                if (cmd.contains("export=")) {
                    this.setCmd(cmd.replace(cmdArr[cmdArr.length - 1], ""));
                }
                return cmdArr[2];
            case "create":
                this.setOptions(OP_USE_VVID);
                return cmdArr[2];
            case "usedsize":
                if (cmdArr.length != 2) {
                    return cmdArr[3].split("/")[1];
                }
        }
        return FSSCommander.BOTH_SLOT;
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
