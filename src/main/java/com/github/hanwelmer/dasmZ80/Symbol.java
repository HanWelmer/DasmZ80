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
  private String expression;
  private ArrayList<Integer> references;
  private ArrayList<String> comments;

  public Symbol(String name, SymbolType type, Integer value, String expression) {
	this.name = name;
	this.type = type;
	this.value = value;
	this.expression = expression;
	references = new ArrayList<Integer>();
	comments = new ArrayList<String>();
  }

  public void add(Integer reference) {
	references.add(reference);
  }

  public void add(String comment) {
	comments.add(comment);
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

  public String getExpression() {
	return expression;
  }

  public ArrayList<Integer> getReferences() {
	return references;
  }

  public ArrayList<String> getComments() {
	return comments;
  }

  public String toString() {
	String def = String.format("%04X%12s%-7s EQU", getValue(), " ", getName());
	if (def.length() < 28) {
	  def += "  ";
	} else {
	  def += " ";
	}
	def += getExpression();
	if (getComments().size() > 0) {
	  if (def.length() < 44) {
		String indent = "%" + (44 - def.length()) + "s";
		def += String.format(indent, " ");
	  }
	  def += getComments().get(0);
	}
	def += "\n";
	for (int index = 1; index < getComments().size(); index++) {
	  def += String.format("%24s%s\n", " ", getComments().get(index));
	}
	return def;
  }

}
