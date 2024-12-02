package com.github.hanwelmer.dasmZ80;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SymbolFileReader implements AbstractReader {

  private FileReader fr = null;
  private BufferedReader input = null;
  private String lastLine = "";
  private int pos = 0;

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
  public String getWord() {
	return getString(true);
  }

  @Override
  public String getValue() {
	return getString(false);
  }

  private String getString(boolean asIdentifier) {
	String value = "";
	// skip spaces
	while (pos < lastLine.length() && Character.isWhitespace(lastLine.charAt(pos))) {
	  pos++;
	}
	// treat comments separately
	if (pos < lastLine.length() && lastLine.charAt(pos) == ';') {
	  value = lastLine.substring(pos);
	} else {
	  // form a single word by adding valid letters and digits (asIdentifier) or
	  // anything until ';' or end of line (otherwise).
	  while (validChar(asIdentifier)) {
		value += lastLine.charAt(pos);
		pos++;
	  }
	}
	return value;
  }

  private boolean validChar(boolean asIdentifier) {
	boolean result = pos < lastLine.length();
	if (result) {
	  if (asIdentifier) {
		result = Character.isUnicodeIdentifierPart(lastLine.charAt(pos));
	  } else {
		result = (lastLine.charAt(pos) != ';');
	  }
	}
	return result;
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
