package com.baselet.gwt.client.view;

import org.vectomatic.file.FileUploadExt;

import com.baselet.control.config.SharedConfig;
import com.baselet.diagram.draw.helper.theme.Theme;
import com.baselet.diagram.draw.helper.theme.ThemeChangeListener;
import com.baselet.diagram.draw.helper.theme.ThemeFactory;
import com.baselet.element.interfaces.Diagram;
import com.baselet.gwt.client.base.Converter;
import com.baselet.gwt.client.base.Notification;
import com.baselet.gwt.client.element.DiagramXmlParser;
import com.baselet.gwt.client.element.WebStorage;
import com.baselet.gwt.client.logging.CustomLogger;
import com.baselet.gwt.client.logging.CustomLoggerFactory;
import com.baselet.gwt.client.view.commands.SaveCommand;
import com.baselet.gwt.client.view.panel.wrapper.AutoResizeScrollDropPanel;
import com.baselet.gwt.client.view.panel.wrapper.FileOpenHandler;
import com.baselet.gwt.client.view.utils.DropboxIntegration;
import com.baselet.gwt.client.view.utils.StartupDiagramLoader;
import com.baselet.gwt.client.view.widgets.DownloadPopupPanel;
import com.baselet.gwt.client.view.widgets.FilenameAndScaleHolder;
import com.baselet.gwt.client.view.widgets.SaveDialogBox;
import com.baselet.gwt.client.view.widgets.SaveDialogBox.Callback;
import com.baselet.gwt.client.view.widgets.ShortcutDialogBox;
import com.baselet.gwt.client.view.widgets.propertiespanel.PropertiesTextArea;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.dom.client.Element;

public class MainView extends Composite implements ThemeChangeListener {

	private static MainViewUiBinder uiBinder = GWT.create(MainViewUiBinder.class);

	interface MainViewUiBinder extends UiBinder<Widget, MainView> {}

	interface MyStyle extends CssResource {
		String menuItem();
	}

	@UiField
	MyStyle style;

	@UiField
	FocusPanel mainPanel;

	@UiField(provided = true)
	protected SplitLayoutPanel diagramPaletteSplitter = new SplitLayoutPanel(4) {
		@Override
		public void onResize() {
			super.onResize();
			updateNotificationPosition();
		}
	};

	@UiField
	FlowPanel menuPanel;

	@UiField
	DivElement propertiesDiv;

	@UiField
	FlowPanel restoreMenuPanel;

	@UiField(provided = true)
	SplitLayoutPanel palettePropertiesSplitter = new SplitLayoutPanel() {
		@Override
		public void onResize() {
			diagramPanel.redraw();
			palettePanel.redraw();
		}
	};

	@UiField
	DockLayoutPanel paletteChooserCanvasSplitter;

	@UiField
	SimpleLayoutPanel diagramPanelWrapper;

	@UiField
	ListBox paletteChooser;

	@UiField
	PropertiesTextArea propertiesPanel;

	@UiField
	SimpleLayoutPanel palettePanelWrapper;

	private final DrawPanel diagramPanel;
	private final AutoResizeScrollDropPanel diagramScrollPanel;

	private final DrawPanel palettePanel;
	private final AutoResizeScrollDropPanel paletteScrollPanel;

	private final FileUploadExt hiddenUploadButton = new FileUploadExt();
	private final FileOpenHandler handler;

	private final CustomLogger log = CustomLoggerFactory.getLogger(MainView.class);

	private final DropboxIntegration dropboxInt;

	private final FilenameAndScaleHolder lastExportFilename = new FilenameAndScaleHolder("");

	private final SaveCommand saveCommand;

	private final DownloadPopupPanel popupPanel;

	private final ScheduledCommand exportToDropbox = new ScheduledCommand() {
		private final SaveDialogBox saveDialogBox = new SaveDialogBox(new Callback() {
			@Override
			public void callback(final String chosenName) {

				String uxfUrl = "data:text/xml;charset=utf-8," + DiagramXmlParser.diagramToXml(true, false, diagramPanel.getDiagram());
				dropboxInt.openDropboxExport(uxfUrl, chosenName);

			}
		}, "Export to Dropbox");

		@Override
		public void execute() {
			saveDialogBox.clearAndCenter();
		}
	};

	public void setDiagram(Diagram diagram) {
		diagramPanel.setDiagram(diagram);
	}

	public void hideSideBars() {
		diagramPaletteSplitter.setWidgetSize(menuPanel, 0.0);
		diagramPaletteSplitter.setWidgetSize(palettePropertiesSplitter, 0.0);
	}

	public SaveCommand getSaveCommand() {
		return saveCommand;
	}

	public MainView() {
		initWidget(uiBinder.createAndBindUi(this));

		diagramPaletteSplitter.setWidgetToggleDisplayAllowed(palettePropertiesSplitter, true);
		diagramPaletteSplitter.setWidgetSnapClosedSize(palettePropertiesSplitter, 100);
		diagramPaletteSplitter.setWidgetMinSize(palettePropertiesSplitter, 200);
		diagramPaletteSplitter.setWidgetToggleDisplayAllowed(menuPanel, true);
		diagramPaletteSplitter.setWidgetSnapClosedSize(menuPanel, 25);
		diagramPaletteSplitter.setWidgetMinSize(menuPanel, 50);
		palettePropertiesSplitter.setWidgetToggleDisplayAllowed(paletteChooserCanvasSplitter, true);
		diagramPanel = new DrawPanelDiagram(this, propertiesPanel);
		palettePanel = new DrawPanelPalette(this, propertiesPanel, paletteChooser);
		diagramPanel.setOtherDrawFocusPanel(palettePanel);
		palettePanel.setOtherDrawFocusPanel(diagramPanel);
		diagramScrollPanel = new AutoResizeScrollDropPanel(diagramPanel);
		paletteScrollPanel = new AutoResizeScrollDropPanel(palettePanel);
		updateNotificationPosition();
		ThemeFactory.addListener(this);

		for (String diagramName : WebStorage.getSavedDiagramKeys()) {
			addRestoreMenuItem(diagramName);
		}

		saveCommand = GWT.create(SaveCommand.class);
		saveCommand.init(this);

		onThemeChange();

		log.trace("Main View initialized");

		handler = new FileOpenHandler(diagramPanel);

		diagramPanelWrapper.add(diagramScrollPanel);

		palettePanelWrapper.add(paletteScrollPanel);

		RootLayoutPanel.get().add(hiddenUploadButton);
		hiddenUploadButton.setVisible(false);
		hiddenUploadButton.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				handler.processFiles(hiddenUploadButton.getFiles());
			}
		});

		EventHandlingUtils.addEventHandler(mainPanel, diagramPanel, palettePanel);
		mainPanel.addFocusHandler(new FocusHandler() {
			@Override
			public void onFocus(FocusEvent event) {
				SharedConfig.getInstance().setStickingEnabled(true); // shift button may have stopped being pressed, therefore assume sticking is enabled again
			}
		});

		// Add Dropbox dropins.js
		ScriptInjector.fromUrl("https://www.dropbox.com/static/api/2/dropins.js?data-app-key='3mmyizdvtldctng'")
				.setWindow(ScriptInjector.TOP_WINDOW)
				.inject();
		dropboxInt = new DropboxIntegration(diagramPanel);
		dropboxInt.exposeDropboxImportJSCallback(dropboxInt);
		dropboxInt.exposeDropboxShowNotification(dropboxInt);

		StartupDiagramLoader startupDiagramLoader = GWT.create(StartupDiagramLoader.class);
		startupDiagramLoader.loadDiagram(this);

		onThemeChange();
		popupPanel = GWT.create(DownloadPopupPanel.class);
		popupPanel.init((DrawPanelDiagram) diagramPanel);
	}

	public void addRestoreMenuItem(final String chosenName) {
		final HorizontalPanel hp = new HorizontalPanel();

		Label label = new Label(chosenName);
		label.setTitle("open diagram " + chosenName);
		label.addStyleName(style.menuItem());
		label.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				diagramPanel.setDiagram(DiagramXmlParser.xmlToDiagram(WebStorage.getSavedDiagram(chosenName)));
				Notification.showInfo("Diagram opened: " + chosenName);
			}
		});
		Image img = new Image("data:image/gif;base64,R0lGODlhCgAKAJEAAAAAAP////8AAP///yH5BAEAAAMALAAAAAAKAAoAAAIUnI8jgmvLlHtwnpqkpZh72UTZUQAAOw==");
		img.setTitle("delete diagram " + chosenName);
		img.addStyleName(style.menuItem());
		img.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (Window.confirm("Delete saved diagram " + chosenName)) {
					WebStorage.removeSavedDiagram(chosenName);
					restoreMenuPanel.remove(hp);
					Notification.showInfo("Deleted diagram: " + chosenName);
				}
			}
		});

		hp.add(img);
		hp.add(label);
		restoreMenuPanel.add(hp);
	}

	@UiHandler("importMenuItem")
	void onImportMenuItemClick(ClickEvent event) {
		hiddenUploadButton.click();
	}

	@UiHandler("exportMenuItem")
	void onExportMenuItemClick(ClickEvent event) {
		initialiseExportDialog();
	}

	@UiHandler("importDropboxMenuItem")
	void onImportDropboxMenuItemClick(ClickEvent event) {
		dropboxInt.openDropboxImport();
	}

	public void initialiseExportDialog() {
		popupPanel.prepare(lastExportFilename);
		popupPanel.center();
	}

	@UiHandler("exportDropboxMenuItem")
	void onExportDropboxMenuItemClick(ClickEvent event) {
		exportToDropbox.execute();
	}

	@UiHandler("saveMenuItem")
	void onSaveMenuItemClick(ClickEvent event) {
		saveCommand.execute();
	}

	@UiHandler("helpMenuItem")
	void onHelpMenuItemClick(ClickEvent event) {
		ShortcutDialogBox.getInstance().center();
	}

	@UiHandler("paletteChooser")
	void onPaletteChooserMouseWheel(MouseWheelEvent event) {
		// determine new index based on scroll direction
		int newIndex = paletteChooser.getSelectedIndex();
		if (event.getDeltaY() < 0) {
			newIndex--;
		}
		else {
			newIndex++;
		}

		// set new index (if it's valid) and trigger change event (is not automatically triggered)
		if (newIndex >= 0 && newIndex < paletteChooser.getItemCount()) {
			paletteChooser.setSelectedIndex(newIndex);
			DomEvent.fireNativeEvent(Document.get().createChangeEvent(), paletteChooser);
		}
	}

	private void updateNotificationPosition() {
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				RootPanel.get("featurewarning").getElement().getStyle().setMarginLeft(menuPanel.getOffsetWidth(), Unit.PX);
			}
		});
	}

	public DrawPanel getDiagramPanel() {
		return diagramPanel;
	}

	@Override
	public void onThemeChange() {
		String backgroundColor = Converter.convert(ThemeFactory.getCurrentTheme().getColor(Theme.ColorStyle.DEFAULT_BACKGROUND)).value();
		String foregroundColor = Converter.convert(ThemeFactory.getCurrentTheme().getColor(Theme.ColorStyle.DEFAULT_FOREGROUND)).value();
		String splitterColor = Converter.convert(ThemeFactory.getCurrentTheme().getColor(Theme.ColorStyle.DEFAULT_SPLITTER_COLOR)).value();
		paletteChooser.getElement().getStyle().setBackgroundColor(backgroundColor);
		paletteChooser.getElement().getStyle().setColor(foregroundColor);

		// Splitter between main-canvas and east side
		diagramPaletteSplitter.getWidget(3).getElement().getStyle().setBackgroundColor(splitterColor);

		// Splitter between palette chooser and textarea
		palettePropertiesSplitter.getWidget(1).getElement().getStyle().setBackgroundColor(splitterColor);

		propertiesDiv.getStyle().setBackgroundColor(splitterColor);
		propertiesDiv.getStyle().setColor(foregroundColor);
		propertiesPanel.getElement().getStyle().setBackgroundColor(backgroundColor);
		propertiesPanel.getElement().getStyle().setColor(foregroundColor);
		propertiesPanel.getElement().getStyle().setBorderColor(backgroundColor);

		mainPanel.getElement().getStyle().setBackgroundColor(backgroundColor);
		mainPanel.getElement().getStyle().setColor(foregroundColor);

		// There seems to be no better way to retrieve the needed rows to color their hover effect
		for (int i = 0; i < menuPanel.getWidgetCount(); i++) {
			Element element = menuPanel.getWidget(i).getElement();
			if (element.getClassName().contains("com-baselet-gwt-client-view-MainView_MainViewUiBinderImpl_GenCss_style-menuItem")) {
				element.removeClassName("dark");
				element.removeClassName("light");
				switch (ThemeFactory.getActiveThemeEnum()) {
					case DARK:
						element.addClassName("dark");
						break;
					case LIGHT:
					default:
						element.addClassName("light");
						break;
				}
			}
		}
	}
}
