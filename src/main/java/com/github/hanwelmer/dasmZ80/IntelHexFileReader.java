package com.github.hanwelmer.dasmZ80;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class IntelHexFileReader implements ByteReader {

  private FileReader inputFile = null;
  private BufferedReader br = null;

  @Override
  public void open(ConfigurationParameters parameters) throws FileNotFoundException {
	inputFile = new FileReader(parameters.fileName);
	br = new BufferedReader(inputFile);
  }

  @Override
  public Byte getByte() {
	// TODO Auto-generated method stub
	return null;
  }

  @Override
  public Byte getNextByte() {
	return getByte();
  }

  @Override
  public void close() {
	br = null;
	inputFile = null;
  }

}
