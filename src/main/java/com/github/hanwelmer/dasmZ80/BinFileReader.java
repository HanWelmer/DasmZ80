package com.github.hanwelmer.dasmZ80;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class BinFileReader implements ByteReader {

  private File inputFile = null;
  private RandomAccessFile fis = null;
  private int address = -1;

  @Override
  public void open(String fileName) throws FileNotFoundException {
	inputFile = new File(fileName);
	fis = new RandomAccessFile(inputFile, "r");
  }

  @Override
  public Byte getByte(int address) throws IOException {
	if (address != this.address) {
	  fis.seek(address);
	  this.address = address;
	}
	return getNextByte();
  }

  @Override
  public Byte getNextByte() throws IOException {
	Byte nextByte = null;
	int nextValue = fis.read();
	address++;
	if (nextValue != -1) {
	  nextByte = (byte) nextValue;
	}
	return nextByte;
  }

  @Override
  public int getSize() {
	int result = 0;
	try {
	  result = (int) fis.length();
	} catch (IOException e) {
	  e.printStackTrace();
	}
	return result;
  }

  @Override
  public void close() {
	fis = null;
	inputFile = null;
  }

}
