package com.github.hanwelmer.dasmZ80;

import java.util.ArrayList;

public class StringWriter implements AbstractWriter {

  public ArrayList<String> output = new ArrayList<String>();

  @Override
  public void open(String fileName) {
	output.clear();
  }

  @Override
  public void write(AssemblyCode code) {
	output.add(code.toString());
  }

  @Override
  public void close() {
	// nothing to do.
  }

  @Override
  public void write(String msg) {
	output.add(msg);
  }

}
