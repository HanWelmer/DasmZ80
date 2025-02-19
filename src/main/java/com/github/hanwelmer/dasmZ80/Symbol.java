package com.github.hanwelmer.dasmZ80;

import java.util.ArrayList;

/**
 * Class to manage a named value and references to it.
 * 
 * @author welmerhj
 *
 */
public class Symbol {

  protected String name;
  protected Integer value;
  private SymbolType type;
  private String expression;
  protected ArrayList<String> comments;
  protected ArrayList<Integer> references;

  // constructor
  public Symbol(SymbolType type, Integer value) {
	this.name = "";
	this.type = type;
	this.value = value;
	this.expression = "";
	references = new ArrayList<Integer>();
	comments = new ArrayList<String>();
  }

  // constructor
  public Symbol(String name, SymbolType type, Integer value, String expression) {
	this.name = name;
	this.type = type;
	this.value = value;
	this.expression = expression;
	references = new ArrayList<Integer>();
	comments = new ArrayList<String>();
  }

  public String getName() {
	return name;
  }

  public void setName(String name) {
	this.name = name;
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

  public void addReference(Integer reference) {
	references.add(reference);
  }

  public ArrayList<String> getComments() {
	return comments;
  }

  public void addComment(String comment) {
	comments.add(comment);
  }

  public String toString() {
	String format = null;
	// value
	String def = String.format("%04X", getValue());
	if (type != SymbolType.comment) {
	  // name
	  def += String.format("%10s%-9s EQU", " ", getName());

	  // expression
	  if (def.length() < 28) {
		def += "  ";
	  } else {
		def += " ";
	  }
	  def += getExpression();

	  // first comment
	  if (comments.size() > 0) {
		if (def.length() < 44) {
		  String indent = "%" + (44 - def.length()) + "s";
		  def += String.format(indent, " ");
		}
		def += comments.get(0);
	  }

	  // format for additional comments;
	  format = "%44s%s\n";
	} else {
	  // first comment
	  def += String.format("%40s%s", " ", comments.get(0));

	  // format for additional comments;
	  format = "%44s%s\n";
	}

	def += "\n";
	// additional comments
	for (int index = 1; index < getComments().size(); index++) {
	  def += String.format(format, " ", getComments().get(index));
	}
	return def;
  }

}
