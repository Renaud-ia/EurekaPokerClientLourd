package analyzor.modele.licence;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.ComputerSystem;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.*;

import java.util.Objects;
class CleMachine {
    static String generer() {
        SystemInfo systemInfo = new SystemInfo();
        OperatingSystem operatingSystem = systemInfo.getOperatingSystem();
        HardwareAbstractionLayer hardwareAbstractionLayer = systemInfo.getHardware();
        CentralProcessor centralProcessor = hardwareAbstractionLayer.getProcessor();
        ComputerSystem computerSystem = hardwareAbstractionLayer.getComputerSystem();

        String vendor = operatingSystem.getManufacturer();
        String processorSerialNumber = computerSystem.getSerialNumber();
        String processerIdentifier = centralProcessor.getProcessorIdentifier().getIdentifier();
        int processors = centralProcessor.getLogicalProcessorCount();

        String delimiter = "@";

        String cleMachine = vendor + delimiter +
                processorSerialNumber + delimiter +
                processerIdentifier + delimiter +
                processors;

        return cleMachine.replace(" ", "");
    }


    static boolean verifier(String cleMachine) {
        return Objects.equals(cleMachine, generer());
    }

    public static void main(String[] args) {
        System.out.println(generer());
        System.out.println(verifier(generer()));
    }
 }
