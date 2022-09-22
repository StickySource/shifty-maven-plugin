package net.stickycode.plugin.shifty;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.components.io.filemappers.FileMapper;
import org.codehaus.plexus.components.io.filemappers.FlattenFileMapper;
import org.codehaus.plexus.components.io.filemappers.PrefixFileMapper;

public class FileMappers {

  private String[] stripprefixes;

  private boolean flatten;

  private String addprefix;

  public FileMapper[] build() {
    List<FileMapper> mappers = new ArrayList<>();
    stripPrefixes(mappers);

    if (flatten)
      mappers.add(new FlattenFileMapper());

    addPrefix(mappers);

    return mappers.toArray(new FileMapper[0]);
  }

  private void addPrefix(List<FileMapper> mappers) {
    if (addprefix != null) {
      PrefixFileMapper prefixMapper = new PrefixFileMapper();
      prefixMapper.setPrefix(addprefix);
      mappers.add(prefixMapper);
    }
  }

  private void stripPrefixes(List<FileMapper> mappers) {
    if (stripprefixes != null)
      for (String prefix : stripprefixes) {
        mappers.add(new StripPrefixMapper(prefix));
      }
  }

  public boolean isFlatten() {
    return flatten;
  }

  public void setFlatten(boolean flatten) {
    this.flatten = flatten;
  }

  public String[] getStripPrefixes() {
    return stripprefixes;
  }

  public void setStripPrefixes(String[] stripPrefixes) {
    this.stripprefixes = stripPrefixes;
  }
}
