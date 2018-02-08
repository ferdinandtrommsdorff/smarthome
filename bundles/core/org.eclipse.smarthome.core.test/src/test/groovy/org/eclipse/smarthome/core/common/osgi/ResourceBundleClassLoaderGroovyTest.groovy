/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.core.common.osgi

import static org.junit.Assert.*;

import java.net.URL
import java.nio.file.Files;
import java.nio.file.Path
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;

import org.junit.Test
import org.osgi.framework.Bundle;;

class ResourceBundleClassLoaderGroovyTest {

	static URL createTmpTestPropetiesFile(Path root, String relativeFile){
		def file = Paths.get(relativeFile);
		def fileDir = file.parent;

		new File(root.toFile(),fileDir.toString()).mkdirs();
		def _file = new File(root.toFile(), relativeFile);
		_file.createNewFile();

		return _file.toURL()
	}

	@Test
	void testName() {
		def Path tmp = Files.createTempDirectory("tmp");
		def hostPropertiesURL = createTmpTestPropetiesFile(tmp, "host/ESH-INF/i18n/test.properties")
		def fragmentPropertiesURL = createTmpTestPropetiesFile(tmp, "fragment/ESH-INF/i18n/test.properties")

		def bundleMock = [
			findEntries: {String ignored, String ignored2, boolean ignored3 ->
				Collections.enumeration([
					hostPropertiesURL,
					fragmentPropertiesURL
				])
			},
			getEntry: {String ignored -> hostPropertiesURL}
		] as Bundle


		ResourceBundleClassLoader classloader = new ResourceBundleClassLoader(bundleMock, "/ESH-INF/i18n", "*.properties");

		ArrayList<URL> propertiesURLs = Collections.list(bundleMock.findEntries("/ESH-INF/i18n", "*.properties", true));

		assertEquals("Check bundleMock.findEntries state.",2, propertiesURLs.size());
		assertTrue("Check bundleMock.findEntries state.",propertiesURLs.contains(hostPropertiesURL));
		assertTrue("Check bundleMock.findEntries state.",propertiesURLs.contains(fragmentPropertiesURL));

		assertEquals("Check bundleMock.getEntry state.", hostPropertiesURL, bundleMock.getEntry(null));
		assertEquals("Check bundleMock.getEntry state.",hostPropertiesURL, bundleMock.getEntry("always-return-hostPropertiesURL"));

		URL resource = classloader.getResource("test.properties");
		assertEquals("Test new implementation with bundleMock.",fragmentPropertiesURL, resource);
	}
}