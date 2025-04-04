package org.snmp;

import org.snmp.mibnode.GetSchedule;
import org.snmp.mibnode.RtsTftp;
import org.snmp.mibnode.SysNameNode;
import org.snmp.mibnode.TTDownload;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static org.snmp.SnmpConstants.SNMP_LISTEN_ADDRESS;

public class SnmpTop implements CommandResponder {
    public static final ExecutorService executorService = Executors.newFixedThreadPool(10); // set 操作线程池
    public static Snmp snmp;
    public static CommunityTarget target;
    public static Map<OID, Consumer<Variable>> trapHandlers; // 存储 OID 和对应的处理逻辑
    public RtsTftp rtsTftp;
    public TTDownload ttDownload;
    public SysNameNode sysNameNode;
    public GetSchedule getSchedule;
    public SnmpTop(String address) throws IOException {
        // snmp初始化
        initialize(address);

        // 初始化节点
        sysNameNode = new SysNameNode();
        ttDownload = new TTDownload();
        rtsTftp = new RtsTftp(ttDownload);
        getSchedule = new GetSchedule();
    }
    public void initialize(String address) throws IOException{
        // 初始化target
        target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));
        target.setAddress(GenericAddress.parse(address));
        target.setRetries(2);
        target.setTimeout(50000);
        target.setVersion(SnmpConstants.version2c);

        // 创建UDP传输通道
        TransportMapping transport = new DefaultUdpTransportMapping(new UdpAddress(SNMP_LISTEN_ADDRESS));

        // 创建snmp对象
        snmp = new Snmp(transport);

        // 添加udp端口监听
        snmp.addCommandResponder(this);
        transport.listen();

        // 初始化trap处理逻辑数组
        trapHandlers = new HashMap<>();

        System.out.println("SNMP 连接初始化成功");
    }
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
    public static void addTrapHandler(OID oid, Consumer<Variable> handler) {
        trapHandlers.put(oid, handler);
    }

    public static void close() {
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
