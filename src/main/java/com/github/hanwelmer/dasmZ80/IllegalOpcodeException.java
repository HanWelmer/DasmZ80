package com.github.hanwelmer.dasmZ80;

public class IllegalOpcodeException extends Exception {

  public IllegalOpcodeException(String msg) {
	super(msg);
  }
}
