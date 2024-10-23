package com.github.hanwelmer.dasmZ80;

import java.io.IOException;

import org.junit.Test;

public class TestDecoder extends Decoder {

  @Test
  public void testNop() {
	try {
	  Byte byte0 = 0x00;
	  Byte[] bytes = {};
	  ByteReader reader = new ReadFromArray(bytes);
	  AssemblyCode result = get(0, byte0, reader);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length + 1);
	  assert ("       NOP\n".equals(result.toAsmString()));
	} catch (IOException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	}
  }

  @Test
  public void testHalt() {
	try {
	  Byte byte0 = 0x76;
	  Byte[] bytes = {};
	  ByteReader reader = new ReadFromArray(bytes);
	  AssemblyCode result = get(0, byte0, reader);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length + 1);
	  assert ("       HALT\n".equals(result.toAsmString()));
	} catch (IOException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	}
  }

  @Test
  public void testLdBcA() {
	try {
	  Byte byte0 = 0x02;
	  Byte[] bytes = {};
	  ByteReader reader = new ReadFromArray(bytes);
	  AssemblyCode result = get(0, byte0, reader);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length + 1);
	  assert ("       LD   (BC),A\n".equals(result.toAsmString()));
	} catch (IOException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	}
  }

  @Test
  public void testLdBcWordSmall() {
	try {
	  Byte byte0 = 0x01;
	  Byte[] bytes = { 0x34, 0x12 };
	  ByteReader reader = new ReadFromArray(bytes);
	  AssemblyCode result = get(0, byte0, reader);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length + 1);
	  assert ("       LD   BC,0x1234\n".equals(result.toAsmString()));
	} catch (IOException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	}
  }

  @Test
  public void testLdBcWordBig() {
	try {
	  Byte byte0 = 0x01;
	  Byte[] bytes = { 0xDE - 256, 0xBC - 256 };
	  ByteReader reader = new ReadFromArray(bytes);
	  AssemblyCode result = get(0, byte0, reader);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length + 1);
	  assert ("       LD   BC,0xBCDE\n".equals(result.toAsmString()));
	} catch (IOException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	}
  }

  @Test
  public void testLdBByteSmall() {
	try {
	  Byte byte0 = 0x06;
	  Byte[] bytes = { 0x12 };
	  ByteReader reader = new ReadFromArray(bytes);
	  AssemblyCode result = get(0, byte0, reader);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length + 1);
	  assert ("       LD   B,0x12\n".equals(result.toAsmString()));
	} catch (IOException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	}
  }

  @Test
  public void testLdBByteBig() {
	try {
	  Byte byte0 = 0x06;
	  Byte[] bytes = { 0xAB - 256 };
	  ByteReader reader = new ReadFromArray(bytes);
	  AssemblyCode result = get(0, byte0, reader);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length + 1);
	  assert ("       LD   B,0xAB\n".equals(result.toAsmString()));
	} catch (IOException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	}
  }

  @Test
  public void testDjnzForward() {
	try {
	  Byte byte0 = 0x10;
	  Byte[] bytes = { 0x12 };
	  ByteReader reader = new ReadFromArray(bytes);
	  AssemblyCode result = get(0, byte0, reader);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length + 1);
	  assert ("       DJNZ lbl0014\n".equals(result.toAsmString()));
	} catch (IOException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	}
  }

  @Test
  public void testDjnzBackwards() {
	try {
	  Byte byte0 = 0x10;
	  Byte[] bytes = { -2 };
	  ByteReader reader = new ReadFromArray(bytes);
	  AssemblyCode result = get(0, byte0, reader);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length + 1);
	  assert ("       DJNZ lbl0000\n".equals(result.toAsmString()));
	} catch (IOException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	}
  }

  @Test
  public void testOutPortASmall() {
	try {
	  Byte byte0 = 0xD3 - 256;
	  Byte[] bytes = { 0x12 };
	  ByteReader reader = new ReadFromArray(bytes);
	  AssemblyCode result = get(0, byte0, reader);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length + 1);
	  assert ("       OUT  (0x12),A\n".equals(result.toAsmString()));
	} catch (IOException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	}
  }

  @Test
  public void testOutPortABig() {
	try {
	  Byte byte0 = 0xD3 - 256;
	  Byte[] bytes = { 0xAB - 256 };
	  ByteReader reader = new ReadFromArray(bytes);
	  AssemblyCode result = get(0, byte0, reader);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length + 1);
	  assert ("       OUT  (0xAB),A\n".equals(result.toAsmString()));
	} catch (IOException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	}
  }

  @Test
  public void testBitOperations() {
	try {
	  Byte byte0 = 0xCB - 256;
	  Byte[] bytes = { 0x00 };
	  ByteReader reader = new ReadFromArray(bytes);
	  AssemblyCode result = get(0, byte0, reader);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length + 1);
	  assert ("       RLC  B\n".equals(result.toAsmString()));
	} catch (IOException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	}
  }

  @Test
  public void testAddIxDe() {
	try {
	  Byte byte0 = 0xDD - 256;
	  Byte[] bytes = { 0x19 };
	  ByteReader reader = new ReadFromArray(bytes);
	  AssemblyCode result = get(0, byte0, reader);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length + 1);
	  assert ("       ADD  IX,DE\n".equals(result.toAsmString()));
	} catch (IOException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	}
  }

  @Test
  public void testLdIx() {
	try {
	  Byte byte0 = 0xDD - 256;
	  Byte[] bytes = { 0x21, 0x34, 0x12 };
	  ByteReader reader = new ReadFromArray(bytes);
	  AssemblyCode result = get(0, byte0, reader);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length + 1);
	  assert ("       LD   IX,0x1234\n".equals(result.toAsmString()));
	} catch (IOException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	}
  }

  @Test
  public void testLdIxHigh() {
	try {
	  Byte byte0 = 0xDD - 256;
	  Byte[] bytes = { 0x26, 0x12 };
	  ByteReader reader = new ReadFromArray(bytes);
	  AssemblyCode result = get(0, byte0, reader);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length + 1);
	  assert ("       LD   IXH,0x12\n".equals(result.toAsmString()));
	} catch (IOException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	}
  }

  @Test
  public void testIncIxPlusDisplacementSmall() {
	try {
	  Byte byte0 = 0xDD - 256;
	  Byte[] bytes = { 0x34, 0x12 };
	  ByteReader reader = new ReadFromArray(bytes);
	  AssemblyCode result = get(0, byte0, reader);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length + 1);
	  assert ("       INC  (IX+18)\n".equals(result.toAsmString()));
	} catch (IOException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	}
  }

  @Test
  public void testIncIxPlusDisplacementBig() {
	try {
	  Byte byte0 = 0xDD - 256;
	  Byte[] bytes = { 0x34, 0xFE - 256 };
	  ByteReader reader = new ReadFromArray(bytes);
	  AssemblyCode result = get(0, byte0, reader);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length + 1);
	  assert ("       INC  (IX-2)\n".equals(result.toAsmString()));
	} catch (IOException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	}
  }

  @Test
  public void testLdIxPlusDisplacementByte() {
	try {
	  Byte byte0 = 0xDD - 256;
	  Byte[] bytes = { 0x36, 0x12, 0x34 };
	  ByteReader reader = new ReadFromArray(bytes);
	  AssemblyCode result = get(0, byte0, reader);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length + 1);
	  assert ("       LD   (IX+18),0x34\n".equals(result.toAsmString()));
	} catch (IOException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	}
  }

  @Test
  public void testInputBPortC() {
	try {
	  Byte byte0 = 0xED - 256;
	  Byte[] bytes = { 0x40 };
	  ByteReader reader = new ReadFromArray(bytes);
	  AssemblyCode result = get(0, byte0, reader);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length + 1);
	  assert ("       IN   B,(C)\n".equals(result.toAsmString()));
	} catch (IOException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	}
  }

  @Test
  public void testLdAddressBC() {
	try {
	  Byte byte0 = 0xED - 256;
	  Byte[] bytes = { 0x43, 0x34, 0x12 };
	  ByteReader reader = new ReadFromArray(bytes);
	  AssemblyCode result = get(0, byte0, reader);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length + 1);
	  assert ("       LD   (0x1234),BC\n".equals(result.toAsmString()));
	} catch (IOException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	}
  }

  @Test
  public void testLdIyHigh() {
	try {
	  Byte byte0 = 0xFD - 256;
	  Byte[] bytes = { 0x26, 0x12 };
	  ByteReader reader = new ReadFromArray(bytes);
	  AssemblyCode result = get(0, byte0, reader);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length + 1);
	  assert ("       LD   IYH,0x12\n".equals(result.toAsmString()));
	} catch (IOException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	}
  }

  @Test
  public void testDecIyPlusDisplacementSmall() {
	try {
	  Byte byte0 = 0xFD - 256;
	  Byte[] bytes = { 0x35, 0x0 };
	  ByteReader reader = new ReadFromArray(bytes);
	  AssemblyCode result = get(0, byte0, reader);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length + 1);
	  assert ("       DEC  (IY+0)\n".equals(result.toAsmString()));
	} catch (IOException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	}
  }

  public void testDecIyPlusDisplacementBig() {
	try {
	  Byte byte0 = 0xFD - 256;
	  Byte[] bytes = { 0x35, 0x80 - 256 };
	  ByteReader reader = new ReadFromArray(bytes);
	  AssemblyCode result = get(0, byte0, reader);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length + 1);
	  assert ("       DEC  (IY-128)\n".equals(result.toAsmString()));
	} catch (IOException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	}
  }

  @Test
  public void testLdIyPlusDisplacementByte() {
	try {
	  Byte byte0 = 0xFD - 256;
	  Byte[] bytes = { 0x36, 0xFF - 256, 0xFF - 256 };
	  ByteReader reader = new ReadFromArray(bytes);
	  AssemblyCode result = get(0, byte0, reader);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length + 1);
	  assert ("       LD   (IY-1),0xFF\n".equals(result.toAsmString()));
	} catch (IOException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	}
  }

  @Test
  public void testLdBRlcIxPlusDisplacement() {
	try {
	  Byte byte0 = 0xDD - 256;
	  Byte[] bytes = { 0xCB - 256, 0x12, 0x00 };
	  ByteReader reader = new ReadFromArray(bytes);
	  AssemblyCode result = get(0, byte0, reader);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length + 1);
	  assert ("       LD   B,RLC (IX+18)\n".equals(result.toAsmString()));
	} catch (IOException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	}
  }

  @Test
  public void testLdCRlcIyPlusDisplacementSmall() {
	try {
	  Byte byte0 = 0xFD - 256;
	  Byte[] bytes = { 0xCB - 256, 0x12, 0x01 };
	  ByteReader reader = new ReadFromArray(bytes);
	  AssemblyCode result = get(0, byte0, reader);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length + 1);
	  assert ("       LD   C,RLC (IY+18)\n".equals(result.toAsmString()));
	} catch (IOException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	}
  }

  @Test
  public void testLdCRlcIyPlusDisplacementBig() {
	try {
	  Byte byte0 = 0xFD - 256;
	  Byte[] bytes = { 0xCB - 256, 0xFF - 256, 0x01 };
	  ByteReader reader = new ReadFromArray(bytes);
	  AssemblyCode result = get(0, byte0, reader);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length + 1);
	  assert ("       LD   C,RLC (IY-1)\n".equals(result.toAsmString()));
	} catch (IOException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	}
  }

  @Test
  public void testLdBSet0IyPlusDisplacementBig() {
	try {
	  Byte byte0 = 0xFD - 256;
	  Byte[] bytes = { 0xCB - 256, 0xFF - 256, 0x80 - 256 };
	  ByteReader reader = new ReadFromArray(bytes);
	  AssemblyCode result = get(0, byte0, reader);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length + 1);
	  assert ("       LD   B,RES 0,(IY-1)\n".equals(result.toAsmString()));
	} catch (IOException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	}
  }

  @Test
  public void testIllegalOpcode() {
	try {
	  Byte byte0 = 0xDD - 256;
	  Byte[] bytes = { 0xDC - 256, 0x34, 0x12 };
	  ByteReader reader = new ReadFromArray(bytes);
	  AssemblyCode result = get(0, byte0, reader);
	} catch (IOException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	  assert (false);
	} catch (IllegalOpcodeException e) {
	  assert ("Unsupported code 0xDDDC at address 0x0000\n".equals(e.getMessage()));
	}
  }

}
