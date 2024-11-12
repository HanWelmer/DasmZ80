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

  public AssemblyCode(int address, ArrayList<Byte> bytes) {
	this.address = address;
	this.bytes = bytes;
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

  public int getAddress() {
	return address;
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

  public void setLabel(String label) {
	this.label = label;
  }

  public void setMnemonic(String mnemonic) {
	this.mnemonic = mnemonic;
  }

  public String toString() {
	// address
	String result = String.format("%04X", address);

	if ((mnemonic == null || mnemonic.length() == 0) && (label == null || label.length() == 0) && bytes == null) {
	  // only a comment or nothing:
	  if (comment == null || comment.length() == 0) {
		result = "";
	  } else {
		result = String.format("%24s%s", "", comment);
	  }
	} else {
	  // binary code
	  if (bytes != null) {
		int i = 0;
		for (Byte byt : bytes) {
		  if (i == 0) {
			result += " ";
			i = 4;
		  }
		  i--;
		  result += String.format("%02X", byt);
		}
	  }

	  // label
	  if (label != null && label.length() > 0) {
		if (result.length() < 14) {
		  result += String.format("%14s", "");
		  result = result.substring(0, 14);
		}
		result += label + ":";
	  }

	  // mnemonic
	  if (mnemonic != null && mnemonic.length() > 0) {
		if (result.length() < 24) {
		  result += String.format("%24s", "");
		  ;
		  result = result.substring(0, 24);
		}
		result += mnemonic;
	  }

	  // comment
	  if (comment != null && comment.length() > 0) {
		if (result.length() < 44) {
		  result += String.format("%44s", "");
		  result = result.substring(0, 44);
		}
		result += comment;
	  }
	}

	return result + "\n";
  }
}
