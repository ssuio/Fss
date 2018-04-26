package com.ift.sw.fss;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class FSSRetryer extends ReentrantLock implements Runnable {
    private boolean retry = true;
    private boolean isRetrying = false;
    private BlockingQueue<Object> infoQueue = new LinkedBlockingQueue<>();
    private Condition con = this.newCondition();

    @Override
    public void run() {
        Object info = null;
        while (retry) {
            try {
                this.lock();
                while (infoQueue.size() <= 0 && retry) {
                    isRetrying = false;
                    con.await();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                Tool.printErrorMsg(info.toString() + " interrupted exception.", e);
            } finally {
                con.signalAll();
                this.unlock();
            }

            try {
                info = infoQueue.peek();
                if (info != null) {
                    FSSSocketManager.closeSingle(info);
                    Tool.printErrorMsg(info.toString() + " reconnect...");
                    FSSSocketManager.register(info);
                }
                infoQueue.poll();
            } catch (Exception e) {
                e.printStackTrace();
                Tool.printErrorMsg("Retry failed. Sleep 10 minutes.", e);
                try {
                    Thread.sleep(10 * 60 * 1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                    Tool.printErrorMsg(info.toString() + " interrupted exception.", e1);
                }
            }
        }
        if (info != null) {
            FSSSocketManager.closeSingle(info);
        }
    }

    public void addRetryInfo(Object info) {
        try {
            this.lock();
            if(!infoQueue.contains(info)){
                infoQueue.add(info);
                isRetrying = true;
                Tool.printDebugMsg(info.toString() + " add into retry queue");
            }
        } finally {
            con.signalAll();
            this.unlock();
        }
    }

    public boolean isRetrying() {
        return isRetrying;
    }

    public boolean isRetry() {
        return retry;
    }

    public void setRetry(boolean retry) {
        try {
            this.retry = retry;
            this.lock();
        } finally {
            con.signalAll();
            this.unlock();
        }
    }

    @Override
    public String toString() {
        return "FSSRetryer{" +
                "retry=" + retry +
                ", isRetrying=" + isRetrying +
                ", infoQueue=" + infoQueue +
                ", con=" + con +
                '}';
    }
}
