package com.example.sdksamples;

import com.impinj.octane.*;

import java.util.Scanner;

public class ReadTagsOverTLS {
    public static void main(String[] args) {
        try {
            String hostname = System.getProperty(SampleProperties.hostname);

            if (hostname == null) {
                throw new Exception("Must specify the '"
                        + SampleProperties.hostname + "' property");
            }

            ImpinjReader reader = new ImpinjReader();

            // To connect to a reader over TLS, the reader must first be configured to use encryption.
            // To do this, connect to the reader and enter the following rshell command :
            // 
            //     config rfid llrp inbound tcp security encrypt
            // 
            // If using the default settings, this command will both enable encryption, and change the 
            // port over which the LLRP connection will be made from 5084 to 5085. It should be noted
            // that the reader considers ports 5084 and 5085 to be special : 
            // 5084 will ONLY allow unsecure connections, and
            // 5085 will ONLY allow encrypted connections
            System.out.println("Connecting");

            final boolean useTLS = true;
            reader.connect(hostname, useTLS);

            // If these default ports are not desired, the reader can be configured to use a custom port 
            // to facilitate an LLRP connection by using the following rshell command.
            // 
            //     config rfid llrp inbound tcp port <custom port>
            // 
            // Example :
            // 
            //     config rfid llrp inbound tcp port 9999
            // 
            // Then the following method can be used from the SDK to connect to the reader through the custom
            // port :
            // 
            // final int customPort = 9999;
            // reader.connect(hostname, customPort, useTLS);

            reader.setTagReportListener(new TagReportListenerImplementation());

            System.out.println("Applying Settings");
            reader.applySettings(reader.queryDefaultSettings());

            System.out.println("Starting");
            reader.start();

            System.out.println("Press Enter to exit.");
            Scanner s = new Scanner(System.in);
            s.nextLine();
            s.close();

            reader.stop();
            reader.disconnect();
        } catch (OctaneSdkException ex) {
            System.out.println(ex.getMessage());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace(System.out);
        }
    }
}
