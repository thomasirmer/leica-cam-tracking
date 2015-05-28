package LiveMicroscopy;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.ParticleAnalyzer;
import ij.process.ByteProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements the cell tracking algorithms.
 * 
 * @author Thomas Irmer
 */
public class CellTracking implements Measurements {

	private static Integer counter = 0;

	public void track(ImagePlus imp) {
		IJ.setThreshold(imp, 31, 255, "Black & White");

		ResultsTable rt = ResultsTable.getResultsTable();
		if (rt == null)
			rt = new ResultsTable();
		rt.showRowNumbers(true);
		ParticleAnalyzer particleAnalyzer = new ParticleAnalyzer(ParticleAnalyzer.CLEAR_WORKSHEET
				+ ParticleAnalyzer.SHOW_OUTLINES + ParticleAnalyzer.AREA + ParticleAnalyzer.CENTROID, Measurements.AREA
				+ Measurements.CENTROID, rt, 100, 10000, 0, 1);
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
		rt.show("Results");

		if (counter > 0) {
			ByteProcessor bp = (ByteProcessor) imp.getProcessor();
			List<Particle> oldParticleList = ParticleContainer.getInstance().getParticles().get(counter);
			double distance;
			double minDistance;

			for (int i = 0; i < currentParticleList.size(); i++) {
				minDistance = 9999;
				for (int j = 0; j < oldParticleList.size(); j++) {
					distance = currentParticleList.get(i).CompareTo(oldParticleList.get(j));

					if (distance < minDistance) {
						minDistance = distance;
						currentParticleList.get(i).setId(oldParticleList.get(j).getId());
					}
				}
				System.out.println("ID: " + currentParticleList.get(i).getId() + "\tArea: " + area[i] + "\tX: " + x[i]
						+ "\tY: " + y[i]);
				bp.drawString(Integer.toString(currentParticleList.get(i).getId()), (int) currentParticleList.get(i).getX(),
						(int) currentParticleList.get(i).getY());
			}
		}
		counter++;
		ParticleContainer.getInstance().getParticles().put(counter, currentParticleList);

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
		// FeatureFilter filter2=new FeatureFilter("Track_Displacement", 10d,
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
		// //ParticleAnalyzer pa = new ParticleAnalyzer(0, CENTROID, rt, 500,
		// 10000);
		// //pa.analyze(image);
		//
		// float[] xRes = rt.getColumn(ResultsTable.X_CENTROID);
		// float[] yRes = rt.getColumn(ResultsTable.Y_CENTROID);
		//
		// BufferedImage markedCellsImage = new BufferedImage(image.getWidth(),
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
		// markedCellsGraphics.fillRect(particle.x - 10, particle.y - 10, 20,
		// 20);
		// }

		// FileSaver saver = new FileSaver(new ImagePlus("Marked Cells Image",
		// markedCellsImage));
		// saver.saveAsJpeg("C:\\Users\\thoirm\\Desktop\\image.png");
	}
}
