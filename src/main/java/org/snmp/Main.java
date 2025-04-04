package org.snmp;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            SnmpTop snmpTop = new SnmpTop(SnmpConstants.SNMP_TARGET_ADDRESS);

            // 下发调度表
            snmpTop.rtsTftp.setValue();

            Thread.sleep(5000);

            // 读取调度表
            snmpTop.getSchedule.setValue(1);

            // 获取设备名字
            //String str = snmpTop.sysNameNode.getValue().toString();
            //System.out.println(str);

            Thread.sleep(10000);
        } catch (IOException e) {
            System.err.println("调度表下载失败" + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("线程被中断: " + e.getMessage());
       }
    }
}