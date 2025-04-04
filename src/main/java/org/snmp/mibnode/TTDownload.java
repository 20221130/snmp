package org.snmp.mibnode;

import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;

import java.io.IOException;
import static org.snmp.SnmpTop.addTrapHandler;
public class TTDownload extends MibNode {
    public static final OID DOWNLOAD_ACTION_OID = new OID(".1.3.6.1.4.1.77696.1.0");
    public static final OID DOWNLOAD_STAYUS_OID = new OID(".1.3.6.1.4.1.77696.2.0");
    public static final OID DOWNLOAD_NOTIFICATION_OID = new OID(".1.3.6.1.4.1.77696.3.0");
    public TTDownload() {
        registerTrapHandlers();
    }

    private static void registerTrapHandlers() {
        addTrapHandler(DOWNLOAD_STAYUS_OID, value -> {
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

    public Variable getValue() throws IOException {
        return null;
    }
    public void setValue() throws IOException {
        set(DOWNLOAD_ACTION_OID, 1);
    }
}
