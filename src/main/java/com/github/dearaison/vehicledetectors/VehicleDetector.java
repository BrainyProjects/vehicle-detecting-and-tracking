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
    private static final String frontCarFile = "frontCar.xml";
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
    private Thread frontCarThread;
    private Thread frontTruckThread;
    private Thread frontBusThread;
    private Thread backCarThread;
    private Thread backBusThread;


    public VehicleDetector(boolean frontSide) {
        if (frontSide) {
            frontCarClassifier = new CascadeClassifier(getAbsolutePathOfLocalResource(frontCarFile));

            frontBusClassifier = new CascadeClassifier(getAbsolutePathOfLocalResource(frontBusFile));


            frontTruckClassifier = new CascadeClassifier(getAbsolutePathOfLocalResource(frontTruckFile));
        } else {
            backCarClassifier = new CascadeClassifier(getAbsolutePathOfLocalResource(backCarFile));

            backBusClassifier = new CascadeClassifier(getAbsolutePathOfLocalResource(backBusFile));
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

        frontCarThread = new Thread(() -> frontCarClassifier.detectMultiScale(grayFrame, frontCarRects));
        frontBusThread = new Thread(() -> frontBusClassifier.detectMultiScale(grayFrame, frontBusRects));
        frontTruckThread = new Thread(() -> frontTruckClassifier.detectMultiScale(grayFrame, frontTruckRects));

        frontCarThread.start();
        frontBusThread.start();
        frontTruckThread.start();

        frontCarThread.join();
        frontBusThread.join();
        frontTruckThread.join();
    }

    public void detectFromBack(Mat frame) throws InterruptedException {
        processImage(frame);

        backCarThread = new Thread(() -> backCarClassifier.detectMultiScale(grayFrame, backCarRects));
        backBusThread = new Thread(() -> backBusClassifier.detectMultiScale(grayFrame, backBusRects));

        backCarRects = new MatOfRect();
        backBusRects = new MatOfRect();

        backCarThread.start();
        backBusThread.start();

        backCarThread.join();
        backBusThread.join();
    }
}
