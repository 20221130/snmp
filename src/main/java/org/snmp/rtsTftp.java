package org.snmp;

import org.snmp4j.smi.OID;

import java.io.IOException;

public class rtsTftp {
    private final snmpUtils snmpUtils;

    public rtsTftp(String address) {
        snmpUtils = new snmpUtils(address);

        // 添加 Trap 处理器
        snmpUtils.addTrapHandler(new OID(".1.3.6.1.4.1.828483.1.1.1.4.4.0"), value -> {
            int status = value.toInt();
            switch (status) {
                case 19:
                    System.out.println("TFTP 操作成功");
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
        snmpUtils.set(new OID(".1.3.6.1.4.1.828483.1.1.1.4.2.0"), "192.168.83.100"); // rtsTftpClientSourceAddress.0
        snmpUtils.set(new OID(".1.3.6.1.4.1.828483.1.1.1.4.3.0"), 3); // rtsTftpSourceOperateType.0
    }

    public static void main(String[] args) {
        try {
            rtsTftp manager = new rtsTftp(SnmpConstants.SNMP_TARGET_ADDRESS);
            manager.setTftpParameters(); // 设置 TFTP 参数
        } catch (IOException e) {
            System.err.println("rtsTftp failed!" + e.getMessage());
        }
    }
}