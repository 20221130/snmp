package org.snmp;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            rtsTftp manager = new rtsTftp(SnmpConstants.SNMP_TARGET_ADDRESS);
            manager.setTftpParameters(); // 设置 TFTP 参数
        } catch (IOException e) {
            System.err.println("调度表下载失败" + e.getMessage());
        }
    }
}