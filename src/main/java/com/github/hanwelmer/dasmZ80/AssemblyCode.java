package com.github.hanwelmer.dasmZ80;

import java.util.ArrayList;

public class AssemblyCode {
  private int address = 0;
  private ArrayList<Byte> bytes;
  private String label;
  private String mnemonic;
  private String comment;

  private static final String INDENT8 = "        ";
  private static final String INDENT20 = "                    ";
  private static final String INDENT24 = "                        ";

  public AssemblyCode(int address, ArrayList<Byte> bytes, String label, String mnemonic, String comment) {
	this.address = address;
	this.bytes = bytes;
	this.label = label;
	this.mnemonic = mnemonic;
	this.comment = comment;
  }

  public AssemblyCode(int address, String mnemonic, String comment) {
	this.address = address;
	this.label = "";
	this.mnemonic = mnemonic;
	this.comment = comment;
  }

  public AssemblyCode(int address, String mnemonic) {
	this.address = address;
	this.label = "";
	this.mnemonic = mnemonic;
	this.comment = "";
  }

  public void addByte(Byte nextByte) {
	if (bytes == null) {
	  bytes = new ArrayList<Byte>();
	}
	bytes.add(nextByte);
  }

  public String toAsmString() {
	String result = INDENT8;
	// label
	if (label != null && label.length() > 0) {
	  result = label + ":";
	}

	// mnemonic
	if (mnemonic != null && mnemonic.length() > 0) {
	  result += INDENT8;
	  result = result.substring(0, 7);
	  result += mnemonic;
	}

	// comment
	if (comment != null && comment.length() > 0) {
	  result += INDENT24;
	  result = result.substring(0, 31);
	  result += comment;
	}

	result += "\n";
	return result;
  }

  public String toLstString() {
	String result = String.format("%04X:", address);
	if (bytes != null) {
	  for (Byte byt : bytes) {
		result += String.format(" %02X", byt);
	  }
	}
	result += INDENT20;
	result = result.substring(0, 19);
	return result + toAsmString();
  }

  public void updateMnemonic(String oldValue, String newValue) {
	mnemonic = mnemonic.replace(oldValue, newValue);
  }

  public ArrayList<Byte> getBytes() {
	return bytes;
  }

  public String getMnemonic() {
	return mnemonic;
  }

  public void setMnemonic(String mnemonic) {
	this.mnemonic = mnemonic;
  }
}
