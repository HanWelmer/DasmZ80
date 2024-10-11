package com.github.hanwelmer.dasmZ80;

public class BinaryCode {

  private int code;
  private int nrOfBytes;
  private String mask;
  private String mnemonic;

  // Constructor.
  public BinaryCode(int code, int nrOfBytes, String mask, String mnemonic) {
	this.code = code;
	this.nrOfBytes = nrOfBytes;
	this.mask = mask;
	this.mnemonic = mnemonic;
  }

  public int getCode() {
	return code;
  }

  public int getNrOfBytes() {
	return nrOfBytes;
  }

  public String getMask() {
	return mask;
  }

  public String getMnemonic() {
	return mnemonic;
  }

}
