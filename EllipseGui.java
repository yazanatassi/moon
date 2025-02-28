package lipfd.ellipse;




import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;


import javax.swing.*;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.awt.Image;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import lipfd.commons.*;
import lipfd.commons.model.ImageMetadata;
import lipfd.commons.model.dao.ImageMetadataDao;
import lipfd.commons.model.dao.jdbc.ImageMetadataDaoImpl;


public class EllipseGui {
	
    static List<Crater> groundTruth;
	
    final static String SMALLPANEL = "Small Craters";
    final static String MEDPANEL = "Medium Craters";
    final static String TEXTPANEL2 = "Large Craters";
    final static String COMBINE = "Combined Results";
    final static String RESULTS = "DATA";
    final static int extraWindowWidth = 100;
    static Image procImage;
    static Image dlImage;
    static Image procImage2;
    static Image dlImage2;
    static Image procImage3;
    static Image dlImage3;
    static Image dlImage4;
    static Mat m;
    static String filename = "source.tif";
    static JFrame frame;


    //resize the windows
    static JScrollPane imgPanec1;

    static JScrollPane imgPanec2;
    static JScrollPane imgPanec3;
    static JScrollPane imgPanec4;
    static JScrollPane imgPanec5;
    static JScrollPane imgPanec6;
    static JScrollPane imgPanec7;

    static ImagePanel1 imgPanelc1;
    static ImagePanel2 imgPanelc2;
    static ImagePanel3 imgPanelc3;
    static ImagePanel4 imgPanelc4;
    static ImagePanel5 imgPanelc5;
    static ImagePanel6 imgPanelc6;
    static comby imgPanelc7;


    static EnclosingRect a = new EnclosingRect();



	static JTextArea area = new JTextArea("");
	static String defaultSunAngle = "";
	static double SUN_ANGLE;
	static String RESULTS_DIR = "results/EllipseGUI/";
	static String IMG_NAME;

    public void addComponentToPane(Container pane) throws IOException{


        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel card1 = new JPanel();

        procImage = toBufferedImage(m);
        dlImage = toBufferedImage(m);
        procImage2 = toBufferedImage(m);
        dlImage2 = toBufferedImage(m);
        procImage3 = toBufferedImage(m);
        dlImage3 = toBufferedImage(m);
        dlImage4 = toBufferedImage(m);

        Image imageC1 = procImage;// Left image
		Image imageC2 = dlImage;
		Image imageC3 = procImage2;// Left image
		Image imageC4 = dlImage2;
		Image imageC5 = procImage3;// Left image
		Image imageC6 = dlImage3;
		Image imageC7 = dlImage4;
		final Dimension dimensionSize1 = new Dimension(m.width(),m.height());
		final Dimension rightsize  = new Dimension(m.width()/2,m.height()/2);
		final Dimension rightsize3 = new Dimension(900,560);

        imgPanelc1 = new ImagePanel1(imageC1);
        imgPanelc1.setPreferredSize(dimensionSize1);
		imgPanec1 = new JScrollPane(imgPanelc1);

		imgPanelc2 = new ImagePanel2(imageC2);
		imgPanelc2.setPreferredSize(dimensionSize1);
		imgPanec2 = new JScrollPane(imgPanelc2);

		//second seen********************************
		imgPanelc3 = new ImagePanel3(imageC3);
        imgPanelc3.setPreferredSize(dimensionSize1);
		imgPanec3 = new JScrollPane(imgPanelc3);


		imgPanelc4 = new ImagePanel4(imageC4);
		imgPanelc4.setPreferredSize(dimensionSize1);
		imgPanec4 = new JScrollPane(imgPanelc4);


		//second seen********************************
		imgPanelc5 = new ImagePanel5(imageC5);
		imgPanelc5.setPreferredSize(dimensionSize1);
		imgPanec5 = new JScrollPane(imgPanelc5);


		imgPanelc6 = new ImagePanel6(imageC6);
		imgPanelc6.setPreferredSize(dimensionSize1);
		imgPanec6 = new JScrollPane(imgPanelc6);


		imgPanelc7 = new comby(imageC7);
		imgPanelc7.setPreferredSize(dimensionSize1);
		imgPanec7 = new JScrollPane(imgPanelc7);
		imgPanec7.setPreferredSize(rightsize3);


	    card1.setLayout(new BorderLayout());

        card1.add(imgPanec1, BorderLayout.WEST);
        card1.add(imgPanec2, BorderLayout.CENTER);

        JPanel options = new JPanel();
        options.setLayout(new FlowLayout());



        JLabel le = new JLabel("Light Erode: ");
        le.setForeground(Color.WHITE);
        JLabel ld = new JLabel("Light Dilate: ");
        ld.setForeground(Color.WHITE);
        JLabel de = new JLabel("Dark Erode: ");
        de.setForeground(Color.WHITE);
        JLabel dd = new JLabel("Dark Dilate: ");
        dd.setForeground(Color.WHITE);
        JLabel sunlabel = new JLabel("Sun Angle: ");
        sunlabel.setForeground(Color.WHITE);


		final JTextField lighterode = new JTextField("4");
		lighterode.setColumns(4);
		final JTextField lightdilate = new JTextField("4");
		lightdilate.setColumns(4);
		final JTextField darkerode = new JTextField("4");
		darkerode.setColumns(4);
		final JTextField darkdilate = new JTextField("4");
		darkdilate.setColumns(4);
		final JTextField sunAngle = new JTextField(defaultSunAngle);
		sunAngle.setColumns(5);

		ImageIcon icon = new ImageIcon("cal.jpg");
        JLabel label1 = new JLabel(icon);

		options.add(label1);
		options.setBackground(Color.BLACK);
		options.add(le);
		options.add(lighterode);
		options.add(ld);
		options.add(lightdilate);
		options.add(de);
		options.add(darkerode);
		options.add(dd);
		options.add(darkdilate);
		options.add(sunlabel);
		options.add(sunAngle);


		JButton submit = new JButton("Generate elipses");

		options.add(submit);

		submit.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{


				int le = part(lighterode.getText());
				int ld = part(lightdilate.getText());
				int de = part(darkerode.getText());
				int dd = part(darkdilate.getText());
				double sun = Double.parseDouble(sunAngle.getText());



				ArrayList<Crater> craterList = a.getBoundRect(m,le,ld,de,dd,sun);
				draw();



			}

		});


		card1.add(options,BorderLayout.SOUTH);

        JPanel card2 = new JPanel();
        card2.setLayout(new BorderLayout());
        card2.setBackground(Color.BLACK);
        card2.add(imgPanec5, BorderLayout.WEST);
        card2.add(imgPanec6, BorderLayout.EAST);


        JPanel card3 = new JPanel();
        card3.setLayout(new BorderLayout());
        card3.setBackground(Color.BLACK);
        card3.add(imgPanec3, BorderLayout.WEST);
        card3.add(imgPanec4, BorderLayout.EAST);

        JPanel card4 = new JPanel();
        card4.setLayout(new BorderLayout());
        card4.setBackground(Color.BLACK);
        card4.add(imgPanec7, BorderLayout.WEST);




        JPanel card5 = new JPanel();


        final Dimension rightsize2  = new Dimension(m.width(),450);

        area = new JTextArea("fdfeef");
        JScrollPane area2 = new JScrollPane(area);
		area2.setPreferredSize(rightsize2);
        card5.add(area2);


        tabbedPane.addTab(SMALLPANEL, card1);
        tabbedPane.addTab(MEDPANEL, card2);
        tabbedPane.addTab(TEXTPANEL2, card3);
        tabbedPane.addTab(COMBINE, card4);
        //tabbedPane.addTab(RESULTS, card5);

        pane.add(tabbedPane, BorderLayout.CENTER);
    }


    private static void createAndShowGUI() throws IOException {
        //Create and set up the window.
        frame = new JFrame("Ellipse Algorithm");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(Color.BLACK);
        //Create and set up the content pane.
        EllipseGui demo = new EllipseGui();
        demo.addComponentToPane(frame.getContentPane());

        //Display the window.
        frame.pack();
        frame.setVisible(true);


        //Sets the size of the image windows
        Dimension rightsize4 = new Dimension(frame.getWidth()/2,frame.getHeight());
        Dimension rightsize5 = new Dimension(frame.getWidth()/2,frame.getHeight()/2);
        Dimension finalSize = new Dimension(m.width(),m.height());
        imgPanec1.setPreferredSize(rightsize4);
        imgPanec2.setPreferredSize(rightsize4);
        imgPanec3.setPreferredSize(rightsize5);
        imgPanec4.setPreferredSize(rightsize5);
        imgPanec5.setPreferredSize(rightsize5);
        imgPanec6.setPreferredSize(rightsize5);
        imgPanec7.setPreferredSize(finalSize);

        //After it sets the size, it runs the detection with the default values
		EnclosingRect a = new EnclosingRect();

      // ArrayList<Crater> craterList = a.getBoundRect(m,Double.parseDouble(defaultSunAngle));
       ArrayList<Crater> craterList = a.getBoundRect(m,SUN_ANGLE);
      	draw();


    }

    public static void main(String[] args) throws IOException{

     System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

      //  Placeholder test code

      //get input image 
      String productID = args[0];
      IMG_NAME = productID;
      // get crop bounds 
      int ulx = Integer.parseInt(args[1]);
      int uly = Integer.parseInt(args[2]);
      int lrx = Integer.parseInt(args[3]);
      int lry = Integer.parseInt(args[4]);
      String groundTruthFileName = args[5];
      groundTruth = Util.parseMetaData(Util.readFile(groundTruthFileName),
					(int)ulx, (int)uly, (int)lrx, (int)lry);

      //check to see if the image passed has been downloaded
      ImageMetadataDao imageDao = new ImageMetadataDaoImpl("lipfd", "lipfd");
      ImageMetadata bestInputImageMetadata = imageDao.getMetadata(productID);
      System.out.println("\nchecking the downloaded files.");
      if(!Util.fileExists("downloadedImages/" + productID + ".IMG")){
        System.out.println("downloading the image:");
        Util.download("http://lroc.sese.asu.edu/data/" + bestInputImageMetadata.file_specification_name,
          "downloadedImages/" + productID + ".IMG");
      }

      String originalInputImagePath = "downloadedImages/" + productID + ".IMG";
      String newInputImagePath = "";
      String commandString = "";
      /*if(System.getProperty("os.name").equalsIgnoreCase("linux")){
        newInputImagePath = "downloadedImages/" + productID + ".pgm";
        commandString = "gdal_translate -srcwin 0 0 " + String.valueOf(bestInputImageMetadata.line_samples) + " " +
          String.valueOf(bestInputImageMetadata.image_lines) +
          " -ot Byte -of PNM -scale " +
          originalInputImagePath + " " + newInputImagePath;
      }
      else {
        newInputImagePath = "downloadedImages/" + bestInputImageMetadata.product_id + ".tiff";
        commandString = "gdal_translate -srcwin 0 0 " + String.valueOf(bestInputImageMetadata.line_samples) + " " +
          String.valueOf(bestInputImageMetadata.image_lines) + " " + originalInputImagePath +
          " " + newInputImagePath +
          " -ot Byte -scale 0 4095 0 255";
      }
      System.out.println("running gdal:");
      System.out.println(commandString);
	*/
	newInputImagePath=("downloadedImages/"+bestInputImageMetadata.product_id+".tiff");
    if(!Util.fileExists(newInputImagePath))
        Util.runProgram(commandString);
	
      //load image as grayscale and crop based on values passed in 
      lipfd.commons.Image inputImage = new lipfd.commons.Image(newInputImagePath).crop(ulx, uly, lrx, lry);
      m = inputImage.getMat();

      double sunAzimuthAngle =bestInputImageMetadata.sub_solar_azimuth;
      SUN_ANGLE = bestInputImageMetadata.sub_solar_azimuth;
        //(bestInputImageMetadata.usage_note.equals("F")?(bestInputImageMetadata.sub_solar_azimuth):(360 - bestInputImageMetadata.sub_solar_azimuth))/
        //  180 * (Math.PI);
      while(sunAzimuthAngle > 2 * Math.PI)
        sunAzimuthAngle -= 2 * Math.PI;
      while(sunAzimuthAngle < 0)
        sunAzimuthAngle += 2 * Math.PI;

      //change to -180 to 180
      sunAzimuthAngle = (sunAzimuthAngle<Math.PI?sunAzimuthAngle:(sunAzimuthAngle-2*Math.PI)) * 180 / Math.PI;

      defaultSunAngle = String.valueOf(sunAzimuthAngle);

	  System.out.println("sunAzimuthAngle: " + sunAzimuthAngle);
	  System.out.println("defaultSunAngle: " + defaultSunAngle);
      //  End placeholder test code

      //source Mat
		//m = Highgui.imread(filename);
		//the sun angle
		//defaultSunAngle = "-150";


		/*It will run the getboundrect in the createAndShowGUI(),
		 * after the gui is created
		 */


        try {

            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        UIManager.put("swing.boldMetal", Boolean.FALSE);

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
			createAndShowGUI();
		} catch (IOException e) {
		// TODO Auto-generated catch block
			e.printStackTrace();
		}
            }
        });
    }

    public static class ImagePanel1 extends JPanel
	{
		private static final long serialVersionUID = 1L;



		public ImagePanel1(Image imageC1) {

		}

		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			g.drawImage(procImage, 0, 0, this);
		}


	}

    public static class ImagePanel2 extends JPanel
   	{
   		private static final long serialVersionUID = 1L;



   		public ImagePanel2(Image imageC1) {

   		}

   		public void paintComponent(Graphics g)
   		{
   			super.paintComponent(g);
   			g.drawImage(dlImage, 0, 0, this);
   		}


   	}


    //second seen
    public static class ImagePanel3 extends JPanel
	{
		private static final long serialVersionUID = 1L;



		public ImagePanel3(Image imageC2) {

		}

		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			g.drawImage(procImage2, 0, 0, this);
		}


	}

    public static class ImagePanel4 extends JPanel
   	{
   		private static final long serialVersionUID = 1L;



   		public ImagePanel4(Image imageC3) {

   		}

   		public void paintComponent(Graphics g)
   		{
   			super.paintComponent(g);
   			g.drawImage(dlImage2, 0, 0, this);
   		}


   	}

  //second seen
    public static class ImagePanel5 extends JPanel
	{
		private static final long serialVersionUID = 1L;



		public ImagePanel5(Image imageC2) {

		}

		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			g.drawImage(procImage3, 0, 0, this);
		}


	}

    public static class ImagePanel6 extends JPanel
   	{
   		private static final long serialVersionUID = 1L;



   		public ImagePanel6(Image imageC3) {

   		}

   		public void paintComponent(Graphics g)
   		{
   			super.paintComponent(g);
   			g.drawImage(dlImage3, 0, 0, this);
   		}


   	}

    public static class comby extends JPanel
   	{
   		private static final long serialVersionUID = 1L;


   		public comby(Image imageC3) {

   		}

   		public void paintComponent(Graphics g)
   		{
   			super.paintComponent(g);
   			g.drawImage(dlImage4, 0, 0, this);
   		}

   	}

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



    public static int part(String a)
	{
		return Integer.parseInt(a);

	}

    public static void draw(){
    	    	Mat com = m.clone();
		Mat smalldetect = m.clone();

		for(int i = 0; i < EnclosingRect.e1.size(); i++){
			Core.ellipse(smalldetect,EnclosingRect.e1.get(i),new Scalar(255,255,255,255),2);
			Core.ellipse(com,EnclosingRect.e1.get(i),new Scalar(255,255,255,255),2);

		}
		procImage = toBufferedImage(smalldetect);

		Mat meddetect = m.clone();
		for(int i = 0; i < EnclosingRect.e2.size(); i++){

			Core.ellipse(meddetect,EnclosingRect.e2.get(i),new Scalar(255,255,255,255),2);
			Core.ellipse(com,EnclosingRect.e2.get(i),new Scalar(255,255,255,255),2);

		}
		procImage3 = toBufferedImage(meddetect);

		Mat largedetect = m.clone();
		for(int i = 0; i < EnclosingRect.e3.size(); i++){
			Core.ellipse(largedetect,EnclosingRect.e3.get(i),new Scalar(255,255,255,255),2);
			Core.ellipse(com,EnclosingRect.e3.get(i),new Scalar(255,255,255,255),2);
		}

		procImage2 = toBufferedImage(largedetect);
		//add ground truth to the cumulative image 
		for(Crater crater : groundTruth)
		{
		//	Core.rectangle(groundTruthImg.getMat(), new Point(crater.enclosingRect[0], crater.enclosingRect[1]),
		//		new Point(crater.enclosingRect[2], crater.enclosingRect[3]), new Scalar(255, 0, 0), 1);
				// Core.circle(colorImage.getMat(), new Point(crater.centerX, crater.centerY), (int) crater.radius,
				// 	new Scalar(255, 0, 0));
			Core.rectangle(com,new Point(crater.enclosingRect[0], crater.enclosingRect[1]),
				new Point(crater.enclosingRect[2], crater.enclosingRect[3]), new Scalar(255, 0, 0), 1);	
		}


		dlImage = toBufferedImage(EnclosingRect.lightdarkClone);
	    	dlImage3 = toBufferedImage(EnclosingRect.lightdarkClone2);
	   	dlImage2 = toBufferedImage(EnclosingRect.lightdarkClone3);
	   	dlImage4 = toBufferedImage(com);
		saveImage(RESULTS_DIR+IMG_NAME+".png",com);
	   	imgPanelc1.repaint();
		imgPanelc3.repaint();
		imgPanelc5.repaint();
		imgPanelc2.repaint();
		imgPanelc4.repaint();
		imgPanelc6.repaint();
		imgPanelc7.repaint();
		EnclosingRect.e1.clear();
		EnclosingRect.e2.clear();
		EnclosingRect.e3.clear();
    }
	
 public static void saveImage(String filename, Mat imageData){
	
	//check if image path has directories and add them if they don't exsist
	(new File((new File(filename)).getParent())).mkdirs();
	
	lipfd.commons.Image image = new lipfd.commons.Image(imageData);
	//Imgproc.cvtColor(imageData, image.getMat(), Imgproc.COLOR_GRAY2BGR);
	image.saveImage(filename);

 }


}
