package com.indago.labeleditor.plugin.behaviours;

import com.indago.labeleditor.core.controller.LabelEditorController;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.model.tagging.LabelEditorTag;
import net.imglib2.roi.labeling.LabelingType;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ConflictSelectionBehaviours<L> extends SelectionBehaviours<L> {

	public ConflictSelectionBehaviours(LabelEditorModel<L> model, LabelEditorController<L> controller) {
		super(model, controller);
	}

	@Override
	protected void handleClick() {
		//TODO start collect tagging events, pause listeners
		if (!noLabelsAtMousePosition()) {
			selectFirst(currentLabels);
		}
		//TODO resume model listeners and send collected events
	}

	@Override
	protected void handleShiftClick() {
		handleClick();
	}

	@Override
	protected void selectFirst(LabelingType<L> currentLabels) {
		L label = getFirst(currentLabels);
		Set<L> conflicts = getConflictingLabels(label);
		if(model.tagging().getTags(label).contains(LabelEditorTag.SELECTED)) return;
		deselect(conflicts);
		select(label);
	}

	private void deselect(Set<L> labels) {
		labels.forEach(label -> model.tagging().removeTag(LabelEditorTag.SELECTED, label));
	}

	private Set<L> getConflictingLabels(L label) {
		Set<L> res = new HashSet<>();
		model.labels().forEach(labelset -> {
			if(labelset.contains(label)) {
				res.addAll(labelset);
			}
		});
		return res;
	}

	@Override
	protected void selectNext(Collection<L> labels) {

		boolean foundSelected = false;
		for (Iterator<L> iterator = labels.iterator(); iterator.hasNext(); ) {
			L label = iterator.next();
			if (isSelected(label)) {
				foundSelected = true;
			} else {
				if (foundSelected) {
					if(model.tagging().getTags(label).contains(LabelEditorTag.SELECTED)) return;
					Set<L> conflicts = getConflictingLabels(label);
					deselect(conflicts);
					select(label);
					return;
				}
			}
		}
	}
}