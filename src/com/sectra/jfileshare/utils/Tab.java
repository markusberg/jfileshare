package com.sectra.jfileshare.utils;

/**
 * Helper class for the tabbed interface
 * @author markus
 */
public class Tab {

    private String sTitle;
    private String urlLink;
    private boolean bSelected;
    private boolean bEnabled;

    public void Tab() {
    }

    public Tab(String sTitle, String urlLink, boolean bSelected, boolean bEnabled) {
        this.sTitle = sTitle;
        this.urlLink = urlLink;
        this.bSelected = bSelected;
        this.bEnabled = bEnabled;
    }

    public String getTitle() {
        return this.sTitle;
    }

    public String getLink() {
        return this.urlLink;
    }

    public boolean isSelected() {
        return this.bSelected;
    }

    public boolean isEnabled() {
        return this.bEnabled;
    }
}


