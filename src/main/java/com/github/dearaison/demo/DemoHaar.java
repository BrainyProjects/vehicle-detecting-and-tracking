package com.github.dearaison.demo;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;

/**
 * Created by IntelliJ on Monday, 07 September, 2020 at 08:24.
 *
 * @author Joseph Maria
 */
public class DemoHaar {
    Scalar color = new Scalar(0, 0, 255);

    private void detectVehicle(VideoCapture videoCapture, VideoWriter videoWriter) {
        // Init classifier
        CascadeClassifier cascadeClassifier = new CascadeClassifier("car.xml");
        MatOfRect boundingBoxes = new MatOfRect();

        Mat frame = new Mat();
        Mat grayFrame = new Mat();
        while (videoCapture.read(frame)) {
            // Pre-process image
            Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
            Imgproc.equalizeHist(grayFrame, grayFrame);

            // Detection
            cascadeClassifier.detectMultiScale(grayFrame, boundingBoxes);
            // Draw result
            for (Rect rect : boundingBoxes.toArray()) {
                Imgproc.rectangle(frame, rect, color, 3);
            }
            videoWriter.write(frame);
        }
    }
}

