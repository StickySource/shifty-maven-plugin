package net.stickycode.plugin.shifty;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.components.io.filemappers.FileMapper;
import org.codehaus.plexus.components.io.filemappers.FlattenFileMapper;
import org.codehaus.plexus.components.io.filemappers.MergeFileMapper;

public class FileMappers {

  private String[] stripprefixes;

  private boolean flatten;

  private String merge;

  public FileMapper[] build() {
    List<FileMapper> mappers = new ArrayList<>();
    addStripPrefixes(mappers);

    if (flatten)
      mappers.add(new FlattenFileMapper());

    mergeMapper(mappers);

    return mappers.toArray(new FileMapper[0]);
  }

  private void mergeMapper(List<FileMapper> mappers) {
    if (merge == null)
      return;

    MergeFileMapper mapper = new MergeFileMapper();
    mapper.setTargetName(merge);
    mappers.add(mapper);
  }

  private void addStripPrefixes(List<FileMapper> mappers) {
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
