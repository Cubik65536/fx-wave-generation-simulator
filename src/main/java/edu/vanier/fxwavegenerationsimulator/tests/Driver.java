package edu.vanier.fxwavegenerationsimulator.tests;

import edu.vanier.fxwavegenerationsimulator.controllers.JsonDataController;
import edu.vanier.fxwavegenerationsimulator.controllers.WaveSimulationController;
import edu.vanier.fxwavegenerationsimulator.enums.WaveTypes;
import edu.vanier.fxwavegenerationsimulator.models.Wave;
import edu.vanier.fxwavegenerationsimulator.models.WaveSimulationDisplay;

import java.util.Map;

class WaveSimulationTestDisplay implements WaveSimulationDisplay {
    @Override
    public void update(Map<Wave, double[]> dataPoints, double milliseconds) {
        for (Wave wave : dataPoints.keySet()) {
            System.out.println("Data of " + wave.getColor() + " wave at x = 0: " + dataPoints.get(wave)[0]);
        }
    }
}

/**
 * This is the driver class to test the functionalities of the application.
 */
public class Driver {
    private static void simulationTest() {
        WaveSimulationTestDisplay waveSimulationTestDisplay = new WaveSimulationTestDisplay();
        WaveSimulationController waveSimulationController = new WaveSimulationController(10.0, waveSimulationTestDisplay);

        Wave wave1 = new Wave(WaveTypes.SIN, 1, 1);
        Wave wave2 = new Wave(WaveTypes.SIN, 1, -1);

        waveSimulationController.addWave(wave1);
        waveSimulationController.addWave(wave2);

        waveSimulationController.start();
    }

    private static void exportTest() {
        WaveSimulationTestDisplay waveSimulationTestDisplay = new WaveSimulationTestDisplay();
        WaveSimulationController waveSimulationController = new WaveSimulationController(10.0, waveSimulationTestDisplay);

        Wave wave1 = new Wave(WaveTypes.SIN, 1, 1);
        Wave wave2 = new Wave(WaveTypes.SIN, 1, -1);

        waveSimulationController.addWave(wave1);
        waveSimulationController.addWave(wave2);

        String json = JsonDataController.exportWaveSimulation(waveSimulationController);
        System.out.println(json);
    }

    private static void importTest() {
        WaveSimulationTestDisplay waveSimulationTestDisplay = new WaveSimulationTestDisplay();
        WaveSimulationController waveSimulationController = new WaveSimulationController(10.0, waveSimulationTestDisplay);

        String jsonString = "[{\"waveType\":\"SIN\",\"frequency\":1,\"amplitude\":1.0,\"color\":{\"red\":145,\"green\":16,\"blue\":215}},{\"waveType\":\"SIN\",\"frequency\":1,\"amplitude\":-1.0,\"color\":{\"red\":198,\"green\":205,\"blue\":90}}]";
        JsonDataController.importWaveSimulation(waveSimulationController, jsonString);

        waveSimulationController.start();
    }

    public static void main(String[] args) {
//        simulationTest();
//        exportTest();
        importTest();
    }
}
