// ASM: a very small and fast Java bytecode manipulation framework
// Copyright (c) 2000-2011 INRIA, France Telecom
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
// 1. Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
// 3. Neither the name of the copyright holders nor the names of its
//    contributors may be used to endorse or promote products derived from
//    this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
// THE POSSIBILITY OF SUCH DAMAGE.
package org.objectweb.asm;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Handler tests.
 *
 * @author Eric Bruneton
 */
public class HandlerTest {

  @Test
  public void testConstructor() {
    Label startPc = new Label();
    Label endPc = new Label();
    Label handlerPc = new Label();
    int catchType = 123;
    String catchDescriptor = "123";
    Handler handler = new Handler(startPc, endPc, handlerPc, catchType, catchDescriptor);
    assertEquals(startPc, handler.startPc);
    assertEquals(endPc, handler.endPc);
    assertEquals(handlerPc, handler.handlerPc);
    assertEquals(catchType, handler.catchType);
    assertEquals(catchDescriptor, handler.catchTypeDescriptor);
  }

  @Test
  public void testCopyConstructor() {
    Label startPc = new Label();
    Label endPc = new Label();
    Label handlerPc = new Label();
    int catchType = 123;
    String catchDescriptor = "123";
    Handler handler = new Handler(startPc, endPc, handlerPc, catchType, catchDescriptor);

    Label newStartPc = new Label();
    Label newEndPc = new Label();
    handler = new Handler(handler, newStartPc, newEndPc);
    assertEquals(newStartPc, handler.startPc);
    assertEquals(newEndPc, handler.endPc);
    assertEquals(handlerPc, handler.handlerPc);
    assertEquals(catchType, handler.catchType);
    assertEquals(catchDescriptor, handler.catchTypeDescriptor);
  }

  @Test
  public void testRemoveRange() {
    Handler handler = newHandler(10, 20);
    assertEquals(null, Handler.removeRange(null, newLabel(0), newLabel(10)));
    assertEquals(handler, Handler.removeRange(handler, newLabel(0), newLabel(10)));
    assertEquals(handler, Handler.removeRange(handler, newLabel(20), newLabel(30)));
    assertEquals(handler, Handler.removeRange(handler, newLabel(20), null));
    assertEquals(null, Handler.removeRange(handler, newLabel(0), newLabel(30)));

    Handler handler1 = Handler.removeRange(handler, newLabel(0), newLabel(15));
    assertEquals(15, handler1.startPc.bytecodeOffset);
    assertEquals(20, handler1.endPc.bytecodeOffset);
    assertEquals(null, handler1.nextHandler);

    Handler handler2 = Handler.removeRange(handler, newLabel(15), newLabel(30));
    assertEquals(10, handler2.startPc.bytecodeOffset);
    assertEquals(15, handler2.endPc.bytecodeOffset);
    assertEquals(null, handler2.nextHandler);

    Handler handler3 = Handler.removeRange(handler, newLabel(13), newLabel(17));
    assertEquals(10, handler3.startPc.bytecodeOffset);
    assertEquals(13, handler3.endPc.bytecodeOffset);
    assertEquals(17, handler3.nextHandler.startPc.bytecodeOffset);
    assertEquals(20, handler3.nextHandler.endPc.bytecodeOffset);
    assertEquals(null, handler3.nextHandler.nextHandler);
  }

  @Test
  public void testExceptionTable() {
    assertEquals(0, Handler.getExceptionTableLength(null));

    Handler handler = newHandler(10, 20);
    assertEquals(1, Handler.getExceptionTableLength(handler));

    Handler handlerList = Handler.removeRange(handler, newLabel(13), newLabel(17));
    assertEquals(2, Handler.getExceptionTableLength(handlerList));
    assertEquals(18, Handler.getExceptionTableSize(handlerList));

    ByteVector byteVector = new ByteVector();
    Handler.putExceptionTable(handlerList, byteVector);
    assertEquals(18, byteVector.length);
  }

  private Handler newHandler(final int startPc, final int endPc) {
    return new Handler(newLabel(startPc), newLabel(endPc), newLabel(0), 0, "");
  }

  private Label newLabel(final int bytecodeOffset) {
    Label label = new Label();
    label.bytecodeOffset = bytecodeOffset;
    return label;
  }
}
