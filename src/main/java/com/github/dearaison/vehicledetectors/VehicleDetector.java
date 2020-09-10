package com.github.dearaison.vehicledetectors;

import lombok.Getter;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import static com.github.dearaison.utilities.Utilities.getAbsolutePathOfLocalResource;

/**
 * Created by IntelliJ on Wednesday, 19 August, 2020 at 13:03.
 *
 * @author Joseph Maria
 */
public class VehicleDetector {
    // Cascade File
    private static final String frontCarFile = "car.xml";
    private static final String frontTruckFile = "frontBus.xml";
    private static final String frontBusFile = "frontTruck.xml";
    private static final String backCarFile = "backCar.xml";
    private static final String backBusFile = "backBus.xml";
    private final Mat grayFrame = new Mat();
    // Cascade classifier
    private CascadeClassifier frontCarClassifier;
    private CascadeClassifier frontTruckClassifier;
    private CascadeClassifier frontBusClassifier;
    private CascadeClassifier backCarClassifier;
    private CascadeClassifier backBusClassifier;

    // Result
    @Getter
    private MatOfRect frontCarRects;
    @Getter
    private MatOfRect frontTruckRects;
    @Getter
    private MatOfRect frontBusRects;
    @Getter
    private MatOfRect backCarRects;
    @Getter
    private MatOfRect backBusRects;

    // Threads
    private Runnable frontCarRunable;
    private Runnable frontTruckRunable;
    private Runnable frontBusRunable;
    private Runnable backCarRunable;
    private Runnable backBusRunable;


    public VehicleDetector(boolean frontSide) {
        if (frontSide) {
            frontCarClassifier = new CascadeClassifier(getAbsolutePathOfLocalResource(frontCarFile));
            frontCarRunable = () -> frontCarClassifier.detectMultiScale(grayFrame, frontCarRects);

            frontBusClassifier = new CascadeClassifier(getAbsolutePathOfLocalResource(frontBusFile));
            frontBusRunable = () -> frontBusClassifier.detectMultiScale(grayFrame, frontBusRects);

            frontTruckClassifier = new CascadeClassifier(getAbsolutePathOfLocalResource(frontTruckFile));
            frontTruckRunable = () -> frontTruckClassifier.detectMultiScale(grayFrame, frontTruckRects);
        } else {
            backCarClassifier = new CascadeClassifier(getAbsolutePathOfLocalResource(backCarFile));
            backCarRunable = () -> backCarClassifier.detectMultiScale(grayFrame, backCarRects);

            backBusClassifier = new CascadeClassifier(getAbsolutePathOfLocalResource(backBusFile));
            backBusRunable = () -> backBusClassifier.detectMultiScale(grayFrame, backBusRects);
        }
    }

    private void processImage(Mat frame) {
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(grayFrame, grayFrame);
    }

    public void detectFromFront(Mat frame) throws InterruptedException {
        processImage(frame);

        frontCarRects = new MatOfRect();
        frontBusRects = new MatOfRect();
        frontTruckRects = new MatOfRect();

        Thread frontCarThread = new Thread(frontCarRunable);
        Thread frontBusThread = new Thread(frontBusRunable);
        Thread frontTruckThread = new Thread(frontTruckRunable);

        frontCarThread.start();
        frontBusThread.start();
        frontTruckThread.start();

        frontCarThread.join();
        frontBusThread.join();
        frontTruckThread.join();
    }

    public void detectFromBack(Mat frame) throws InterruptedException {
        processImage(frame);

        Thread backCarThread = new Thread(backCarRunable);
        Thread backBusThread = new Thread(backBusRunable);

        backCarRects = new MatOfRect();
        backBusRects = new MatOfRect();

        backCarThread.start();
        backBusThread.start();

        backCarThread.join();
        backBusThread.join();
    }
}
