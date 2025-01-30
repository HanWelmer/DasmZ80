package com.github.hanwelmer.dasmZ80;

import java.util.ArrayList;
import java.util.HashMap;

public class Symbols {

  // symbols, grouped by symbol type:
  private HashMap<Integer, Symbol> entryPoints = new HashMap<Integer, Symbol>();
  private HashMap<Integer, Symbol> portAddresses = new HashMap<Integer, Symbol>();
  private HashMap<Integer, Symbol> memoryAddresses = new HashMap<Integer, Symbol>();
  private HashMap<Integer, Symbol> constants = new HashMap<Integer, Symbol>();
  private HashMap<Integer, Symbol> labels = new HashMap<Integer, Symbol>();

  public void clear() {
	entryPoints.clear();
	portAddresses.clear();
	memoryAddresses.clear();
	constants.clear();
	labels.clear();
  }

  public HashMap<Integer, Symbol> getEntryPoints() {
	return entryPoints;
  }

  public ArrayList<Symbol> getSymbolsByType(SymbolType symbolType) {
	ArrayList<Symbol> symbolList = new ArrayList<Symbol>();
	switch (symbolType) {
	case entryPoint:
	  entryPoints.forEach((Integer key, Symbol symbol) -> symbolList.add(symbol));
	  break;
	case memoryAddress:
	  memoryAddresses.forEach((Integer key, Symbol symbol) -> symbolList.add(symbol));
	  break;
	case portAddress:
	  portAddresses.forEach((Integer key, Symbol symbol) -> symbolList.add(symbol));
	  break;
	case constant:
	  constants.forEach((Integer key, Symbol symbol) -> symbolList.add(symbol));
	  break;
	case label:
	  labels.forEach((Integer key, Symbol symbol) -> symbolList.add(symbol));
	  break;
	default:
	  break;
	}

	return symbolList;
  } // getSymbolsByType()

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
  public Symbol getOrMakeSymbol(String name, SymbolType symbolType, Integer value, String expression) {
	Symbol symbol = new Symbol(name, symbolType, value, expression);
	switch (symbolType) {
	case entryPoint:
	  symbol = getOrMakeSymbol(entryPoints, symbol);
	  break;
	case memoryAddress:
	  symbol = getOrMakeSymbol(memoryAddresses, symbol);
	  break;
	case portAddress:
	  symbol = getOrMakeSymbol(portAddresses, symbol);
	  break;
	case constant:
	  symbol = getOrMakeSymbol(constants, symbol);
	  break;
	case label:
	  // check if label is already defined as entry point or memory address.
	  if (entryPoints.get(value) != null) {
		symbol = entryPoints.get(value);
	  } else if (memoryAddresses.get(value) != null) {
		symbol = memoryAddresses.get(value);
	  } else {
		symbol = getOrMakeSymbol(labels, symbol);
	  }
	  break;
	default:
	  break;
	}

	return symbol;
  } // getOrMakeSymbol()

  private Symbol getOrMakeSymbol(HashMap<Integer, Symbol> symbols, Symbol newSymbol) {
	// If available use symbolType or constant.
	Symbol symbol = symbols.get(newSymbol.getValue());

	// If not, add label and value as symbol for the symbolType.
	if (symbol == null) {
	  symbols.put(newSymbol.getValue(), newSymbol);
	  symbol = newSymbol;
	}
	return symbol;
  }

  public void addAsMemoryAddress(Symbol point) {
	memoryAddresses.put(point.getValue(), point);
  }
}
