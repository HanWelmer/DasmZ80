package com.github.hanwelmer.dasmZ80;

public class BinaryCode {

  private int code;
  private String mask;
  private String mnemonic;

  // Constructor.
  public BinaryCode(int code, String mask, String mnemonic) {
	this.code = code;
	this.mask = mask;
	this.mnemonic = mnemonic;
  }

  public int getCode() {
	return code;
  }

  public String getMask() {
	return mask;
  }

  public String getMnemonic() {
	return mnemonic;
  }

}
