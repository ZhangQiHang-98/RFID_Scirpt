package com.example.sdksamples;

import com.impinj.octane.*;

import java.util.Scanner;


public class QT {

    ImpinjReader reader;

    public static void main(String[] args) {

        QT qt = new QT();

        qt.run();
    }

    void setQtMode(QtDataProfile mode, QtAccessRange range,
                   QtPersistence persistence) throws OctaneSdkException {
        TagOpSequence seq = new TagOpSequence();
        // just try once in this example
        seq.setExecutionCount((short) 1);

        // Use target tag to only apply to some EPCs
        String targetEpc = System.getProperty(SampleProperties.targetTag);

        if (targetEpc != null) {
            seq.setTargetTag(new TargetTag());
            seq.getTargetTag().setBitPointer(BitPointers.Epc);
            seq.getTargetTag().setMemoryBank(MemoryBank.Epc);
            seq.getTargetTag().setData(targetEpc);
        } else {
            // or just send NULL to apply to all tags
            seq.setTargetTag(null);
        }

        TagQtSetOp tq = new TagQtSetOp();
        tq.setAccessRange(range);
        tq.setDataProfile(mode);
        tq.setPersistence(persistence);

        seq.getOps().add(tq);
        reader.addOpSequence(seq);
    }

    void run() {

        try {
            String hostname = System.getProperty(SampleProperties.hostname);

            if (hostname == null) {
                throw new Exception("Must specify the '"
                        + SampleProperties.hostname + "' property");
            }

            String qtModeStr = System.getProperty(SampleProperties.qtMode);
            if (qtModeStr == null) {
                throw new Exception("Must specify the '"
                        + SampleProperties.qtMode + "' property");
            }

            short qtMode = (short) Integer.parseInt(qtModeStr);

            reader = new ImpinjReader();

            // Connect
            System.out.println("Connecting to " + hostname);
            reader.connect(hostname);

            // Get the default settings
            Settings settings = reader.queryDefaultSettings();

            settings.getReport().setIncludeAntennaPortNumber(true);

            // Apply the new settings
            reader.applySettings(settings);

            QtDataProfile profile = (qtMode == 1) ? QtDataProfile.Private
                    : QtDataProfile.Public;
            setQtMode(profile, QtAccessRange.NormalRange,
                    QtPersistence.Permanent);

            // set up listeners to hear stuff back from SDK

            // this one is optional but you can have it on to prove the tag
            // change Qt mode
            reader.setTagReportListener(new TagReportListenerImplementation());

            // this one gets the operation complete on the QT
            reader.setTagOpCompleteListener(
                    new TagOpCompleteListenerImplementation());

            // Start the reader
            reader.start();

            System.out.println("Press Enter to exit.");
            Scanner s = new Scanner(System.in);
            s.nextLine();

            System.out.println("Stopping  " + hostname);
            reader.stop();

            System.out.println("Disconnecting from " + hostname);
            reader.disconnect();

            System.out.println("Done");
        } catch (OctaneSdkException ex) {
            System.out.println(ex.getMessage());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace(System.out);
        }
    }
}
