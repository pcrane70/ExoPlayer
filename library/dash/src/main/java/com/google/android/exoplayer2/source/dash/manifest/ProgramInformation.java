package com.google.android.exoplayer2.source.dash.manifest;

import java.util.Map;

public class ProgramInformation {
    /**
     * The title for the media presentation.
     */
    public final String title;

    /**
     * Information about the original source of the media presentation.
     */
    public final String source;

    /**
     * A copyright statement for the media presentation.
     */
    public final String copyright;

    public final String other;

    public final Map<String, String> attributeMap;

    public ProgramInformation(String title, String source, String copyright, String other,
                              Map<String, String> attributeMap) {
        this.title = title;
        this.source = source;
        this.copyright = copyright;
        this.other = other;
        this.attributeMap = attributeMap;
    }
}

