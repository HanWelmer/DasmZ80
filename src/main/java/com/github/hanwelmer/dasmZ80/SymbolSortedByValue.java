package com.github.hanwelmer.dasmZ80;

public class SymbolSortedByValue extends Symbol implements Comparable<Symbol> {

  public SymbolSortedByValue(String name, SymbolType type, Integer value, String expression) {
	super(name, type, value, expression);
  }

  public SymbolSortedByValue(Symbol symbol) {
	super(symbol.getName(), symbol.getType(), symbol.getValue(), symbol.getExpression());
	this.comments.addAll(symbol.getComments());
  }

  @Override
  public int compareTo(Symbol other) {
	return this.value.compareTo(other.getValue());
  }

}
