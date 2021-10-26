package com.example.sdksamples;

import com.impinj.octane.*;

import java.util.Scanner;

// Direction-specific imports:
// For filtering

public class SpatialReaderDirection {

    public static void main(String[] args) {
        try {
            // Specify hostname with -Dhostname=example.com when invoking java
            String hostname = System.getProperty(SampleProperties.hostname);

            if (hostname == null) {
                throw new Exception("Must specify the '"
                        + SampleProperties.hostname + "' property");
            }

            ImpinjReader reader = new ImpinjReader();
            reader.connect(hostname);
            reader.setDirectionReportListener(new DirectionReportListenerImplementation());

            Settings settings = reader.queryDefaultSettings();

            // Tells the spatial reader that we want to operate in Direction mode
            settings.getSpatialConfig().setMode(SpatialMode.Direction);

            // The spatial reader's direction feature works best with a smaller tag population,
            // so we can set up a filter on the reader to only track tags we care about.
            //
            // For example, if you run this sample with "-DtargetTag=9999" specified on
            // the command line, only tags whose EPC starts with "9999" will be tracked
            // as part of the tag direction population.
            //
            // How one sets up this filter is shown below:
            String targetEpc = System.getProperty(SampleProperties.targetTag);
            if (targetEpc != null) {
                TagFilter t1 = settings.getFilters().getTagFilter1();
                t1.setBitCount(16);
                t1.setBitPointer(BitPointers.Epc);
                t1.setMemoryBank(MemoryBank.Epc);
                t1.setFilterOp(TagFilterOp.Match);
                t1.setTagMask(targetEpc);
                settings.getFilters().setMode(TagFilterMode.OnlyFilter1);
                System.out.println("Matching 1st 16 bits of epc "
                        + targetEpc);
            }

            // Retrieve the DirectionConfig object stored on the reader so that we can
            // modify the settings we are interested in.
            DirectionConfig directionConfig = settings.getSpatialConfig().getDirection();

            // Tells the spatial reader to perform tag reads more quickly at the expense of sensitivity.
            directionConfig.setMode(DirectionMode.HighPerformance);

            // Enable the sectors you want to track tags in here. Note that you may only enable
            // non-adjacent sectors (e.g. 2 and 4, but not 2 and 3). Further note that sectors 2
            // and 9 are also considered adjacent.
            directionConfig.enableSector((short) 2);
            //directionConfig.enableSector((short) 6);
            // If xSpan, enable sector 3 instead of sector 6
            directionConfig.enableSector((short) 3);

            // Enable any reports you are interested in here. Entry reports are generated when
            // a tag is first read.  Updates are sent every "update interval" seconds indicating
            // that a tag is still visible to the reader. Exit reports are sent when a tag that
            // was seen previously, has not been read for "tag age interval" seconds. Both
            // "update interval" and "tag age interval" are set below to two and four seconds
            // respectively.
            directionConfig.setEntryReportEnabled(true);
            directionConfig.setExitReportEnabled(true);
            directionConfig.setUpdateReportEnabled(false);

            // Tells the spatial reader we want to track tags in as wide of an area as possible,
            // though a NARROW field of view is also available.
            directionConfig.setFieldOfView(DirectionFieldOfView.WIDE);

            // Sets our application to only receive tag updates (or heartbeats) every two seconds.
            directionConfig.setUpdateIntervalSeconds((short) 2);

            // Sets our application to only receive a tag's exit report after it has not been read
            // in any sector for four seconds.
            directionConfig.setTagAgeIntervalSeconds((short) 4);

            // Pushes our specified configuration to the reader. If the set of enabled sectors violates the rules specified above,
            // an OctaneSDKException will be thrown here.
            reader.applySettings(settings);

            // Initiates our application and we should start to receive direction reports.
            reader.start();

            // The application will terminate when the "Enter" key is pressed.
            System.out.println("Press enter to continue.");
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

    // This static nested class provides the DirectionReportListener implementation
    // to actually receive direction reports.
    public static class DirectionReportListenerImplementation implements
            DirectionReportListener {

        // This function is invoked when a DirectionReport is dispatched. In this example,
        // we simply print the contents of the report to the console.
        public void onDirectionReported(ImpinjReader reader, DirectionReport report) {
            System.out.println(report.toString());
        }
    }
}
