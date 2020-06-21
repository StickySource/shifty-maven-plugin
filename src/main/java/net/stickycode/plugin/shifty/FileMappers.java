package net.stickycode.plugin.shifty;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.components.io.filemappers.FileMapper;

public class FileMappers {

  private String[] stripprefixes;

  public String[] getStripPrefixes() {
    return stripprefixes;
  }

  public void setStripPrefixes(String[] stripPrefixes) {
    this.stripprefixes = stripPrefixes;
  }

  public FileMapper[] build() {
    List<FileMapper> mappers = new ArrayList<>();
    addStripPrefixes(mappers);
    return mappers.toArray(new FileMapper[0]);
  }

  private void addStripPrefixes(List<FileMapper> mappers) {
    if (stripprefixes != null)
      for (String prefix : stripprefixes) {
        mappers.add(new StripPrefixMapper(prefix));
      }
  }
}
