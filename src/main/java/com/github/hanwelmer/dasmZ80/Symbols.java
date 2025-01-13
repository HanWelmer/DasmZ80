package com.github.hanwelmer.dasmZ80;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

public class Symbols {

  private HashMap<Integer, Symbol> symbols = new HashMap<Integer, Symbol>();
  private HashMap<Integer, Symbol> entryPoints = new HashMap<Integer, Symbol>();

  /**
   * Get the symbol with the given name, if one exists. Otherwise create a new
   * symbol with the given name, type and value.
   * 
   * @param name
   *          name for the symbol (must be unique within the list of symbols).
   * @param type
   *          type of symbol (see enumeration SymbolType).
   * @param value
   *          8-bit or 16-bit value for the symbol.
   * @param expression
   * @return existing or newly added symbol with the given name.
   */
  public Symbol getOrMakeSymbol(String name, SymbolType type, Integer value, String expression) {
	// If available use symbolType or constant.
	Symbol symbol = symbols.get(value);

	// If not, add label and value as symbol for the symbolType.
	if (symbol == null) {
	  symbols.put(value, new Symbol(name, type, value, expression));
	  symbol = symbols.get(value);
	}

	// Keep copies of entry points in a separate table.
	if (type == SymbolType.entryPoint) {
	  entryPoints.put(symbol.getValue(), symbol);
	}

	return symbol;
  }

  public ArrayList<Symbol> getSymbolsByType(SymbolType symbolType) {
	ArrayList<Symbol> symbolList = new ArrayList<Symbol>();

	// select symbols of the requested type.
	SortedSet<Integer> values = new TreeSet<>(symbols.keySet());
	for (Integer value : values) {
	  Symbol symbol = symbols.get(value);
	  if (symbol.getType() == symbolType) {
		symbolList.add(symbol);
	  }
	}

	return symbolList;
  }

  public HashMap<Integer, Symbol> getEntryPoints() {
	return entryPoints;
  }

  public void clear() {
	symbols.clear();
	entryPoints.clear();
  }

  // get symbol by value
  public Object get(Integer value) {
	return symbols.get(value);
  }
}
