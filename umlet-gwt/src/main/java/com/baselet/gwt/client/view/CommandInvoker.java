package com.baselet.gwt.client.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.baselet.command.AddGridElementCommand;
import com.baselet.command.CommandTarget;
import com.baselet.command.Controller;
import com.baselet.command.RemoveGridElementCommand;
import com.baselet.control.basics.geom.Rectangle;
import com.baselet.control.constants.SharedConstants;
import com.baselet.element.GridElementUtils;
import com.baselet.element.Selector;
import com.baselet.element.interfaces.Diagram;
import com.baselet.element.interfaces.GridElement;
import com.baselet.gwt.client.element.BrowserStorage;
import com.baselet.gwt.client.element.ElementFactoryGwt;

public class CommandInvoker extends Controller {

	private static final CommandInvoker instance = new CommandInvoker();

	public static CommandInvoker getInstance() {
		return instance;
	}

	private CommandInvoker() {
		super();
	}

	void addElements(CommandTarget target, List<GridElement> elements) {
		executeCommand(new AddGridElementCommand(target, elements));
	}

	void removeElements(CommandTarget target, List<GridElement> elements) {
		executeCommand(new RemoveGridElementCommand(target, elements));
	}

	void removeSelectedElements(CommandTarget target) {
		removeElements(target, target.getSelector().getSelectedElements());
	}

	// TODO implement copy & paste as commands

	void copySelectedElements(CommandTarget target) {
		BrowserStorage.setClipboard(copyElementsInList(target.getSelector().getSelectedElements(), target.getDiagram())); // must be copied here to ensure location etc. will not be changed
	}

	void cutSelectedElements(CommandTarget target) {
		copySelectedElements(target);
		removeSelectedElements(target);
	}

	void pasteElements(CommandTarget target) {
		List<GridElement> copyOfElements = copyElementsInList(BrowserStorage.getClipboard(), target.getDiagram());
		Selector.replaceGroupsWithNewGroups(copyOfElements, target.getSelector());
		realignElementsToVisibleRect(target, copyOfElements);
		addElements(target, copyOfElements); // copy here to make sure it can be pasted multiple times
	}

	private List<GridElement> copyElementsInList(Collection<GridElement> sourceElements, Diagram targetDiagram) {
		List<GridElement> targetElements = new ArrayList<GridElement>();
		for (GridElement ge : sourceElements) {
			GridElement e = ElementFactoryGwt.create(ge, targetDiagram);
			targetElements.add(e);
		}
		return targetElements;
	}

	void realignElementsToVisibleRect(CommandTarget target, List<GridElement> gridElements) {
		Rectangle rect = GridElementUtils.getGridElementsRectangle(gridElements);
		Rectangle visible = target.getVisibleBounds();
		for (GridElement ge : gridElements) {
			ge.getRectangle().move(visible.getX() - rect.getX() + SharedConstants.DEFAULT_GRID_SIZE, visible.getY() - rect.getY() + SharedConstants.DEFAULT_GRID_SIZE);
		}
	}

	public void updateSelectedElementsProperty(CommandTarget target, String key, Object value) {
		for (GridElement e : target.getSelector().getSelectedElements()) {
			e.setProperty(key, value);
		}
		target.updatePropertiesPanelWithSelectedElement();
	}
}
