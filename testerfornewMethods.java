package lipfd.ellipse;

import java.util.ArrayList;

import lipfd.commons.*;
import lipfd.commons.model.ImageMetadata;
import lipfd.commons.model.dao.ImageMetadataDao;
import lipfd.commons.model.dao.jdbc.ImageMetadataDaoImpl;
import org.opencv.core.RotatedRect;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

public class testerfornewMethods {


	static String filename = "source.tif";
	
	public static void main(String[] args) {
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		EnclosingRect a = new EnclosingRect();
		
		Mat m = Highgui.imread(filename);
		//runs the ellipse algorithm and returns a craterList filled with
		//the centerx, centery, enclosing Rectangle, shadow center x and y
		//it outputs the info onto a txt file (enclosingRect-ellipse.txt)
		ArrayList<Crater> craterList = a.getBoundRect(m,-156.19);
        System.out.println("Total Craters: " + craterList.size());
        
        DBWrite.createTable(craterList);
		
		//Example of enclosing rectangles
		ArrayList<Crater> bounds = new ArrayList<Crater>();
		for(int i = 0; i < craterList.size(); i++){
			Crater c = craterList.get(i);
			bounds.add(new Crater(c.enclosingRect[0],c.enclosingRect[1],c.enclosingRect[2],c.enclosingRect[3]));
		}
		
		
		
		//Draws the ellipses on the enclosing rect, takes in 
		  // light erode, light dilate, dark erode, dark dilate, sun angle and arraylist of enclosing rect
		drawingEllipse draw = new drawingEllipse();
		ArrayList<RotatedRect> rect = draw.drawEllipse(bounds,m);
		System.out.println("Rotated rect: "+rect.size());
		
		//getLightDar() gets the Mat with the black and white blops on the same image
		//System.out.println(a.getLightDark().dump());
		
		
		
	}
	
	
	
	
	
	
	

}
