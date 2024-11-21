package com.github.hanwelmer.dasmZ80;

import java.util.ArrayList;

/**
 * Class to manage a named value and references to it.
 * 
 * @author welmerhj
 *
 */
public class Symbol {

  private String name;
  private SymbolType type;
  private Integer value;
  private ArrayList<Integer> references;

  public Symbol(String name, SymbolType type, Integer value) {
	this.name = name;
	this.type = type;
	this.value = value;
	references = new ArrayList<Integer>();
  }

  public void add(Integer reference) {
	references.add(reference);
  }

  public String getName() {
	return name;
  }

  public SymbolType getType() {
	return type;
  }

  public Integer getValue() {
	return value;
  }

  public ArrayList<Integer> getReferences() {
	return references;
  }

}
