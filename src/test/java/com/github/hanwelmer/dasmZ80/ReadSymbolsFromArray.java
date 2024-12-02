package com.github.hanwelmer.dasmZ80;

import java.io.IOException;
import java.util.ArrayList;

public class ReadSymbolsFromArray implements AbstractReader {

  ArrayList<String> array = new ArrayList<String>();
  int index = 0;
  String lastLine = "";
  int pos = 0;

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
  }

}
