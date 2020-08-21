package com.github.dearaison.utilities;

import org.opencv.core.Mat;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

/**
 * Created by IntelliJ on Tuesday, 10 September, 2019 at 01:22.
 *
 * @author Joseph Maria
 */
public final class ImageViewer {
    private static final ImageViewer INSTANCE = new ImageViewer();
    private static final Toolkit TOOLKIT = Toolkit.getDefaultToolkit();
    /**
     * Previous window's information including size and location
     */
    private static Rectangle previousWindowBounds;
    private static int windowId;
    private JLabel imageJLabel;

    private ImageViewer() {
        previousWindowBounds = new Rectangle(0, 0, 0, 0);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }

    public static ImageViewer getInstance() {
        return INSTANCE;
    }

    public static Image convertMatToBufferedImage(Mat src) {
        int rows = src.rows(), cols = src.cols(), channels = src.channels();
        int imgType = BufferedImage.TYPE_BYTE_GRAY;
        if (channels > 1) {
            imgType = BufferedImage.TYPE_3BYTE_BGR;
        }
        byte[] data = new byte[rows * cols * channels];

        src.get(0, 0, data);
        BufferedImage image = new BufferedImage(cols, rows, imgType);
        byte[] bufferedImgData = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(data, 0, bufferedImgData, 0, data.length);
        return image;
    }

    public void show(Mat src) {
        show("Image " + windowId++, src);
    }

    public void show(String winName, Mat src) {
        JFrame jFrame = createJFrame(winName);
        imageJLabel.setIcon(new ImageIcon(convertMatToBufferedImage(src)));
        jFrame.pack();
        jFrame.setLocation(calculateLocation());
        jFrame.setVisible(true);
        previousWindowBounds = jFrame.getBounds();
    }

    private JFrame createJFrame(String winname) {
        imageJLabel = new JLabel();
        JScrollPane jScrollPane = new JScrollPane(imageJLabel);
        JFrame jFrame = new JFrame(winname);
        jFrame.add(jScrollPane, BorderLayout.CENTER);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        return jFrame;
    }

    /**
     * Calculating the coordinate of top left corner for a window.
     *
     * @return the coordinate of top left corner
     */
    private Point calculateLocation() {
        Dimension screenSize = TOOLKIT.getScreenSize();

        // current x coordinate will be the sum of previous window width and previous window x coordinate (next on right side of previous window)
        int x = previousWindowBounds.x + previousWindowBounds.width;

        // current y coordinate is same as previous window (on same row)
        int y = previousWindowBounds.y;

        // If the width of current window goes beyond the width of the screen,
        // x coordinate will be reset to 0
        // and y coordinate will be sum of previous window height and previous window y coordinate
        // (right below of the first window being on previous row)
        if (x + imageJLabel.getWidth() > screenSize.width) {
            x = 0;
            y += previousWindowBounds.height;
            // If the height of current window goes beyond the height of the screen,
            // y coordinate will be reset to 0 (back to the first row)
            if (y + imageJLabel.getHeight() > screenSize.height) {
                y = screenSize.height - y;
            }
        }
        return new Point(x, y);
    }
}
