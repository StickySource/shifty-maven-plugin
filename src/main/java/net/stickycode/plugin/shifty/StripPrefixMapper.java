package net.stickycode.plugin.shifty;

import org.codehaus.plexus.components.io.filemappers.FileMapper;

public class StripPrefixMapper
    implements FileMapper {

  private String prefix;

  public StripPrefixMapper(String prefix) {
    super();
    this.prefix = prefix;
  }

  @Override
  public String getMappedFileName(String pName) {
    if (pName.startsWith(prefix))
      return pName.substring(prefix.length());

    return pName;
  }

}
