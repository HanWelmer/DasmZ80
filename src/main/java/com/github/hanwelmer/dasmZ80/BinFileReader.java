package com.github.hanwelmer.dasmZ80;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class BinFileReader implements ByteReader {

  private File inputFile = null;
  private RandomAccessFile fis = null;

  @Override
  public void open(String fileName) throws FileNotFoundException {
	inputFile = new File(fileName);
	fis = new RandomAccessFile(inputFile, "r");
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

  @Override
  public void seek(int address) throws IOException {
	fis.seek(address);
  }

}
