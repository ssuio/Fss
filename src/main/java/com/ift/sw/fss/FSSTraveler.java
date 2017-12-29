package com.ift.sw.fss;

import com.ift.sw.fss.cmd.FSSCmd;

public abstract class FSSTraveler {
    public abstract String getFSSCmdAssignment(String assignmentVal, String cmd, boolean useVVId) throws FSSException;

    public abstract String executeWhenFSSNotAlive(String cmd, short cmdType) throws FSSException;
}
