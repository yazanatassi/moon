package lipfd.ellipse;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class ImageProcessing {

	
	
	
	ImageProcessing(){
		
		
		
		
	}



	public Mat erode(Mat m, int n)
	{
		Mat  kernel = Imgproc.getStructuringElement(2, new Size(n,n));
		Imgproc.morphologyEx(m,m,Imgproc.MORPH_ERODE,kernel);
		return m;
	}

	public Mat dilate(Mat m, int n)
	{
		Mat kernel = Imgproc.getStructuringElement(2, new Size(n,n));
		Imgproc.morphologyEx(m,m,Imgproc.MORPH_DILATE,kernel);
		return m;
	}

	public Mat close(Mat m, int n)
	{
		
		Mat kernel = Imgproc.getStructuringElement(2, new Size(n,n));
		Imgproc.morphologyEx(m,m,Imgproc.MORPH_CLOSE,kernel);
		return m;
	}

	
	/**
	   image - hsv
           mm -  RGB
	**/
	public int otsuNew(Mat image, Mat mm)
	{
		Mat histogram = new Mat(500, 256, CvType.CV_8UC1, new Scalar(0));

		Mat m = mm.clone();

		int[] source = new int[m.rows()*m.cols()];
		int[] hist = new int[256];
		double[] r = {0,0,0};

		int count = 0;
		//go through all pixels and get the value component of each pixels
		for(int i = 0; i < m.rows(); i++)
		{
			for(int j = 0; j < m.cols(); j++)
			{

				r = image.get(i,j);
				source[count] = (int) r[2];//this represents the value channel.
				count++;
			}
		}

	
                //generate histogram data for analysis
		int ptr = 0;
		while (ptr < source.length) 
		{
			int h = source[ptr];//this produces a value between 0-255
			hist[h]++;//index represents the value component. The value at the index represents the frequency of that value
			ptr++;
		}
		
		
		
		//Display the histogram
		for(int i = 0; i < 256 ; i++)
		{

			if(hist[i] < histogram.height())
			{
				Core.line(histogram, new Point(i, 500-hist[i]),new Point(i,500),new Scalar(255,255,255,255),1);
				//histogram.put(500 - i, hist[i], white);
			}
			else
			{
				Core.line(histogram, new Point(i, 0),new Point(i,500),new Scalar(255,255,255,255),1);
			}

		}

		Mat clon = m.clone();
		Imgproc.cvtColor(clon,clon, Imgproc.COLOR_RGB2GRAY, 3);
		
		//calling the height function can be optamized. is width and cols the same thing?? is height and rows the same thing??
		double[] a = {0,0,0};
		for(int i = 0; i < m.height(); i++)
		{
			for(int j = 0; j < m.width(); j++)
			{
				a = image.get(i,j);//hsv image 
				clon.put(i,j,a[2]);//

			}
		}

		double b = Imgproc.threshold(clon,clon, 0, 255, Imgproc.THRESH_OTSU);
		//System.out.println("Threshold: " + b);
		
		Core.line(histogram, new Point(b, 0),new Point(b,500),new Scalar(200,200,200,255),3);

		
		
		//standard deviation calculation------------
		
		double sum = 0;
	
		for(int i = 0; i < m.height(); i++)
		{
			for(int j = 0; j < m.width(); j++)
			{
				a = image.get(i,j);
				sum += Math.pow((a[2] - b),2);
			}
		}

		
		double stanDev = Math.sqrt(sum/((m.height()*m.width())));
		//System.out.println("StandDev: "+ stanDev);
		
		Core.line(histogram, new Point(b-(int)stanDev, 0),new Point(b-(int)stanDev,500),new Scalar(100,155,100,255),2);
		Core.line(histogram, new Point(b+(int)stanDev, 0),new Point(b+(int)stanDev,500),new Scalar(100,155,100,255),2);
		//displayImage2(toBufferedImage(histogram),"histogram");
		
		
		return (int)b;
	}

	/**
		image - hsv image
		mm - RGB

	**/
	public int stanD(Mat image, double thres, Mat mm)
	{
		Mat m = mm.clone();
	        double sum = 0;
		double[] a = {0,0,0};
		for(int i = 0; i < m.height(); i++)
		{
			for(int j = 0; j < m.width(); j++)
			{
				a = image.get(i,j);
				sum += Math.pow(a[2] - thres,2);
                
			}
		}

		
		double stanDev = Math.sqrt(sum/((m.height()*m.width())));
		
		return (int)stanDev;
		
	}
	
}
