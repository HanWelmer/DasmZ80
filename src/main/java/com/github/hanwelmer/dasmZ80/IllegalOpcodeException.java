package com.github.hanwelmer.dasmZ80;

public class IllegalOpcodeException extends Exception {

  /**
   * Needed by serializable.
   */
  private static final long serialVersionUID = 1L;

  public IllegalOpcodeException(String msg) {
	super(msg);
  }
}
