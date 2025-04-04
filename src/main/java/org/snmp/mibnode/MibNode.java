package org.snmp.mibnode;

import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.smi.*;
import java.io.IOException;
import java.net.InetAddress;

import static org.snmp.SnmpTop.snmp;
import static org.snmp.SnmpTop.target;

public abstract class MibNode {
    // get方法
    public Variable get(OID oid) throws IOException {
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(oid));
        pdu.setType(PDU.GET);
        ResponseEvent event = snmp.send(pdu, target);

        if (event != null && event.getResponse() != null) {
            return event.getResponse().get(0).getVariable();
        } else {
            System.out.println("Get 操作失败: 无响应");
            return null;
        }
    }
    // 重载的set方法
    public void set(OID oid, String value) throws IOException {
        set(oid, new OctetString(value));
    }
    public void set(OID oid, int value) throws IOException {
        set(oid, new Integer32(value));
    }
    public void set(OID oid, byte[] value) throws IOException {
        set(oid, new OctetString(value));
    }
    public void set(OID oid, InetAddress ipAddress) throws IOException {
        set(oid, new IpAddress(ipAddress));
    }
    public void set(OID oid, Variable value) throws IOException {
        PDU pdu = createPDU(oid, value, PDU.SET);

        ResponseEvent event = snmp.send(pdu, target);
        if (event != null && event.getResponse() != null) {
            PDU responsePDU = event.getResponse();
            int errorStatus = responsePDU.getErrorStatus();
            if (errorStatus == PDU.noError) {
                System.out.println("Set 操作成功: " + oid + " = " + value);
            } else {
                System.out.println("Set 操作失败: " + oid);
                System.out.println("错误状态: " + errorStatus + " - " + responsePDU.getErrorStatusText());
            }
        } else {
            System.out.println("Set 操作: " + oid + " = " + value);
            System.out.println("Set 操作失败: 无响应");
        }
    }
    // 创建PDU的通用方法
    private PDU createPDU(OID oid, Variable value, int pduType) {
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(oid, value));
        pdu.setType(pduType);
        return pdu;
    }
}