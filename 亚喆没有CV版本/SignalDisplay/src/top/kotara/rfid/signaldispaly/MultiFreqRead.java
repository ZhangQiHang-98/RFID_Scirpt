package top.kotara.rfid.signaldispaly;
import com.impinj.octane.*;
public class MultiFreqRead {
    /*
    public static void main(String[] args) {
        try {
            String hostname="192.168.1.27";
            if (hostname == null) {
                throw new Exception("Must specify the '"
                        + SampleProperties.hostname + "' property");
            }

            ImpinjReader reader = new ImpinjReader();
            for(int i=0;i<16;i++) {
                SampleProperties.currMHZ = SampleProperties.mHz[i];
                for(int j=0;j<3;j++) {
                    SampleProperties.outfilename = "190331-group-" +i+"-"+j+".csv";
                    System.out.println(SampleProperties.outfilename);
                    // Connect
                    while (!reader.isConnected()) {
                        try {
                            System.out.println("Connecting to " + hostname);
                            reader.connect(hostname);
                        } catch (OctaneSdkException e1) {
                            System.out.println("******************************************************************" + e1.getMessage());
                        } catch (Exception e2) {
                            System.out.println("******************************************************************" + e2.getMessage());
                            e2.printStackTrace(System.out);
                        }
                    }
                    // Get the default settings
                    Settings settings = reader.queryDefaultSettings();
                    // settings.save("./impinj-setting.txt");
                    settings.getTxFrequenciesInMhz().add(SampleProperties.currMHZ);
                    //settings.getReport().setMode(ReportMode.Individual);
                    settings.setReaderMode(ReaderMode.MaxThroughput);
                    System.out.println("----------------------------" + settings.getTxFrequenciesInMhz());
                    // send a tag report for every tag read

                    AntennaConfigGroup antennaConfigs = settings.getAntennas();
                    antennaConfigs.disableAll();
                    antennaConfigs.setIsMaxTxPower(false);
                    antennaConfigs.setTxPowerinDbm(SampleProperties.txPower);
                    antennaConfigs.enableAll();

                    settings.getReport().setMode(ReportMode.Individual);
                    settings.getReport().setIncludeChannel(true);
                    settings.getReport().setIncludePhaseAngle(true);
                    settings.getReport().setIncludePeakRssi(true);
                    settings.getReport().setIncludeLastSeenTime(true);
                    settings.getReport().setIncludeAntennaPortNumber(true);
                    settings.getReport().setIncludeSeenCount(true);

                    // set up low duty cycle mode
                    LowDutyCycleSettings ldc = settings.getLowDutyCycle();

                    ldc.setEmptyFieldTimeoutInMs(2000);
                    ldc.setFieldPingIntervalInMs(1000);
                    ldc.setIsEnabled(false);

                    // Apply the new settings
                    reader.applySettings(settings);

                    // connect a listener
//                //操作设置
//                reader.setTagOpCompleteListener(new OnTagOpCompleteListener());
//                TagOpSequence seq=new TagOpSequence();
//                seq.setOps(new ArrayList<TagOp>());
//                seq.setExecutionCount((short) 1);
//                seq.setState(SequenceState.Active);
//                seq.setId(1);
//
//
//                //设置目标EPC
//                String targetEpc = System.getProperty("EA01");
//
//                if (targetEpc != null) {
//                    seq.setTargetTag(new TargetTag());
//                    seq.getTargetTag().setBitPointer(BitPointers.Epc);
//                    seq.getTargetTag().setMemoryBank(MemoryBank.Epc);
//                    seq.getTargetTag().setData(targetEpc);
//                } else {
//                    // or just send NULL to apply to all tags
//                    seq.setTargetTag(null);
//                }
//
//
//                TagWriteOp writeOp=new TagWriteOp();
//
//                writeOp.setAccessPassword(null);
//                writeOp.setMemoryBank(MemoryBank.Reserved);
//                writeOp.setWordPointer(WordPointers.AccessPassword);
//                writeOp.setData(TagData.fromHexString("11112222"));
//
//                seq.getOps().add(writeOp);
//
//                TagLockOp lockAccessPwOp=new TagLockOp();
//                lockAccessPwOp.setAccessPasswordLockType(TagLockState.Lock);
//
//                seq.getOps().add(lockAccessPwOp);
//
//                TagLockOp lockUserOp=new TagLockOp();
//                lockUserOp.setEpcLockType(TagLockState.Lock);
//
//                seq.getOps().add(lockUserOp);
//                reader.addOpSequence(seq);
                    reader.setTagReportListener(new TagReportListenerImplementation());
                    reader.setReaderStartListener(new OnReaderModifiedListener());
                    reader.setReaderStopListener(new OnReaderModifiedListener());
                    reader.start();
                    Thread.sleep(50000);

                    while (reader.isConnected()) {
                        try {
                            System.out.println("-------------------------------------------Stopping  " + hostname);
                            reader.stop();
                            System.out.println("-------------------------------------------Disconnecting from " + hostname);
                            reader.disconnect();
                        } catch (OctaneSdkException e1) {
                            System.out.println("******************************************************************" + e1.getMessage());
                        } catch (Exception e2) {
                            System.out.println("******************************************************************" + e2.getMessage());
                            e2.printStackTrace(System.out);
                        }

                    }
                }
            }
            System.out.println("-----------Done-------------------");
        } catch (OctaneSdkException ex) {
            System.out.println("******************************************************************"+ex.getMessage());
        } catch (Exception ex) {
            System.out.println("******************************************************************"+ex.getMessage());
            ex.printStackTrace(System.out);
        }
        System.exit(0);
    }
*/
}
