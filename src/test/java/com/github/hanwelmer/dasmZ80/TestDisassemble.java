package com.github.hanwelmer.dasmZ80;

import java.util.HashMap;

import org.junit.Test;

public class TestDisassemble extends DasmZ80 {

  @Test
  public void testNop() {
	Byte[] bytes = { 0x00 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble("test", reader);
	assert (result != null && result.size() == 1);
	assert (result.get(0) != null);
	assert (result.get(0).getBytes().size() == bytes.length);
	assert ("       NOP\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testHalt() {
	Byte[] bytes = { 0x76 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble("test", reader);
	assert (result != null && result.size() == 1);
	assert (result.get(0) != null);
	assert (result.get(0).getBytes().size() == bytes.length);
	assert ("       HALT\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testLdBcA() {
	Byte[] bytes = { 0x02 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble("test", reader);
	assert (result != null && result.size() == 1);
	assert (result.get(0) != null);
	assert (result.get(0).getBytes().size() == bytes.length);
	assert ("       LD   (BC),A\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testLdBcWordSmall() {
	Byte[] bytes = { 0x01, 0x34, 0x12 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble("test", reader);
	assert (result != null && result.size() == 1);
	assert (result.get(0) != null);
	assert (result.get(0).getBytes().size() == bytes.length);
	assert ("       LD   BC,0x1234\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testLdBcWordBig() {
	Byte[] bytes = { 0x01, 0xDE - 256, 0xBC - 256 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble("test", reader);
	assert (result != null && result.size() == 1);
	assert (result.get(0) != null);
	assert (result.get(0).getBytes().size() == bytes.length);
	assert ("       LD   BC,0xBCDE\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testLdBByteSmall() {
	Byte[] bytes = { 0x06, 0x12 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble("test", reader);
	assert (result != null && result.size() == 1);
	assert (result.get(0) != null);
	assert (result.get(0).getBytes().size() == bytes.length);
	assert ("       LD   B,0x12\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testLdBByteBig() {
	Byte[] bytes = { 0x06, 0xAB - 256 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble("test", reader);
	assert (result != null && result.size() == 1);
	assert (result.get(0) != null);
	assert (result.get(0).getBytes().size() == bytes.length);
	assert ("       LD   B,0xAB\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testDjnzForward() {
	Byte[] bytes = { 0x10, 0x12 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble("test", reader);
	assert (result != null && result.size() == 1);
	assert (result.get(0) != null);
	assert (result.get(0).getBytes().size() == bytes.length);
	assert ("       DJNZ lbl0014\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testDjnzBackwards() {
	Byte[] bytes = { 0x10, -2 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble("test", reader);
	assert (result != null && result.size() == 1);
	assert (result.get(0) != null);
	assert (result.get(0).getBytes().size() == bytes.length);
	assert ("       DJNZ lbl0000\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testOutPortASmall() {
	Byte[] bytes = { 0xD3 - 256, 0x12 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble("test", reader);
	assert (result != null && result.size() == 1);
	assert (result.get(0) != null);
	assert (result.get(0).getBytes().size() == bytes.length);
	assert ("       OUT  (0x12),A\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testOutPortABig() {
	Byte[] bytes = { 0xD3 - 256, 0xAB - 256 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble("test", reader);
	assert (result != null && result.size() == 1);
	assert (result.get(0) != null);
	assert (result.get(0).getBytes().size() == bytes.length);
	assert ("       OUT  (0xAB),A\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testBitOperations() {
	Byte[] bytes = { 0xCB - 256, 0x00 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble("test", reader);
	assert (result != null && result.size() == 1);
	assert (result.get(0) != null);
	assert (result.get(0).getBytes().size() == bytes.length);
	assert ("       RLC  B\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testAddIxDe() {
	Byte[] bytes = { 0xDD - 256, 0x19 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble("test", reader);
	assert (result != null && result.size() == 1);
	assert (result.get(0) != null);
	assert (result.get(0).getBytes().size() == bytes.length);
	assert ("       ADD  IX,DE\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testLdIx() {
	Byte[] bytes = { 0xDD - 256, 0x21, 0x34, 0x12 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble("test", reader);
	assert (result != null && result.size() == 1);
	assert (result.get(0) != null);
	assert (result.get(0).getBytes().size() == bytes.length);
	assert ("       LD   IX,0x1234\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testLdIxHigh() {
	Byte[] bytes = { 0xDD - 256, 0x26, 0x12 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble("test", reader);
	assert (result != null && result.size() == 1);
	assert (result.get(0) != null);
	assert (result.get(0).getBytes().size() == bytes.length);
	assert ("       LD   IXH,0x12\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testIncIxPlusDisplacementSmall() {
	Byte[] bytes = { 0xDD - 256, 0x34, 0x12 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble("test", reader);
	assert (result != null && result.size() == 1);
	assert (result.get(0) != null);
	assert (result.get(0).getBytes().size() == bytes.length);
	assert ("       INC  (IX+18)\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testIncIxPlusDisplacementBig() {
	Byte[] bytes = { 0xDD - 256, 0x34, 0xFE - 256 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble("test", reader);
	assert (result != null && result.size() == 1);
	assert (result.get(0) != null);
	assert (result.get(0).getBytes().size() == bytes.length);
	assert ("       INC  (IX-2)\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testLdIxPlusDisplacementByte() {
	Byte[] bytes = { 0xDD - 256, 0x36, 0x12, 0x34 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble("test", reader);
	assert (result != null && result.size() == 1);
	assert (result.get(0) != null);
	assert (result.get(0).getBytes().size() == bytes.length);
	assert ("       LD   (IX+18),0x34\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testInputBPortC() {
	Byte[] bytes = { 0xED - 256, 0x40 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble("test", reader);
	assert (result != null && result.size() == 1);
	assert (result.get(0) != null);
	assert (result.get(0).getBytes().size() == bytes.length);
	assert ("       IN   B,(C)\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testLdAddressBC() {
	Byte[] bytes = { 0xED - 256, 0x43, 0x34, 0x12 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble("test", reader);
	assert (result != null && result.size() == 1);
	assert (result.get(0) != null);
	assert (result.get(0).getBytes().size() == bytes.length);
	assert ("       LD   (0x1234),BC\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testLdIyHigh() {
	Byte[] bytes = { 0xFD - 256, 0x26, 0x12 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble("test", reader);
	assert (result != null && result.size() == 1);
	assert (result.get(0) != null);
	assert (result.get(0).getBytes().size() == bytes.length);
	assert ("       LD   IYH,0x12\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testDecIyPlusDisplacementSmall() {
	Byte[] bytes = { 0xFD - 256, 0x35, 0x0 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble("test", reader);
	assert (result != null && result.size() == 1);
	assert (result.get(0) != null);
	assert (result.get(0).getBytes().size() == bytes.length);
	assert ("       DEC  (IY+0)\n".equals(result.get(0).toAsmString()));
  }

  public void testDecIyPlusDisplacementBig() {
	Byte[] bytes = { 0xFD - 256, 0x35, 0x80 - 256 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble("test", reader);
	assert (result != null && result.size() == 1);
	assert (result.get(0) != null);
	assert (result.get(0).getBytes().size() == bytes.length);
	assert ("       DEC  (IY-128)\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testLdIyPlusDisplacementByte() {
	Byte[] bytes = { 0xFD - 256, 0x36, 0xFF - 256, 0xFF - 256 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble("test", reader);
	assert (result != null && result.size() == 1);
	assert (result.get(0) != null);
	assert (result.get(0).getBytes().size() == bytes.length);
	assert ("       LD   (IY-1),0xFF\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testLdBRlcIxPlusDisplacement() {
	Byte[] bytes = { 0xDD - 256, 0xCB - 256, 0x12, 0x00 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble("test", reader);
	assert (result != null && result.size() == 1);
	assert (result.get(0) != null);
	assert (result.get(0).getBytes().size() == bytes.length);
	assert ("       LD   B,RLC (IX+18)\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testLdCRlcIyPlusDisplacementSmall() {
	Byte[] bytes = { 0xFD - 256, 0xCB - 256, 0x12, 0x01 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble("test", reader);
	assert (result != null && result.size() == 1);
	assert (result.get(0) != null);
	assert (result.get(0).getBytes().size() == bytes.length);
	assert ("       LD   C,RLC (IY+18)\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testLdCRlcIyPlusDisplacementBig() {
	Byte[] bytes = { 0xFD - 256, 0xCB - 256, 0xFF - 256, 0x01 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble("test", reader);
	assert (result != null && result.size() == 1);
	assert (result.get(0) != null);
	assert (result.get(0).getBytes().size() == bytes.length);
	assert ("       LD   C,RLC (IY-1)\n".equals(result.get(0).toAsmString()));
  }

  @Test
  public void testLdBSet0IyPlusDisplacementBig() {
	Byte[] bytes = { 0xFD - 256, 0xCB - 256, 0xFF - 256, 0x80 - 256 };
	ByteReader reader = new ReadFromArray(bytes);
	HashMap<Integer, AssemblyCode> result = disassemble("test", reader);
	assert (result != null && result.size() == 1);
	assert (result.get(0) != null);
	assert (result.get(0).getBytes().size() == bytes.length);
	assert ("       LD   B,RES 0,(IY-1)\n".equals(result.get(0).toAsmString()));
  }

}
