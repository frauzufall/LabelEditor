package com.indago.labeleditor.action;

import bdv.util.BdvHandlePanel;
import net.imglib2.roi.labeling.LabelingType;

import java.awt.event.MouseEvent;

public interface ActionHandler<L> {
	void init();

	LabelingType<L> getLabelsAtMousePosition(MouseEvent e);

	void set3DViewMode(boolean mode3D);
}