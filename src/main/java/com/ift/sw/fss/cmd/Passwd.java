package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSCommander;
import com.ift.sw.fss.FSSException;
import com.ift.sw.fss.FSSTag;
import org.json.JSONObject;

@FSSTag(key = "passwd")
public class Passwd extends FSSCmd {
    public Passwd(FSSAgent fss, String cmd) throws FSSException{
        super(fss, cmd);
    }

    @Override
    public void beforeExecute() throws FSSException {
        this.setCmd(FSSCommander.formatDoubleQuote(cmd));
    }

    @Override
    public JSONObject parse(String oriResp) throws Exception {
        this.setShowList(true);
        return FSSCommander.generalSetCmdParser(oriResp, BOTH_SLOT);
    }
}
