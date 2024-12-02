package com.github.hanwelmer.dasmZ80;

import java.io.IOException;
import java.util.ArrayList;

public class ReadSymbolsFromArray extends AbstractSymbolReader {

  ArrayList<String> array = new ArrayList<String>();
  int index = 0;

  public ReadSymbolsFromArray() {
	this.array.clear();
	index = 0;
  }

  public void add(String line) {
	array.add(line);
  }

  @Override
  public void open(String fileName) throws IOException {
  }

  @Override
  public boolean ready() throws IOException {
	return index < array.size();
  }

  @Override
  public String readLine() throws IOException {
	if (index < array.size()) {
	  lastLine = array.get(index);
	  index++;
	  pos = 0;
	} else {
	  throw new IOException("Unexpected end of file");
	}
	return lastLine;
  }

  @Override
  public void close() {
  }

}
