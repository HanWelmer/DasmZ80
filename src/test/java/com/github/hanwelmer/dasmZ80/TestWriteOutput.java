package com.github.hanwelmer.dasmZ80;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

public class TestWriteOutput extends DasmZ80 {

  @Test
  public void test() {
	ArrayList<AssemblyCode> decoded = new ArrayList<AssemblyCode>();

	ArrayList<Byte> bytes0x00 = new ArrayList<Byte>();
	bytes0x00.add((byte) 0x00);
	decoded.add(new AssemblyCode(0, bytes0x00, "start", "NOP", ";comment"));

	ArrayList<Byte> bytes0x02 = new ArrayList<Byte>();
	bytes0x02.add((byte) 0x02);
	decoded.add(new AssemblyCode(1, bytes0x02, null, "LD (BC),A", ";comment"));

	ArrayList<Byte> bytes0x76 = new ArrayList<Byte>();
	bytes0x76.add((byte) 0x76);
	decoded.add(new AssemblyCode(2, bytes0x76, null, "HALT", ";comment"));

	String fileName = "testWriteOutput.bin";
	ListingWriter writer = new ListingWriter();
	try {
	  writer.open(fileName);
	  writeOutput(3, decoded, writer);
	} catch (IOException e) {
	  e.printStackTrace();
	} finally {
	  writer.close();
	}
  }

}
