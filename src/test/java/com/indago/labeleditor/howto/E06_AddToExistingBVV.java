package com.indago.labeleditor.howto;

import bvv.util.Bvv;
import bvv.util.BvvFunctions;
import bvv.util.BvvStackSource;
import com.indago.labeleditor.core.view.LabelEditorView;
import com.indago.labeleditor.core.model.DefaultLabelEditorModel;
import com.indago.labeleditor.plugin.bvv.BvvInterface;
import net.imagej.ImageJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.Views;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class E06_AddToExistingBVV {

	static ImageJ ij = new ImageJ();

	@Test
	public <T extends RealType<T>> void run() throws IOException {

		Img input = (Img) ij.io().open(getClass().getResource("/blobs.png").getPath());
		RandomAccessibleInterval inputStack = input;
		List<RandomAccessibleInterval<T>> stack = new ArrayList<>();
		for (int i = 0; i < 50; i++) {
			stack.add(inputStack);
		}
		inputStack = Views.stack(stack);
		Img thresholded = (Img) ij.op().threshold().otsu(Views.iterable(inputStack));
		ImgLabeling<Integer, IntType> labeling = ij.op().labeling().cca(thresholded, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);

		DefaultLabelEditorModel<Integer> model = new DefaultLabelEditorModel<>(labeling);
		model.labelRegions().forEach((label, regions) -> {
			model.tagging().addTag("displayed", label);
		});

		LabelEditorView<Integer> renderer = new LabelEditorView<>(model);
		renderer.addDefaultRenderings();
		renderer.initRenderings();
		renderer.setTagColor("displayed", ARGBType.rgba(255,255,0,55));
		renderer.updateOnTagChange();

		List<BvvStackSource> sources = new ArrayList<>();
		BvvStackSource source = null;
		for (Map.Entry<String, RandomAccessibleInterval> entry : renderer.getNamedRenderings().entrySet()) {
			String title = entry.getKey();
			RandomAccessibleInterval img = entry.getValue();
			if (source == null) {
				source = BvvFunctions.show(img, title, Bvv.options());
			} else {
				source = BvvFunctions.show(img, title, Bvv.options().addTo(source.getBvvHandle()));
			}
			sources.add(source);
		}

		BvvInterface.control(source.getBvvHandle(), sources, model, renderer);

	}


	@AfterClass
	public static void dispose() {
		ij.context().dispose();
	}

	public static void main(String... args) throws IOException {
		new E06_AddToExistingBVV().run();
	}


}
