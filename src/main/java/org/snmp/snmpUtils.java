package org.snmp;

import org.snmp4j.*;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class snmpUtils implements CommandResponder {
    private Snmp snmp;
    private Address targetAddress;
    private Map<OID, Consumer<Variable>> trapHandlers; // 存储 OID 和对应的处理逻辑

    /**
     * 初始化 SNMP 工具类
     *
     * @param address 目标设备地址，例如 "udp:127.0.0.1/161"
     */
    public snmpUtils(String address) {
        try {
            targetAddress = GenericAddress.parse(address);
            TransportMapping transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            snmp.addCommandResponder(this); // 添加 Trap 监听器
            transport.listen();
            trapHandlers = new HashMap<>(); // 初始化 Trap 处理器
            System.out.println("SNMP 工具类初始化成功，监听地址: " + address);
        } catch (IOException e) {
            System.err.println("SNMP 工具类初始化失败: " + e.getMessage());
        }
    }

    /**
     * 设置 OID 的值（支持多种数据类型）
     *
     * @param oid   OID
     * @param value 值
     * @throws IOException 如果通信失败
     */
    public void set(OID oid, String value) throws IOException {
        set(oid, new OctetString(value));
    }

    public void set(OID oid, int value) throws IOException {
        set(oid, new Integer32(value));
    }

    public void set(OID oid, byte[] value) throws IOException {
        set(oid, new OctetString(value));
    }

    private void set(OID oid, Variable value) throws IOException {
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(oid, value));
        pdu.setType(PDU.SET);

        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString("private"));
        target.setAddress(targetAddress);
        target.setRetries(2);
        target.setTimeout(1500);
        target.setVersion(SnmpConstants.version2c);

        ResponseEvent event = snmp.send(pdu, target);
        if (event != null && event.getResponse() != null) {
            System.out.println("Set 操作成功: " + oid + " = " + value);
        } else {
            System.out.println("Set 操作失败: 无响应");
        }
    }

    /**
     * 获取 OID 的值
     *
     * @param oid OID
     * @return 值
     * @throws IOException 如果通信失败
     */
    public String get(OID oid) throws IOException {
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(oid));
        pdu.setType(PDU.GET);

        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));
        target.setAddress(targetAddress);
        target.setRetries(2);
        target.setTimeout(1500);
        target.setVersion(SnmpConstants.version2c);

        ResponseEvent event = snmp.send(pdu, target);
        if (event != null && event.getResponse() != null) {
            return event.getResponse().get(0).getVariable().toString();
        } else {
            System.out.println("Get 操作失败: 无响应");
            return null;
        }
    }

    /**
     * 添加 Trap 处理器
     *
     * @param oid      OID
     * @param handler  处理逻辑
     */
    public void addTrapHandler(OID oid, Consumer<Variable> handler) {
        trapHandlers.put(oid, handler);
    }

    /**
     * 处理 Trap 消息
     *
     * @param event Trap 事件
     */
    @Override
    public void processPdu(CommandResponderEvent event) {
        PDU pdu = event.getPDU();
        if (pdu != null) {
            Address senderAddress = event.getPeerAddress();
            System.out.println("收到 Trap 来自: " + senderAddress);

            // 遍历 Trap 中的变量绑定
            for (VariableBinding vb : pdu.getVariableBindings()) {
                OID oid = vb.getOid();
                Variable value = vb.getVariable();

                // 检查是否有对应的处理器
                if (trapHandlers.containsKey(oid)) {
                    trapHandlers.get(oid).accept(value); // 执行处理逻辑
                } else {
                    System.out.println("未处理的 OID: " + oid + " = " + value);
                }
            }
        }
    }

    /**
     * 关闭 SNMP 连接
     */
    public void close() {
        try {
            if (snmp != null) {
                snmp.close();
                System.out.println("SNMP 连接已关闭");
            }
        } catch (IOException e) {
            System.err.println("关闭 SNMP 连接时出错: " + e.getMessage());
        }
    }
}