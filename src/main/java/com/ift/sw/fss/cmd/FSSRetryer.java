package com.ift.sw.fss.cmd;

import com.ift.sw.fss.FSSAgent;
import com.ift.sw.fss.FSSSocketManager;
import com.ift.sw.fss.Tool;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class FSSRetryer implements Runnable {
    private boolean retry = true;
    private boolean isRetrying = false;
    private BlockingQueue<Object> infoQueue = new LinkedBlockingQueue<>();

    @Override
    public void run() {
        Object info = null;
        while (retry) {
            try {
                synchronized (infoQueue) {
                    if (infoQueue.size() <= 0) {
                        isRetrying = false;
                        infoQueue.wait();
                    }
                }
                info = infoQueue.peek();
                if (info != null) {
                    FSSSocketManager.closeSingle(info);
                    Tool.printErrorMsg(info.toString()+" reconnect...");
                    FSSSocketManager.register(info);
                }
                infoQueue.poll();
            } catch (Exception e) {
                Tool.printErrorMsg("Retry failed.", e);
                synchronized (infoQueue){
                    try {
                        infoQueue.wait(600*1000); //Retry failed wait 10 mins.
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
                e.printStackTrace();
            }
        }
        if(info != null){
            FSSSocketManager.closeSingle(info);
        }
    }

    public void addRetryInfo(Object info) {
        synchronized (infoQueue) {
            infoQueue.add(info);
            isRetrying = true;
            Tool.printInfoMsg(info.toString() + " add into retry queue");
            infoQueue.notifyAll();
        }
    }

    public boolean isRetrying(){
        return isRetrying;
    }

    public boolean isRetry() {
        return retry;
    }

    public void setRetry(boolean retry) {
        this.retry = retry;
        synchronized (infoQueue){
            infoQueue.notifyAll();
        }
    }
}
