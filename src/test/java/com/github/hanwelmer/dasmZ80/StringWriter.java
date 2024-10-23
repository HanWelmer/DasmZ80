package com.github.hanwelmer.dasmZ80;

import java.io.IOException;
import java.util.ArrayList;

public class StringWriter implements AbstractWriter {

  public ArrayList<String> output = new ArrayList<String>();

  @Override
  public void open(String fileName) throws IOException {
	output.clear();
  }

  @Override
  public void write(AssemblyCode code) throws IOException {
	output.add(code.toAsmString());
  }

  @Override
  public void close() {
	// nothing to do.
  }

}
