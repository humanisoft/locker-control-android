package com.headleader.lockertest.global;

public class Monitor extends Thread
{
    @Override
    public void run() {

        while (true){
            try {
                Thread.sleep(500);
                Common.refreshBoardStatus();              // 需要自动刷新状态时开启
            } catch (Exception e) {
            }
        }
    }
}
