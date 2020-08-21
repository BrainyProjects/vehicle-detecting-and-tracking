package com.github.dearaison.demo;

import com.github.dearaison.utilities.ImageViewer;
import org.opencv.core.*;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;
import org.opencv.videoio.VideoCapture;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by IntelliJ on Tuesday, 24 December, 2019 at 10:54.
 *
 * @author Joseph Maria
 */
public class DNNDemo {
    private static final String YOLO_WEIGHTS = DNNDemo.class.getResource("/yolov3-tiny.weights").getPath().substring(1);
    private static final String YOLO_CFG = DNNDemo.class.getResource("/yolov3-tiny.cfg").getPath().substring(1);
    private static final String[] CLASS_NAME = loadClassName();
    private static final Scalar[] COLORS = generateRandomColors();
    static int totalFrames = 0;
    static int totalMilis = 0;

    private static Scalar[] generateRandomColors() {
        Scalar[] res = new Scalar[CLASS_NAME.length];
        Random random = new Random();
        for (int i = 0; i < res.length; i++) {
            res[i] = new Scalar(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        }
        return res;
    }

    private static String[] loadClassName() {
        try {
            return Files.readAllLines(Path.of(DNNDemo.class.getResource("/coco.names").getPath().substring(1))).toArray(new String[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static List<String> getOutputLayerName(Net net) {
        List<String> names = new ArrayList<>();
        List<String> layerNames = net.getLayerNames();
        List<Integer> outputLayers = net.getUnconnectedOutLayers().toList();
        outputLayers.forEach(i -> names.add(layerNames.get(i - 1)));
        return names;
    }

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        String filePath = "D:\\data\\20191018_122415.mp4";
        VideoCapture capture = new VideoCapture(filePath);

        Mat frame = new Mat();
        Mat dst = new Mat();

        JFrame jFrame = new JFrame("Video");
        JLabel videoCanvas = new JLabel();
        jFrame.setContentPane(videoCanvas);
        jFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jFrame.setLocationRelativeTo(null);
        jFrame.setVisible(true);

        Net net = Dnn.readNetFromDarknet(YOLO_CFG, YOLO_WEIGHTS);
        List<Mat> result = new ArrayList<>();
        List<String> outBlobNames = getOutputLayerName(net);

        // Should be same as size in cfg file
        Size size = new Size(320, 320);
        Scalar mean = new Scalar(0);
        while (true) {
            if (capture.read(frame)) {
                long st = System.currentTimeMillis();
                Imgproc.resize(frame, frame, new Size(858, 480));
                Mat blob = Dnn.blobFromImage(frame, 0.00392, size, mean, true, false);
                net.setInput(blob);
                net.forward(result, outBlobNames);
                float scoreThreshold = 0.7f;
                List<Integer> classIds = new ArrayList<>();
                List<Float> confidences = new ArrayList<>();
                List<Rect2d> rect2ds = new ArrayList<>();
                for (int i = 0; i < result.size(); i++) {
                    // each row of result mat is a candidate detection, the first 5 numbers are bounding box information in format[center x, center y, width, height, obj score], followed by (N-4) class probabilities
                    Mat level = result.get(i);
                    for (int j = 0; j < level.rows(); j++) {
                        Mat row = level.row(j);
                        Mat classProbabilities = row.colRange(5, level.cols());
                        Core.MinMaxLocResult minMaxLocResult = Core.minMaxLoc(classProbabilities);
                        float confidence = (float) minMaxLocResult.maxVal;
                        Point classIdPoint = minMaxLocResult.maxLoc;
                        if (confidence >= scoreThreshold) {
                            double centerX = row.get(0, 0)[0] * frame.cols();
                            double centerY = row.get(0, 1)[0] * frame.rows();
                            double width = row.get(0, 2)[0] * frame.cols();
                            double height = row.get(0, 3)[0] * frame.rows();
                            double left = centerX - width / 2;
                            double top = centerY - height / 2;

                            classIds.add((int) classIdPoint.x);
                            confidences.add(confidence);
                            rect2ds.add(new Rect2d(left, top, width, height));
                        }
                    }
                }
                // apply non-max suppression
                if (!confidences.isEmpty()) {
                    float nmsThreshold = 0.1f;
                    MatOfFloat confidence = new MatOfFloat(Converters.vector_float_to_Mat(confidences));
                    Rect2d[] boxesArray = rect2ds.toArray(new Rect2d[0]);
                    MatOfRect2d boxes = new MatOfRect2d(boxesArray);
                    MatOfInt indices = new MatOfInt();
                    Dnn.NMSBoxes(boxes, confidence, scoreThreshold, nmsThreshold, indices);

                    int[] ind = indices.toArray();
                    for (int i = 0; i < ind.length; ++i) {
                        int idx = ind[i];
                        Rect2d box = boxesArray[idx];
                        int classId = classIds.get(idx);
                        Imgproc.rectangle(frame, box.tl(), box.br(), COLORS[classId], 2);
                        Imgproc.putText(frame, CLASS_NAME[classId], new Point(box.x - 10, box.y - 10), Imgproc.FONT_HERSHEY_SIMPLEX, 1.05, COLORS[classId], 2);
//                    System.out.println(box);
                    }
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
}
