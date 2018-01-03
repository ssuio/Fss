package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONException;
import org.json.JSONObject;

@FSSTag(key = "refreshdu")
public class Refreshdu extends FSSCmd {
    public Refreshdu(FSSAgent fss, String cmd) throws FSSException{
        super(fss, cmd);
    }

    @Override
    protected JSONObject execSetup() throws FSSException, JSONException {
        this.setShowList(true);
        return FSSCommander.generalSetCmdParser(executeFSSCmd(cmd), BOTH_SLOT);
    }

}
