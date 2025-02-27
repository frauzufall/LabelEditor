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
package org.scijava.table.interactive;

import org.scijava.listeners.Listeners;

import java.util.Collection;
import java.util.Set;

public interface SelectionModel< T >
{
	/**
	 * Get the selected state of a object.
	 *
	 * @param object
	 *            a object.
	 * @return {@code true} if specified object is selected.
	 */
	boolean isSelected(final T object);

	/**
	 * Sets the selected state of a object.
	 *
	 * @param object
	 *            a object.
	 * @param select
	 *            selected state to set for specified object.
	 */
	void setSelected(final T object, final boolean select);

	/**
	 * Toggles the selected state of a object.
	 *
	 * @param object
	 *            a object.
	 */
	void toggle(final T object);

	/**
	 * Focus on an object without changing its selection state.
	 *
	 * @param object
	 *            a object.
	 */
	void focus(final T object);

	/**
	 * Get the focus state of an object
	 *
	 * @param object
	 *            a object.
	 */
	boolean isFocused(final T object);

	/**
	 * Sets the selected state of a collection of imagesegment.
	 *
	 * @param objects
	 *            the object collection.
	 * @param select
	 *            selected state to set for specified object collection.
	 * @return {@code true} if the select was changed by this call.
	 */
	boolean setSelected(final Collection<T> objects, final boolean select);

	/**
	 * Clears this select.
	 *
	 * @return {@code true} if this select was not empty prior to
	 *         calling this method.
	 */
	boolean clearSelection();

	/**
	 * Get the selected imagesegment.
	 **
	 * @return a <b>new</b> {@link Set} containing all the selected imagesegment.
	 */
	Set< T > getSelected();

	T getFocused();

	boolean isEmpty();

	/**
	 * Get the list of select listeners. Add a {@link SelectionListener} to
	 * this list, for being notified when the object/edge select changes.
	 *
	 * @return the list of listeners
	 */
	Listeners<SelectionListener> listeners();

	void resumeListeners();

	void pauseListeners();
}

