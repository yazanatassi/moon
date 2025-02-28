package lipfd.ellipse;

import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
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
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;




public class EnclosingRect {

	static ellipseUtil u;
	static ImageProcessing imProcess;
	static Mat m;
	static Mat mCloneO;
	static ImageProcessing imgProcess;
	static String filename = "";
	static ArrayList<Crater> craterList;
	static List<Point> containcenter = new ArrayList<Point>();
	static List<Double> containsize = new ArrayList<Double>();
	static ArrayList<String> list = new ArrayList<String>();
	static ArrayList<RotatedRect> e1 = new ArrayList<RotatedRect>();
	static ArrayList<RotatedRect> e2 = new ArrayList<RotatedRect>();
	static ArrayList<RotatedRect> e3 = new ArrayList<RotatedRect>();


	static Mat lightdarkClone;
	static Mat lightdarkClone2;
	static Mat lightdarkClone3;


	EnclosingRect(){

	}

	public static void findBounds(int lerode,int ldilate,int derode, int ddilate,double sunangle,int g,Mat mm){
		m = mm.clone();
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		u = new ellipseUtil();
		imProcess = new ImageProcessing();

		//colors that will be used
		double[] white = {200,200,200};
		double[] r = {255,255,255};


		//the save mat contains the HSV version of the image m
		Mat save = new Mat();
		//Change 'save' image to hue saturated value space
		Imgproc.cvtColor(m, save, Imgproc.COLOR_RGB2HSV, 3);

	        //calculate threshold
		double threshold = imProcess.otsuNew(save,m);
	
		//calculate standard deviation 
		double s_dev = imProcess.stanD(save, threshold,m);


		Mat dark = makeEmptyMat(m);//clear image so that everything is black 
		Mat light = makeEmptyMat(m);//clear image so that everything is black
		Mat mClone = mCloneO.clone();//mClone0 is cropped RGB image


                //apply thresh holding to see relevant areas of the image 
		//this will make two images, light patch and dark patch
		for(int i = 0; i < save.height() ;i++){
			for(int j = 0; j < save.width() ;j++){
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


		//apply erosion and diolation for darker region of the image
		dark = imProcess.erode(dark,derode);
		dark = imProcess.dilate(dark,ddilate);

		//apply erosion and diolation for lighter region of the image
		light = imProcess.erode(light,lerode);
		light = imProcess.dilate(light,ldilate);


		//These list find the contours of the blops of the dark image (dst)

		//get dark patches
		Mat contoursFrame = dark.clone();
		List<MatOfPoint> contours = getContours(contoursFrame);
		Imgproc.cvtColor(dark, dark, Imgproc.COLOR_GRAY2BGR);

		//get light patches
		Mat contoursFrame2 = light.clone();
		List<MatOfPoint> contours2 = getContours(contoursFrame2);
		Imgproc.cvtColor(light, light, Imgproc.COLOR_GRAY2BGR);





                //combine both light and dark images into one
		Mat lightdark = combinePatchs(dark,light);
		if(g == 0){
			lightdarkClone = lightdark.clone();
		}else if(g == 1){
			lightdarkClone2 = lightdark.clone();
		}else{
			lightdarkClone3 = lightdark.clone();
		}



		//list d holds the center of the dark patches
		List<Point> d = getPatchsCenters(contours,0,g);

		//list l holds center of the light patches
		List<Point> l = getPatchsCenters(contours2,1,g);



		//used to find smallest distance, starts with a long distance
		int distemp = mClone.width()*mClone.height();//mClone is the original image (rgb) 

		//sun angle stuff
		double adjSunAngle = sunangle; //(int) Math.toDegrees(Math.atan2((sunX - midx), (sunY - midy)));


		//saves an index of the matched light and dark patch
		int[][] kk = new int[2500][2];
		int counter = 0;

		//this stores points to later check to make sure there are no duplicate patches
		List<Point> containx = new ArrayList<Point>();
		List<Point> containy = new ArrayList<Point>();

		//stores the center of dark patches that have been matched
		List<Point> darkcenter = new ArrayList<Point>();
		List<Point> lightcenter = new ArrayList<Point>();

		double pairAngle = 0;

		//goes through the patch matching
		//this loop gets each dark patch and matches it to all light patches then finds the best one
		for(int i = 0; i < d.size();i++)
		{
			//will be used to get the size of the dark patch
			MatOfPoint2f temp = new MatOfPoint2f(contours.get(i).toArray());
			MatOfPoint2f temp2 = new MatOfPoint2f();

			Point x = new Point(0,0);
			Point y = new Point(0,0);
			pairAngle = 0;
			int dis = 0;

			int lightX = 0;
			int lightY = 0;
			int darkX = 0;
			int darkY = 0;

			for(int j = 0; j < l.size(); j++)
			{

				temp2 = new MatOfPoint2f(contours2.get(j).toArray());

				//this if just does not count really small patches
				if(temp2.total() > 2)
				{
					lightX = (int)l.get(j).x;
					lightY = (int)l.get(j).y;
					darkX = (int)d.get(i).x;
					darkY = (int)d.get(i).y;

					//if (darkX > 1)
						//System.out.println("Debug");

					//get distance between both points
					dis = (int) Math.sqrt(Math.pow((l.get(j).x - d.get(i).x), 2)+ Math.pow((l.get(j).y - d.get(i).y), 2));

					double xdif = darkX - lightX;
					double ydif = -1 * (darkY - lightY);


					//gets the angle of the two points that are being checked for matching
					pairAngle = Math.toDegrees(Math.atan2(ydif, xdif));

					//pairAngle += 180;

					//Restraint 1: sun angle
					//checks if the angle is between the inputted sun angle standard deviance 20
					if(pairAngle > (adjSunAngle - 20) && pairAngle < (adjSunAngle + 20))
					{
						//Restraint 2: Patch Sizes
						//doesnt allow large craters to map to really small craters
						if(temp.total() < temp2.total() + 80 && temp.total() > temp.total() - 80){
							//Restraint 3: Distance
							//Gets pair with shortest distance
							//smaller patches are allowed short distance between each other
							//while larger patches allow for greater distance between each other

							if(dis < temp.total())
							{

								distemp = dis;
								//gets centers
								x = d.get(i);
								y = l.get(j);
								//savs index of light and dark patch
								kk[counter][0] = i;
								kk[counter][1] = j;
							}


						}
					}
				}
			}

			distemp = mClone.width()*mClone.height();

			//checks for repeated accepted patches
			if(!containx.contains(x) && !containy.contains(y))
			{
				//idk, forgot why i put this, I'm not deleting incase it was important
				//if(temp.total() > temp2.total()-10)
				//{
				//adds the accepted dark center to the list
				darkcenter.add(x);
				lightcenter.add(y);
				counter++;
				//}
			}
			containx.add(x);
			containy.add(y);
		}


 //****************************************************
		Mat blank = makeEmptyMat(m);

		blank = imProcess.close(blank,5);

		List<Integer> y = new ArrayList<Integer>(kk.length);//why not counter?

		//will contain the content of the output txt


		//ellipse fitting******************************************888888888888
		for(int i = 0; i < kk.length; i++)
		{


			if(kk[i][0] != 0 & !y.contains(kk[i][1]))
			{


				y.add(kk[i][1]);
				//combines the points for the light and dark patch into one point array
				Point[] a = contours2.get(kk[i][1]).toArray();//kk stores index of patched being pared; 1st index is ith pair , 2nd light or dark
				Point[] b = contours.get(kk[i][0]).toArray();
				Point[] c = new Point[a.length+b.length];

				for(int k = 0; k < a.length;k++){
					c[k] = a[k];
				}
				for(int k = 0; k < b.length;k++){
					c[k+a.length] = b[k];
				}

				//points need to be in matofpoint2f format
				MatOfPoint2f temp = new MatOfPoint2f(c);



				if(c.length >= 5)
				{
					DecimalFormat df = new DecimalFormat("#.#######");
					df.format(0.912385);
					Imgproc.fitEllipse(temp);//???
					RotatedRect elipse =Imgproc.fitEllipse(temp);


					//get bounds for ellipse
					int xtopbound = (int)(elipse.center.x - elipse.size.width/2)-10;
					int ytopbound = (int)(elipse.center.y - elipse.size.height/2)-10;
					int xbotbound = (int)(elipse.center.x + elipse.size.width/2)+10;
					int ybotbound = (int)(elipse.center.y + elipse.size.height/2)+10;



					boolean has = false;
					for(int k = 0; k < containcenter.size();k++){


						if(u.distance(elipse.center,containcenter.get(k)) < .1){
							if(u.sub(elipse.size.area(),containsize.get(k)) < 5 & u.sub(elipse.size.area(),containsize.get(k)) > -5){
								has = true;

							}
						}


					}

					if(has == false){


						if(g == 0){
							if(elipse.size.width <= 15){

								craterList.add(new Crater(elipse.center.x,elipse.center.y, xtopbound,ytopbound,xbotbound,ybotbound,darkcenter.get(i).x,darkcenter.get(i).y));


								e1.add(elipse);
							}
						}else if(g == 1){
							if(elipse.size.width > 10 & elipse.size.width <= 60){
								craterList.add(new Crater(elipse.center.x,elipse.center.y, xtopbound,ytopbound,xbotbound,ybotbound,darkcenter.get(i).x,darkcenter.get(i).y));
								e2.add(elipse);
							}

						}else{
							if(elipse.size.width > 50){
								craterList.add(new Crater(elipse.center.x,elipse.center.y, xtopbound,ytopbound,xbotbound,ybotbound,darkcenter.get(i).x,darkcenter.get(i).y));
								e3.add(elipse);
							}

						}
					}else{
						System.out.println("got caout");
					}


				}
			}
		}

		/*
		try {
	        BufferedWriter out = new BufferedWriter(new FileWriter("enclosingRect-ellipse.txt"));
	        out.write("Crater List-enclosingRect-------"+ filename);
            out.newLine();
            out.newLine();

            out.write("x           y       Tleft    Bright     shadowX     shadowY");
            out.newLine();
            System.out.println(list.size());
            DecimalFormat df = new DecimalFormat("#.#######");
	            for (int i = 0; i < craterList.size()-1; i++) {
	                out.write(df.format(craterList.get(i).centerX)+" "+ df.format(craterList.get(i).centerY)+" "+
	                		   "("+craterList.get(i).enclosingRect[0]+" "+craterList.get(i).enclosingRect[1]+
	                		   ") ("+craterList.get(i).enclosingRect[2]+" "+craterList.get(i).enclosingRect[3]+") "+
	                		   		craterList.get(i).shadowCenterX+" "+craterList.get(i).shadowCenterY +" \n");
	                out.newLine();
	            }
	            out.close();
	        } catch (IOException e) {}
            */




	}



	public static List<Point> getPatchsCenters(List<MatOfPoint> a,int b,int g){
		Scalar color;
		if(b == 0){
			color = new Scalar(255,255,0,255);
		}else{
			color = new Scalar(255,0,0,255);
		}

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
			if(g == 0){
				Core.line(lightdarkClone, new Point(x-4, y),new Point(x+4,y),color,1);
				Core.line(lightdarkClone, new Point(x, y-4),new Point(x,y+4),color,1);
				Core.line(lightdarkClone, new Point(x-2, y-2),new Point(x+2,y+2),color,1);
				Core.line(lightdarkClone, new Point(x+1, y-1),new Point(x-1,y+1),color,1);
			}else if(g == 1){
				Core.line(lightdarkClone2, new Point(x-4, y),new Point(x+4,y),color,1);
				Core.line(lightdarkClone2, new Point(x, y-4),new Point(x,y+4),color,1);
				Core.line(lightdarkClone2, new Point(x-2, y-2),new Point(x+2,y+2),color,1);
				Core.line(lightdarkClone2, new Point(x+1, y-1),new Point(x-1,y+1),color,1);
			}else{
				Core.line(lightdarkClone3, new Point(x-4, y),new Point(x+4,y),color,1);
				Core.line(lightdarkClone3, new Point(x, y-4),new Point(x,y+4),color,1);
				Core.line(lightdarkClone3, new Point(x-2, y-2),new Point(x+2,y+2),color,1);
				Core.line(lightdarkClone3, new Point(x+1, y-1),new Point(x-1,y+1),color,1);

			}
		}

		return center;

	}




	public static List<MatOfPoint> getContours(Mat a){
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(a, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
		return contours;
	}

	public static Mat combinePatchs(Mat dark, Mat light){

		Mat lightdark = new Mat(m.height(), m.width(), CvType.CV_8U);
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

	public static ArrayList<Crater> getBoundRect(Mat a, double sunAngle){

		//System.out.print("Number of channels: "+a.channels());		

		if(a.channels() == 1){
			//convert to rgb space
			Imgproc.cvtColor(a, a, Imgproc.COLOR_GRAY2RGB);
		}


		mCloneO = a.clone();
		craterList = new ArrayList<Crater>();
		imgProcess = new ImageProcessing();
		//make a ArrayList for Crater called craterList
		ArrayList<Crater> finalCraterList = new ArrayList<Crater>();

		int sm = 0;
		int md = 0;
		int lg = 0;

		//run the algorithm three times
		//then the craterList will be filled with the bounding box


		System.out.println("Detecting Craters...");

		findBounds(4,4,4,4,sunAngle,0,a);
		sm = craterList.size();
		System.out.println("Sm Craters: " + sm);

		findBounds(6,6,6,6,sunAngle,1,a);
		md = craterList.size() - sm;
		System.out.println("Md Craters: " + md);

		findBounds(9,9,9,9,sunAngle,2,a);
		lg = craterList.size() - md - sm;
		System.out.println("Lg Craters: " + lg);

		for(int i = 0 ; i < EnclosingRect.craterList.size(); i++){
			finalCraterList.add(EnclosingRect.craterList.get(i));
		}



		return finalCraterList;

	}



public static ArrayList<Crater> getBoundRect(Mat a, int le,int ld,int de,int dd, double sun){


		if(a.channels() == 1){
			Imgproc.cvtColor(a, a, Imgproc.COLOR_GRAY2RGB);
		}


		mCloneO = a.clone();
		craterList = new ArrayList<Crater>();
		imgProcess = new ImageProcessing();
		//make a ArrayList for Crater called craterList
		ArrayList<Crater> finalCraterList = new ArrayList<Crater>();

		int sm = 0;
		int md = 0;
		int lg = 0;
		int total = 0;

		//run the algorithm three times
		//then the craterList will be filled with the bounding box


		System.out.println("Detecting Craters...");

		findBounds(le,ld,de,dd,sun,0,a);
		sm = craterList.size();
		System.out.println("Sm Craters: " + sm);

		findBounds(le+2,ld+2,de+2,dd+2,sun,1,a);
		md = craterList.size() - sm;
		System.out.println("Md Craters: " + md);

		findBounds(le+3,ld+3,de+3,dd+3,sun,2,a);
		lg = craterList.size() - md - sm;
		System.out.println("Lg Craters: " + lg);

		total = craterList.size();
		System.out.println("Total Craters: " + total);

		for(int i = 0 ; i < EnclosingRect.craterList.size(); i++){
			finalCraterList.add(EnclosingRect.craterList.get(i));
		}



		return finalCraterList;

	}


	public Mat getLightDark(){

		return lightdarkClone;
	}

	public static BufferedImage matToBufferedImage(Mat m) {
		int type = BufferedImage.TYPE_BYTE_GRAY;
		if (m.channels() > 1) {
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		int bufferSize = m.channels() * m.cols() * m.rows();
		byte[] b = new byte[bufferSize];
		m.get(0, 0, b);
		BufferedImage img = new BufferedImage(m.cols(), m.rows(), type);
		final byte[] targetPixels = ((DataBufferByte) img.getRaster()
				.getDataBuffer()).getData();
		System.arraycopy(b, 0, targetPixels, 0, b.length);

		return img;
	}

    public static void displayImage(BufferedImage img2, String title){
	    ImageIcon icon = new ImageIcon(img2);
	    JFrame frame=new JFrame();
	    frame.setLayout(new FlowLayout());
	    frame.setSize(img2.getWidth(null)+50, img2.getHeight(null)+50);
	    JLabel lbl=new JLabel();
	    lbl.setIcon(icon);
	    frame.add(lbl);
	    frame.setTitle(title);
	    frame.setVisible(true);
    }


}
