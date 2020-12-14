package com.baselet.gwt.client.element;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baselet.element.interfaces.GridElement;
import com.baselet.gwt.client.BaseletGWT;
import com.baselet.gwt.client.clipboard.ClipboardStorage;
import com.baselet.gwt.client.view.DrawPanel;
import com.baselet.gwt.client.view.EventHandlingUtils;
import com.google.gwt.core.client.GWT;

public class WebStorage {
	private static ClipboardStorage clipboardStorage;
	static Logger log = LoggerFactory.getLogger(BaseletGWT.class);

	public static boolean initClipboard() {
		clipboardStorage = GWT.create(ClipboardStorage.class);
		return clipboardStorage.init();
	}

	public static void addSavedDiagram(String name, String diagramXml) {
		clipboardStorage.setSaved(name, diagramXml);
		log.debug("test");
	}

	public static void removeSavedDiagram(String chosenName) {
		clipboardStorage.remove(chosenName);
	}

	public static String getSavedDiagram(String name) {
		return clipboardStorage.getSaved(name);
	}

	public static Collection<String> getSavedDiagramKeys() {
		return clipboardStorage.getAllSaved(true).keySet();
	}

	public static void setClipboard(List<GridElement> gridelements) {
		clipboardStorage.set(DiagramXmlParser.gridElementsToXml(gridelements));
	}

	public static void getClipboardAsync() {
		clipboardStorage.get();
	}

	public static void updateTargetPanel(EventHandlingUtils.EventHandlingTarget target) {
		clipboardStorage.updateTargetPanel((DrawPanel) target);
	}
}
