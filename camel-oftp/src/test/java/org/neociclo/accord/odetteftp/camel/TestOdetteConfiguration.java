package org.neociclo.accord.odetteftp.camel;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestOdetteConfiguration extends Object {

	@Test
	public void testOdetteConfig() {
		OdetteConfiguration cfg = new OdetteConfiguration();
		assertNotNull(cfg.getTmpDir());
		assertTrue(cfg.getTmpDir().exists());
	}
}
