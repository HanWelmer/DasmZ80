package com.github.hanwelmer.dasmZ80;

public class SymbolSortedByName extends Symbol implements Comparable<Symbol> {

  public SymbolSortedByName(String name, SymbolType type, Integer value, String expression) {
	super(name, type, value, expression);
  }

  public SymbolSortedByName(Symbol symbol) {
	super(symbol.getName(), symbol.getType(), symbol.getValue(), symbol.getExpression());
	this.references.addAll(symbol.getReferences());
  }

  @Override
  public int compareTo(Symbol other) {
	return this.name.compareTo(other.getName());
  }

}
