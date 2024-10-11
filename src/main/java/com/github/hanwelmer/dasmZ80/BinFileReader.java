package com.github.hanwelmer.dasmZ80;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class BinFileReader implements ByteReader {

  private File inputFile = null;
  private FileInputStream fis = null;

  @Override
  public void open(ConfigurationParameters parameters) throws FileNotFoundException {
	inputFile = new File(parameters.fileName);
	fis = new FileInputStream(inputFile);
  }

  @Override
  public Byte getByte() throws IOException {
	Byte nextByte = null;
	int nextValue = fis.read();
	if (nextValue != -1) {
	  nextByte = (byte) nextValue;
	}
	return nextByte;
  }

  @Override
  public Byte getNextByte() throws IOException {
	return getByte();
  }

  @Override
  public void close() {
	fis = null;
	inputFile = null;
  }

}
