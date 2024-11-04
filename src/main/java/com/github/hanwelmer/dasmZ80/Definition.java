package com.github.hanwelmer.dasmZ80;

import java.util.ArrayList;

/**
 * Class to manage a named value and references to it.
 * 
 * @author welmerhj
 *
 */
public class Definition {

  private String name;
  private Integer value;
  private ArrayList<Integer> references;

  public Definition(String name, Integer value) {
	super();
	this.name = name;
	this.value = value;
	references = new ArrayList<Integer>();
  }

  public void add(Integer reference) {
	references.add(reference);

  }

  public ArrayList<Integer> getReferences() {
	return references;
  }

}
