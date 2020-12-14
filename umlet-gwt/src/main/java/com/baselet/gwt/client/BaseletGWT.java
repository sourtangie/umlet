package com.baselet.gwt.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baselet.control.config.SharedConfig;
import com.baselet.control.enums.Program;
import com.baselet.control.enums.RuntimeType;
import com.baselet.gwt.client.base.Browser;
import com.baselet.gwt.client.base.Notification;
import com.baselet.gwt.client.element.BrowserStorage;
import com.baselet.gwt.client.version.BuildInfoProperties;
import com.baselet.gwt.client.view.MainView;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.RootLayoutPanel;

public class BaseletGWT implements EntryPoint {

	Logger log = LoggerFactory.getLogger(BaseletGWT.class);

	@Override
	public void onModuleLoad() {
		log.info("Starting GUI ...");
		Program.init(BuildInfoProperties.getVersion(), RuntimeType.GWT);
		SharedConfig.getInstance().setDev_mode(Location.getParameter("dev") != null);

		if (!BrowserStorage.initLocalStorageAndCheckIfAvailable()) {
			if (Browser.get() == Browser.INTERNET_EXPLORER && GWT.getHostPageBaseURL().startsWith("file:")) {
				Notification.showFeatureNotSupported("You have opened this webpage from your filesystem, therefore<br/>Internet Explorer will not support local storage<br/><br/>Please use another browser like Firefox or Chrome,<br/>or open this application using the web url", false);
			}
			else {
				Notification.showFeatureNotSupported("Sorry, but your browser does not support the required HTML 5 feature 'local storage' (or has cookies disabled)<br/>Suggested browsers are Firefox, Chrome, Opera, Internet Explorer 10+", false);
			}
		}
		else if (!browserSupportsFileReader()) {
			Notification.showFeatureNotSupported("Sorry, but your browser does not support the required HTML 5 feature 'file reader'<br/>Suggested browsers are Firefox, Chrome, Opera, Internet Explorer 10+", false);
		}
		else {
			Notification.showInfo("Loading application ... please wait ...");
			GWT.runAsync(new RunAsyncCallback() {
				@Override
				public void onSuccess() {
					Notification.showInfo("");
					RootLayoutPanel.get().add(new MainView());
				}

				@Override
				public void onFailure(Throwable reason) {
					Notification.showFeatureNotSupported("Cannot load application from server", false);
				}
			});
			if (!SharedConfig.getInstance().isDev_mode()) {
				Window.addWindowClosingHandler(new Window.ClosingHandler() {
					@Override
					public void onWindowClosing(Window.ClosingEvent closingEvent) {
						closingEvent.setMessage("Do you really want to leave the page? You will lose any unsaved changes.");
					}
				});
			}
		}
		log.info("GUI started");
	}

	private final native boolean browserSupportsFileReader() /*-{
																return typeof FileReader != "undefined";
																}-*/;
}
