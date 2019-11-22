package a.fiji.labeleditor.plugin.imagej;

import net.imglib2.roi.labeling.ImgLabeling;
import org.scijava.display.AbstractDisplay;
import org.scijava.display.Display;
import org.scijava.plugin.Plugin;

@Plugin(type = Display.class)
public class ImgLabelingDisplay extends AbstractDisplay<ImgLabeling> {

	public ImgLabelingDisplay() {
		super(ImgLabeling.class);
	}
}