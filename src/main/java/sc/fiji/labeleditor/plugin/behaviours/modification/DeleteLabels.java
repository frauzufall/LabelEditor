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
package sc.fiji.labeleditor.plugin.behaviours.modification;

import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.Regions;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.roi.labeling.LabelingType;
import org.scijava.ui.behaviour.Behaviour;
import sc.fiji.labeleditor.core.controller.InteractiveLabeling;
import sc.fiji.labeleditor.core.model.tagging.LabelEditorTag;

import java.util.List;

public class DeleteLabels<L> implements Behaviour {

	private final InteractiveLabeling<L> labeling;

	public DeleteLabels(InteractiveLabeling<L> labeling) {
		this.labeling = labeling;
	}

	public void deleteSelected() {
		List<L> selected = labeling.model().tagging().getLabels(LabelEditorTag.SELECTED);
		selected.forEach(label -> labeling.model().tagging().removeTagFromLabel(LabelEditorTag.SELECTED, label));
		delete(selected, labeling.getLabelingInScope());
		labeling.model().notifyLabelingListeners();
	}

	private static <L> void delete(List<L> labels, RandomAccessibleInterval<LabelingType<L>> labeling) {
		LabelRegions<L> regions = new LabelRegions<>(labeling);
		for (L label : labels) {
			IterableInterval<LabelingType<L>> sample = Regions.sample(regions.getLabelRegion(label), labeling);
			sample.forEach(pixel -> pixel.remove(label));
		}
	}

	static <L> void delete(L label, RandomAccessibleInterval<LabelingType<L>> labeling) {
		LabelRegions<L> regions = new LabelRegions<>(labeling);
		IterableInterval<LabelingType<L>> sample = Regions.sample(regions.getLabelRegion(label), labeling);
		sample.forEach(pixel -> pixel.remove(label));
	}
}
