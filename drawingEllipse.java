package lipfd.ellipse;

import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import lipfd.commons.*;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

public class drawingEllipse {

	static ellipseUtil u;
	static ImageProcessing imProcess;
	static Mat mCloneO;
	static Mat m;
	static Mat lightdarkClone;
	static Mat lightdark;
	static ArrayList<Crater> craterList;
	static int count = 0;
	static ArrayList<RotatedRect> rotatedList = new ArrayList<>();
	
	
	
	
	public static ArrayList<RotatedRect> drawing(int lerode,int ldilate,int derode, int ddilate,double sunangle, List<Crater> bounds,Mat mm){
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		m = mm;
		u = new ellipseUtil();
		imProcess = new ImageProcessing();

		

		
		//the save mat contains the HSV version of the image m
		
		
		Mat save = new Mat();
		
		if(m.channels() == 1){
			Imgproc.cvtColor(m,m, Imgproc.COLOR_GRAY2RGB);
		}
		
		
		Imgproc.cvtColor(m,save, Imgproc.COLOR_RGB2HSV, 3);
		
		double threshold = imProcess.otsuNew(save,m);
		double s_dev =  imProcess.stanD(save, threshold,m);

	
		Mat dark = makeEmptyMat(save);
		Mat light = makeEmptyMat(save);
		
		

		//colors that will be used
		double[] white = {255,255,255};
		double[] r = {255,255,255};

		
			
			for(int i = 0; i < m.height() ;i++){
				for(int j = 0; j < m.width() ;j++){
					r = save.get(i, j);

					if(r[2] > threshold+s_dev){
						//if pixel gray is larger then average + standard dev, save it to the light mat
						light.put(i,j,white);

					}else if(r[2] < threshold-s_dev){
						//if pixel gray is smaller then average - standard dev, save it to the dark mat
						dark.put(i,j,white);

					}

				}   
			}
			
			dark = imProcess.erode(dark,4);
			dark = imProcess.dilate(dark,4);
			light = imProcess.erode(light,4);
			light = imProcess.dilate(light,4);
			ArrayList<Mat> lightbox = cutMat(light,bounds);
			ArrayList<Mat> darkbox = cutMat(dark,bounds);
			
			
			
			
			
			
			
			for(int k = 0; k < lightbox.size(); k++){
				MatOfPoint biggestLightPatch = new MatOfPoint();
				MatOfPoint biggestDarkPatch = new MatOfPoint();
				

				Mat contoursFrame = darkbox.get(k).clone();
				List<MatOfPoint> contours = getContours(contoursFrame);
				Imgproc.cvtColor(darkbox.get(k), darkbox.get(k), Imgproc.COLOR_GRAY2BGR);

				Mat contoursFrame2 = lightbox.get(k).clone();
				List<MatOfPoint> contours2 = getContours(contoursFrame2);
				Imgproc.cvtColor(lightbox.get(k), lightbox.get(k), Imgproc.COLOR_GRAY2BGR);

				
				
				Point win1 = new Point();
				for(int i = 0; i < contours.size(); i++){
					Point[] hh = contours.get(i).toArray();
					Point[] jj = biggestDarkPatch.toArray();
					if(hh.length > jj.length){
						biggestDarkPatch = contours.get(i);
						
							
						
					}
				}
				
				
				
			
				
				Point win2 = new Point();
				for(int i = 0; i < contours2.size(); i++){
					Point[] hh = contours2.get(i).toArray();
					Point[] jj = biggestLightPatch.toArray();
					if(hh.length > jj.length){
						biggestLightPatch = contours2.get(i);
					}
				}
			
			
				
				
				Point[] a = biggestLightPatch.toArray();
				Point[] b = biggestDarkPatch.toArray();
				Point[] c = new Point[a.length+b.length];

				for(int p = 0; p < a.length;p++){
					c[p] = a[p];
				}
				for(int p = 0; p < b.length;p++){
					c[p+a.length] = b[p];
				}

				//points need to be in matofpoint2f format
				MatOfPoint2f temp = new MatOfPoint2f(c);



				if(c.length >= 5)
				{
					DecimalFormat df = new DecimalFormat("#.#######");
					df.format(0.912385);
					Imgproc.fitEllipse(temp);
					
					RotatedRect elipse =Imgproc.fitEllipse(temp);
					
					
					//Core.ellipse(darkbox.get(k),elipse,new Scalar(255,255,255,255),2);
					elipse.center.x += bounds.get(k).enclosingRect[0];
					elipse.center.y += bounds.get(k).enclosingRect[1];
					
					Core.ellipse(m,elipse,new Scalar(255,255,255,255),2);
					rotatedList.add(elipse);
					
					
				}else{
					int length = bounds.get(k).enclosingRect[2]-bounds.get(k).enclosingRect[0];
					int width = bounds.get(k).enclosingRect[3]-bounds.get(k).enclosingRect[1];
					
					double centerX = bounds.get(k).centerX;
					double centerY = bounds.get(k).centerY;
					
					Size size = new Size(length,width);
					RotatedRect rects = new RotatedRect(new Point(centerX,centerY),size,0);
					
					rotatedList.add(rects);
					
				}

			}
			
			
		
		return rotatedList;
		
		
		

	
	}
	
	
	
	
	public static ArrayList<Mat> cutMat(Mat a, List<Crater> bound){
		
		ArrayList<Mat> result = new ArrayList<Mat>();
		
		for(int s = 0; s < bound.size(); s++){
			int size1 = bound.get(s).enclosingRect[2]-bound.get(s).enclosingRect[0];
			int size2 = bound.get(s).enclosingRect[3]-bound.get(s).enclosingRect[1];

			Mat sub = new Mat(size2,size1 , CvType.CV_8UC1);
			
			

			double[] aa = {0,0,0};
			for(int i = 0; i < size1; i++){
				for(int j = 0; j < size2; j++){
					int x = j+bound.get(s).enclosingRect[1];
					int y = i+bound.get(s).enclosingRect[0];
					
					if(x < 0){
						x = 0;
					}
					if(y < 0){
						y = 0;
					}
					if(x > a.width()-1){
						x = a.width()-1;
					}
					if(y > a.height()-1){
						y = a.height()-1;
					}
					
							
					aa = a.get(x,y);
					
					
					sub.put(j,i,aa[0]);
				}
			}
			
			Core.line(sub, new Point(0, 0),new Point(sub.width()-1, 0),new Scalar(0,0,0,255),1);
			Core.line(sub, new Point(0, sub.height()-1),new Point(sub.width()-1, sub.height()-1),new Scalar(0,0,0,255),1);
			Core.line(sub, new Point(0, 0),new Point(0, sub.height()-1),new Scalar(0,0,0,255),1);
			Core.line(sub, new Point(sub.width()-1, 0),new Point(sub.width()-1, sub.height()-1),new Scalar(0,0,0,255),1);
			
			
			result.add(sub);
			
		}

		return result;
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


    public static ArrayList<RotatedRect> drawEllipse(List<Crater> bounds,Mat m){
    	
    	
    	ArrayList<RotatedRect> rect = drawing(4,4,4,4,180,bounds,m);
    	
    	return rect;
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


	public static Mat makeEmptyMat(Mat m){
		Mat a = new Mat(m.height(), m.width(), CvType.CV_8U);

		double[] rr = {0,0,0};
		for(int i = 0; i < a.height() ;i++){
			for(int j = 0; j < a.width() ;j++){
				a.put(j,i, rr);
			}   
		}





		return a;

	}


	public static List<MatOfPoint> getContours(Mat a){
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(a, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
		return contours;
	}

	public static Mat combinePatchs(Mat dark, Mat light){

		Mat lightdark = new Mat(dark.height(), dark.width(), CvType.CV_8U);



		double[] black = {0,0,0};
		double[] white = {200,200,200};

		double[] color_W = {255,255,255};
		double[] color_G = {150,150,150};
		for(int i = 0; i < light.height() ;i++)
		{
			for(int j = 0; j < light.width() ;j++)
			{
				black = dark.get(i, j);
				white = light.get(i,j);

				if(white[0] == 200)
				{

					lightdark.put(i,j,color_W);

				}
				if(black[0] == 200)
				{

					lightdark.put(i,j,color_G);

				}

			}   
		}
		Imgproc.cvtColor(lightdark, lightdark, Imgproc.COLOR_GRAY2BGR);

		return lightdark;

	}

	public static List<Point> getPatchsCenters(List<MatOfPoint> a){
		List<Point> center = new ArrayList<Point>(a.size());
		List<Moments> mu = new ArrayList<Moments>(a.size());
		for (int i = 0; i < a.size(); i++) 
		{
			mu.add(i, Imgproc.moments(a.get(i), false));
			Moments p = mu.get(i);
			//gets the center and saves it to l list
			int x = (int) (p.get_m10() / p.get_m00());
			int y = (int) (p.get_m01() / p.get_m00());
			center.add(new Point(x,y));
			Core.line(lightdark, new Point(x-4, y),new Point(x+4,y),new Scalar(255,255,0,255),1);
			Core.line(lightdark, new Point(x, y-4),new Point(x,y+4),new Scalar(255,255,0,255),1);
			Core.line(lightdark, new Point(x-2, y-2),new Point(x+2,y+2),new Scalar(255,255,0,255),1);
			Core.line(lightdark, new Point(x+1, y-1),new Point(x-1,y+1),new Scalar(200,200,0,255),1);
		}


		return center;

	}



}
