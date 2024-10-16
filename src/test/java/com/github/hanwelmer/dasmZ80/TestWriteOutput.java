package com.github.hanwelmer.dasmZ80;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

public class TestWriteOutput extends DasmZ80 {

  @Test
  public void test() {
	HashMap<Integer, AssemblyCode> decoded = new HashMap<Integer, AssemblyCode>();

	ArrayList<Byte> bytes = new ArrayList<Byte>();
	bytes.add((byte) 0x00);
	decoded.put(0, new AssemblyCode(0, bytes, "start", "NOP", ";comment"));
	bytes.clear();
	bytes.add((byte) 0x02);
	decoded.put(1, new AssemblyCode(0, bytes, null, "LD (BC),A", ";comment"));
	bytes.clear();
	bytes.add((byte) 0x76);
	decoded.put(1, new AssemblyCode(0, bytes, null, "HALT", ";comment"));

	writeOutput("testWriteOutput.hex", decoded);
  }

}
