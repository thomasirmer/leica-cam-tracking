package LiveMicroscopy;
import ij.ImagePlus;
import ij.io.FileSaver;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.ParticleAnalyzer;
import ij.process.ColorProcessor;
import ij.process.ByteProcessor;
import ij.process.BinaryProcessor;
import ij.process.ShortProcessor;
import ij.plugin.filter.EDM;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;


/**
 * This class implements the cell tracking algorithms.
 * 
 * @author Thomas Irmer
 */
public class CellTracking implements Measurements {

	public void track(ImagePlus image) {
		ResultsTable rt = new ResultsTable();
		rt.reset();
		//ParticleAnalyzer pa = new ParticleAnalyzer(0, CENTROID, rt, 500, 10000);
		//pa.analyze(image);
		
		float[] xRes = rt.getColumn(ResultsTable.X_CENTROID);
		float[] yRes = rt.getColumn(ResultsTable.Y_CENTROID);
		
		BufferedImage markedCellsImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics markedCellsGraphics = markedCellsImage.getGraphics();
		ShortProcessor sp = (ShortProcessor)image.getProcessor();		
		ByteProcessor bp = sp.convertToByteProcessor();
		image.setProcessor(bp);
		bp.autoThreshold();		
		image.updateAndDraw();
		//Watershed
		EDM watershedPlugin = new EDM();
		watershedPlugin.toWatershed(bp);
		
		//To Do: Cell tracking with CellProfiler
		
		
		
		
//		for (int i = 0; i < xRes.length; i++) {
//			Point particle = new Point((int) xRes[i], (int) yRes[i]); 
//			markedCellsGraphics.fillRect(particle.x - 10, particle.y - 10, 20, 20);
//		}
		
		//FileSaver saver = new FileSaver(new ImagePlus("Marked Cells Image", markedCellsImage));
		//saver.saveAsJpeg("C:\\Users\\thoirm\\Desktop\\image.png");
	}
}
