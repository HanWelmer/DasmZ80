package com.github.hanwelmer.dasmZ80;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SymbolFileReader extends AbstractSymbolReader {

  private FileReader fr = null;
  private BufferedReader input = null;

  @Override
  public void open(String fileName) throws IOException {
	fr = new FileReader(fileName);
	input = new BufferedReader(fr);
  }

  @Override
  public boolean ready() throws IOException {
	return input.ready();
  }

  @Override
  public String readLine() throws IOException {
	lastLine = input.readLine();
	pos = 0;
	return lastLine;
  }

  @Override
  public void close() {
	if (input != null) {
	  try {
		input.close();
	  } catch (IOException e) {
		System.out.println(e.getMessage());
	  }
	}
	if (fr != null) {
	  try {
		fr.close();
	  } catch (IOException e) {
		System.out.println(e.getMessage());
	  }
	}
  }

}
