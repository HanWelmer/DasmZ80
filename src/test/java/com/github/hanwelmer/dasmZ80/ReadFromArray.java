package com.github.hanwelmer.dasmZ80;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class ReadFromArray implements ByteReader {

  ArrayList<Byte> array;
  int index = 0;

  public ReadFromArray(Byte[] bytes) {
	this.array = new ArrayList<Byte>(Arrays.asList(bytes));
	index = 0;
  }

  public void open(String fileName) {
  }

  @Override
  public Byte getByte(int address) {
	index = address;
	return (index < array.size()) ? array.get(index++) : null;
  }

  @Override
  public Byte getNextByte() throws IOException {
	Byte nextByte = getByte(index);
	if (nextByte == null) {
	  throw new IOException("Unexpected end of file.");
	}
	return nextByte;
  }

  @Override
  public int getSize() {
	return array.size();
  }

  @Override
  public void close() {
  }

}
