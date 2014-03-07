package org.jenkinsci.test.acceptance.po;

import com.google.inject.Injector;

import java.net.URL;

/**
 * Page object for Jenkins slave.
 *
 * @author Kohsuke Kawaguchi
 */
public class Slave extends ContainerPageObject {
    public Slave(Injector injector, URL url) {
        super(injector, url);
    }
}
