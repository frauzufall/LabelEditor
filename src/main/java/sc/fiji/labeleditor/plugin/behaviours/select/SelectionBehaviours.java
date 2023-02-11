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
package sc.fiji.labeleditor.plugin.behaviours.select;

import net.imglib2.roi.labeling.LabelingType;
import org.scijava.command.CommandService;
import org.scijava.listeners.Listeners;
import org.scijava.plugin.Parameter;
import org.scijava.table.interactive.SelectionListener;
import org.scijava.table.interactive.SelectionModel;
import org.scijava.ui.behaviour.Behaviour;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.ScrollBehaviour;
import org.scijava.ui.behaviour.util.Behaviours;
import sc.fiji.labeleditor.core.controller.InteractiveLabeling;
import sc.fiji.labeleditor.core.controller.LabelEditorBehaviours;
import sc.fiji.labeleditor.core.model.tagging.LabelEditorTag;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SelectionBehaviours<L> implements SelectionModel< L >, LabelEditorBehaviours<L> {

	@Parameter
	CommandService commandService;

	protected InteractiveLabeling<L> labeling;

	private final Listeners.List<SelectionListener> listeners = new Listeners.SynchronizedList<>();
	private boolean listenersPaused = false;

	protected static final String TOGGLE_LABEL_SELECTION_NAME = "LABELEDITOR_TOGGLELABELSELECTION";
	protected static final String TOGGLE_LABEL_SELECTION_TRIGGERS = "shift scroll";
	protected static final String SELECT_FIRST_LABEL_NAME = "LABELEDITOR_SELECTFIRSTLABEL";
	protected static final String SELECT_FIRST_LABEL_TRIGGERS = "button1";
	protected static final String ADD_LABEL_TO_SELECTION_NAME = "LABELEDITOR_ADDLABELTOSELECTION";
	protected static final String ADD_LABEL_TO_SELECTION_TRIGGERS = "shift button1";
	protected static final String SELECT_ALL_LABELS_NAME = "LABELEDITOR_SELECTALL";
	protected static final String SELECT_ALL_LABELS_TRIGGERS = "ctrl A";

	@Override
	public void init(InteractiveLabeling<L> labeling) {
		this.labeling = labeling;
	}

	@Override
	public void install(Behaviours behaviours, Component panel) {

		behaviours.behaviour(getToggleLabelSelectionBehaviour(), TOGGLE_LABEL_SELECTION_NAME + labeling.toString(), TOGGLE_LABEL_SELECTION_TRIGGERS);
		behaviours.behaviour(getSelectFirstLabelBehaviour(), SELECT_FIRST_LABEL_NAME + labeling.toString(), SELECT_FIRST_LABEL_TRIGGERS);
		behaviours.behaviour(getAddFirstLabelToSelectionBehaviour(), ADD_LABEL_TO_SELECTION_NAME + labeling.toString(), ADD_LABEL_TO_SELECTION_TRIGGERS);
		behaviours.behaviour(getSelectAllBehaviour(), SELECT_ALL_LABELS_NAME + labeling.toString(), SELECT_ALL_LABELS_TRIGGERS);

	}

	private Behaviour getSelectAllBehaviour() {
		return (ClickBehaviour) (arg0, arg1) -> {
			labeling.model().tagging().pauseListeners();
			selectAll();
			labeling.model().tagging().resumeListeners();
		};
	}

	protected Behaviour getAddFirstLabelToSelectionBehaviour() {
		return (ClickBehaviour) (arg0, arg1) -> {
			labeling.model().tagging().pauseListeners();
			addFirstLabelToSelection(arg0, arg1);
			labeling.model().tagging().resumeListeners();
		};
	}

	protected Behaviour getSelectFirstLabelBehaviour() {
		return (ClickBehaviour) (arg0, arg1) -> {
			labeling.model().tagging().pauseListeners();
			selectFirstLabel(arg0, arg1);
			labeling.model().tagging().resumeListeners();
		};
	}

	protected Behaviour getToggleLabelSelectionBehaviour() {
		return (ScrollBehaviour) (wheelRotation, isHorizontal, x, y) -> {
			if(!isHorizontal) {
				labeling.model().tagging().pauseListeners();
				toggleLabelSelection(wheelRotation > 0, x, y);
				labeling.model().tagging().resumeListeners();
			}};
	}

	public void selectAll() {
		labeling.getLabelSetInScope().forEach(this::select);
	}

	protected void selectFirstLabel(int x, int y) {
		LabelingType<L> labels = labeling.interfaceInstance().findLabelsAtMousePosition(x, y, labeling);
		if (foundLabels(labels)) {
			selectFirst(labels);
		} else {
			deselectAll();
		}
	}

	private boolean foundLabels(LabelingType<L> labels) {
		return labels != null && labels.size() > 0;
	}

	protected void addFirstLabelToSelection(int x, int y) {
		LabelingType<L> labels = labeling.interfaceInstance().findLabelsAtMousePosition(x, y, labeling);
		if (foundLabels(labels)) {
			toggleSelectionOfFirst(labels);
		}
	}

	protected void toggleLabelSelection(boolean forwardDirection, int x, int y) {
		LabelingType<L> labels = labeling.interfaceInstance().findLabelsAtMousePosition(x, y, labeling);
		if(!foundLabels(labels)) return;
		if(!anySelected(labels)) {
			selectFirst(labels);
			return;
		}
		if (forwardDirection)
			selectNext(labels);
		else
			selectPrevious(labels);
	}

	protected void selectFirst(LabelingType<L> labels) {
		L label = getFirst(labels);
		if(labeling.model().tagging().getTags(label).contains(LabelEditorTag.SELECTED)) return;
		deselectAll();
		select(label);
	}

	protected void toggleSelectionOfFirst(LabelingType<L> labels) {
		L label = getFirst(labels);
		if(labeling.model().tagging().getTags(label).contains(LabelEditorTag.SELECTED)) {
			deselect(label);
		} else {
			select(label);
		}
	}

	protected L getFirst(LabelingType<L> labels) {
		if(labels.size() == 0) return null;
		List<L> orderedLabels = new ArrayList<>(labels);
		orderedLabels.sort(labeling.model().getLabelComparator());
		return orderedLabels.get(0);
	}

	public boolean isSelected(L label) {
		return labeling.model().tagging().getTags(label).contains(LabelEditorTag.SELECTED);
	}

	@Override
	public void setSelected(L label, boolean select) {
		if(select) {
			labeling.model().tagging().removeTagFromLabel(LabelEditorTag.SELECTED);
			labeling.model().tagging().addTagToLabel(LabelEditorTag.SELECTED, label);
		} else {
			labeling.model().tagging().removeTagFromLabel(LabelEditorTag.SELECTED, label);
		}
		notifyListeners();
		focus(label);
	}

	@Override
	public void toggle(L label) {
		setSelected(label, !isSelected(label));
	}

	@Override
	public void focus(L label) {
		labeling.model().tagging().addTagToLabel(LabelEditorTag.FOCUS, label);
		notifyListenersFocusChanged();
	}

	@Override
	public boolean isFocused(L label) {
		return labeling.model().tagging().getTags(label).contains(LabelEditorTag.FOCUS);
	}

	@Override
	public boolean setSelected(Collection<L> labels, boolean select) {
		if(select) {
			labels.forEach(label -> {
				labeling.model().tagging().addTagToLabel(LabelEditorTag.SELECTED, label);
			});
		} else {
			labels.forEach(label -> {
				labeling.model().tagging().removeTagFromLabel(LabelEditorTag.SELECTED, label);
			});
		}
		notifyListeners();
		return true;
	}

	@Override
	public boolean clearSelection() {
		deselectAll();
		notifyListeners();
		return false;
	}

	@Override
	public Set<L> getSelected() {
		return new HashSet<>(labeling.model().tagging().getLabels(LabelEditorTag.SELECTED));
	}

	@Override
	public L getFocused() {
		List<L> labels = labeling.model().tagging().getLabels(LabelEditorTag.FOCUS);
		if(labels == null || labels.size() == 0) return null;
		return labels.iterator().next();
	}

	@Override
	public boolean isEmpty() {
		List<L> selected = labeling.model().tagging().getLabels(LabelEditorTag.SELECTED);
		if(selected == null) return true;
		return selected.size() == 0;
	}

	@Override
	public Listeners<SelectionListener> listeners() {
		return listeners;
	}

	@Override
	public void resumeListeners() {
		listenersPaused = false;
	}

	@Override
	public void pauseListeners() {
		listenersPaused = true;
	}

	private void notifyListeners() {
		listeners.list.forEach(listener -> {
			listener.selectionChanged();
		});
	}

	private void notifyListenersFocusChanged() {
		listeners.list.forEach(listener -> {
			listener.focusChanged();
		});
	}

	protected boolean anySelected(LabelingType<L> labels) {
		return labels.stream().anyMatch(label -> labeling.model().tagging().getTags(label).contains(LabelEditorTag.SELECTED));
	}

	protected void select(L label) {
		labeling.model().tagging().addTagToLabel(LabelEditorTag.SELECTED, label);
		labeling.model().tagging().removeTagFromLabel(LabelEditorTag.MOUSE_OVER, label);
		labeling.model().tagging().removeTagFromLabel(LabelEditorTag.FOCUS);
		focus(label);
		notifyListeners();
	}

	protected void selectPrevious(LabelingType<L> labels) {
		List<L> reverseLabels = new ArrayList<>(labels);
		Collections.reverse(reverseLabels);
		selectNext(reverseLabels);
	}

	protected void selectNext(Collection<L> labels) {
		boolean foundSelected = false;
		for (Iterator<L> iterator = labels.iterator(); iterator.hasNext(); ) {
			L label = iterator.next();
			if (isSelected(label)) {
				foundSelected = true;
				if(iterator.hasNext()) {
					deselect(label);
				}
			} else {
				if (foundSelected) {
					select(label);
					return;
				}
			}
		}
	}

	protected void deselect(L label) {
		labeling.model().tagging().removeTagFromLabel(LabelEditorTag.SELECTED, label);
		labeling.model().tagging().removeTagFromLabel(LabelEditorTag.FOCUS);
		labeling.model().tagging().addTagToLabel(LabelEditorTag.FOCUS, label);
	}

	public void deselectAll() {
		labeling.getLabelSetInScope().forEach(label -> labeling.model().tagging().removeTagFromLabel(LabelEditorTag.SELECTED, label));
	}

	public void invertSelection() {
		List<L> all = new ArrayList<>(labeling.getLabelSetInScope());
		List<L> selected = labeling.model().tagging().filterLabelsWithTag(all, LabelEditorTag.SELECTED);
		all.removeAll(selected);
		all.forEach(this::select);
		selected.forEach(this::deselect);
	}

	public void selectByTag() {
		commandService.run(SelectByTagCommand.class, true, "labeling", labeling);
	}
}
