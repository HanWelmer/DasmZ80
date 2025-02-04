package com.github.hanwelmer.dasmZ80;

import java.util.ArrayList;

public class Path {

  public Symbol entryPoint = null;
  public int startAddress = 0;
  public ArrayList<AssemblyCode> decoded = null;
  public int nextAddress = 0;

  public Path(Symbol symbol, ArrayList<AssemblyCode> decoded) {
	entryPoint = symbol;
	startAddress = entryPoint.getValue();
	this.decoded = decoded;
	nextAddress = startAddress;
	for (AssemblyCode code : this.decoded) {
	  if (code.getBytes() != null) {
		nextAddress += code.getBytes().size();
	  }
	}
  }

}
