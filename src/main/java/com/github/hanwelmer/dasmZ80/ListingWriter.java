package com.github.hanwelmer.dasmZ80;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ListingWriter implements AbstractWriter {

  private File file = null;
  private BufferedWriter writer = null;
  private static final String INDENT = "                    ";

  @Override
  public void open(String fileName) throws IOException {
	file = new File(setExtension(fileName));
	writer = new BufferedWriter(new FileWriter(file));
  }

  @Override
  public void write(AssemblyCode code) throws IOException {
	writer.write(code.toLstString());
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

  private String setExtension(String fileName) {
	if (fileName.endsWith(".bin")) {
	  return fileName.replace(".bin", ".lst");
	} else if (fileName.endsWith(".hex")) {
	  return fileName.replace(".hex", ".lst");
	} else {
	  return fileName + ".lst";
	}
  }

}
