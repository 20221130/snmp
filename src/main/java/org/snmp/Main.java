package org.snmp;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            SnmpTop snmpTop = new SnmpTop(SnmpConstants.SNMP_TARGET_ADDRESS);
            snmpTop.rtsTftp.setTftpParameters(); // 设置 TFTP 参数
            Thread.sleep(10000);
            snmpTop.snmpUtils.close();
        } catch (IOException e) {
            System.err.println("调度表下载失败" + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("线程被中断: " + e.getMessage());
       }
    }
}