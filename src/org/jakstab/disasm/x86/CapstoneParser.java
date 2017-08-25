package org.jakstab.disasm.x86;

import capstone.Capstone;
import capstone.X86;
import capstone.X86_const;
import org.jakstab.asm.DataType;
import org.jakstab.asm.Immediate;
import org.jakstab.asm.Instruction;
import org.jakstab.asm.Operand;
import org.jakstab.asm.x86.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created by dmium on 7/28/17.
 */
public class CapstoneParser {
    public static Instruction getInstruction(Capstone.CsInsn csinstr, int prefixes, X86InstructionFactory factory) {
        //TODO Dom-Fix mem segment register default values.
        if (csinstr.group(X86_const.X86_GRP_CALL))
            return getCallInstruction(csinstr, prefixes, factory);
        if (csinstr.group(X86_const.X86_GRP_RET)) {
            if (csinstr.opCount(X86_const.X86_OP_IMM) != 0)
                return factory.newRetInstruction(csinstr.mnemonic, (Immediate) getOperand(((X86.OpInfo) (csinstr.operands)).op[0], csinstr), csinstr.size, prefixes);
            return factory.newRetInstruction(csinstr.mnemonic, csinstr.size, prefixes);
        }
        if (csinstr.group(X86_const.X86_GRP_JUMP)) {
            if (csinstr.mnemonic.startsWith("jmp")) {
                if(csinstr.mnemonic.endsWith("l"))
                    return factory.newJmpInstruction(csinstr.mnemonic, getOperand(((X86.OpInfo) (csinstr.operands)).op[0], csinstr), csinstr.size, prefixes);
                return factory.newJmpInstruction(csinstr.mnemonic, new X86PCRelativeAddress((((X86.OpInfo)(csinstr.operands)).op[0].value.imm - csinstr.size) - csinstr.address) /*getOperand(((X86.OpInfo) (csinstr.operands)).op[0], csinstr)*/, csinstr.size, prefixes);
            } else {
                return factory.newCondJmpInstruction(csinstr.mnemonic, new X86PCRelativeAddress((((X86.OpInfo) (csinstr.operands)).op[0].value.imm - csinstr.size) - csinstr.address), csinstr.size, prefixes);
            }
        }
        switch (((X86.OpInfo) (csinstr.operands)).op.length) {
            case 0://TODO-Dom Check this isn't evil/add new factory method
                return factory.newGeneralInstruction(csinstr.mnemonic, null, csinstr.size, prefixes);
            case 1:
                return factory.newGeneralInstruction(csinstr.mnemonic, getOperand(((X86.OpInfo) (csinstr.operands)).op[0], csinstr), csinstr.size, prefixes);
            case 2:
                return factory.newGeneralInstruction(csinstr.mnemonic, getOperand(((X86.OpInfo) (csinstr.operands)).op[1], csinstr), getOperand(((X86.OpInfo) (csinstr.operands)).op[0], csinstr), csinstr.size, prefixes);
            case 3:
                return factory.newGeneralInstruction(csinstr.mnemonic, getOperand(((X86.OpInfo) (csinstr.operands)).op[2], csinstr), getOperand(((X86.OpInfo) (csinstr.operands)).op[1], csinstr), getOperand(((X86.OpInfo) (csinstr.operands)).op[0], csinstr), csinstr.size, prefixes);
        }
        return null;
    }

    private static Instruction getCallInstruction(Capstone.CsInsn csinstr, int prefixes, X86InstructionFactory factory) {
        //TODO-Dom Correctly differentiate between relative and absolute
        if (getOperand(((X86.OpInfo)(csinstr.operands)).op[0], csinstr) instanceof Immediate)
            return factory.newCallInstruction(csinstr.mnemonic, new X86PCRelativeAddress((((X86.OpInfo) (csinstr.operands)).op[0].value.imm - csinstr.size) - csinstr.address), csinstr.size, prefixes);
            //return factory.newCallInstruction(csinstr.mnemonic, new X86AbsoluteAddress(((X86.OpInfo)(csinstr.operands)).op[0].value.imm), csinstr.size, prefixes);
        //throw new NotImplementedException();
        return factory.newCallInstruction(csinstr.mnemonic, getOperand(((X86.OpInfo)(csinstr.operands)).op[0], csinstr), csinstr.size, prefixes);
    }

    private static Operand getOperand(X86.Operand op, Capstone.CsInsn csinstr) {
        switch (op.type) {
            case X86_const.X86_OP_REG:
                return getRegister(op.value.reg, csinstr);
            case X86_const.X86_OP_IMM:
                return getImmediate(op.value.imm, op.size);
            case X86_const.X86_OP_MEM:
                return getMemOp(op, csinstr);
            case X86_const.X86_OP_FP:
                //TODO-Dom Not sure if this works so throwing exception until tested
                throw new NotImplementedException();
                //return getFPImmidiate(op.value.fp, op.size);
            //case X86_const.X86_OP_INVALID:
            default:
                throw new NotImplementedException();
        }
    }

    private static Immediate getImmediate(long imm, int size) {
        return new Immediate(getNumber(imm, getDataType(size, false)), getDataType(size, false));
    }

    private static X86Register getRegister(int reg, Capstone.CsInsn csinstr) {
        return new X86Register(reg, "%" + csinstr.regName(reg));
    }

    private static X86MemoryOperand getMemOp(X86.Operand op, Capstone.CsInsn csinstr) {
        X86SegmentRegister sr = null;
        X86Register ir = null;
        X86Register br = null;
        if (op.value.mem.segment != 0)
            sr = new X86SegmentRegister(op.value.mem.segment, csinstr.regName(op.value.mem.segment));
        if (op.value.mem.index != 0)
            ir = getRegister(op.value.mem.index, csinstr);
        if (op.value.mem.base != 0)
            ir = getRegister(op.value.mem.base, csinstr);
        return new X86MemoryOperand(getDataType(op.size, false), sr, br, ir, op.value.mem.disp, op.value.mem.scale);
    }

    private static Immediate getFPImmidiate(double imm, int size) {
        return new Immediate(getNumber(imm, getDataType(size,false)), getDataType(size, true));
    }

    private static DataType getDataType(int size, boolean fp) {
        if (fp) {
            switch (size) {//TODO-Dom careful with this. may break with strings. Also boolean is not the way to go.
                case 4:
                    return DataType.FL_SINGLE;
                case 8:
                    return DataType.FL_DOUBLE;
                case 10:
                    return DataType.FL_EXT_DOUBLE;
                case 16:
                    return DataType.FL_QUAD;
            }
            return DataType.FL_DOUBLE;
        } else {
            switch (size) {
                case 1:
                    return DataType.INT8;
                case 2:
                    return DataType.INT16;
                case 4:
                    return DataType.INT32;
                case 8:
                    return DataType.INT64;
                case 16:
                    return DataType.INT128;
            }
        }
        return DataType.STRING;
        //return null;
    }

    private static Number getNumber(long val, DataType type) {
        switch (type) {
            case INT8:
                return (byte) val;
            case INT16:
                return (short) val;
            case INT32:
                return (int) val;
            case INT64:
                return (long) val;
            default:
                return val;
        }
        //return null;
    }

    private static Number getNumber(double val, DataType type){
        switch (type){
            case FL_SINGLE:
                return (float)val;
            case FL_EXT_DOUBLE:
            case FL_QUAD:
                throw new NotImplementedException();
            //case FL_DOUBLE:
            default:
                return val;

        }
    }
/*
    private static X86SegmentRegister getSegmentRegister(int regID) {
*//*switch (regID){

        }*//*
        return null;//TODO-Dom Implement this?
    }*/
}
