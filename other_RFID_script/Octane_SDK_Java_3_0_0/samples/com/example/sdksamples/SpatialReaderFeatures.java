package com.example.sdksamples;

import com.impinj.octane.ImpinjReader;
import com.impinj.octane.OctaneSdkException;
import com.impinj.octane.Settings;
import com.impinj.octane.Status;

import java.io.DataInputStream;
import java.util.Scanner;

public class SpatialReaderFeatures {

    public static void main(String[] args) {

        try {
            String hostname = System.getProperty(SampleProperties.hostname);

            if (hostname == null) {
                throw new Exception("Must specify the '"
                        + SampleProperties.hostname + "' property");
            }

            ImpinjReader reader = new ImpinjReader();

            reader.connect(hostname);

            Settings settings = reader.queryDefaultSettings();
            reader.applySettings(settings);

            System.out.println("Enabling Spatial Reader Beacon");

            reader.turnBeaconOn(10000);

            System.out.println("Press enter to continue.");
            Scanner s = new Scanner(System.in);
            s.nextLine();

            reader.turnBeaconOff();

            System.out.println("Querying Tilt sensor. Press enter key to exit");

            DataInputStream dis = new DataInputStream(System.in);

            while (dis.available() == 0) {

                Status status = reader.queryStatus();
                if (status.getTiltSensorValue() != null) {
                    System.out.println("Tilt: x-"
                            + status.getTiltSensorValue().getxAxis() + " y-"
                            + status.getTiltSensorValue().getyAxis());
                } else {
                    System.out.println("No Tilt Status Available");
                }
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
