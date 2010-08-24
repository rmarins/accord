package org.neociclo.accord.camel.oftp;

import static org.junit.Assert.*;
import org.junit.Test;
import org.neociclo.accord.camel.odette.OdetteConfiguration;

public class TestOdetteConfiguration extends Object {

	@Test
	public void testOdetteConfig() {
		OdetteConfiguration cfg = new OdetteConfiguration();
		assertNotNull(cfg.getTmpDir());
		assertTrue(cfg.getTmpDir().exists());
	}
}
