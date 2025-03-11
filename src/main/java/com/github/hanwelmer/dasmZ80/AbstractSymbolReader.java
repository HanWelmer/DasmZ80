package com.github.hanwelmer.dasmZ80;

public abstract class AbstractSymbolReader implements AbstractReader {

  protected String lastLine = "";
  protected int pos = 0;

  public String getWord() {
	return getString(true);
  }

  public String getValue() {
	return getString(false);
  }

  public String getComment() {
	String comment = "";
	// skip until ';'
	while (pos < lastLine.length() && lastLine.charAt(pos) != ';') {
	  pos++;
	}
	if (pos < lastLine.length() && lastLine.charAt(pos) == ';') {
	  comment += lastLine.substring(pos);
	}
	return comment;
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

}
