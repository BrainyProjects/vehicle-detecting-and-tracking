package com.github.dearaison.demo;

import com.github.dearaison.utilities.ImageViewer;
import com.github.dearaison.vehicledetectors.VehicleDetector;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.tracking.MultiTracker;
import org.opencv.tracking.TrackerKCF;
import org.opencv.videoio.VideoCapture;

import javax.swing.*;

/**
 * Created by IntelliJ on Thursday, 20 August, 2020 at 17:22.
 *
 * @author Joseph Maria
 */
public class AppMulitpleTracker {
    //    private static final String videoPath = "C:\\Users\\baotr\\Downloads\\By Canon 700D-20200904T022759Z-002\\By Canon 700D\\29082020\\MVI_2459.MOV";
    private static final String videoPath = "D:\\TheBestVideo.mp4";
    //    private static final String videoPath = "D:\\data\\20191018_122415.mp4";
    //    private static final String videoPath = "d:\\front-car.avi";
    private static final Rect ROI = new Rect(new Point(180, 250), new Point(640, 480));
//    private static final Rect ROI = new Rect(new Point(100, 150), new Point(500, 480));

    private final Scalar carColor = new Scalar(255, 0, 0);
    private final Scalar busColor = new Scalar(0, 255, 0);
    private final Scalar truckColor = new Scalar(0, 0, 255);
    private final Scalar ROIColor = new Scalar(0, 0, 0);
    MultiTracker multiTracker = MultiTracker.create();
    MatOfRect2d matOfRect2dCarTrackerBB = new MatOfRect2d();
    Rect2d[] rect2ds;
    private int totalFrames;
    private long totalMilis;
    private Rect tempRect;
    private Rect2d tempRect2d;

    public static void main(String[] args) throws InterruptedException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        AppMulitpleTracker app = new AppMulitpleTracker();
        app.runApp();
    }

    public static String getDataSetPath() throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        JFileChooser fileChooser = new JFileChooser("D:");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile().getAbsolutePath();
        }
        System.exit(0);
        return null;
    }

    private static void calculateRealBoundingBox(Rect objectBB) {
        objectBB.x += ROI.x;
        objectBB.y += ROI.y;
    }

    private static Rect toRect(Rect2d rect2d) {
        return new Rect((int) rect2d.x, (int) rect2d.y, (int) rect2d.width, (int) rect2d.height);
    }

    private static Rect2d toRect2d(Rect rect) {
        return new Rect2d(rect.x, rect.y, rect.width, rect.height);
    }

    private void runApp() throws InterruptedException {
        VideoCapture capture = new VideoCapture(videoPath);
        Mat frame = new Mat();
        capture.read(frame);

        Mat dst = new Mat();

        Rect[] cars, buses, trucks;

        JFrame jFrame = new JFrame("Video");
        JLabel videoCanvas = new JLabel();
        videoCanvas.setIcon(new ImageIcon(ImageViewer.convertMatToBufferedImage(frame)));
        jFrame.setContentPane(videoCanvas);
        jFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jFrame.setLocationRelativeTo(null);
        jFrame.setVisible(true);

        boolean carTrackState;
        boolean busTrackState;
        boolean truckTrackState;

        Rect2d carTrackerBB = new Rect2d();
        Rect2d busTrackerBB = new Rect2d();
        Rect2d truckTrackerBB = new Rect2d();


        VehicleDetector vehicleDetector = new VehicleDetector(true);
        while (capture.read(frame)) {
            if (frame.empty()) {
                break;
            }
            long st = System.currentTimeMillis();
            Imgproc.resize(frame, frame, new Size(858, 480));
            Mat frame2 = frame.clone();

            if (carTrackState = multiTracker.update(frame, matOfRect2dCarTrackerBB)) {
                rect2ds = matOfRect2dCarTrackerBB.toArray();
                for (Rect2d rect2d : rect2ds) {
                    tempRect = toRect(carTrackerBB);
                    Imgproc.rectangle(frame, tempRect, truckColor, 3);
                    Imgproc.rectangle(frame2, tempRect, truckColor, -1);
                }
            }

            vehicleDetector.detectFromFront(frame2.submat(ROI));

            cars = vehicleDetector.getFrontCarRects().toArray();
            buses = vehicleDetector.getFrontBusRects().toArray();
            trucks = vehicleDetector.getFrontTruckRects().toArray();

            drawDetected(cars, frame);
            drawDetected(buses, frame);
            drawDetected(trucks, frame);

            // FIXME: 04-Sep-20 debug code
//            long end = System.currentTimeMillis();
//            ++totalFrames;
//            totalMilis += end - st;
//            System.out.println(totalFrames / (totalMilis / 1000.0));
            Imgproc.rectangle(frame, ROI, ROIColor, 3);
            videoCanvas.setIcon(new ImageIcon(ImageViewer.convertMatToBufferedImage(frame)));
            videoCanvas.repaint();
        }
    }

    private void drawDetected(Rect[] rects, Mat frame) {
        for (Rect rect : rects) {
            calculateRealBoundingBox(rect);
            multiTracker.add(TrackerKCF.create(), frame, toRect2d(rect));
            Imgproc.rectangle(frame, rect, carColor, 3);
            Imgproc.putText(frame, "car", new Point(rect.x - 10, rect.y - 10), Imgproc.FONT_HERSHEY_SIMPLEX, 1.05, carColor, 2);
        }
    }
}
