package com.example.sdksamples;

import com.impinj.octane.OctaneSdkException;
import com.impinj.octane.RshellEngine;
import com.impinj.octane.RshellReply;

public class RshellExample {

    static String defaultCmd = "show system platform";

    public static void main(String[] args) {
        try {
            String hostname = System.getProperty(SampleProperties.hostname);

            if (hostname == null) {
                throw new Exception("Must specify the '"
                        + SampleProperties.hostname + "' property");
            }

            String cmd = defaultCmd;

            RshellEngine rshell = new RshellEngine();

            /* login can take some time to give username and password */
            rshell.openSecureSession(hostname, "root", "impinj", 10000);

            System.out.println("Sending command '" + cmd + "' to " + hostname);
            String reply = rshell.send(cmd);

            System.out.println("Raw Reply");
            System.out.print(reply);
            System.out.println("\n\n");

            // parse the output. This works on most commands
            RshellReply r = new RshellReply(reply);

            String status = r.get("StatusString");

            if (status != null) {
                System.out.println("Command returned: " + status);

                if (status.equals("Success")) {
                    String uptime = r.get("UptimeSeconds");
                    if (uptime != null) {
                        System.out.println("Uptime for unit is: " + uptime
                                + " seconds");
                    }
                }
            }

            rshell.close();

        } catch (OctaneSdkException ex) {
            System.out.print(ex.toString());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace(System.out);
        }

    }
}
