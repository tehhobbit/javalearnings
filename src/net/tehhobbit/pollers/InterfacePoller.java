package net.tehhobbit.pollers;

import net.tehhobbit.SnmpClient;
import org.apache.log4j.Logger;

public class InterfacePoller implements Runnable {
    private static Logger log = Logger.getLogger(InterfacePoller.class.getName());
    public String hostName;
    SnmpClient snmpClient;

    public InterfacePoller(String hostName, SnmpClient snmpClient) {
        this.hostName = hostName;
        this.snmpClient = snmpClient;
        log.info(this);
    }

    public void run() {
        log.debug("Running interface poller for " + hostName);
        log.info("Doing things with interfaces ");
        try {
            snmpClient.poll(".1");
        } catch (Exception e) {
            log.error(e.toString());
        }
    }
}

