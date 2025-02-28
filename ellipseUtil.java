package lipfd.ellipse;

import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Mat;
import org.opencv.core.Point;

public class ellipseUtil {

	ellipseUtil(){
		
		
	}
	


	//s mat to image
	public static Image toBufferedImage(Mat m)
	{

		int type = BufferedImage.TYPE_BYTE_GRAY;
		if ( m.channels() > 1 ) {
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		int bufferSize = m.channels()*m.cols()*m.rows();
		byte [] b = new byte[bufferSize];
		m.get(0,0,b); // get all the pixels
		BufferedImage image = new BufferedImage(m.cols(),m.rows(), type);
		final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		System.arraycopy(b, 0, targetPixels, 0, b.length);  
		return image;

	}


	
	
	
	public static void displayImage2(Image img2, String a)
	{   
		ImageIcon icon=new ImageIcon(img2);
		JFrame frame=new JFrame(a);
		frame.setLayout(new FlowLayout());        
		frame.setSize(img2.getWidth(null)+50, img2.getHeight(null)+50);     
		JLabel lbl=new JLabel();
		lbl.setIcon(icon);
		frame.add(lbl);
		frame.setVisible(true);
	}
	
	
	public static double distance(Point a, Point b){
		double distance = Math.sqrt(Math.pow((b.y - a.y),2)+Math.pow((b.x - a.x),2));
		return distance;
	}
	
	public static double sub(double a, double b){
		double distance = a - b;
		return distance;
	}
	
}
