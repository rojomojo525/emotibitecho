package net.medimuse.emotibitconnector.service;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

public class Activator implements BundleActivator {

	private static BundleContext context;

	EventAdmin eventAdmin = null;

	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {

		Activator.context = bundleContext;

	}

	public void stop(BundleContext bundleContext) throws Exception {

		Activator.context = null;
	}

}
