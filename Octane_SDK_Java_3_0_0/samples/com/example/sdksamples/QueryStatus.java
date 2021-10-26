package com.example.sdksamples;

import com.impinj.octane.*;

public class QueryStatus {

    public static void main(String[] args) {

        try {
            String hostname = System.getProperty(SampleProperties.hostname);

            if (hostname == null) {
                throw new Exception("Must specify the '"
                        + SampleProperties.hostname + "' property");
            }

            ImpinjReader reader = new ImpinjReader();

            reader.connect(hostname);

            System.out.println("Querying reader status");
            Status status = reader.queryStatus();

            System.out.println("Reader Temperature: "
                    + status.getTemperatureCelsius());
            System.out.println("Singulating: " + status.getIsSingulating());
            System.out.println("Connected:" + status.getIsConnected());

            System.out.println("Antenna Status");
            for (AntennaStatus as :
                    status.getAntennaStatusGroup().getAntennaList()) {
                System.out.println("  Antenna " + as.getPortNumber()
                        + " status " + as.isConnected());
            }

            System.out.println("GPI Status");
            for (GpiStatus gs : status.getGpiStatusGroup().getGpiList()) {
                System.out.println("  GPI " + gs.getPortNumber() + " status "
                        + gs.isState());
            }

            System.out.println("Antenna Hub Status");
            for (AntennaHubStatus ahs :
                    status.getAntennaHubStatusGroup().getAntennaHubList()) {
                System.out.println("  Hub " + ahs.getHubId() + " connected "
                        + ahs.getConnected() + " fault " + ahs.getFault());
            }

            if (status.getTiltSensorValue() != null) {
                System.out.println("Tilt:  x-"
                        + status.getTiltSensorValue().getxAxis() + " y-"
                        + status.getTiltSensorValue().getyAxis());
            }
            reader.disconnect();
        } catch (OctaneSdkException ex) {
            System.out.println(ex.getMessage());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace(System.out);
        }
    }
}
