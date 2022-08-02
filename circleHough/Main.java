package circleHough;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import circleHough.filters.GaussianBlur;
import circleHough.filters.Sobel;
import circleHough.houghTransform.Circle;
import circleHough.houghTransform.CircleDetector;

public class Main {

	public static void main(String[] args) throws Exception {

		/* Default Variables/Arguments */
		final String path = "./results/";
		int threshold = 150;
		int minRadius = 10;
		int maxRadius = 100;
		int numberOfCircles = 1;

		/* Checks argument and result folder */
		if (args.length < 1 || args.length > 5) {
			System.out.println("Usage: Main imagePath [circles] [threshold] [minRadius] [maxRadius]");
			System.exit(1);
		} else if (args.length > 2) {

			try {
				switch (args.length) {
				case 4:
					maxRadius = Integer.parseInt(args[4]);
				case 3:
					minRadius = Integer.parseInt(args[3]);
				case 2:
					threshold = Integer.parseInt(args[2]);
				case 1:
					numberOfCircles = Integer.parseInt(args[1]);
				}
			} catch (NumberFormatException e) {
				System.out.println("Specified arguments must be integers");
				System.exit(1);
			}
		}

		if (maxRadius - minRadius <= 0) {
			System.out.println("Minimum and maximum radius are not valid");
			System.exit(1);
		}

		File resFolder = new File(path);
		if (!(resFolder.exists() && resFolder.isDirectory())) {
			try {
				resFolder.mkdir();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/* Reads the original image */
		File origin = new File(args[0]);
		BufferedImage originalImage = null;

		try {
			originalImage = ImageIO.read(origin);
		} catch (IOException e) {
			System.out.println("Image not found");
			System.exit(1);
		}

		/* Converts image into gray-scale */
		BufferedImage gray = Utils.toGray(originalImage);
		Utils.writeImage(gray, path + "gray.png");

		/* Applies Gaussian Blur */
		BufferedImage blurred = GaussianBlur.blur(gray);
		for (int i = 0; i < 2; i++) {
			blurred = GaussianBlur.blur(blurred);
		}
		Utils.writeImage(blurred, path + "blur.png");

		/* Applies Sobel edge detection */
		Sobel sob = new Sobel(blurred);
		sob.combineSobel();

		double[][] sobel = sob.getSobel();

		BufferedImage imgSobel = sob.getSobelImage();
		BufferedImage imgSobelThreshold = sob.getAboveThresholdImage(threshold);

		Utils.writeImage(imgSobel, path + "sobel.png");
		Utils.writeImage(imgSobelThreshold, path + "sobelThreshold.png");

		/* Performs the circle detection */
		BufferedImage imgCircles = new BufferedImage(gray.getWidth(), gray.getHeight(), BufferedImage.TYPE_INT_ARGB);
		CircleDetector circleFinder = new CircleDetector(threshold, numberOfCircles, minRadius, maxRadius);
		List<Circle> detectedCircles = circleFinder.circleDetection(imgSobel, sobel);
		Collections.sort(detectedCircles, Collections.reverseOrder());

		imgCircles.getGraphics().drawImage(originalImage, 0, 0, null);
		Graphics2D g = imgCircles.createGraphics();
		g.setColor(Color.RED);
		for (int i = 0; i < numberOfCircles; i++) {
			Circle circleToDraw = detectedCircles.get(i);
			double x = circleToDraw.getX() - circleToDraw.getR() * Math.cos(0 * Math.PI / 180);
			double y = circleToDraw.getY() - circleToDraw.getR() * Math.sin(90 * Math.PI / 180);
			g.drawOval((int) x, (int) y, 2 * circleToDraw.getR(), 2 * circleToDraw.getR());
		}

		Utils.writeImage(imgCircles, path + "detectedCircles.png");
	}
}
