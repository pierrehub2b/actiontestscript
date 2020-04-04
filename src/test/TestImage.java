package test;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import com.ats.graphic.TemplateMatchingSimple;

public class TestImage {

	public static void main(String[] args) {
				
		
		
		System.out.println(roots);
		/*check("src\\main\\java\\test\\screen_alert.png", "src\\\\main\\\\java\\\\test\\alert_eq.png");
		check("src\\main\\java\\test\\screen_alert.png", "src\\\\main\\\\java\\\\test\\alert_ext.png");
		
		check("src\\main\\java\\test\\screen_alert.png", "src\\\\main\\\\java\\\\test\\robot0.png");
		check("src\\main\\java\\test\\screen_alert.png", "src\\\\main\\\\java\\\\test\\robot1.png");
		
		check("src\\main\\java\\test\\screen_pin.png", "src\\\\main\\\\java\\\\test\\pin0.png");
		check("src\\main\\java\\test\\screen_pin.png", "src\\\\main\\\\java\\\\test\\pin1.png");*/
		
	}
	
	private static void check(String main, String sub) {
		BufferedImage mainImage = null;
		BufferedImage subImage = null;
		try {
			mainImage = ImageIO.read(new File(main));
			subImage = ImageIO.read(new File(sub));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		long start = new Date().getTime();
		
		TemplateMatchingSimple template = new TemplateMatchingSimple(subImage);
		//template.setPercentError(0.05);
		
		ArrayList<Rectangle> result = template.findOccurrences(mainImage);
		
		System.out.println("found -> " + result.size() + "  in " + (new Date().getTime() - start) + " ms");
		System.out.println(result);
	}

}
