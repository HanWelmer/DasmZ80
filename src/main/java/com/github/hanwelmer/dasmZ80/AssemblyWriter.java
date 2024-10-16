package com.github.hanwelmer.dasmZ80;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class AssemblyWriter implements AbstractWriter {

  private File file = null;
  private BufferedWriter writer = null;

  @Override
  public void open(String fileName) throws IOException {
	file = new File(setExtension(fileName));
	writer = new BufferedWriter(new FileWriter(file));
  }

  @Override
  public void write(AssemblyCode code) throws IOException {
	writer.write(code.toAsmString());
  }

  @Override
  public void close() {
	try {
	  writer.close();
	} catch (IOException e) {
	  System.out.println("Error closing assembler output file:");
	  e.printStackTrace();
	} finally {
	  writer = null;
	  file = null;
	}
  }

  private String setExtension(String fileName) {
	if (fileName.endsWith(".bin")) {
	  return fileName.replace(".bin", ".asm");
	} else if (fileName.endsWith(".hex")) {
	  return fileName.replace(".hex", ".asm");
	} else {
	  return fileName + ".asm";
	}
  }

}
