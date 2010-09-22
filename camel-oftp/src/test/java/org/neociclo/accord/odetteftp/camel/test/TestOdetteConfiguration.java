package org.neociclo.accord.odetteftp.camel.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.neociclo.accord.odetteftp.camel.OdetteConfiguration;

public class TestOdetteConfiguration extends Object {

	@Test
	public void testOdetteConfig() {
		OdetteConfiguration cfg = new OdetteConfiguration();
		assertNotNull(cfg.getWorkpath());
		assertTrue(cfg.getWorkpath().exists());
	}
}
