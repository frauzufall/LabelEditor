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
package sc.fiji.labeleditor.core.model.tagging;

import org.scijava.listeners.Listeners;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.table.DefaultGenericTable;
import sc.fiji.labeleditor.core.model.LabelEditorModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class DefaultLabelEditorTagging<L> implements LabelEditorTagging<L> {

	@Parameter
	LogService log;

	private final LabelEditorModel model;
	private final TaggingTable<Object, L> table = new TaggingTable<>();

	private final Listeners.List<TagChangeListener> listeners = new Listeners.SynchronizedList<>();
	private boolean listenersPaused = false;
	private List<TagChangedEvent> keptEvents = new ArrayList<>();

	public DefaultLabelEditorTagging(LabelEditorModel model) {
		this.model = model;
	}

	@Override
	public Listeners< TagChangeListener > listeners() {
		return listeners;
	}

	@Override
	public void pauseListeners() {
		listenersPaused = true;
	}

	@Override
	public void resumeListeners() {
		listenersPaused = false;
		if(keptEvents.size() > 0) {
//			System.out.println(keptEvents);
			listeners.list.forEach(listener -> listener.tagChanged(keptEvents));
			keptEvents.clear();
		}
	}

	@Override
	public List<Object> getAllTags() {
		return table.getColumns();
	}

	@Override
	public List<L> filterLabelsWithTag(List<L> labels, Object tag) {
		return table.filter(labels, tag);
	}

	@Override
	public List filterLabelsWithAnyTag(List<L> labels, Set<Object> tags) {
		return table.filterAny(labels, tags);
	}

	@Override
	public void toggleTag(Object tag, L label) {
		if(getTags(label).contains(tag)) removeTagFromLabel(tag, label);
		else addTagToLabel(tag, label);
	}

	private void notifyListeners(TagChangedEvent e) {
		if(log!= null) log.debug(e.toString());
		if(listenersPaused) {
			keptEvents.add(e);
		} else {
//			System.out.println(e);
			listeners.list.forEach(listener -> listener.tagChanged(Collections.singletonList(e)));
		}
	}

	private synchronized void notifyListeners(List<Object> tags, List<L> labels, LabelEditorModel model, TagChangedEvent.Action action) {
		TagChangedEvent e = new TagChangedEvent();
		e.action = action;
		e.tags = tags;
		e.model = model;
		e.labels = labels;
		notifyListeners(e);
	}

	@Override
	public void addTagToLabel(Object tag, L label) {
		if(table.add(tag, label)) {
			notifyListeners(Collections.singletonList(tag), Collections.singletonList(label), model, TagChangedEvent.Action.ADDED);
		}
	}

	@Override
	public void addTagToLabels(Object tag, List<L> labels) {
		List<L> added = table.add(tag, labels);
		notifyListeners(Collections.singletonList(tag), added, model, TagChangedEvent.Action.ADDED);
	}

	@Override
	public void addValueToLabel(Object tag, Object value, L label) {
		if(table.add(tag, value, label)) {
			notifyListeners(Collections.singletonList(tag), Collections.singletonList(label), model, TagChangedEvent.Action.ADDED);
		}
	}

	@Override
	public Object getValue(Object tag, L label) {
		return table.get(tag, label);
	}

	@Override
	public List filterLabelsWithAnyTag(Set<Object> tags) {
		return table.filterAny(tags);
	}

	@Override
	public List filterLabelsWithTag(Object tag) {
		return table.filter(tag);
	}

	@Override
	public void addTag(Object tag) {
		table.addColumn(tag);
	}

	@Override
	public void removeTagFromLabel(Object tag, L label) {
		if(table.removeEntry(tag, label)) {
			notifyListeners(Collections.singletonList(tag), Collections.singletonList(label), model, TagChangedEvent.Action.REMOVED);
		}
	}

	@Override
	public void removeTagFromLabels(Object tag, List<L> labels) {

	}

	@Override
	public List<Object> getTags(L label) {
		return table.get(label);
	}

	@Override
	public synchronized void removeTagFromLabel(Object tag) {
		List<L> labels = table.getRows(tag);
		table.clearColumn(tag);
		notifyListeners(Collections.singletonList(tag), labels, model, TagChangedEvent.Action.REMOVED);
	}

	@Override
	public List<L> getLabels(Object tag) {
		return table.getRows(tag);
	}

	class TaggingTable<ColumnType, RowType> extends DefaultGenericTable {

		private final HashMap<ColumnType, Integer> tagToColumn = new HashMap<>();
		private final HashMap<Integer, ColumnType> columnToTag = new HashMap<>();
		private final HashMap<RowType, Integer> labelToRow = new HashMap<>();
		private final HashMap<Integer, RowType> rowToLabel = new HashMap<>();

		synchronized List<ColumnType> getColumns() {
			return new ArrayList<>(tagToColumn.keySet());
		}

		synchronized List<RowType> filter(List<RowType> labels, ColumnType tag) {
			List<RowType> res = new ArrayList<>();
			int column = tagToColumn.get(tag);
			for (RowType label : labels) {
				Integer row = labelToRow.get(label);
				if(row == null) continue;
				Object val = get(column, row);
				if(exists(val)) res.add(label);
			}
			return res;
		}

		synchronized List<RowType> filter(ColumnType tag) {
			List<RowType> res = new ArrayList<>();
			int column = tagToColumn.get(tag);
			for (int i = 0; i < getRowCount(); i++) {
				Object val = get(column, i);
				if(exists(val)) res.add(rowToLabel.get(i));
			}
			return res;
		}

		synchronized List<RowType> filterAny(List<RowType> labels, Set<ColumnType> tags) {
			List<RowType> res = new ArrayList<>();
			for (RowType label : labels) {
				for (ColumnType tag : tags) {
					int column = tagToColumn.get(tag);
					Object val = get(column, labelToRow.get(label));
					if (exists(val)) {
						res.add(label);
						break;
					}
				}
			}
			return res;
		}

		synchronized List<RowType> filterAny(Set<ColumnType> tags) {
			List<RowType> res = new ArrayList<>();
			for (ColumnType tag : tags) {
				for (int i = 0; i < getRowCount(); i++) {
					int column = tagToColumn.get(tag);
					Object val = get(column, i);
					if (exists(val)) {
						res.add(rowToLabel.get(i));
						break;
					}
				}
			}
			return res;
		}

		synchronized boolean add(ColumnType tag, RowType label) {
			Integer row = labelToRow.get(label);
			if(row == null) {
				table.appendRow();
				row = table.getRowCount()-1;
				labelToRow.put(label, row);
				rowToLabel.put(row, label);
			}
			Integer col = tagToColumn.get(tag);
			if(col == null) {
				table.appendColumn();
				col = table.getColumnCount()-1;
				tagToColumn.put(tag, col);
				columnToTag.put(col, tag);
			}
			if(table.get(col, row) != null) return false;
			table.set(col, row, true);
			return true;
		}

		synchronized boolean add(ColumnType tag, Object value, RowType label) {
			Integer row = labelToRow.get(label);
			if(row == null) {
				table.appendRow();
				row = table.getRowCount()-1;
				labelToRow.put(label, row);
				rowToLabel.put(row, label);
			}
			Integer col = tagToColumn.get(tag);
			if(col == null) {
				table.appendColumn();
				col = table.getColumnCount()-1;
				tagToColumn.put(tag, col);
				columnToTag.put(col, tag);
			}
			if(table.get(col, row) != null) return false;
			table.set(col, row, value);
			return true;
		}

		private boolean exists(Object val) {
			return val != null && val != "";
		}

		synchronized Object get(ColumnType tag, RowType label) {
			Integer row = labelToRow.get(label);
			if(row == null) return null;
			Integer col = tagToColumn.get(tag);
			if(col == null) return null;
			return table.get(col, row);
		}

		synchronized boolean removeEntry(ColumnType tag, RowType label) {
			Integer row = labelToRow.get(label);
			if(row == null) return false;
			Integer col = tagToColumn.get(tag);
			if(col == null) return false;
			if(table.get(col, row) == null) return false;
			table.set(col, row, null);
			return true;
		}

		synchronized List<ColumnType> get(RowType label) {
			synchronized(table) {
				synchronized (labelToRow) {
					Integer row = labelToRow.get(label);
					if(row == null) return Collections.emptyList();
					List<ColumnType> res = new ArrayList<>();
					for (int i = 0; i < table.getColumnCount(); i++) {
						Object val = table.get(i, row);
						if(exists(val)) {
							ColumnType tag = columnToTag.get(i);
							res.add(tag);
						}
					}
					return res;
				}
			}
		}

		synchronized void clearColumn(ColumnType tag) {
			Integer col = tagToColumn.get(tag);
			if(col == null) return;
			for (int i = 0; i < getRowCount(); i++) {
				if(exists(get(col, i))) set(col, i, null);
			}
//			int rows = get(col).size();
//			get(col).clear();
//			get(col).setSize(rows);
		}

		synchronized List<RowType> getRows(ColumnType tag) {
			Integer col = tagToColumn.get(tag);
			if(col == null) return Collections.emptyList();
			List<RowType> res = new ArrayList<>();
			for (int i = 0; i < table.getRowCount(); i++) {
				if(exists(table.get(col, i))) res.add(rowToLabel.get(i));
			}
			return Collections.unmodifiableList(res);
		}

		synchronized List<RowType> add(ColumnType tag, List<RowType> labels) {
			List<RowType> res = new ArrayList<>();
			Integer col = tagToColumn.get(tag);
			if(col == null) {
				table.appendColumn();
				col = table.getColumnCount()-1;
				tagToColumn.put(tag, col);
				columnToTag.put(col, tag);
			}
			for (RowType label : labels) {
				Integer row = labelToRow.get(label);
				if(row == null) {
					table.appendRow();
					row = table.getRowCount()-1;
					labelToRow.put(label, row);
					rowToLabel.put(row, label);
				}
				if(table.get(col, row) != null) continue;
				table.set(col, row, true);
				res.add(label);
			}
			return res;
		}

		synchronized void addColumn(ColumnType tag) {
			table.appendColumn();
			int col = table.getColumnCount() - 1;
			tagToColumn.put(tag, col);
			columnToTag.put(col, tag);
		}
	}

}
