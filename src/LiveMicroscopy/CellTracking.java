package LiveMicroscopy;

import ij.IJ;
import ij.ImagePlus;
import ij.io.FileSaver;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.ParticleAnalyzer;
import ij.process.AutoThresholder;
import ij.process.ColorProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * This class implements the cell tracking algorithms.
 * 
 * @author Thomas Irmer
 */
public class CellTracking implements Measurements {

	private static Integer counter = 0;

	private void convertToGray(ImagePlus img) {
		ImageConverter converter = new ImageConverter(img);
		converter.convertToGray8();
	}

	private void threshold(ImagePlus img) {
		ImageProcessor proc = img.getProcessor();
		proc.setAutoThreshold(AutoThresholder.Method.MaxEntropy, true, ImageProcessor.BLACK_AND_WHITE_LUT);
		img.setProcessor(proc);
	}

	public void track(ImagePlus imp) {
		
		// prepare image
		convertToGray(imp);
		threshold(imp);

		// init result table
		ResultsTable rt = ResultsTable.getResultsTable();
		if (rt == null)
			rt = new ResultsTable();
		rt.showRowNumbers(true);
		rt.reset();

		// analyze image
		ParticleAnalyzer particleAnalyzer = new ParticleAnalyzer(ParticleAnalyzer.AREA | ParticleAnalyzer.CENTROID
				| ParticleAnalyzer.SHOW_NONE, Measurements.AREA | Measurements.CENTROID, rt, 100, 10000, 0, 1);
		particleAnalyzer.setHideOutputImage(true);
		particleAnalyzer.analyze(imp);

		// i=0: Area | i=6: X | i=7: Y
		float[] area = rt.getColumn(0);
		float[] x = rt.getColumn(6);
		float[] y = rt.getColumn(7);

		List<Particle> currentParticleList = new ArrayList<Particle>();
		for (int i = 0; i < area.length; i++) {
			Particle currentParticle = new Particle(i, area[i], x[i], y[i]);
			currentParticleList.add(currentParticle);
			System.out.println("ID: " + i + "\tArea: " + area[i] + "\tX: " + x[i] + "\tY: " + y[i]);
		}

		System.out.println("Vergleich");
		//rt.show("Results");

		Color textColor = Color.RED;
		double umrechnung = 1.0; // 4.3845;

		ImagePlus duplicatedImage = imp.duplicate();

		if (counter <= 0) { // first image --> draw cell ids
			for (int i = 0; i < currentParticleList.size(); i++) {
				ColorProcessor proc = duplicatedImage.getProcessor().convertToColorProcessor();
				duplicatedImage.setProcessor(proc);
				proc.setColor(textColor);
				proc.drawString(Integer.toString(currentParticleList.get(i).getId()), (int) ((int) currentParticleList.get(i)
						.getX() * umrechnung), (int) ((int) currentParticleList.get(i).getY() * umrechnung));

			}
		} else { // following images --> match cell ids and draw cell ids
			ColorProcessor proc = duplicatedImage.getProcessor().convertToColorProcessor();
			duplicatedImage.setProcessor(proc);

			List<Particle> oldParticleList = ParticleContainer.getInstance().getParticles().get(counter);
			double distance;
			double minDistance;

			proc.setColor(textColor);

			for (int i = 0; i < currentParticleList.size(); i++) {
				minDistance = 9999;
				for (int j = 0; j < oldParticleList.size(); j++) {

					distance = currentParticleList.get(i).CompareTo(oldParticleList.get(j));

					if (distance < minDistance) {
						minDistance = distance;
						currentParticleList.get(i).setId(oldParticleList.get(j).getId());
					}
				}
				proc.drawString(Integer.toString(currentParticleList.get(i).getId()), (int) ((int) currentParticleList.get(i)
						.getX() * umrechnung), (int) ((int) currentParticleList.get(i).getY() * umrechnung));
				System.out.println("ID: " + currentParticleList.get(i).getId() + "\tArea: " + area[i] + "\tX: " + x[i]
						+ "\tY: " + y[i]);
			}
		}

		counter++;

		// save as file
		ParticleContainer.getInstance().getParticles().put(counter, currentParticleList);
		IJ.resetThreshold(imp);
		FileSaver fs = new FileSaver(duplicatedImage);
		fs.saveAsPng("./res/tracked-images/" + String.format("%03d", counter) + ".png");
		duplicatedImage.flush();
		duplicatedImage = null;

		// imp.show();
		// Model model=new Model();
		// model.setLogger(Logger.IJ_LOGGER);
		// Settings settings=new Settings();
		// settings.setFrom(imp);
		// settings.detectorFactory = new LogDetectorFactory();
		//
		// Map<String,Object> detectorSettingsMap = new
		// HashMap<String,Object>();
		// detectorSettingsMap.put("DO_SUBPIXEL_LOCALIZATION", true);
		// detectorSettingsMap.put("RADIUS", 2.5);
		// detectorSettingsMap.put("TARGET_CHANNEL", 1.0);
		// detectorSettingsMap.put("THRESHOLD", 0.0);
		// detectorSettingsMap.put("DO_MEDIAN_FILTERING", false);
		//
		// settings.detectorSettings = detectorSettingsMap;
		//
		//
		// FeatureFilter filter1=new FeatureFilter("Quality", 1d, true);
		// //settings.addSpotFilter(filter1);
		//
		// settings.trackerFactory=new SparseLAPTrackerFactory();
		// settings.trackerSettings=LAPUtils.getDefaultLAPSettingsMap();
		// settings.trackerSettings.put("ALLOW_TRACK_SPLITTING", true);
		// settings.trackerSettings.put("ALLOW_TRACK_MERGING", true);
		//
		// TrackDurationAnalyzer analyzer = new TrackDurationAnalyzer();
		// settings.addTrackAnalyzer(analyzer);
		//
		//
		// FeatureFilter filter2=new FeatureFilter("Track_Displacement",
		// 10d,
		// true);
		// settings.addTrackFilter(filter2);
		//
		// TrackMate trackmate=new TrackMate(model, settings);
		// boolean ok = trackmate.checkInput();
		// if(!ok)
		// System.out.println("Nicht OK!\n"+trackmate.getErrorMessage());
		// else
		// System.out.println("OK");
		//
		// ok=trackmate.process();
		// if(!ok)
		// System.out.println("Nicht OK!\n"+trackmate.getErrorMessage());
		// else
		// System.out.println("OK");
		//
		// SelectionModel selectionModel=new SelectionModel(model);
		// HyperStackDisplayer displayer=new HyperStackDisplayer(model,
		// selectionModel, imp);
		// displayer.render();
		// displayer.refresh();
		//
		//
		// model.getLogger().log(model.toString());

		// TrackMateTest t = new TrackMateTest();
		// t.Start();

		// ResultsTable rt = new ResultsTable();
		// rt.reset();
		// //ParticleAnalyzer pa = new ParticleAnalyzer(0, CENTROID, rt,
		// 500,
		// 10000);
		// //pa.analyze(image);
		//
		// float[] xRes = rt.getColumn(ResultsTable.X_CENTROID);
		// float[] yRes = rt.getColumn(ResultsTable.Y_CENTROID);
		//
		// BufferedImage markedCellsImage = new
		// BufferedImage(image.getWidth(),
		// image.getHeight(), BufferedImage.TYPE_INT_RGB);
		// Graphics markedCellsGraphics = markedCellsImage.getGraphics();
		// ShortProcessor sp = (ShortProcessor)image.getProcessor();
		// ByteProcessor bp = sp.convertToByteProcessor();
		// image.setProcessor(bp);
		// bp.autoThreshold();
		// image.updateAndDraw();
		// //Watershed
		// EDM watershedPlugin = new EDM();
		// watershedPlugin.toWatershed(bp);

		// To Do: Cell tracking with CellProfiler

		// for (int i = 0; i < xRes.length; i++) {
		// Point particle = new Point((int) xRes[i], (int) yRes[i]);
		// markedCellsGraphics.fillRect(particle.x - 10, particle.y - 10,
		// 20,
		// 20);
		// }

		// FileSaver saver = new FileSaver(new
		// ImagePlus("Marked Cells Image",
		// markedCellsImage));
		// saver.saveAsJpeg("C:\\Users\\thoirm\\Desktop\\image.png");

	}
}
