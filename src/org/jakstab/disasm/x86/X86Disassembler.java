/*
 * Copyright 2002-2003 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *  
 */

/*
 * JDoc annotations are Copyright 2007-2015 Johannes Kinder
 */

/* 
 * Original code for this class taken from the Java HotSpot VM. 
 * Modified for use with the Jakstab project. All modifications 
 * Copyright 2007-2015 Johannes Kinder <jk@jakstab.org>
 */

package org.jakstab.disasm.x86;
import org.jakstab.asm.*;
import org.jakstab.asm.x86.*;
import org.jakstab.util.BinaryInputBuffer;
import org.jakstab.util.Logger;
import org.jakstab.disasm.Disassembler;
import capstone.Capstone;

public class X86Disassembler implements Disassembler, X86Opcodes {
    private final static Logger logger = Logger.getLogger(X86Disassembler.class);

    protected final X86InstructionFactory factory;
    protected final BinaryInputBuffer code;
    private int byteIndex;
    private Capstone cs;

    private X86Disassembler(BinaryInputBuffer code, X86InstructionFactory factory) {
        this.code = code;
        this.factory = factory;
        cs = new Capstone(Capstone.CS_ARCH_X86, Capstone.CS_MODE_32);
        cs.setSyntax(cs.CS_OPT_SYNTAX_ATT);
        cs.setDetail(cs.CS_OPT_ON);
    }

    /**
     * Creates a new disassembler working on the given bytearray.
     *
     * @param code Byte array of code to be disassembled.
     */
    public X86Disassembler(BinaryInputBuffer code) {
        this(code, new X86InstructionFactoryImpl());
    }

    @Override
    public final Instruction decodeInstruction(long index, long addr) {
        Instruction instr;
        byteIndex = (int) index; // For 64bit systems, this needs to be fixed //TODO-Dom test replacing this

        int prefixes ;
        int instrStartIndex = byteIndex;
        Capstone.CsInsn csinstr;
        try {
            prefixes = getPrefixes();
            byte[] insbytes = new byte[15 + (byteIndex - instrStartIndex)];
            for (int i = instrStartIndex; i < 15 + byteIndex; i++) {// TODO Dom - This is a (mostly) arbitrary number should probably calculate this.
                insbytes[i - instrStartIndex] = (byte) InstructionDecoder.readByte(code, i);
            }
            csinstr = cs.disasm(insbytes, addr, 1)[0];
           //logger.warn(csinstr.address + " " + csinstr.mnemonic + " " + csinstr.opStr);
            instr = X86CapstoneParser.getInstruction(csinstr, prefixes, factory);
            byteIndex = csinstr.size + instrStartIndex;
        } catch (Exception exp) {
            logger.error("Error during disassembly:", exp);
            if (logger.isInfoEnabled())
                exp.printStackTrace();
            return null;
        }
        ((X86Instruction)instr).checkLock();
        return instr;
    }

    private final int getPrefixes() {

        int prefixes = 0;
        boolean isPrefix = true;

        while (isPrefix) {
            int prefixByte = InstructionDecoder.readByte(code, byteIndex);

            switch (prefixByte) {
                case 0xf3:
                    prefixes |= PREFIX_REPZ;
                    break;
                case 0xf2:
                    prefixes |= PREFIX_REPNZ;
                    break;
                case 0xf0:
                    prefixes |= PREFIX_LOCK;
                    break;
                case 0x2e:
                    prefixes |= PREFIX_CS;
                    break;
                case 0x36:
                    prefixes |= PREFIX_SS;
                    break;
                case 0x3e:
                    prefixes |= PREFIX_DS;
                    break;
                case 0x26:
                    prefixes |= PREFIX_ES;
                    break;
                case 0x64:
                    prefixes |= PREFIX_FS;
                    break;
                case 0x65:
                    prefixes |= PREFIX_GS;
                    break;
                case 0x66:
                    prefixes |= PREFIX_DATA;
                    break;
                case 0x67:
                    prefixes |= PREFIX_ADR;
                    break;
/*	
 * This had to be removed, since FWAIT is really an instruction and can appear on its own. 
 * Mnemonics like FSTSW are only macros, anyway, and a jump to the FNSTSW part after 0x9B is ok.
 * To make this really work, we would need to check for an earlier FWAIT instruction while parsing
 * instructions like FNSTSW.			
			case 0x9b:
				prefixes |= PREFIX_FWAIT;
				break;
*/
                default:
                    isPrefix = false;
            }
            if (isPrefix)
                byteIndex++;
        }
        return prefixes;
    }
}
