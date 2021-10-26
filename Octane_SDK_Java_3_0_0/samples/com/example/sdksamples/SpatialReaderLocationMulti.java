package com.example.sdksamples;

import com.impinj.octane.*;

import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class SpatialReaderLocationMulti {
    // Shared between Both Roles
    final ReaderMode READER_MODE = ReaderMode.AutoSetDenseReaderDeepScan;   // Recommended mode for spatial reader
    final short COMPUTE_WINDOW_SEC = 30;                                      // Medium Compute Window, lengthen for more accuracy,
    // shorten for just a few moving tags
    final short TAG_AGE_SEC = 2 * COMPUTE_WINDOW_SEC;                  // 2 X COMPUTE_WINDOW is typical
    final short UPDATE_INTERVAL_SEC = 10;                  // 2 X COMPUTE_WINDOW is typical
    // This example does a weighted average with just 2 spatialReaders.  You can additional spatialReaders by adding spatial reader elements to the
    // 2 spatialReaders defined in spatialReaders[] below:
    //                                       Reader       HeightCM, FacXcm, FacYcm, Orient, Session
    SpatialReader spatialReaders[] = {new SpatialReader("xarray-XX-XX-XX", (short) 300, 0, 0, (short) 0, 2),
            new SpatialReader("xarray-XX-XX-XX", (short) 300, 0, 400, (short) 0, 3)};
    // Use dictionaries to store Confidence, WeightedX and WeightedY and Cycle Lengths.
    HashMap<String, Integer> cycleLengths = new HashMap<String, Integer>();
    HashMap<String, TagReadInfo> tagReadInfos = new HashMap<String, TagReadInfo>();

    public SpatialReaderLocationMulti() {
        ImpinjReader[] readers = new ImpinjReader[spatialReaders.length];
        for (int i = 0; i < readers.length; i++) {
            readers[i] = new ImpinjReader();
            LaunchSpatialReader(readers[i], spatialReaders[i]);
        }

        System.out.println("Press Enter to exit.");
        Scanner s = new Scanner(System.in);
        s.nextLine();
        s.close();

        for (int i = 0; i < spatialReaders.length; i++) {
            CloseSpatialReader(readers[i]);
        }
    }

    public static void main(String[] args) {
        new SpatialReaderLocationMulti();
    }

    public void LaunchSpatialReader(ImpinjReader reader, SpatialReader spatialReader) {
        try {
            // Connect to the reader.
            // Change the ReaderHostname constant in SolutionConstants.cs
            // to the IP address or hostname of your reader.
            reader.connect(spatialReader.Hostname);

            // Assign the LocationReported event handler.
            // This specifies which method to call
            // when a location report is available.
            reader.setLocationReportListener(new LocationReportListenerImplementation());
            // Don't forget to define diagnostic method
            reader.setDiagnosticsReportListener(new DiagnosticsReportListenerImplementation());

            // Apply the newly modified settings.
            reader.applySettings(GetPrepareSettings(reader, spatialReader));

            // Start the reader
            reader.start();
        } catch (OctaneSdkException e) {
            // Handle Octane SDK errors.
            System.out.println("Octane SDK exception: " + e.getMessage() + " Hostname=" + spatialReader.Hostname);
        } catch (Exception e) {
            // Handle other .NET errors.
            System.out.println("Exception : " + e.getMessage());
        }
    }

    public Settings GetPrepareSettings(ImpinjReader reader, SpatialReader spatialReader) {
        // Get the default settings
        // We'll use these as a starting point
        // and then modify the settings we're
        // interested in.
        Settings settings = reader.queryDefaultSettings();

        // Put the spatialReader into location mode
        settings.getSpatialConfig().setMode(SpatialMode.Location);

        LocationConfig locationConfig = settings.getSpatialConfig().getLocation();
        // Enable all three report types
        locationConfig.setEntryReportEnabled(true);
        locationConfig.setUpdateReportEnabled(true);
        locationConfig.setExitReportEnabled(true);
        // Enable Diagnostic reports here, soon to be deprecated
        locationConfig.setDiagnosticReportEnabled(true);

        // Set spatialReader placement parameters
        // The mounting height of the spatialReader, in centimeters
        PlacementConfig placementConfig = settings.getSpatialConfig().getPlacement();
        placementConfig.setHeightCm(spatialReader.Height);
        placementConfig.setFacilityXLocationCm(spatialReader.FacilityXcm);
        placementConfig.setFacilityYLocationCm(spatialReader.FacilityYcm);
        placementConfig.setOrientationDegrees(spatialReader.Orientation);

        // Compute Window and Gen2 Settings
        locationConfig.setComputeWindowSeconds(COMPUTE_WINDOW_SEC);
        settings.setReaderMode(ReaderMode.AutoSetDenseReader);
        settings.setSession(spatialReader.Session);
        locationConfig.setTagAgeIntervalSeconds(TAG_AGE_SEC);

        // Specify how often we want to receive location reports
        locationConfig.setUpdateIntervalSeconds(UPDATE_INTERVAL_SEC);

        // Set filter if needed
     /* TagFilter t1 = settings.getFilters().getTagFilter1();
        t1.setBitCount(16);
        t1.setBitPointer(BitPointers.Epc);
        t1.setMemoryBank(MemoryBank.Epc);
        t1.setFilterOp(TagFilterOp.Match);
        t1.setTagMask("9999");
        settings.getFilters().setMode(TagFilterMode.OnlyFilter1);
      */
        return settings;
    }

    private void CloseSpatialReader(ImpinjReader reader) {
        // Apply the default settings before exiting.
        try {
            reader.applyDefaultSettings();
        } catch (OctaneSdkException e) {
            e.printStackTrace();
        }
        // Disconnect from the reader.
        reader.disconnect();
    }

    class DiagnosticsReportListenerImplementation implements DiagnosticsReportListener {
        public void onDiagnosticsReported(ImpinjReader reader, DiagnosticReport report) {
            List<Integer> reportMetricsList = report.getMetrics();
            // Warning!!! Accessing diagnostic codes will not be supported in future releases.
            if (reportMetricsList.get(0) == 100) { // End of Cycle
                System.out.println("Spatial reader=" + reader.getAddress() + "  CycleTime=" + reportMetricsList.get(1) / 1000 + "ms");
                // Store latest Cycle time
                cycleLengths.put(reader.getAddress(), reportMetricsList.get(1));
            }
        }
    }

    class LocationReportListenerImplementation implements LocationReportListener {
        public void onLocationReported(ImpinjReader reader, LocationReport report) {
            String EpcStr = report.getEpc().toHexString();

            // Compute confidence. Make sure that the first cycle report came in before computing the Weighted averages.
            if (!cycleLengths.containsKey(reader.getAddress()) || cycleLengths.get(reader.getAddress()) == 0)
                return;

            // If first time
            if (!tagReadInfos.containsKey(EpcStr))
                tagReadInfos.put(EpcStr, new TagReadInfo());

            double mult = Math.floor(((double) COMPUTE_WINDOW_SEC * 1000000)/ cycleLengths.get(reader.getAddress()));
            if (mult == 0) mult = 1;
            double confidence = report.getConfidenceFactors().getReadCount() / mult;
            System.out.println(reader.getAddress() + "  " + EpcStr + " x=" + report.getLocationXCm() + " y=" + report.getLocationYCm() + " conf=" + confidence);
            // Weighted X
            double wgtX = confidence * report.getLocationXCm();
            double wgtY = confidence * report.getLocationYCm();
            // Sum the weighted averages
            TagReadInfo tagReadInfo = tagReadInfos.get(EpcStr);
            tagReadInfo.setWeightedX(tagReadInfo.getWeightedX() + wgtX);
            tagReadInfo.setWeightedY(tagReadInfo.getWeightedY() + wgtY);
            tagReadInfo.setConfidence(tagReadInfo.getConfidence() + confidence);
            tagReadInfos.put(EpcStr, tagReadInfo);

            // Pick a reader to key off the Averaging calculation
            // Let's use the last one.
            if (reader.getAddress().equals(spatialReaders[spatialReaders.length - 1].Hostname)) {
                System.out.print("Weighted: " + EpcStr);
                if (tagReadInfo.getConfidence() != 0) {
                    System.out.print(" x=" + Math.floor(tagReadInfo.getWeightedX() / tagReadInfo.getConfidence()));
                    System.out.println(" y=" + Math.floor(tagReadInfo.getWeightedY() / tagReadInfo.getConfidence()));
                } else {
                    System.out.println("Invalid Read. Confidence is 0");
                }
                // Reinitialize variables
                tagReadInfos.put(EpcStr, new TagReadInfo());
            }
        }
    }

    class SpatialReader {
        public String Hostname;
        public short Height;
        public int FacilityXcm;
        public int FacilityYcm;
        public short Orientation;
        public int Session;

        public SpatialReader(String Hostname, short Height, int FacilityXcm, int FacilityYcm, short Orientation, int Session) {
            this.Hostname = Hostname;
            this.Height = Height;
            this.FacilityXcm = FacilityXcm;
            this.FacilityYcm = FacilityYcm;
            this.Session = Session;
        }

        // Getters and Setters
        public String getHostname() {
            return Hostname;
        }

        public void setHostname(String Hostname) {
            this.Hostname = Hostname;
        }

        public short getHeight() {
            return Height;
        }

        public void setHeight(short Height) {
            this.Height = Height;
        }

        public int getFacilityXcm() {
            return FacilityXcm;
        }

        public void setFacilityXcm(int FacilityXcm) {
            this.FacilityXcm = FacilityXcm;
        }

        public int getFacilityYcm() {
            return FacilityYcm;
        }

        public void setFacilityYcm(int FacilityYcm) {
            this.FacilityYcm = FacilityYcm;
        }

        public short getOrientation() {
            return Orientation;
        }

        public void setOrientation(short Orientation) {
            this.Orientation = Orientation;
        }

        public int getSession() {
            return Session;
        }

        public void setSession(int Session) {
            this.Session = Session;
        }
    }

    class TagReadInfo {
        double confidence;
        double weightedX, weightedY;

        public TagReadInfo() {
            initialize();
        }

        public void initialize() {
            setConfidence(0);
            setWeightedX(0);
            setWeightedY(0);
        }

        // Getters and Setters
        public double getConfidence() {
            return confidence;
        }

        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }

        public double getWeightedX() {
            return weightedX;
        }

        public void setWeightedX(double weightedX) {
            this.weightedX = weightedX;
        }

        public double getWeightedY() {
            return weightedY;
        }

        public void setWeightedY(double weightedY) {
            this.weightedY = weightedY;
        }
    }
}
