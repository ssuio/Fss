package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONException;
import org.json.JSONObject;

@FSSTag(key="appsvc")
public class ServicesSwitch extends FSSCmd{
    public static String[] keys = {
            "antivirus", "explorer", "ldapserver", "proxy", "syslog", "synccloud",
            "vpn", "NVR", "dnssvr", "websvr", "cifs", "ftp", "sftp", "nfs",
            "afp", "webdav", "rsyncd", "nis", "oss"
    };

    public ServicesSwitch(FSSAgent fss, String cmd) throws FSSException {
        super(fss, cmd);
    }

    @Override
    protected JSONObject execSetup() throws FSSException, JSONException {
        return FSSCommander.generalGetCmdParser(executeFSSCmd(cmd), keys);
    }

}
