package org.snmp.mibnode;

import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Integer32;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static org.snmp.SnmpTop.addTrapHandler;

public class GetSchedule extends MibNode {
    public static final OID GET_SCHEDULE_PORT_OID = new OID(".1.3.6.1.4.1.77697.1.0");
    public static final OID GET_SCHEDULE_NOTIFICATION_OID = new OID(".1.3.6.1.4.1.77697.2.0");
    public static final OID GET_SCHEDULE_DATA_OID = new OID(".1.3.6.1.4.1.77697.3.0");

    private int port;

    // 数据结构定义
    public static class ScheduleItem {
        public long recStart;
        public long recEnd;
        public short flowId;
        public short bufId;
        public short itemId;
        public short flowLength;
        public byte port;
        public byte endPort;
        public byte revSendPort;
        public int revOffset;
    }

    public static class ScheduleData {
        public List<ScheduleItem> receiveItems = new ArrayList<>();
        public List<ScheduleItem> sendItems = new ArrayList<>();
    }

    public GetSchedule() {
        registerTrapHandlers();
    }

    private void registerTrapHandlers() {
        addTrapHandler(GET_SCHEDULE_DATA_OID, variable -> {
            if (variable instanceof OctetString) {
                byte[] data = ((OctetString) variable).getValue();
                ScheduleData schedule = parseScheduleData(data);

                if (schedule != null) {
                    // 打印表头
                    System.out.println("\n\n-------------------------------------");
                    System.out.printf("-%8s PORT %s GET SCHEDULE %6s-\n", "", port, "");
                    System.out.println("-------------------------------------");
                    printScheduleItems(port, "receive", schedule.receiveItems);
                    printScheduleItems(port, "send", schedule.sendItems);
                }
            }
        });
    }

    private static ScheduleData parseScheduleData(byte[] data) {
        try {
            ByteBuffer bb = ByteBuffer.wrap(data);
            bb.order(ByteOrder.BIG_ENDIAN); // 网络字节序是大端

            ScheduleData schedule = new ScheduleData();

            // 解析接收调度表
            int recCount = bb.getShort() & 0xFFFF;
            for (int i = 0; i < recCount; i++) {
                schedule.receiveItems.add(parseScheduleItem(bb));
            }

            // 解析发送调度表
            int sendCount = bb.getShort() & 0xFFFF;
            for (int i = 0; i < sendCount; i++) {
                schedule.sendItems.add(parseScheduleItem(bb));
            }

            return schedule;
        } catch (Exception e) {
            System.err.println("解析调度表数据失败: " + e.getMessage());
            return null;
        }
    }

    private static ScheduleItem parseScheduleItem(ByteBuffer bb) {
        ScheduleItem item = new ScheduleItem();
        item.recStart = bb.getLong();
        item.recEnd = bb.getLong();
        item.flowId = bb.getShort();
        item.bufId = bb.getShort();
        item.itemId = bb.getShort();
        item.flowLength = bb.getShort();
        item.port = bb.get();
        item.endPort = bb.get();
        item.revSendPort = bb.get();
        item.revOffset = bb.getInt();
        return item;
    }

    private static void printScheduleItems(int port, String type, List<ScheduleItem> items) {
        final String ITEM_HEADER = "%-12s: %10s\n";
        final String ITEM_FIELD = "%-12s: %10d\n";

        System.out.printf("*****Port %s Get %d %s schedule item*****\n\n",
                port, items.size(), type.toLowerCase());

        // 打印每个表项
        for (int i = 0; i < items.size(); i++) {
            ScheduleItem item = items.get(i);

            System.out.printf(ITEM_HEADER,
                    type.substring(0, 1).toUpperCase() + type.substring(1) + " item",
                    String.format("%5d", i + 1));

            if (type.equalsIgnoreCase("receive")) {
                System.out.printf(ITEM_FIELD, "rec_start", item.recStart);
            } else {
                System.out.printf(ITEM_FIELD, "send_pit", item.recStart);
            }

            System.out.printf(ITEM_FIELD, "Period", item.recEnd);
            System.out.printf(ITEM_FIELD, "Flow_id", item.flowId);
            System.out.printf(ITEM_FIELD, "buf_id", item.bufId);
            System.out.printf(ITEM_FIELD, "item_id", item.itemId);
            System.out.printf(ITEM_FIELD, "length", item.flowLength);

            if (type.equalsIgnoreCase("receive")) {
                System.out.printf(ITEM_FIELD, "rx_endport", item.endPort);
                System.out.printf(ITEM_FIELD, "sendport", item.revSendPort);
                System.out.printf(ITEM_FIELD, "hold time", item.revOffset);
            } else {
                System.out.printf(ITEM_FIELD, "tx_endport", item.endPort);
            }
            System.out.println();
        }
    }

    private Variable getValue() throws IOException {
        return null;
    }


    // 输入为 1-8，实际使用为 0-7
    public void setValue(int port) throws IOException {
        if (port >= 1 && port <= 8) {
            this.port = port;
            set(GET_SCHEDULE_PORT_OID, port);
        } else {
            System.err.println("错误: 端口号必须在1-8之间");
        }
    }
}