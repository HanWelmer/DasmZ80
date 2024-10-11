package com.github.hanwelmer.dasmZ80;

import java.io.IOException;

public interface AbstractWriter {

  public void open(ConfigurationParameters parameters) throws IOException;

  public void write(AssemblyCode code) throws IOException;

  public void close();
}
