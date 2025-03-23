package org.snmp;

import org.snmp4j.smi.OID;
import java.io.IOException;
import java.net.InetAddress;

public class RtsTftp {
    private SnmpUtils snmpUtils;
    private TTDownload ttDownload;
    public RtsTftp(SnmpUtils snmpUtils, TTDownload ttDownload) {
        this.snmpUtils = snmpUtils;
        this.ttDownload = ttDownload;
        // 添加 Trap 处理器
        snmpUtils.addTrapHandler(new OID(".1.3.6.1.4.1.828483.1.1.1.4.4.0"), value -> {
            int status = value.toInt();
            switch (status) {
                case 19:
                    System.out.println("TFTP 操作成功");
                    startDownload(); // TFTP 操作成功后执行下载操作
                    break;
                case 18:
                    System.out.println("TFTP 操作失败");
                    break;
                case 17:
                    System.out.println("警告: 下载文件不是 tar 或 zip 格式");
                    break;
                default:
                    System.out.println("未知状态: " + status);
            }
        });
    }

    // 设置 TFTP 参数
    public void setTftpParameters() throws IOException {
        snmpUtils.set(new OID(".1.3.6.1.4.1.828483.1.1.1.4.1.0"), "schedule.zip"); // rtsTftpSourceFileName.0
        snmpUtils.set(new OID(".1.3.6.1.4.1.828483.1.1.1.4.2.0"), InetAddress.getByName("192.168.83.100")); // rtsTftpClientSourceAddress.0
        snmpUtils.set(new OID(".1.3.6.1.4.1.828483.1.1.1.4.3.0"), 3); // rtsTftpSourceOperateType.0
    }

    // 启动下载操作
    private void startDownload() {
        snmpUtils.executorService.submit(() -> {
            try {
                ttDownload.setDownloadAction();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("ttDownload failed");
            }
        });
    }
}