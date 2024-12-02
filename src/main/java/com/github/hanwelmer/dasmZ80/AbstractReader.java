package com.github.hanwelmer.dasmZ80;

import java.io.IOException;

public interface AbstractReader {

  public void open(String fileName) throws IOException;

  public boolean ready() throws IOException;

  public String readLine() throws IOException;

  public String getWord();

  public String getValue();

  public void close();
}
