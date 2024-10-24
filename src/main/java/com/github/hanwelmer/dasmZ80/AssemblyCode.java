package com.github.hanwelmer.dasmZ80;

import java.util.ArrayList;

public class AssemblyCode {
  private int address = 0;
  private ArrayList<Byte> bytes;
  private String label;
  private String mnemonic;
  private String comment;

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

  public String toString() {
	// address
	String result = String.format("%04X: ", address);

	// binary code
	if (bytes != null) {
	  for (Byte byt : bytes) {
		result += String.format("%02X ", byt);
	  }
	}

	// label
	if (label != null && label.length() > 0) {
	  if (result.length() < 18) {
		result += String.format("%18s", "");
		result = result.substring(0, 18);
	  }
	  result += label + ":";
	}

	// mnemonic
	if (mnemonic != null && mnemonic.length() > 0) {
	  if (result.length() < 28) {
		result += String.format("%28s", "");
		;
		result = result.substring(0, 28);
	  }
	  result += mnemonic;
	}

	// comment
	if (comment != null && comment.length() > 0) {
	  if (result.length() < 50) {
		result += String.format("%50s", "");
		result = result.substring(0, 50);
	  }
	  result += comment;
	}

	return result + "\n";
  }
}
