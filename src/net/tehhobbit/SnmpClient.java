package net.tehhobbit;

import org.apache.log4j.Logger;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;

public class SnmpClient {
    private static Logger log = Logger.getLogger(SnmpClient.class.getName());
    private String hostName;
    private String community;
    private int snmpVersion = SnmpConstants.version2c;
    private int port = 161;
    private CommunityTarget comTarget = null;
    private TransportMapping transport = null;
    private Snmp snmp = null;

    public SnmpClient(String host) {

        hostName = host;
    }

    public Boolean isInitialized() {
        if (snmp != null) {
            return (true);
        } else {
            return (false);
        }
    }

    public Boolean init() {
        try {
            transport = new DefaultUdpTransportMapping();
            transport.listen();
        } catch (IOException e) {
            log.error("Failed initialize snmp object " + hostName);
            return (false);
        }

        // Create Target Address object
        comTarget = new CommunityTarget();
        comTarget.setCommunity(new OctetString(community));
        comTarget.setVersion(snmpVersion);
        comTarget.setAddress(new UdpAddress(hostName + "/" + port));
        comTarget.setRetries(2);
        comTarget.setTimeout(1000);
        snmp = new Snmp(transport);
        return (true);

    }

    public String getHostName() {
        return hostName;
    }

    public String getCommunity() {
        return community;
    }

    public String setCommunity(String snmp_com) {
        community = snmp_com;
        return community;
    }

    public PDU poll(String oidValue) throws Exception {
        // Create TransportMapping and Listen

        // Create the PDU object
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(oidValue)));
        pdu.setType(PDU.GET);
        pdu.setRequestID(new Integer32(1));

        // Create Snmp object for sending data to Agent

        log.debug("Sending Request to Agent " + hostName);
        ResponseEvent response = snmp.get(pdu, comTarget);

        // Process Agent Response
        if (response != null) {
            log.debug("Got Response from Agent " + hostName);
            PDU responsePDU = response.getResponse();

            if (responsePDU != null) {
                int errorStatus = responsePDU.getErrorStatus();
                int errorIndex = responsePDU.getErrorIndex();
                String errorStatusText = responsePDU.getErrorStatusText();

                if (errorStatus == PDU.noError) {
                    return responsePDU;
                } else {
                    log.error("Agent error (" + hostName + "): Request failed for host ");
                    log.error("Agent error status (" + hostName + ") = " + errorStatus);
                    log.error("Agent error Index (" + hostName + ") = " + errorIndex);
                    log.error("Error Status Text (" + hostName + ") = " + errorStatusText);
                }
            } else {
                log.error("Error: Response PDU is null for host: " + hostName);
            }
        } else {
            log.error("Error: Agent Timeout for host " + hostName);
        }
        snmp.close();
        return (null);
    }

}
