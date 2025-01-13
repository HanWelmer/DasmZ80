package com.github.hanwelmer.dasmZ80;

import java.util.ArrayList;

public class Path {

  public int startAddress = 0;
  public int nextAddress = 0;
  public ArrayList<AssemblyCode> decoded = null;

  public Path(Symbol symbol, ArrayList<AssemblyCode> decoded) {
	startAddress = symbol.getValue();
	this.decoded = decoded;
	nextAddress = startAddress;
	for (AssemblyCode code : this.decoded) {
	  if (code.getBytes() != null) {
		nextAddress += code.getBytes().size();
	  }
	}
  }

}
