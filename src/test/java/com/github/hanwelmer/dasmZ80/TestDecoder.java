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
	  assert ("0000: 00                    NOP\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
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
	  assert ("0000: 76                    HALT\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
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
	  assert ("0000: 02                    LD   (BC),A\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
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
	  assert ("0000: 01 34 12              LD   BC,0x1234\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
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
	  assert ("0000: 01 DE BC              LD   BC,0xBCDE\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
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
	  assert ("0000: 06 12                 LD   B,0x12\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
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
	  assert ("0000: 06 AB                 LD   B,0xAB\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
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
	  assert ("0000: 10 12                 DJNZ lbl0014\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
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
	  assert ("0000: 10 FE                 DJNZ lbl0000\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
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
	  assert ("0000: D3 12                 OUT  (0x12),A\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
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
	  assert ("0000: D3 AB                 OUT  (0xAB),A\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
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
	  assert ("0000: CB 00                 RLC  B\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
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
	  assert ("0000: DD 19                 ADD  IX,DE\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
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
	  assert ("0000: DD 21 34 12           LD   IX,0x1234\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
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
	  assert ("0000: DD 26 12              LD   IXH,0x12\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
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
	  assert ("0000: DD 34 12              INC  (IX+18)\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
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
	  assert ("0000: DD 34 FE              INC  (IX-2)\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
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
	  assert ("0000: DD 36 12 34           LD   (IX+18),0x34\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
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
	  assert ("0000: ED 40                 IN   B,(C)\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
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
	  assert ("0000: ED 43 34 12           LD   (0x1234),BC\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
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
	  assert ("0000: FD 26 12              LD   IYH,0x12\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  e.printStackTrace();
	}
  }

  @Test
  public void testDecIyPlusDisplacementSmall() {
	try {
	  Byte byte0 = 0xFD - 256;
	  Byte[] bytes = { 0x35, 0x00 };
	  ByteReader reader = new ReadFromArray(bytes);
	  AssemblyCode result = get(0, byte0, reader);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length + 1);
	  assert ("0000: FD 35 00              DEC  (IY+0)\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
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
	  assert ("0000: FD 35 80              DEC  (IY-128)\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
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
	  assert ("0000: FD 36 FF FF           LD   (IY-1),0xFF\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
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
	  assert ("0000: DD CB 12 00           LD   B,RLC (IX+18)\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
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
	  assert ("0000: FD CB 12 01           LD   C,RLC (IY+18)\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
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
	  assert ("0000: FD CB FF 01           LD   C,RLC (IY-1)\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
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
	  assert ("0000: FD CB FF 80           LD   B,RES 0,(IY-1)\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
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
	  e.printStackTrace();
	  assert (false);
	} catch (IllegalOpcodeException e) {
	  assert ("Unsupported code 0xDDDC at address 0x0000\n".equals(e.getMessage()));
	}
  }

}
