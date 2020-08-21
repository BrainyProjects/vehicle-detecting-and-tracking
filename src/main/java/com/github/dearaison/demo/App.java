package com.github.dearaison.demo;

import com.github.dearaison.utilities.ImageViewer;
import com.github.dearaison.vehicledetectors.VehicleDetector;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import javax.swing.*;
import java.util.List;

/**
 * Created by IntelliJ on Thursday, 20 August, 2020 at 17:22.
 *
 * @author Joseph Maria
 */
public class App {
    private static final String videoPath = "D:\\data\\20191018_122415.mp4";
    private int totalFrames;
    private long totalMilis;

    public static void main(String[] args) throws InterruptedException, ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        App app = new App();
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

    private void runApp() throws InterruptedException, ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        VideoCapture capture = new VideoCapture(videoPath);
        Mat frame = new Mat();
        Mat dst = new Mat();

        List<Rect> cars, buses, trucks;

        JFrame jFrame = new JFrame("Video");
        JLabel videoCanvas = new JLabel();
        jFrame.setContentPane(videoCanvas);
        jFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jFrame.setLocationRelativeTo(null);
        jFrame.setVisible(true);

        VehicleDetector vehicleDetector = new VehicleDetector(true);

        while (capture.read(frame)) {
            if (frame.empty()) {
                break;
            }
            long st = System.currentTimeMillis();
            Imgproc.resize(frame, frame, new Size(858, 480));

            vehicleDetector.detectFromFront(frame);

            cars = vehicleDetector.getFrontCarRects().toList();
            buses = vehicleDetector.getFrontBusRects().toList();
            trucks = vehicleDetector.getFrontTruckRects().toList();

            for (Rect car : cars) {
                Imgproc.rectangle(frame, car, new Scalar(255, 0, 0), 3);
            }
            for (Rect bus : buses) {
                Imgproc.rectangle(frame, bus, new Scalar(0, 255, 0), 3);
            }
            for (Rect truck : trucks) {
                Imgproc.rectangle(frame, truck, new Scalar(0, 0, 255), 3);
            }
            long end = System.currentTimeMillis();
            ++totalFrames;
            totalMilis += end - st;
            System.out.println(totalFrames / (totalMilis / 1000.0));
            ImageIcon imageIcon = new ImageIcon(ImageViewer.convertMatToBufferedImage(frame));
            videoCanvas.setIcon(imageIcon);
            videoCanvas.repaint();
        }
    }
}
