package com.github.hanwelmer.dasmZ80;

public class BinaryCode {

  private String mask;
  private String mnemonic;

  // Constructor.
  public BinaryCode(String mask, String mnemonic) {
	this.mask = mask;
	this.mnemonic = mnemonic;
  }

  public String getMask() {
	return mask;
  }

  public String getMnemonic() {
	return mnemonic;
  }

}
