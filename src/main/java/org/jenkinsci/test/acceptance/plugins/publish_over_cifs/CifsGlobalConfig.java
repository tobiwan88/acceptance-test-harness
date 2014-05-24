package org.jenkinsci.test.acceptance.plugins.publish_over_cifs;

import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.PageArea;
import org.jenkinsci.test.acceptance.po.PageObject;

import javax.inject.Inject;

/**
 * @author Tobias Meyer
 */
public class CifsGlobalConfig extends PageArea {
    @Inject
    JenkinsController controller;

    public final Control add = control("repeatable-add");

    @Inject
    public CifsGlobalConfig(Jenkins jenkins) {
        super(jenkins, "/jenkins-plugins-publish_over_cifs-CifsPublisherPlugin");
    }

    public Site addSite() {
        add.click();
        String p = last(by.xpath(".//div[@name='instance'][starts-with(@path,'/jenkins-plugins-publish_over_cifs-CifsPublisherPlugin/')]")).getAttribute("path");
        return new Site(page, p);
    }

    public static class Site extends PageArea {
        public Site(PageObject parent, String path) {
            super(parent, path);
            Control advanced = control("advanced-button");
            advanced.click();
        }

        public final Control name = control("name");
        public final Control hostname = control("hostname");
        public final Control username = control("username");
        public final Control password = control("password");
        public final Control share = control("remoteRootDir");
        public final Control port = control("port");
        public final Control timeout = control("timeout");
    }
}