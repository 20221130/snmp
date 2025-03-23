package org.snmp;
import org.snmp4j.*;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.mp.SnmpConstants;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SnmpUtils implements CommandResponder {
    private Snmp snmp;
    private Address targetAddress;
    private CommunityTarget target;
    private static Map<OID, Consumer<Variable>> trapHandlers; // 存储 OID 和对应的处理逻辑
    public ExecutorService executorService = Executors.newFixedThreadPool(10); // set 操作线程池

    /**
     * 初始化 SNMP 工具类
     *
     * @param address 目标设备地址，例如 "udp:127.0.0.1/161"
     */
    public SnmpUtils(String address) throws IOException {
        targetAddress = GenericAddress.parse(address);
        trapHandlers = new HashMap<>(); // 初始化 Trap 处理器
        initialize();
    }
    /**
     * 初始化 SNMP 连接
     */
    public void initialize() throws IOException {
        TransportMapping transport = new DefaultUdpTransportMapping(new UdpAddress("192.168.83.100/162"));
        snmp = new Snmp(transport);
        snmp.addCommandResponder(this); // 添加 Trap 监听器
        transport.listen();

        // 创建统一的 target
        target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));
        target.setAddress(targetAddress);
        target.setRetries(2);
        target.setTimeout(50000);
        target.setVersion(SnmpConstants.version2c);

        System.out.println("SNMP 连接初始化成功");
    }

    /**
     * 创建 PDU
     */
    private PDU createPDU(OID oid, Variable value, int pduType) {
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(oid, value));
        pdu.setType(pduType);
        return pdu;
    }

    /**
     * 设置 OID 的值（支持多种数据类型）
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

    public void set(OID oid, InetAddress ipAddress) throws IOException {
        set(oid, new IpAddress(ipAddress));
    }

    private void set(OID oid, Variable value) throws IOException {
        PDU pdu = createPDU(oid, value, PDU.SET);

        ResponseEvent event = snmp.send(pdu, target); // 复用统一的 target
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

    /**
     * 获取 OID 的值
     */
    public String get(OID oid) throws IOException {
        PDU pdu = createPDU(oid, null, PDU.GET);

        ResponseEvent event = snmp.send(pdu, target); // 复用统一的 target
        if (event != null && event.getResponse() != null) {
            return event.getResponse().get(0).getVariable().toString();
        } else {
            System.out.println("Get 操作失败: 无响应");
            return null;
        }
    }

    /**
     * 添加 Trap 处理器
     */
    public void addTrapHandler(OID oid, Consumer<Variable> handler) {
        trapHandlers.put(oid, handler);
    }

    /**
     * 处理 Trap 消息
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
                    //System.out.println("未处理的 OID: " + oid + " = " + value);
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
                executorService.shutdown();//关闭线程池
                System.out.println("SNMP 连接已关闭");
            }
        } catch (IOException e) {
            System.err.println("关闭 SNMP 连接时出错: " + e.getMessage());
        }
    }
}