package net.tehhobbit;

import net.tehhobbit.pollers.InterfacePoller;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

public class Main {

    private static Logger log = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        BasicConfigurator.configure();
        // Should be replaced with a rest client
        String[] hostNames = {
                "1.1.1.1",
                "2.2.2.2",
                "3.3.3.3",
                "4.4.4.4",
                "5.5.5.5",

        };
        PollerPool pool = new PollerPool(2);
        for (String host : hostNames) {
            SnmpClient client = new SnmpClient(host);
            client.setCommunity("public");
            client.init();
            InterfacePoller p = new InterfacePoller(host, client);
            pool.put(new InterfacePoller(host, client));
            log.info("Added " + p + " to queue");
        }
        pool.close();

    }
}
