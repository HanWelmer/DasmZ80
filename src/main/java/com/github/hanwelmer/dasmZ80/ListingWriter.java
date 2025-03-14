package com.github.hanwelmer.dasmZ80;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ListingWriter implements AbstractWriter {

  private File file = null;
  private BufferedWriter writer = null;

  @Override
  public void open(String fileName) throws IOException {
	file = new File(fileName);
	writer = new BufferedWriter(new FileWriter(file));
  }

  @Override
  public void write(AssemblyCode code) throws IOException {
	writer.write(code.toString());
  }

  @Override
  public void close() {
	try {
	  writer.close();
	} catch (IOException e) {
	  System.out.println("Error closing listing output file:");
	  e.printStackTrace();
	} finally {
	  writer = null;
	  file = null;
	}
  }

  @Override
  public void write(String msg) throws IOException {
	writer.write(msg);
  }

}
