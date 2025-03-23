package org.snmp;

import org.snmp4j.CommunityTarget;
import org.snmp4j.Snmp;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class SnmpTop {
    public RtsTftp rtsTftp;
    public TTDownload ttDownload;
    public SnmpUtils snmpUtils;
    public SnmpTop(String address) throws IOException{
        snmpUtils = new SnmpUtils(address);
        ttDownload = new TTDownload(snmpUtils);
        rtsTftp = new RtsTftp(snmpUtils,ttDownload);

    }
}
