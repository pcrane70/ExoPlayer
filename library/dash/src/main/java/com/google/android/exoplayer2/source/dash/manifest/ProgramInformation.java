package com.google.android.exoplayer2.source.dash.manifest;

import java.util.Arrays;
import java.util.List;

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

  /**
   * A list of custom elements.
   */
  public final List<byte[]> customEvents;

  public ProgramInformation(String title, String source, String copyright, List<byte[]> customEvents) {
    this.title = title;
    this.source = source;
    this.copyright = copyright;
    this.customEvents = customEvents;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof ProgramInformation
            && ((ProgramInformation) obj).copyright.equals(this.copyright)
            && ((ProgramInformation) obj).source.equals(this.source)
            && ((ProgramInformation) obj).title.equals(this.title)
            && validateEvents(((ProgramInformation) obj).customEvents);
  }

  private boolean validateEvents(List<byte[]> customEvents) {
    byte[] bytes = customEvents.get(0);
    for (int j = 0; j < bytes.length; j++) {
      if (!(bytes[j] == this.customEvents.get(0)[j]))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + title.hashCode();
    result = 31 * result + source.hashCode();
    result = 31 * result + copyright.hashCode();
    for (int i = 0; i < customEvents.size(); i++) {
      result = 31 * result + Arrays.hashCode(customEvents.get(i));
    }
    return result;
  }
}
