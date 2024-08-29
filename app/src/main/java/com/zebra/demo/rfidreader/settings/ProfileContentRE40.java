package com.zebra.demo.rfidreader.settings;



public class ProfileContentRE40 {
    public static final int LayoutOne = 0;
    public static final  int LayoutTwo = 2;
    private int viewType;
    String Content;
    String profileId;

    public ProfileContentRE40(String content, String profileid) {
        Content = content;
        profileId = profileid;
        viewType = LayoutOne;
    }

    public String getContent() {
        return Content;
    }

    public void setContent(String content) {
        Content = content;
    }

    // Variables for the item of second layout
    public String contentDetails;
    public int powerLevel ;
    public int LinkProfileIndex;
    public int SessionIndex;
    public boolean DPO_On = true;


    // public constructor for the second layout
    public ProfileContentRE40(String content, String profileid, String contentDetails,
                              int powerLevel, int LinkProfileIndex, int SessionIndex, boolean DPO_On)
    {
        Content = content;
        profileId = profileid;
        this.contentDetails = contentDetails;
        this.powerLevel = powerLevel;
        this.LinkProfileIndex = LinkProfileIndex;
        this.viewType = LayoutTwo;
        this.SessionIndex = SessionIndex;
        this.DPO_On = DPO_On;
    }

    public int getViewType() {
        return viewType;
    }
}