package com.github.hanwelmer.dasmZ80;

import java.io.IOException;

import org.junit.Test;

public class TestDecoder extends Decoder {

  @Test
  public void testNop() {
	try {
	  Byte[] bytes = { 0x00 };
	  setReader(new ReadFromArray(bytes));
	  Symbols symbols = new Symbols();

	  AssemblyCode result = get(0, symbols);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length);
	  assert ("0000 00                 NOP\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  e.printStackTrace();
	}
  }

  @Test
  public void testHalt() {
	try {
	  Byte[] bytes = { 0x76 };
	  setReader(new ReadFromArray(bytes));
	  Symbols symbols = new Symbols();

	  AssemblyCode result = get(0, symbols);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length);
	  assert ("0000 76                 HALT\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  e.printStackTrace();
	}
  }

  @Test
  public void testLdBcA() {
	try {
	  Byte[] bytes = { 0x02 };
	  setReader(new ReadFromArray(bytes));
	  Symbols symbols = new Symbols();

	  AssemblyCode result = get(0, symbols);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length);
	  assert ("0000 02                 LD   (BC),A\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  e.printStackTrace();
	}
  }

  @Test
  public void testLdBcWordSmall() {
	try {
	  Byte[] bytes = { 0x01, 0x34, 0x12 };
	  setReader(new ReadFromArray(bytes));
	  Symbols symbols = new Symbols();

	  AssemblyCode result = get(0, symbols);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length);
	  assert ("0000 013412             LD   BC,lbl1234\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  e.printStackTrace();
	}
  }

  @Test
  public void testLdBcWordBig() {
	try {
	  Byte[] bytes = { 0x01, 0xDE - 256, 0xBC - 256 };
	  setReader(new ReadFromArray(bytes));
	  Symbols symbols = new Symbols();

	  AssemblyCode result = get(0, symbols);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length);
	  assert ("0000 01DEBC             LD   BC,lblBCDE\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  e.printStackTrace();
	}
  }

  @Test
  public void testLdBByteSmall() {
	try {
	  Byte[] bytes = { 0x06, 0x12 };
	  setReader(new ReadFromArray(bytes));
	  Symbols symbols = new Symbols();

	  AssemblyCode result = get(0, symbols);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length);
	  assert ("0000 0612               LD   B,0x12\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  e.printStackTrace();
	}
  }

  @Test
  public void testLdBByteBig() {
	try {
	  Byte[] bytes = { 0x06, 0xAB - 256 };
	  setReader(new ReadFromArray(bytes));
	  Symbols symbols = new Symbols();

	  AssemblyCode result = get(0, symbols);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length);
	  assert ("0000 06AB               LD   B,0xAB\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  e.printStackTrace();
	}
  }

  @Test
  public void testDjnzForward() {
	try {
	  Byte[] bytes = { 0x10, 0x12 };
	  setReader(new ReadFromArray(bytes));
	  Symbols symbols = new Symbols();

	  AssemblyCode result = get(0, symbols);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length);
	  assert ("0000 1012               DJNZ lbl0014-$\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  e.printStackTrace();
	}
  }

  @Test
  public void testDjnzBackwards() {
	try {
	  Byte[] bytes = { 0x10, -2 };
	  setReader(new ReadFromArray(bytes));
	  Symbols symbols = new Symbols();

	  AssemblyCode result = get(0, symbols);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length);
	  assert ("0000 10FE               DJNZ lbl0000-$\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  e.printStackTrace();
	}
  }

  @Test
  public void testOutPortASmall() {
	try {
	  Byte[] bytes = { 0xD3 - 256, 0x12 };
	  setReader(new ReadFromArray(bytes));
	  Symbols symbols = new Symbols();

	  AssemblyCode result = get(0, symbols);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length);
	  assert ("0000 D312               OUT  (port12),A\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  e.printStackTrace();
	}
  }

  @Test
  public void testOutPortABig() {
	try {
	  Byte[] bytes = { 0xD3 - 256, 0xAB - 256 };
	  setReader(new ReadFromArray(bytes));
	  Symbols symbols = new Symbols();

	  AssemblyCode result = get(0, symbols);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length);
	  assert ("0000 D3AB               OUT  (portAB),A\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  e.printStackTrace();
	}
  }

  @Test
  public void testBitOperations() {
	try {
	  Byte[] bytes = { 0xCB - 256, 0x00 };
	  setReader(new ReadFromArray(bytes));
	  Symbols symbols = new Symbols();

	  AssemblyCode result = get(0, symbols);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length);
	  assert ("0000 CB00               RLC  B\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  e.printStackTrace();
	}
  }

  @Test
  public void testAddIxDe() {
	try {
	  Byte[] bytes = { 0xDD - 256, 0x19 };
	  setReader(new ReadFromArray(bytes));
	  Symbols symbols = new Symbols();

	  AssemblyCode result = get(0, symbols);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length);
	  assert ("0000 DD19               ADD  IX,DE\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  e.printStackTrace();
	}
  }

  @Test
  public void testLdIx() {
	try {
	  Byte[] bytes = { 0xDD - 256, 0x21, 0x34, 0x12 };
	  setReader(new ReadFromArray(bytes));
	  Symbols symbols = new Symbols();

	  AssemblyCode result = get(0, symbols);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length);
	  assert ("0000 DD213412           LD   IX,lbl1234\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  e.printStackTrace();
	}
  }

  @Test
  public void testLdIxHigh() {
	try {
	  Byte[] bytes = { 0xDD - 256, 0x26, 0x12 };
	  setReader(new ReadFromArray(bytes));
	  Symbols symbols = new Symbols();

	  AssemblyCode result = get(0, symbols);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length);
	  assert ("0000 DD2612             LD   IXH,0x12\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  e.printStackTrace();
	}
  }

  @Test
  public void testIncIxPlusDisplacementSmall() {
	try {
	  Byte[] bytes = { 0xDD - 256, 0x34, 0x12 };
	  setReader(new ReadFromArray(bytes));
	  Symbols symbols = new Symbols();

	  AssemblyCode result = get(0, symbols);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length);
	  assert ("0000 DD3412             INC  (IX+18)\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  e.printStackTrace();
	}
  }

  @Test
  public void testIncIxPlusDisplacementBig() {
	try {
	  Byte[] bytes = { 0xDD - 256, 0x34, 0xFE - 256 };
	  setReader(new ReadFromArray(bytes));
	  Symbols symbols = new Symbols();

	  AssemblyCode result = get(0, symbols);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length);
	  assert ("0000 DD34FE             INC  (IX-2)\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  e.printStackTrace();
	}
  }

  @Test
  public void testLdIxPlusDisplacementByte() {
	try {
	  Byte[] bytes = { 0xDD - 256, 0x36, 0x12, 0x34 };
	  setReader(new ReadFromArray(bytes));
	  Symbols symbols = new Symbols();

	  AssemblyCode result = get(0, symbols);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length);
	  assert ("0000 DD361234           LD   (IX+18),0x34\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  e.printStackTrace();
	}
  }

  @Test
  public void testInputBPortC() {
	try {
	  Byte[] bytes = { 0xED - 256, 0x40 };
	  setReader(new ReadFromArray(bytes));
	  Symbols symbols = new Symbols();

	  AssemblyCode result = get(0, symbols);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length);
	  assert ("0000 ED40               IN   B,(C)\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  e.printStackTrace();
	}
  }

  @Test
  public void testLdAddressBC() {
	try {
	  Byte[] bytes = { 0xED - 256, 0x43, 0x34, 0x12 };
	  setReader(new ReadFromArray(bytes));
	  Symbols symbols = new Symbols();

	  AssemblyCode result = get(0, symbols);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length);
	  assert ("0000 ED433412           LD   (lbl1234),BC\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  e.printStackTrace();
	}
  }

  @Test
  public void testLdIyHigh() {
	try {
	  Byte[] bytes = { 0xFD - 256, 0x26, 0x12 };
	  setReader(new ReadFromArray(bytes));
	  Symbols symbols = new Symbols();

	  AssemblyCode result = get(0, symbols);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length);
	  assert ("0000 FD2612             LD   IYH,0x12\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  e.printStackTrace();
	}
  }

  @Test
  public void testDecIyPlusDisplacementSmall() {
	try {
	  Byte[] bytes = { 0xFD - 256, 0x35, 0x00 };
	  setReader(new ReadFromArray(bytes));
	  Symbols symbols = new Symbols();

	  AssemblyCode result = get(0, symbols);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length);
	  assert ("0000 FD3500             DEC  (IY+0)\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  e.printStackTrace();
	}
  }

  public void testDecIyPlusDisplacementBig() {
	try {
	  Byte[] bytes = { 0xFD - 256, 0x35, 0x80 - 256 };
	  setReader(new ReadFromArray(bytes));
	  Symbols symbols = new Symbols();

	  AssemblyCode result = get(0, symbols);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length);
	  assert ("0000 FD3580             DEC  (IY-128)\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  e.printStackTrace();
	}
  }

  @Test
  public void testLdIyPlusDisplacementByte() {
	try {
	  Byte[] bytes = { 0xFD - 256, 0x36, 0xFF - 256, 0xFF - 256 };
	  setReader(new ReadFromArray(bytes));
	  Symbols symbols = new Symbols();

	  AssemblyCode result = get(0, symbols);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length);
	  assert ("0000 FD36FFFF           LD   (IY-1),0xFF\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  e.printStackTrace();
	}
  }

  @Test
  public void testLdBRlcIxPlusDisplacement() {
	try {
	  Byte[] bytes = { 0xDD - 256, 0xCB - 256, 0x12, 0x00 };
	  setReader(new ReadFromArray(bytes));
	  Symbols symbols = new Symbols();

	  AssemblyCode result = get(0, symbols);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length);
	  assert ("0000 DDCB1200           LD   B,RLC (IX+18)\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  e.printStackTrace();
	}
  }

  @Test
  public void testLdCRlcIyPlusDisplacementSmall() {
	try {
	  Byte[] bytes = { 0xFD - 256, 0xCB - 256, 0x12, 0x01 };
	  setReader(new ReadFromArray(bytes));
	  Symbols symbols = new Symbols();

	  AssemblyCode result = get(0, symbols);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length);
	  assert ("0000 FDCB1201           LD   C,RLC (IY+18)\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  e.printStackTrace();
	}
  }

  @Test
  public void testLdCRlcIyPlusDisplacementBig() {
	try {
	  Byte[] bytes = { 0xFD - 256, 0xCB - 256, 0xFF - 256, 0x01 };
	  setReader(new ReadFromArray(bytes));
	  Symbols symbols = new Symbols();

	  AssemblyCode result = get(0, symbols);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length);
	  assert ("0000 FDCBFF01           LD   C,RLC (IY-1)\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  e.printStackTrace();
	}
  }

  @Test
  public void testLdBSet0IyPlusDisplacementBig() {
	try {
	  Byte[] bytes = { 0xFD - 256, 0xCB - 256, 0xFF - 256, 0x80 - 256 };
	  setReader(new ReadFromArray(bytes));
	  Symbols symbols = new Symbols();

	  AssemblyCode result = get(0, symbols);
	  assert (result != null);
	  assert (result.getBytes().size() == bytes.length);
	  assert ("0000 FDCBFF80           LD   B,RES 0,(IY-1)\n".equals(result.toString()));
	} catch (IOException e) {
	  e.printStackTrace();
	} catch (IllegalOpcodeException e) {
	  e.printStackTrace();
	}
  }

  @Test
  public void testIllegalOpcode() {
	try {
	  Byte[] bytes = { 0xDD - 256, 0xDC - 256, 0x34, 0x12 };
	  setReader(new ReadFromArray(bytes));
	  Symbols symbols = new Symbols();

	  AssemblyCode result = get(0, symbols);
	  assert (result == null);
	} catch (IOException e) {
	  e.printStackTrace();
	  assert (false);
	} catch (IllegalOpcodeException e) {
	  assert ("Unsupported code 0xDDDC at address 0x0000\n".equals(e.getMessage()));
	}
  }

}
