package com.github.hanwelmer.dasmZ80;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface ByteReader {

  public void open(ConfigurationParameters parameters) throws FileNotFoundException;

  public Byte getByte() throws IOException;

  public Byte getNextByte() throws IOException;

  public void close();
}
