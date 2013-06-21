package com.baselet.diagram.commandnew;

import com.baselet.element.GridElement;
import com.baselet.element.Selector;

public class AddGridElementCommand extends Command {

	private CanAddAndRemoveGridElement target;
	private Selector selector;
	private GridElement element;

	public AddGridElementCommand(CanAddAndRemoveGridElement target, Selector selector, GridElement element) {
		this.target = target;
		this.selector = selector;
		this.element = element;
	}

	@Override
	public void execute() {
		target.addGridElement(element);
		selector.singleSelect(element);
	}

	@Override
	public void undo() {
		target.removeGridElement(element);
	}
	
}