package com.github.hanwelmer.dasmZ80;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface ByteReader {

  public void open(String fileName) throws FileNotFoundException;

  public Byte getByte(int address) throws IOException;

  public Byte getNextByte() throws IOException;

  public int getSize();

  public void close();
}
