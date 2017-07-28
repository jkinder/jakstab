package org.jakstab.disasm.x86;

import capstone.Capstone;
import capstone.X86;
import capstone.X86_const;
import org.jakstab.asm.DataType;
import org.jakstab.asm.Immediate;
import org.jakstab.asm.Instruction;
import org.jakstab.asm.Operand;
import org.jakstab.asm.x86.X86InstructionFactory;
import org.jakstab.asm.x86.X86MemoryOperand;
import org.jakstab.asm.x86.X86Register;
import org.jakstab.asm.x86.X86SegmentRegister;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created by dmium on 7/28/17.
 */
public class CapstoneParser {
    public static Instruction getInstruction(Capstone.CsInsn csinstr, int prefixes, X86InstructionFactory factory) {
        switch (((X86.OpInfo) (csinstr.operands)).op.length) {
            //case 0:
            //    return factory.newGeneralInstruction(csinstr.mnemonic, csinstr.size, prefixes);
            case 1:
                return factory.newGeneralInstruction(csinstr.mnemonic, getOperand(((X86.OpInfo) (csinstr.operands)).op[0], csinstr), csinstr.size, prefixes);
            case 2:
                return factory.newGeneralInstruction(csinstr.mnemonic, getOperand(((X86.OpInfo) (csinstr.operands)).op[1], csinstr), getOperand(((X86.OpInfo) (csinstr.operands)).op[0], csinstr), csinstr.size, prefixes);
            case 3:
                return factory.newGeneralInstruction(csinstr.mnemonic, getOperand(((X86.OpInfo) (csinstr.operands)).op[2], csinstr), getOperand(((X86.OpInfo) (csinstr.operands)).op[1], csinstr), getOperand(((X86.OpInfo) (csinstr.operands)).op[0], csinstr), csinstr.size, prefixes);
        }
        return null;
        //return factory.newGeneralInstruction(csinstr.mnemonic, , csinstr.size, prefixes);
    }

    private static Operand getOperand(X86.Operand op, Capstone.CsInsn csinstr) {
        switch (op.type) {
            case X86_const.X86_OP_REG:
                return getRegister(op.value.reg, csinstr);
            case X86_const.X86_OP_IMM:
                if (op.avx_bcast == 0)
                    return getImmediate(op.value.imm, op.size);
                else
                    return getFPImmidiate(op.value.imm, op.size);
            case X86_const.X86_OP_MEM:
                return getMemOp(op, csinstr);
            /*case X86_const.X86_OP_FP:
                return getFPImmidiate(op.value.imm, op.size);*/
            //case X86_const.X86_OP_INVALID:
            default:
                throw new NotImplementedException();
                //return null;
        }
    }

    private static Immediate getImmediate(long imm, int size) {
        return new Immediate(getNumber(imm, getDataType(size, false)), getDataType(size, false));
        /*
        switch (size) {
            case 1:
                return new Immediate(imm, DataType.INT8);
            case 2:
                return new Immediate(imm, DataType.INT16);
            case 4://Size in Bytes
                return new Immediate(imm, DataType.INT32);
            break;
            case 8:
                return new Immediate(imm, DataType.INT64);
            break;
            */
    }

    private static X86Register getRegister(int reg, Capstone.CsInsn csinstr) {
        return new X86Register(reg, csinstr.regName(reg));
    }

    private static X86MemoryOperand getMemOp(X86.Operand op, Capstone.CsInsn csinstr) {
        return new X86MemoryOperand(getDataType(op.size, false), new X86SegmentRegister(getRegister(op.value.mem.segment, csinstr)), getRegister(op.value.mem.base, csinstr), getRegister(op.value.mem.index, csinstr), op.value.mem.disp, op.value.mem.scale);
    }

    private static Immediate getFPImmidiate(long imm, int size) {
        return new Immediate(imm, getDataType(size, true));
    }

    private static DataType getDataType(int size, boolean fp) {
        if (fp) {
            switch (size){
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
    private static Number getNumber(long val, DataType type){
        switch (type){
            case INT8:
                return (byte)val;
            case INT16:
                return (short)val;
            case INT32:
                return (int)val;
            case INT64:
                return (long)val;
            default:
                return val;
        }
        //return null;
    }
}
