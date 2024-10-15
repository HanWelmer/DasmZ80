package com.github.hanwelmer.dasmZ80;

import java.util.HashMap;

import org.junit.Test;

public class TestDisassemble extends DasmZ80 {

  @Test
  public void testNop() {
	ConfigurationParameters parameters = null;
	Byte[] bytes = { 0x00 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble(parameters, reader);
	assert (result != null);
	assert (result.size() == 1);
	assert (result.get(0) != null);
	assert ("       NOP\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testHalt() {
	ConfigurationParameters parameters = null;
	Byte[] bytes = { 0x76 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble(parameters, reader);
	assert (result != null);
	assert (result.size() == 1);
	assert (result.get(0) != null);
	assert ("       HALT\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testLdBcA() {
	ConfigurationParameters parameters = null;
	Byte[] bytes = { 0x02 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble(parameters, reader);
	assert (result != null);
	assert (result.size() == 1);
	assert (result.get(0) != null);
	assert ("       LD   (BC),A\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testLdBcWordSmall() {
	ConfigurationParameters parameters = null;
	Byte[] bytes = { 0x01, 0x34, 0x12 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble(parameters, reader);
	assert (result != null);
	assert (result.size() == 1);
	assert (result.get(0) != null);
	assert ("       LD   BC,0x1234\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testLdBcWordBig() {
	ConfigurationParameters parameters = null;
	Byte[] bytes = { 0x01, 0xDE - 256, 0xBC - 256 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble(parameters, reader);
	assert (result != null);
	assert (result.size() == 1);
	assert (result.get(0) != null);
	assert ("       LD   BC,0xBCDE\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testLdBByteSmall() {
	ConfigurationParameters parameters = null;
	Byte[] bytes = { 0x06, 0x12 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble(parameters, reader);
	assert (result != null);
	assert (result.size() == 1);
	assert (result.get(0) != null);
	assert ("       LD   B,0x12\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testLdBByteBig() {
	ConfigurationParameters parameters = null;
	Byte[] bytes = { 0x06, 0xAB - 256 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble(parameters, reader);
	assert (result != null);
	assert (result.size() == 1);
	assert (result.get(0) != null);
	assert ("       LD   B,0xAB\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testDjnzForward() {
	ConfigurationParameters parameters = null;
	Byte[] bytes = { 0x10, 0x12 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble(parameters, reader);
	assert (result != null);
	assert (result.size() == 1);
	assert (result.get(0) != null);
	assert ("       DJNZ lbl0014\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testDjnzBackwards() {
	ConfigurationParameters parameters = null;
	Byte[] bytes = { 0x10, -2 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble(parameters, reader);
	assert (result != null);
	assert (result.size() == 1);
	assert (result.get(0) != null);
	assert ("       DJNZ lbl0000\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testOutPortASmall() {
	ConfigurationParameters parameters = null;
	Byte[] bytes = { 0xD3 - 256, 0x12 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble(parameters, reader);
	assert (result != null);
	assert (result.size() == 1);
	assert (result.get(0) != null);
	assert ("       OUT  (0x12),A\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testOutPortABig() {
	ConfigurationParameters parameters = null;
	Byte[] bytes = { 0xD3 - 256, 0xAB - 256 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble(parameters, reader);
	assert (result != null);
	assert (result.size() == 1);
	assert (result.get(0) != null);
	assert ("       OUT  (0xAB),A\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testBitOperations() {
	ConfigurationParameters parameters = null;
	Byte[] bytes = { 0xCB - 256, 0x00 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble(parameters, reader);
	assert (result != null);
	assert (result.size() == 1);
	assert (result.get(0) != null);
	assert ("       RLC  B\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testAddIxDe() {
	ConfigurationParameters parameters = null;
	Byte[] bytes = { 0xDD - 256, 0x19 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble(parameters, reader);
	assert (result != null);
	assert (result.size() == 1);
	assert (result.get(0) != null);
	assert ("       ADD  IX,DE\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testLdIx() {
	ConfigurationParameters parameters = null;
	Byte[] bytes = { 0xDD - 256, 0x21, 0x34, 0x12 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble(parameters, reader);
	assert (result != null);
	assert (result.size() == 1);
	assert (result.get(0) != null);
	assert ("       LD   IX,0x1234\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testLdIxHigh() {
	ConfigurationParameters parameters = null;
	Byte[] bytes = { 0xDD - 256, 0x26, 0x12 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble(parameters, reader);
	assert (result != null);
	assert (result.size() == 1);
	assert (result.get(0) != null);
	assert ("       LD   IXH,0x12\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testIncIxPlusDisplacementSmall() {
	ConfigurationParameters parameters = null;
	Byte[] bytes = { 0xDD - 256, 0x34, 0x12 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble(parameters, reader);
	assert (result != null);
	assert (result.size() == 1);
	assert (result.get(0) != null);
	assert ("       INC  (IX+18)\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testIncIxPlusDisplacementBig() {
	ConfigurationParameters parameters = null;
	Byte[] bytes = { 0xDD - 256, 0x34, 0xFE - 256 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble(parameters, reader);
	assert (result != null);
	assert (result.size() == 1);
	assert (result.get(0) != null);
	assert ("       INC  (IX-2)\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testLdIxPlusDisplacementByte() {
	ConfigurationParameters parameters = null;
	Byte[] bytes = { 0xDD - 256, 0x36, 0x12, 0x34 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble(parameters, reader);
	assert (result != null);
	assert (result.size() == 1);
	assert (result.get(0) != null);
	assert ("       LD   (IX+18),0x34\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testInputBPortC() {
	ConfigurationParameters parameters = null;
	Byte[] bytes = { 0xED - 256, 0x40 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble(parameters, reader);
	assert (result != null);
	assert (result.size() == 1);
	assert (result.get(0) != null);
	assert ("       IN   B,(C)\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testLdAddressBC() {
	ConfigurationParameters parameters = null;
	Byte[] bytes = { 0xED - 256, 0x43, 0x34, 0x12 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble(parameters, reader);
	assert (result != null);
	assert (result.size() == 1);
	assert (result.get(0) != null);
	assert ("       LD   (0x1234),BC\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testLdIyHigh() {
	ConfigurationParameters parameters = null;
	Byte[] bytes = { 0xFD - 256, 0x26, 0x12 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble(parameters, reader);
	assert (result != null);
	assert (result.size() == 1);
	assert (result.get(0) != null);
	assert ("       LD   IYH,0x12\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testDecIyPlusDisplacementSmall() {
	ConfigurationParameters parameters = null;
	Byte[] bytes = { 0xFD - 256, 0x35, 0x0 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble(parameters, reader);
	assert (result != null);
	assert (result.size() == 1);
	assert (result.get(0) != null);
	assert ("       DEC  (IY+0)\n".equals(result.get(0).toAsmString()));
  }

  public void testDecIyPlusDisplacementBig() {
	ConfigurationParameters parameters = null;
	Byte[] bytes = { 0xFD - 256, 0x35, 0x80 - 256 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble(parameters, reader);
	assert (result != null);
	assert (result.size() == 1);
	assert (result.get(0) != null);
	assert ("       DEC  (IY-128)\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testLdIyPlusDisplacementByte() {
	ConfigurationParameters parameters = null;
	Byte[] bytes = { 0xFD - 256, 0x36, 0xFF - 256, 0xFF - 256 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble(parameters, reader);
	assert (result != null);
	assert (result.size() == 1);
	assert (result.get(0) != null);
	assert ("       LD   (IY-1),0xFF\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testLdBRlcIxPlusDisplacement() {
	ConfigurationParameters parameters = null;
	Byte[] bytes = { 0xDD - 256, 0xCB - 256, 0x12, 0x00 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble(parameters, reader);
	assert (result != null);
	assert (result.size() == 1);
	assert (result.get(0) != null);
	assert ("       LD   B,RLC (IX+18)\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testLdCRlcIyPlusDisplacementSmall() {
	ConfigurationParameters parameters = null;
	Byte[] bytes = { 0xFD - 256, 0xCB - 256, 0x12, 0x01 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble(parameters, reader);
	assert (result != null);
	assert (result.size() == 1);
	assert (result.get(0) != null);
	assert ("       LD   C,RLC (IY+18)\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testLdCRlcIyPlusDisplacementBig() {
	ConfigurationParameters parameters = null;
	Byte[] bytes = { 0xFD - 256, 0xCB - 256, 0xFF - 256, 0x01 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble(parameters, reader);
	assert (result != null);
	assert (result.size() == 1);
	assert (result.get(0) != null);
	assert ("       LD   C,RLC (IY-1)\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testLdBSet0IyPlusDisplacementBig() {
	ConfigurationParameters parameters = null;
	Byte[] bytes = { 0xFD - 256, 0xCB - 256, 0xFF - 256, 0x80 - 256 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble(parameters, reader);
	assert (result != null);
	assert (result.size() == 1);
	assert (result.get(0) != null);
	assert ("       LD   B,RES 0,(IY-1)\n".equals(result.get(0).toAsmString()));
  }

}
