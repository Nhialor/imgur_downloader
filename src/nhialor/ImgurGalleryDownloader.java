package nhialor;

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class ImgurGalleryDownloader {

	private static String PC = System.getProperty("user.home") + "\\Pictures\\Wallpapers\\";
	private static String Mac = System.getProperty("user.home") + "/Pictures/Wallpapers/";
	private static String destinationFolder;
	
	public static void main(String[] args) throws IOException {
		
		systemCheck();
		
		String input = JOptionPane.showInputDialog("Enter imgur album URL: ");
		String galleryAddr = validURL(input);


		Document doc = Jsoup.connect(galleryAddr).userAgent("Mozilla").get();
		String title = doc.title();
		title = title.replaceAll(" - Imgur", "");
		title = title.replaceAll("[\\/:*?\"<>|]", " ").trim();

		title = capitalize(title);

		destinationFolder += title;
		new File(destinationFolder).mkdirs();

		System.out.println("");

		try{
			Elements script = doc.select("script");

			Pattern p = Pattern.compile("(?is)\"hash\":\"(.+?)\"");
			Matcher m = p.matcher(script.html());

			for(int i = 0; m.find(); i++ )
			{
				System.out.println("Downloading http://imgur.com/" + m.group((1)) + ".jpg");
				downloadImage((m.group(1)), destinationFolder + "/", i);
			}

			System.out.println("Images downloaded to: " + destinationFolder);
			Thread.sleep(2500);


		}catch(Exception e){
			System.out.println(e);
		}



	}

	private static void systemCheck(){
		if(System.getProperty("os.name").contains("Mac")){
			destinationFolder = Mac;
		}else if(System.getProperty("os.name").contains("Windows")){
			destinationFolder = PC;
		}else{
			System.out.println("OS identification failed");
			System.exit(0);
		}
	}

	private static void downloadImage(String imageAddr, String outputFolder , int index) throws IOException{

		URL imgURL = new URL("http://i.imgur.com/" + imageAddr + ".png");
		try{
			Image image = java.awt.Toolkit.getDefaultToolkit().createImage(imgURL);
			BufferedImage buImage = toBufferedImage(image);

			ImageIO.write(buImage, "png", new File((outputFolder) + index + " - " + imageAddr + ".png"));

		}catch(Exception e){

		}

	}

	public static boolean hasAlpha(Image image) {

		if (image instanceof BufferedImage) {
			BufferedImage bimage = (BufferedImage)image;
			return bimage.getColorModel().hasAlpha();
		}

		PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
		}

		ColorModel cm = pg.getColorModel();
		return cm.hasAlpha();
	}

	public static BufferedImage toBufferedImage(Image image) {
		if (image instanceof BufferedImage) {
			return (BufferedImage)image;
		}

		image = new ImageIcon(image).getImage();

		boolean hasAlpha = hasAlpha(image);

		BufferedImage bimage = null;
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		try {
			int transparency = Transparency.OPAQUE;
			if (hasAlpha) {
				transparency = Transparency.BITMASK;
			}

			GraphicsDevice gs = ge.getDefaultScreenDevice();
			GraphicsConfiguration gc = gs.getDefaultConfiguration();
			bimage = gc.createCompatibleImage(
					image.getWidth(null), image.getHeight(null), transparency);
		} catch (HeadlessException e) {

		}

		if (bimage == null) {
			int type = BufferedImage.TYPE_INT_RGB;
			if (hasAlpha) {
				type = BufferedImage.TYPE_INT_ARGB;
			}
			bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
		}

		Graphics g = bimage.createGraphics();

		g.drawImage(image, 0, 0, null);
		g.dispose();

		return bimage;
	}

	private static String capitalize(String string) {
		char[] chars = string.toLowerCase().toCharArray();
		boolean found = false;
		for (int i = 0; i < chars.length; i++) {
			if (!found && Character.isLetter(chars[i])) {
				chars[i] = Character.toUpperCase(chars[i]);
				found = true;
			} else if (Character.isWhitespace(chars[i]) || chars[i]=='.' || chars[i]=='\'') { // You can add other chars here
				found = false;
			}
		}
		return String.valueOf(chars);
	}

	private static String validURL(String addr){
		if(addr.startsWith("http://") || addr.startsWith("https://")){
			return addr;
		}else{
			return "http://" + addr;
		}

	}


}
