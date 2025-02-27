/*-
 * #%L
 * UI component for image segmentation label comparison and selection
 * %%
 * Copyright (C) 2019 - 2023 Deborah Schmidt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package sc.fiji.labeleditor.plugin.imagej;

import bdv.util.AxisOrder;
import bdv.util.BdvOptions;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.viewer.DisplayViewer;
import sc.fiji.labeleditor.plugin.mode.timeslice.TimeSliceLabelEditorBdvPanel;
import sc.fiji.labeleditor.plugin.mode.timeslice.TimeSliceLabelEditorModel;

import javax.swing.*;

/**
 * This class creates a {@link TimeSliceLabelEditorBdvPanel} for a {@link TimeSliceLabelEditorModel}.
 */
@Plugin(type = DisplayViewer.class, priority = 1.0)
public class SwingTimeSliceLabelEditorModelDisplayViewer extends EasySwingDisplayViewer<TimeSliceLabelEditorModel> {

	@Parameter
	Context context;
	private TimeSliceLabelEditorBdvPanel panel;

	public SwingTimeSliceLabelEditorModelDisplayViewer() {
		super(TimeSliceLabelEditorModel.class);
	}

	@Override
	protected boolean canView(TimeSliceLabelEditorModel model) {
		return true;
	}

	@Override
	protected JPanel createDisplayPanel(TimeSliceLabelEditorModel model) {
		BdvOptions options = new BdvOptions();
		if(model.getTimeDimension() == 2) {
			options.axisOrder(AxisOrder.XYT)
					.is2D();
		} else {
			options.axisOrder(AxisOrder.XYZT);
		}
		panel = new TimeSliceLabelEditorBdvPanel(context, options);
		panel.add(model, options);
		return panel;
	}

	@Override
	public void redraw()
	{
		//TODO do I need to update the panel / create a new panel?
		getWindow().pack();
	}

	@Override
	public void redoLayout()
	{
		// ignored
	}

	@Override
	public void setLabel(final String s)
	{
		// ignored
	}

	@Override
	public void dispose() {
		panel.dispose();
		super.dispose();
	}
}
