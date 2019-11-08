package com.indago.labeleditor.howto;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvHandlePanel;
import com.indago.labeleditor.core.action.ActionManager;
import com.indago.labeleditor.plugin.bdv.BdvViewerInstance;
import com.indago.labeleditor.plugin.renderer.BorderLabelEditorRenderer;
import com.indago.labeleditor.core.display.RenderingManager;
import com.indago.labeleditor.core.model.DefaultLabelEditorModel;
import net.imagej.ImageJ;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;
import net.miginfocom.swing.MigLayout;
import org.junit.AfterClass;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * How to display a labeling in an existing BDV instance
 */
public class E12_ShowBorder {

	static ImageJ ij = new ImageJ();
	static JFrame frame = new JFrame("Label editor");
	static BdvHandlePanel panel;

	@Test
	public void run() throws IOException {
		Img input = (Img) ij.io().open(getClass().getResource("/blobs.png").getPath());
		Img thresholded = (Img) ij.op().threshold().otsu(input);
		ImgLabeling<Integer, IntType> labeling = ij.op().labeling().cca(thresholded, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);

		DefaultLabelEditorModel<Integer> model = new DefaultLabelEditorModel<>(labeling);

		RenderingManager<Integer> renderer = new RenderingManager<>(model);
		renderer.add(new BorderLabelEditorRenderer<>());
		model.labelRegions().forEach((label, regions) -> {
			model.tagging().addTag("displayed", label);
		});
		renderer.setTagColor("displayed", ARGBType.rgba(255,255,0,55));

		JPanel viewer = new JPanel(new MigLayout());
		panel = new BdvHandlePanel(frame, Bdv.options().is2D());
//		BdvFunctions.show(input, "RAW", Bdv.options().addTo(panel));
		renderer.getRenderings().forEach(rendering -> BdvFunctions.show(rendering, "", Bdv.options().addTo(panel)));

		viewer.add( panel.getViewerPanel(), "span, grow, push" );
		ActionManager<Integer> actionManager = new ActionManager<>();
		actionManager.init(new BdvViewerInstance(panel, null), model, renderer);
		actionManager.addDefaultActionHandlers();
		actionManager.set3DViewMode(false);

		frame.setMinimumSize(new Dimension(500,500));
		frame.setContentPane(viewer);
		frame.pack();
		frame.setVisible(true);
	}

	@AfterClass
	public static void dispose() {
		ij.context().dispose();
		frame.dispose();
		panel.close();
	}

	public static void main(String...args) throws IOException {
		new E12_ShowBorder().run();
	}

}
