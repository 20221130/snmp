package org.snmp.mibnode;

import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;

import java.io.IOException;
import java.net.InetAddress;

import static org.snmp.SnmpTop.addTrapHandler;
import static org.snmp.SnmpTop.executorService;

public class RtsTftp extends MibNode{
    private TTDownload ttDownload;
    public static final OID RTS_TFTP_SOURCE_FILE_NAME_OID = new OID(".1.3.6.1.4.1.828483.1.1.1.4.1.0");
    public static final OID RTS_TFTP_SOURCE_ADDRESS_OID = new OID(".1.3.6.1.4.1.828483.1.1.1.4.2.0");
    public static final OID RTS_TFTP_OPERATE_TYPE_OID = new OID(".1.3.6.1.4.1.828483.1.1.1.4.3.0");
    public static final OID RTS_TFTP_SOURCE_STATUS_OID = new OID(".1.3.6.1.4.1.828483.1.1.1.4.4.0");
    public static final OID RTS_TFTP_NOTIFICATION_OID = new OID(".1.3.6.1.4.1.828483.1.1.1.4.5.0");
    public final String SOURCE_FILE_NAME = "schedule.zip";
    public final String SOURCE_ADDRESS = "192.168.83.100";
    public final int OPERATE_TYPE = 3; // 下载调度表
    public RtsTftp(TTDownload ttDownload) {
        registerTrapHandlers();
        this.ttDownload = ttDownload;
    }

    private void registerTrapHandlers() {
        addTrapHandler(RTS_TFTP_SOURCE_STATUS_OID, value -> {
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

    public Variable getValue() throws IOException {
        return null;
    }

    public void setValue() throws IOException {
        set(RTS_TFTP_SOURCE_FILE_NAME_OID, SOURCE_FILE_NAME);
        set(RTS_TFTP_SOURCE_ADDRESS_OID, InetAddress.getByName(SOURCE_ADDRESS));
        set(RTS_TFTP_OPERATE_TYPE_OID, OPERATE_TYPE);
    }
    private void startDownload() {
        executorService.submit(() -> {
            try {
                ttDownload.setValue();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("ttDownload failed");
            }
        });
    }
}
