package org.snmp;

import org.snmp4j.smi.OID;

import java.io.IOException;

public class TTDownload {
    private SnmpUtils snmpUtils;

    public TTDownload(SnmpUtils snmpUtils) {
        this.snmpUtils = snmpUtils;

        // 添加 Trap 处理器
        snmpUtils.addTrapHandler(new OID(".1.3.6.1.4.1.77696.2.0"), value -> {
            int status = value.toInt();
            switch (status) {
                case 2:
                    System.out.println("下载操作成功");
                    break;
                case 3:
                    System.out.println("下载操作失败");
                    break;
                default:
                    System.out.println("未知状态: " + status);
            }
        });
    }

    // 设置下载操作
    public void setDownloadAction() throws IOException {
        snmpUtils.set(new OID(".1.3.6.1.4.1.77696.1.0"), 1); // ttDownloadAction.0
    }
}