package org.snmp.mibnode;

import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.smi.*;

import java.io.IOException;

// MIB节点基类
public class SysNameNode extends MibNode{
    private static final OID SYS_NAME_OID = new OID("1.3.6.1.2.1.1.5.0");
    public SysNameNode() {
    }
    public Variable getValue() throws IOException {
        return get(SYS_NAME_OID);
    }
    public void setValue() {
    }
}