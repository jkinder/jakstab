// $ANTLR : "SSL.g" -> "SSLPreprocessor.java"$

	package org.jakstab.ssl.parser;

import antlr.TreeParser;
import antlr.Token;
import antlr.collections.AST;
import antlr.RecognitionException;
import antlr.ANTLRException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.collections.impl.BitSet;
import antlr.ASTPair;
import antlr.collections.impl.ASTArray;

	import java.util.*;
	import org.jakstab.rtl.*;
	import org.jakstab.rtl.expressions.*;
	import org.jakstab.rtl.statements.*;

	@SuppressWarnings("all")


public class SSLPreprocessor extends antlr.TreeParser       implements SSLParserTokenTypes
 {

	private Map<String,Long> constants = new HashMap<String,Long>();
	private Map<String,List<AST>> tables = new HashMap<String,List<AST>>();
	private Map<String,SSLFunction> functions = new HashMap<String,SSLFunction>();
	private Map<String,SSLFunction> instructions = new TreeMap<String,SSLFunction>();
	private Stack<Map<String,AST>> locals = new Stack<Map<String,AST>>();
	private SetOfVariables registers = new SetOfVariables(); 

	public SetOfVariables getRegisters() { return registers; }	
	//public Map<String,SSLFunction> getFunctions() { return functions; }
	public Map<String,SSLFunction> getInstructions() { return instructions; }

	public Map<String,List<AST>> getTables() { return tables; }
	
public SSLPreprocessor() {
	tokenNames = _tokenNames;
}

	public final void start(AST _t) throws RecognitionException {
		
		AST start_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST start_AST = null;
		
		specification(_t);
		_t = _retTree;
		astFactory.addASTChild(currentAST, returnAST);
		start_AST = (AST)currentAST.root;
		returnAST = start_AST;
		_retTree = _t;
	}
	
	public final void specification(AST _t) throws RecognitionException {
		
		AST specification_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST specification_AST = null;
		
		AST __t2560 = _t;
		AST tmp1_AST = null;
		AST tmp1_AST_in = null;
		tmp1_AST = astFactory.create((AST)_t);
		tmp1_AST_in = (AST)_t;
		astFactory.addASTChild(currentAST, tmp1_AST);
		ASTPair __currentAST2560 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,SEMI);
		_t = _t.getFirstChild();
		{
		_loop2562:
		do {
			if (_t==null) _t=ASTNULL;
			if ((_tokenSet_0.member(_t.getType()))) {
				part(_t);
				_t = _retTree;
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop2562;
			}
			
		} while (true);
		}
		currentAST = __currentAST2560;
		_t = __t2560;
		_t = _t.getNextSibling();
		specification_AST = (AST)currentAST.root;
		returnAST = specification_AST;
		_retTree = _t;
	}
	
	public final void part(AST _t) throws RecognitionException {
		
		AST part_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST part_AST = null;
		AST cn = null;
		AST cn_AST = null;
		AST tn = null;
		AST tn_AST = null;
		AST fn = null;
		AST fn_AST = null;
		AST fb = null;
		AST fb_AST = null;
		AST ib = null;
		AST ib_AST = null;
		
				long lv=0; 
				List<AST> tv; 
				List<String> pl; 
				List<SSLInstructionName> inam; 
			
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case CONSTANT:
		{
			AST __t2564 = _t;
			AST tmp2_AST = null;
			AST tmp2_AST_in = null;
			tmp2_AST = astFactory.create((AST)_t);
			tmp2_AST_in = (AST)_t;
			ASTPair __currentAST2564 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,CONSTANT);
			_t = _t.getFirstChild();
			cn = (AST)_t;
			AST cn_AST_in = null;
			cn_AST = astFactory.create(cn);
			match(_t,NAME);
			_t = _t.getNextSibling();
			lv=const_expr(_t);
			_t = _retTree;
			currentAST = __currentAST2564;
			_t = __t2564;
			_t = _t.getNextSibling();
			
						constants.put(cn.getText(), Long.valueOf(lv));
					
			break;
		}
		case REGDECL:
		{
			AST __t2565 = _t;
			AST tmp3_AST = null;
			AST tmp3_AST_in = null;
			tmp3_AST = astFactory.create((AST)_t);
			tmp3_AST_in = (AST)_t;
			ASTPair __currentAST2565 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,REGDECL);
			_t = _t.getFirstChild();
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case LITERAL_INTEGER:
			{
				AST tmp4_AST_in = null;
				match(_t,LITERAL_INTEGER);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_FLOAT:
			{
				AST tmp5_AST_in = null;
				match(_t,LITERAL_FLOAT);
				_t = _t.getNextSibling();
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			{
			_loop2568:
			do {
				if (_t==null) _t=ASTNULL;
				if (((_t.getType() >= REG_ID && _t.getType() <= LSQUARE))) {
					register_decl(_t);
					_t = _retTree;
				}
				else {
					break _loop2568;
				}
				
			} while (true);
			}
			currentAST = __currentAST2565;
			_t = __t2565;
			_t = _t.getNextSibling();
			break;
		}
		case TABLE:
		{
			AST __t2569 = _t;
			AST tmp6_AST = null;
			AST tmp6_AST_in = null;
			tmp6_AST = astFactory.create((AST)_t);
			tmp6_AST_in = (AST)_t;
			ASTPair __currentAST2569 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,TABLE);
			_t = _t.getFirstChild();
			tn = (AST)_t;
			AST tn_AST_in = null;
			tn_AST = astFactory.create(tn);
			match(_t,NAME);
			_t = _t.getNextSibling();
			tv=table_expr(_t);
			_t = _retTree;
			currentAST = __currentAST2569;
			_t = __t2569;
			_t = _t.getNextSibling();
			
						tables.put(tn.getText(), tv); 
					
			break;
		}
		case FUNCTION:
		{
			AST __t2570 = _t;
			AST tmp7_AST = null;
			AST tmp7_AST_in = null;
			tmp7_AST = astFactory.create((AST)_t);
			tmp7_AST_in = (AST)_t;
			ASTPair __currentAST2570 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,FUNCTION);
			_t = _t.getFirstChild();
			fn = (AST)_t;
			AST fn_AST_in = null;
			fn_AST = astFactory.create(fn);
			match(_t,NAME);
			_t = _t.getNextSibling();
			pl=param_list(_t);
			_t = _retTree;
			fb = (AST)_t;
			AST fb_AST_in = null;
			fb_AST = astFactory.create(fb);
			match(_t,RTL);
			_t = _t.getNextSibling();
			currentAST = __currentAST2570;
			_t = __t2570;
			_t = _t.getNextSibling();
			functions.put(fn.getText(), new SSLFunction(fn.getText(), pl, astFactory.dupTree(fb)));
			break;
		}
		case INSTR:
		{
			AST __t2571 = _t;
			AST tmp8_AST = null;
			AST tmp8_AST_in = null;
			tmp8_AST = astFactory.create((AST)_t);
			tmp8_AST_in = (AST)_t;
			ASTPair __currentAST2571 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,INSTR);
			_t = _t.getFirstChild();
			inam=instr_name(_t);
			_t = _retTree;
			pl=param_list(_t);
			_t = _retTree;
			ib = (AST)_t;
			AST ib_AST_in = null;
			ib_AST = astFactory.create(ib);
			match(_t,RTL);
			_t = _t.getNextSibling();
			currentAST = __currentAST2571;
			_t = __t2571;
			_t = _t.getNextSibling();
			
						for (SSLInstructionName in : inam) {
			if (in.getVarMap() != null) 
				locals.push(in.getVarMap()); 
			else 
				locals.push(new HashMap<String,AST>());
			rtl_expand(astFactory.dupTree(ib));
			locals.pop();
			AST rtl = getAST();
			
			if (instructions.containsKey(in.getName())) {
			SSLFunction oldIns = instructions.get(in.getName());
			/*                    if (oldpl != old_ip: TODO: JK - Check parameter list
			throw new SemanticException(#ib, "parameter list of '%s' changed" % n)*/
			if (rtl.getFirstChild() != null)
			oldIns.getAST().addChild(rtl.getFirstChild());
			} else
			instructions.put(in.getName(), new SSLFunction(in.getName(), pl, rtl));
						}
					
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		returnAST = part_AST;
		_retTree = _t;
	}
	
	public final long  const_expr(AST _t) throws RecognitionException {
		long v=0;
		
		AST const_expr_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST const_expr_AST = null;
		AST n = null;
		AST n_AST = null;
		long l,r;
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case NUM:
		{
			n = (AST)_t;
			AST n_AST_in = null;
			n_AST = astFactory.create(n);
			match(_t,NUM);
			_t = _t.getNextSibling();
			v = Long.parseLong(n.getText());
			break;
		}
		case PLUS:
		{
			AST __t2579 = _t;
			AST tmp9_AST = null;
			AST tmp9_AST_in = null;
			tmp9_AST = astFactory.create((AST)_t);
			tmp9_AST_in = (AST)_t;
			ASTPair __currentAST2579 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,PLUS);
			_t = _t.getFirstChild();
			l=const_expr(_t);
			_t = _retTree;
			r=const_expr(_t);
			_t = _retTree;
			currentAST = __currentAST2579;
			_t = __t2579;
			_t = _t.getNextSibling();
			v = l + r;
			break;
		}
		case MINUS:
		{
			AST __t2580 = _t;
			AST tmp10_AST = null;
			AST tmp10_AST_in = null;
			tmp10_AST = astFactory.create((AST)_t);
			tmp10_AST_in = (AST)_t;
			ASTPair __currentAST2580 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,MINUS);
			_t = _t.getFirstChild();
			l=const_expr(_t);
			_t = _retTree;
			r=const_expr(_t);
			_t = _retTree;
			currentAST = __currentAST2580;
			_t = __t2580;
			_t = _t.getNextSibling();
			v = l - r;
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		returnAST = const_expr_AST;
		_retTree = _t;
		return v;
	}
	
	public final void register_decl(AST _t) throws RecognitionException {
		
		AST register_decl_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST register_decl_AST = null;
		AST r1 = null;
		AST r1_AST = null;
		AST r2 = null;
		AST r2_AST = null;
		AST coveredRegFrom = null;
		AST coveredRegFrom_AST = null;
		AST coveredRegTo = null;
		AST coveredRegTo_AST = null;
		AST sharedReg = null;
		AST sharedReg_AST = null;
		
				int bitWidth; int regIdFrom; int regIdTo; int shareFrom = -1; int shareTo = -1;
				List<String> regList;
			
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case INDEX:
		{
			AST tmp11_AST = null;
			AST tmp11_AST_in = null;
			tmp11_AST = astFactory.create((AST)_t);
			tmp11_AST_in = (AST)_t;
			match(_t,INDEX);
			_t = _t.getNextSibling();
			r1 = (AST)_t;
			AST r1_AST_in = null;
			r1_AST = astFactory.create(r1);
			match(_t,REG_ID);
			_t = _t.getNextSibling();
			regIdFrom=intValue(_t);
			_t = _retTree;
			
							registers.add((RTLVariable)ExpressionFactory.createRegisterVariable(r1.getText(), RTLVariable.UNKNOWN_BITWIDTH));
					
			break;
		}
		case REG_ID:
		{
			r2 = (AST)_t;
			AST r2_AST_in = null;
			r2_AST = astFactory.create(r2);
			match(_t,REG_ID);
			_t = _t.getNextSibling();
			AST tmp12_AST = null;
			AST tmp12_AST_in = null;
			tmp12_AST = astFactory.create((AST)_t);
			tmp12_AST_in = (AST)_t;
			match(_t,LSQUARE);
			_t = _t.getNextSibling();
			bitWidth=intValue(_t);
			_t = _retTree;
			AST tmp13_AST = null;
			AST tmp13_AST_in = null;
			tmp13_AST = astFactory.create((AST)_t);
			tmp13_AST_in = (AST)_t;
			match(_t,RSQUARE);
			_t = _t.getNextSibling();
			AST tmp14_AST = null;
			AST tmp14_AST_in = null;
			tmp14_AST = astFactory.create((AST)_t);
			tmp14_AST_in = (AST)_t;
			match(_t,INDEX);
			_t = _t.getNextSibling();
			regIdFrom=intValue(_t);
			_t = _retTree;
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case LITERAL_COVERS:
			{
				AST tmp15_AST_in = null;
				match(_t,LITERAL_COVERS);
				_t = _t.getNextSibling();
				coveredRegFrom = (AST)_t;
				AST coveredRegFrom_AST_in = null;
				coveredRegFrom_AST = astFactory.create(coveredRegFrom);
				match(_t,REG_ID);
				_t = _t.getNextSibling();
				AST tmp16_AST = null;
				AST tmp16_AST_in = null;
				tmp16_AST = astFactory.create((AST)_t);
				tmp16_AST_in = (AST)_t;
				match(_t,TO);
				_t = _t.getNextSibling();
				coveredRegTo = (AST)_t;
				AST coveredRegTo_AST_in = null;
				coveredRegTo_AST = astFactory.create(coveredRegTo);
				match(_t,REG_ID);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_SHARES:
			{
				AST tmp17_AST_in = null;
				match(_t,LITERAL_SHARES);
				_t = _t.getNextSibling();
				sharedReg = (AST)_t;
				AST sharedReg_AST_in = null;
				sharedReg_AST = astFactory.create(sharedReg);
				match(_t,REG_ID);
				_t = _t.getNextSibling();
				AST tmp18_AST = null;
				AST tmp18_AST_in = null;
				tmp18_AST = astFactory.create((AST)_t);
				tmp18_AST_in = (AST)_t;
				match(_t,AT);
				_t = _t.getNextSibling();
				AST tmp19_AST = null;
				AST tmp19_AST_in = null;
				tmp19_AST = astFactory.create((AST)_t);
				tmp19_AST_in = (AST)_t;
				match(_t,LSQUARE);
				_t = _t.getNextSibling();
				shareFrom=intValue(_t);
				_t = _retTree;
				AST tmp20_AST = null;
				AST tmp20_AST_in = null;
				tmp20_AST = astFactory.create((AST)_t);
				tmp20_AST_in = (AST)_t;
				match(_t,TO);
				_t = _t.getNextSibling();
				shareTo=intValue(_t);
				_t = _retTree;
				AST tmp21_AST = null;
				AST tmp21_AST_in = null;
				tmp21_AST = astFactory.create((AST)_t);
				tmp21_AST_in = (AST)_t;
				match(_t,RSQUARE);
				_t = _t.getNextSibling();
				break;
			}
			case 3:
			case REG_ID:
			case INDEX:
			case LSQUARE:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			
							if (coveredRegFrom != null) 
								throw new RuntimeException("COVERS not yet supported!");
							if (sharedReg != null) {
								ExpressionFactory.createSharedRegisterVariable(r2.getText(), sharedReg.getText(), shareFrom, shareTo);
							} else {
								registers.add((RTLVariable)ExpressionFactory.createRegisterVariable(r2.getText(), bitWidth));
							}
						
			break;
		}
		case LSQUARE:
		{
			AST tmp22_AST = null;
			AST tmp22_AST_in = null;
			tmp22_AST = astFactory.create((AST)_t);
			tmp22_AST_in = (AST)_t;
			match(_t,LSQUARE);
			_t = _t.getNextSibling();
			regList=register_list(_t);
			_t = _retTree;
			AST tmp23_AST = null;
			AST tmp23_AST_in = null;
			tmp23_AST = astFactory.create((AST)_t);
			tmp23_AST_in = (AST)_t;
			match(_t,RSQUARE);
			_t = _t.getNextSibling();
			AST tmp24_AST = null;
			AST tmp24_AST_in = null;
			tmp24_AST = astFactory.create((AST)_t);
			tmp24_AST_in = (AST)_t;
			match(_t,LSQUARE);
			_t = _t.getNextSibling();
			bitWidth=intValue(_t);
			_t = _retTree;
			AST tmp25_AST = null;
			AST tmp25_AST_in = null;
			tmp25_AST = astFactory.create((AST)_t);
			tmp25_AST_in = (AST)_t;
			match(_t,RSQUARE);
			_t = _t.getNextSibling();
			AST tmp26_AST = null;
			AST tmp26_AST_in = null;
			tmp26_AST = astFactory.create((AST)_t);
			tmp26_AST_in = (AST)_t;
			match(_t,INDEX);
			_t = _t.getNextSibling();
			regIdFrom=intValue(_t);
			_t = _retTree;
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case TO:
			{
				AST tmp27_AST = null;
				AST tmp27_AST_in = null;
				tmp27_AST = astFactory.create((AST)_t);
				tmp27_AST_in = (AST)_t;
				match(_t,TO);
				_t = _t.getNextSibling();
				regIdTo=intValue(_t);
				_t = _retTree;
				break;
			}
			case 3:
			case REG_ID:
			case INDEX:
			case LSQUARE:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			
						for (String regName : regList) {
							registers.add((RTLVariable)ExpressionFactory.createRegisterVariable(regName, bitWidth));
						}
					
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		returnAST = register_decl_AST;
		_retTree = _t;
	}
	
	public final List<AST>  table_expr(AST _t) throws RecognitionException {
		List<AST> res = null;
		
		AST table_expr_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST table_expr_AST = null;
		AST any = null;
		AST any_AST = null;
		AST n = null;
		AST n_AST = null;
		List<AST> h,t;
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case LCURLY:
		{
			AST __t2582 = _t;
			AST tmp28_AST = null;
			AST tmp28_AST_in = null;
			tmp28_AST = astFactory.create((AST)_t);
			tmp28_AST_in = (AST)_t;
			ASTPair __currentAST2582 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,LCURLY);
			_t = _t.getFirstChild();
			h=table_expr(_t);
			_t = _retTree;
			
				  		res = new LinkedList<AST>(h); /* Copy so we don't change the other table! */ 
				  	
			{
			_loop2584:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_tokenSet_1.member(_t.getType()))) {
					t=table_expr(_t);
					_t = _retTree;
					res.addAll(t);
				}
				else {
					break _loop2584;
				}
				
			} while (true);
			}
			currentAST = __currentAST2582;
			_t = __t2582;
			_t = _t.getNextSibling();
			break;
		}
		case CROSSP:
		{
			AST __t2585 = _t;
			AST tmp29_AST = null;
			AST tmp29_AST_in = null;
			tmp29_AST = astFactory.create((AST)_t);
			tmp29_AST_in = (AST)_t;
			ASTPair __currentAST2585 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,CROSSP);
			_t = _t.getFirstChild();
			h=table_expr(_t);
			_t = _retTree;
			res = h;
			{
			t=table_expr(_t);
			_t = _retTree;
			
						res = new LinkedList<AST>(); 
						for (AST tt : t) for (AST hh : h)
							res.add(astFactory.create(NAME, hh.getText() + tt.getText())); 
					
			}
			currentAST = __currentAST2585;
			_t = __t2585;
			_t = _t.getNextSibling();
			break;
		}
		case QUOTE:
		{
			AST __t2587 = _t;
			AST tmp30_AST = null;
			AST tmp30_AST_in = null;
			tmp30_AST = astFactory.create((AST)_t);
			tmp30_AST_in = (AST)_t;
			ASTPair __currentAST2587 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,QUOTE);
			_t = _t.getFirstChild();
			any = (AST)_t;
			AST any_AST_in = null;
			any_AST = astFactory.create(any);
			if ( _t==null ) throw new MismatchedTokenException();
			_t = _t.getNextSibling();
			currentAST = __currentAST2587;
			_t = __t2587;
			_t = _t.getNextSibling();
			res = new LinkedList<AST>(); res.add(astFactory.dupTree(any));
			break;
		}
		case NAME:
		{
			n = (AST)_t;
			AST n_AST_in = null;
			n_AST = astFactory.create(n);
			match(_t,NAME);
			_t = _t.getNextSibling();
			
					if (tables.containsKey(n.getText())) 
						res = tables.get(n.getText());
					else { res = new LinkedList<AST>(); res.add(n); 
						/*  lax specification of SSL seems to allow missing quotes? treat as string literal. 
						   throw new RecognitionException("Undefined table reference " + n.getText() + "!"); */ 
					}
				
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		returnAST = table_expr_AST;
		_retTree = _t;
		return res;
	}
	
	public final List<String>  param_list(AST _t) throws RecognitionException {
		List<String> res = null;
		
		AST param_list_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST param_list_AST = null;
		AST n = null;
		AST n_AST = null;
		
		AST __t2589 = _t;
		AST tmp31_AST = null;
		AST tmp31_AST_in = null;
		tmp31_AST = astFactory.create((AST)_t);
		tmp31_AST_in = (AST)_t;
		ASTPair __currentAST2589 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,COMMA);
		_t = _t.getFirstChild();
		res = new LinkedList<String>();
		{
		_loop2591:
		do {
			if (_t==null) _t=ASTNULL;
			if ((_t.getType()==NAME)) {
				n = (AST)_t;
				AST n_AST_in = null;
				n_AST = astFactory.create(n);
				match(_t,NAME);
				_t = _t.getNextSibling();
				res.add(n.getText());
			}
			else {
				break _loop2591;
			}
			
		} while (true);
		}
		currentAST = __currentAST2589;
		_t = __t2589;
		_t = _t.getNextSibling();
		returnAST = param_list_AST;
		_retTree = _t;
		return res;
	}
	
	public final List<SSLInstructionName>  instr_name(AST _t) throws RecognitionException {
		List<SSLInstructionName> res = null;;
		
		AST instr_name_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST instr_name_AST = null;
		List<SSLInstructionName> e;
		
		AST __t2593 = _t;
		AST tmp32_AST = null;
		AST tmp32_AST_in = null;
		tmp32_AST = astFactory.create((AST)_t);
		tmp32_AST_in = (AST)_t;
		ASTPair __currentAST2593 = currentAST.copy();
		currentAST.root = currentAST.child;
		currentAST.child = null;
		match(_t,INSTR_NAME);
		_t = _t.getFirstChild();
		res = new LinkedList<SSLInstructionName>();
		{
		_loop2595:
		do {
			if (_t==null) _t=ASTNULL;
			if ((_t.getType()==NAME||_t.getType()==LSQUARE||_t.getType()==DECOR)) {
				e=instr_name_elem(_t);
				_t = _retTree;
				
								// If this is the first element, set result to this element's return value e.
								if (res.size() == 0) 
									res = e;
								// Otherwise, do a cross product of the previous result with e
								else {
					List<SSLInstructionName> tmp = new LinkedList<SSLInstructionName>();
					            for (SSLInstructionName lhsIn : res) {
					            for (SSLInstructionName rhsIn : e) {
					        Map newMap = new HashMap();
					        if (lhsIn.getVarMap() != null) newMap.putAll(lhsIn.getVarMap());
					        if (rhsIn.getVarMap() != null) newMap.putAll(rhsIn.getVarMap());
					                        tmp.add(new SSLInstructionName(lhsIn.getName() + rhsIn.getName(), newMap));
					            }
					    } 
					res = tmp;
								}
							
			}
			else {
				break _loop2595;
			}
			
		} while (true);
		}
		currentAST = __currentAST2593;
		_t = __t2593;
		_t = _t.getNextSibling();
		returnAST = instr_name_AST;
		_retTree = _t;
		return res;
	}
	
	public final int  intValue(AST _t) throws RecognitionException {
		 int value = -1; ;
		
		AST intValue_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST intValue_AST = null;
		AST number = null;
		AST number_AST = null;
		
		number = (AST)_t;
		AST number_AST_in = null;
		number_AST = astFactory.create(number);
		astFactory.addASTChild(currentAST, number_AST);
		match(_t,NUM);
		_t = _t.getNextSibling();
		value = Integer.parseInt(number.getText());
		intValue_AST = (AST)currentAST.root;
		returnAST = intValue_AST;
		_retTree = _t;
		return value;
	}
	
	public final List<String>  register_list(AST _t) throws RecognitionException {
		List<String> res = new LinkedList<String>();
		
		AST register_list_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST register_list_AST = null;
		AST r = null;
		AST r_AST = null;
		AST rn = null;
		AST rn_AST = null;
		
		r = (AST)_t;
		AST r_AST_in = null;
		r_AST = astFactory.create(r);
		match(_t,REG_ID);
		_t = _t.getNextSibling();
		res.add(r.getText());
		{
		_loop2577:
		do {
			if (_t==null) _t=ASTNULL;
			if ((_t.getType()==REG_ID)) {
				rn = (AST)_t;
				AST rn_AST_in = null;
				rn_AST = astFactory.create(rn);
				match(_t,REG_ID);
				_t = _t.getNextSibling();
				res.add(rn.getText());
			}
			else {
				break _loop2577;
			}
			
		} while (true);
		}
		returnAST = register_list_AST;
		_retTree = _t;
		return res;
	}
	
	public final List<SSLInstructionName>  instr_name_elem(AST _t) throws RecognitionException {
		List<SSLInstructionName> res = null;;
		
		AST instr_name_elem_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST instr_name_elem_AST = null;
		AST name = null;
		AST name_AST = null;
		AST tname = null;
		AST tname_AST = null;
		AST vname = null;
		AST vname_AST = null;
		AST tidx = null;
		AST tidx_AST = null;
		AST d = null;
		AST d_AST = null;
		
			res = new LinkedList<SSLInstructionName>();
			List<AST> table = null;
		
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case NAME:
		{
			name = (AST)_t;
			AST name_AST_in = null;
			name_AST = astFactory.create(name);
			match(_t,NAME);
			_t = _t.getNextSibling();
				
						res.add(new SSLInstructionName(name.getText())); 
					
			break;
		}
		case LSQUARE:
		{
			AST __t2597 = _t;
			AST tmp33_AST = null;
			AST tmp33_AST_in = null;
			tmp33_AST = astFactory.create((AST)_t);
			tmp33_AST_in = (AST)_t;
			ASTPair __currentAST2597 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,LSQUARE);
			_t = _t.getFirstChild();
			tname = (AST)_t;
			AST tname_AST_in = null;
			tname_AST = astFactory.create(tname);
			match(_t,NAME);
			_t = _t.getNextSibling();
			
			if (tables.containsKey(tname.getText())) 
				table = tables.get(tname.getText());
						else throw new RecognitionException("Undefined table: "+ tname.getText());
					
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case NAME:
			{
				vname = (AST)_t;
				AST vname_AST_in = null;
				vname_AST = astFactory.create(vname);
				match(_t,NAME);
				_t = _t.getNextSibling();
				
								int i = 0;
								for (AST tableEntry : table) {
									Map<String,AST>  curVars = new HashMap<String,AST> (); 
									curVars.put(vname.getText(), (AST)astFactory.make( (new ASTArray(1)).add(astFactory.create(NUM,Integer.toString(i)))));
									res.add(new SSLInstructionName(tableEntry.getText(), curVars));
									i++;
								}
							
				break;
			}
			case NUM:
			{
				tidx = (AST)_t;
				AST tidx_AST_in = null;
				tidx_AST = astFactory.create(tidx);
				match(_t,NUM);
				_t = _t.getNextSibling();
				
								int index = Integer.parseInt(tidx.getText());
					if (index < table.size()) {
						res.add(new SSLInstructionName(table.get(index).getText())); 
					} else throw new RecognitionException("Index " + index + " out of bounds for table " + tname.getText() + "!");
							
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			currentAST = __currentAST2597;
			_t = __t2597;
			_t = _t.getNextSibling();
			break;
		}
		case DECOR:
		{
			d = (AST)_t;
			AST d_AST_in = null;
			d_AST = astFactory.create(d);
			match(_t,DECOR);
			_t = _t.getNextSibling();
			
						res.add(new SSLInstructionName('.' + d.getText().substring(1))); 
					
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		returnAST = instr_name_elem_AST;
		_retTree = _t;
		return res;
	}
	
	public final void rtl_expand(AST _t) throws RecognitionException {
		
		AST rtl_expand_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST rtl_expand_AST = null;
		AST rt_AST = null;
		AST rt = null;
		AST name = null;
		AST name_AST = null;
		AST etname = null;
		AST etname_AST = null;
		AST etindex_AST = null;
		AST etindex = null;
		AST lexpr_AST = null;
		AST lexpr = null;
		AST otname = null;
		AST otname_AST = null;
		AST otindex_AST = null;
		AST otindex = null;
		AST rexpr_AST = null;
		AST rexpr = null;
		AST fname = null;
		AST fname_AST = null;
		AST farg_AST = null;
		AST farg = null;
		
		if (_t==null) _t=ASTNULL;
		if ((_t.getType()==RTL)) {
			AST __t2600 = _t;
			AST tmp34_AST = null;
			AST tmp34_AST_in = null;
			tmp34_AST = astFactory.create((AST)_t);
			tmp34_AST_in = (AST)_t;
			ASTPair __currentAST2600 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,RTL);
			_t = _t.getFirstChild();
			rtl_expand_AST = (AST)currentAST.root;
			rtl_expand_AST = astFactory.create(RTL,"RTL");
			currentAST.root = rtl_expand_AST;
			currentAST.child = rtl_expand_AST!=null &&rtl_expand_AST.getFirstChild()!=null ?
				rtl_expand_AST.getFirstChild() : rtl_expand_AST;
			currentAST.advanceChildToEnd();
			{
			_loop2602:
			do {
				if (_t==null) _t=ASTNULL;
				if (((_t.getType() >= SEMI && _t.getType() <= DOT))) {
					rt = _t==ASTNULL ? null : (AST)_t;
					rtl_expand(_t);
					_t = _retTree;
					rt_AST = (AST)returnAST;
					rtl_expand_AST = (AST)currentAST.root;
					
					// do not nest RTL blocks
					if (rt != null && rt.getType() == RTL) {
					if (rt.getFirstChild() != null)
					rtl_expand_AST.addChild(rt.getFirstChild());
					} else
					rtl_expand_AST.addChild(rt_AST);
								
				}
				else {
					break _loop2602;
				}
				
			} while (true);
			}
			currentAST = __currentAST2600;
			_t = __t2600;
			_t = _t.getNextSibling();
		}
		else if ((_t.getType()==NAME)) {
			name = (AST)_t;
			AST name_AST_in = null;
			name_AST = astFactory.create(name);
			match(_t,NAME);
			_t = _t.getNextSibling();
			rtl_expand_AST = (AST)currentAST.root;
			
			String s = name_AST.getText();
			if (locals.peek().containsKey(s))
			rtl_expand_AST = astFactory.dupTree(locals.peek().get(s));
			else if (constants.containsKey(s))
			rtl_expand_AST = astFactory.create(NUM, Long.toString(constants.get(s)));
			else
			rtl_expand_AST = astFactory.dupTree(name_AST);
					
			currentAST.root = rtl_expand_AST;
			currentAST.child = rtl_expand_AST!=null &&rtl_expand_AST.getFirstChild()!=null ?
				rtl_expand_AST.getFirstChild() : rtl_expand_AST;
			currentAST.advanceChildToEnd();
		}
		else if ((_t.getType()==LSQUARE)) {
			AST __t2603 = _t;
			AST tmp35_AST = null;
			AST tmp35_AST_in = null;
			tmp35_AST = astFactory.create((AST)_t);
			tmp35_AST_in = (AST)_t;
			ASTPair __currentAST2603 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,LSQUARE);
			_t = _t.getFirstChild();
			etname = (AST)_t;
			AST etname_AST_in = null;
			etname_AST = astFactory.create(etname);
			match(_t,NAME);
			_t = _t.getNextSibling();
			etindex = _t==ASTNULL ? null : (AST)_t;
			rtl_expand(_t);
			_t = _retTree;
			etindex_AST = (AST)returnAST;
			currentAST = __currentAST2603;
			_t = __t2603;
			_t = _t.getNextSibling();
			rtl_expand_AST = (AST)currentAST.root;
			
			List<AST> table = tables.get(etname_AST.getText());
			int index = Integer.parseInt(etindex_AST.getText());
			AST expr = table.get(index);
			rtl_expand_AST = astFactory.dupTree(expr);
					
			currentAST.root = rtl_expand_AST;
			currentAST.child = rtl_expand_AST!=null &&rtl_expand_AST.getFirstChild()!=null ?
				rtl_expand_AST.getFirstChild() : rtl_expand_AST;
			currentAST.advanceChildToEnd();
		}
		else if ((_t.getType()==LOOKUP_OP)) {
			AST __t2604 = _t;
			AST tmp36_AST = null;
			AST tmp36_AST_in = null;
			tmp36_AST = astFactory.create((AST)_t);
			tmp36_AST_in = (AST)_t;
			ASTPair __currentAST2604 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,LOOKUP_OP);
			_t = _t.getFirstChild();
			lexpr = _t==ASTNULL ? null : (AST)_t;
			rtl_expand(_t);
			_t = _retTree;
			lexpr_AST = (AST)returnAST;
			otname = (AST)_t;
			AST otname_AST_in = null;
			otname_AST = astFactory.create(otname);
			match(_t,NAME);
			_t = _t.getNextSibling();
			otindex = _t==ASTNULL ? null : (AST)_t;
			rtl_expand(_t);
			_t = _retTree;
			otindex_AST = (AST)returnAST;
			rexpr = _t==ASTNULL ? null : (AST)_t;
			rtl_expand(_t);
			_t = _retTree;
			rexpr_AST = (AST)returnAST;
			currentAST = __currentAST2604;
			_t = __t2604;
			_t = _t.getNextSibling();
			rtl_expand_AST = (AST)currentAST.root;
			
			List <AST> table = tables.get(otname_AST.getText());
			int index = Integer.parseInt(otindex_AST.getText());
			AST op = table.get(index);
			op = astFactory.dupTree(op);
			rtl_expand_AST = (AST)astFactory.make( (new ASTArray(3)).add(op).add(lexpr_AST).add(rexpr_AST));
					
			currentAST.root = rtl_expand_AST;
			currentAST.child = rtl_expand_AST!=null &&rtl_expand_AST.getFirstChild()!=null ?
				rtl_expand_AST.getFirstChild() : rtl_expand_AST;
			currentAST.advanceChildToEnd();
		}
		else if ((_t.getType()==FUNCTION)) {
			AST __t2605 = _t;
			AST tmp37_AST = null;
			AST tmp37_AST_in = null;
			tmp37_AST = astFactory.create((AST)_t);
			tmp37_AST_in = (AST)_t;
			ASTPair __currentAST2605 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,FUNCTION);
			_t = _t.getFirstChild();
			fname = (AST)_t;
			AST fname_AST_in = null;
			fname_AST = astFactory.create(fname);
			match(_t,NAME);
			_t = _t.getNextSibling();
			List<AST> fargs = new LinkedList<AST>();
			{
			_loop2607:
			do {
				if (_t==null) _t=ASTNULL;
				if (((_t.getType() >= SEMI && _t.getType() <= DOT))) {
					farg = _t==ASTNULL ? null : (AST)_t;
					rtl_expand(_t);
					_t = _retTree;
					farg_AST = (AST)returnAST;
					fargs.add(farg_AST);
				}
				else {
					break _loop2607;
				}
				
			} while (true);
			}
			currentAST = __currentAST2605;
			_t = __t2605;
			_t = _t.getNextSibling();
			rtl_expand_AST = (AST)currentAST.root;
			
			SSLFunction f = functions.get(fname.getText());
						Map<String,AST> assignment = new HashMap<String,AST>();
						for (int i=0; i<f.getParameterCount(); i++)
							assignment.put(f.getParameter(i), fargs.get(i));
						if (assignment != null) locals.push(assignment); else locals.push(new HashMap<String,AST>());
			rtl_expand(f.getAST());
			rtl_expand_AST = this.getAST();
			locals.pop();
					
			currentAST.root = rtl_expand_AST;
			currentAST.child = rtl_expand_AST!=null &&rtl_expand_AST.getFirstChild()!=null ?
				rtl_expand_AST.getFirstChild() : rtl_expand_AST;
			currentAST.advanceChildToEnd();
		}
		else if (((_t.getType() >= SEMI && _t.getType() <= DOT))) {
			AST __t2608 = _t;
			AST tmp38_AST = null;
			AST tmp38_AST_in = null;
			tmp38_AST = astFactory.create((AST)_t);
			tmp38_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp38_AST);
			ASTPair __currentAST2608 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			if ( _t==null ) throw new MismatchedTokenException();
			_t = _t.getFirstChild();
			{
			_loop2610:
			do {
				if (_t==null) _t=ASTNULL;
				if (((_t.getType() >= SEMI && _t.getType() <= DOT))) {
					rtl_expand(_t);
					_t = _retTree;
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop2610;
				}
				
			} while (true);
			}
			currentAST = __currentAST2608;
			_t = __t2608;
			_t = _t.getNextSibling();
			rtl_expand_AST = (AST)currentAST.root;
		}
		else {
			throw new NoViableAltException(_t);
		}
		
		returnAST = rtl_expand_AST;
		_retTree = _t;
	}
	
	public final StatementSequence  convertToRTL(AST _t) throws RecognitionException {
		 StatementSequence statements = new StatementSequence(); ;
		
		AST convertToRTL_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST convertToRTL_AST = null;
		AST type = null;
		AST type_AST = null;
		AST other = null;
		AST other_AST = null;
		
			RTLExpression lhs = null; 
			RTLExpression rhs = null;
			RTLExpression cnt = null; 
			StatementSequence subStatements = null;
			int bitWidth = -1;
		
		
		if (_t==null) _t=ASTNULL;
		if ((_t.getType()==RTL)) {
			AST __t2612 = _t;
			AST tmp39_AST = null;
			AST tmp39_AST_in = null;
			tmp39_AST = astFactory.create((AST)_t);
			tmp39_AST_in = (AST)_t;
			ASTPair __currentAST2612 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,RTL);
			_t = _t.getFirstChild();
			{
			_loop2614:
			do {
				if (_t==null) _t=ASTNULL;
				if (((_t.getType() >= SEMI && _t.getType() <= DOT))) {
					subStatements=convertToRTL(_t);
					_t = _retTree;
					statements.addLast(subStatements);
				}
				else {
					break _loop2614;
				}
				
			} while (true);
			}
			currentAST = __currentAST2612;
			_t = __t2612;
			_t = _t.getNextSibling();
		}
		else if ((_t.getType()==ASSIGNTYPE)) {
			AST __t2615 = _t;
			type = _t==ASTNULL ? null :(AST)_t;
			AST type_AST_in = null;
			type_AST = astFactory.create(type);
			ASTPair __currentAST2615 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,ASSIGNTYPE);
			_t = _t.getFirstChild();
			
						assert type != null : "Matched null assign type";
						String aType = type.getText();
						assert aType.length() >=3 : "Parsed assign type which has less than 3 characters";
						// cut of stars
						aType = aType.substring(1, aType.length() - 1);
						// Cut off 'f' from float assigntypes
						if (aType.startsWith("f")) aType = aType.substring(1);
						bitWidth = Integer.parseInt(aType);
					
			lhs=rtlExpr(_t,bitWidth);
			_t = _retTree;
			rhs=rtlExpr(_t,-bitWidth);
			_t = _retTree;
			currentAST = __currentAST2615;
			_t = __t2615;
			_t = _t.getNextSibling();
			
					statements.addFirst(new AssignmentTemplate(bitWidth, (Writable)lhs, rhs));
					//System.out.println("Got assigntype!" + statements.toString());
				
		}
		else if ((_t.getType()==LITERAL_MEMSET)) {
			AST __t2616 = _t;
			AST tmp40_AST_in = null;
			ASTPair __currentAST2616 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,LITERAL_MEMSET);
			_t = _t.getFirstChild();
			bitWidth = constants.get("ADDRESSBITS").intValue();
			lhs=rtlExpr(_t,bitWidth);
			_t = _retTree;
			rhs=rtlExpr(_t,RTLVariable.UNKNOWN_BITWIDTH);
			_t = _retTree;
			cnt=rtlExpr(_t,bitWidth);
			_t = _retTree;
			currentAST = __currentAST2616;
			_t = __t2616;
			_t = _t.getNextSibling();
			
					statements.addFirst(new RTLMemset(lhs, rhs, cnt));
				
		}
		else if ((_t.getType()==LITERAL_MEMCPY)) {
			AST __t2617 = _t;
			AST tmp41_AST_in = null;
			ASTPair __currentAST2617 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,LITERAL_MEMCPY);
			_t = _t.getFirstChild();
			bitWidth = constants.get("ADDRESSBITS").intValue();
			lhs=rtlExpr(_t,bitWidth);
			_t = _retTree;
			rhs=rtlExpr(_t,bitWidth);
			_t = _retTree;
			cnt=rtlExpr(_t,bitWidth);
			_t = _retTree;
			currentAST = __currentAST2617;
			_t = __t2617;
			_t = _t.getNextSibling();
			
					statements.addFirst(new RTLMemcpy(lhs, rhs, cnt));
				
		}
		else if (((_t.getType() >= SEMI && _t.getType() <= DOT))) {
			AST __t2618 = _t;
			other = _t==ASTNULL ? null :(AST)_t;
			AST other_AST_in = null;
			other_AST = astFactory.create(other);
			ASTPair __currentAST2618 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			if ( _t==null ) throw new MismatchedTokenException();
			_t = _t.getFirstChild();
			{
			_loop2620:
			do {
				if (_t==null) _t=ASTNULL;
				if (((_t.getType() >= SEMI && _t.getType() <= DOT))) {
					AST tmp42_AST_in = null;
					if ( _t==null ) throw new MismatchedTokenException();
					_t = _t.getNextSibling();
				}
				else {
					break _loop2620;
				}
				
			} while (true);
			}
			currentAST = __currentAST2618;
			_t = __t2618;
			_t = _t.getNextSibling();
			
					if (other.getText().equals("halt")) {
						statements.addFirst(new RTLHalt());
					} 
					else statements.addFirst(new RTLSkip()); 
				
		}
		else {
			throw new NoViableAltException(_t);
		}
		
		returnAST = convertToRTL_AST;
		_retTree = _t;
		return statements;
	}
	
	public final RTLExpression  rtlExpr(AST _t,
		int bw
	) throws RecognitionException {
		 RTLExpression ret = null;;
		
		AST rtlExpr_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST rtlExpr_AST = null;
		AST vname = null;
		AST vname_AST = null;
		AST rname = null;
		AST rname_AST = null;
		
			RTLExpression e1 = null, e2 = null, e3 = null;
			RTLExpression[] exprList = new RTLExpression[5]; // Needed for the BUILTIN-rule
			int i = 0; // counter 
			int n1 = -1, n2 = -1; 
			double f1 = Double.NaN;
			String str = null; 
		
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case EQ:
		{
			AST __t2627 = _t;
			AST tmp43_AST = null;
			AST tmp43_AST_in = null;
			tmp43_AST = astFactory.create((AST)_t);
			tmp43_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp43_AST);
			ASTPair __currentAST2627 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,EQ);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2627;
			_t = __t2627;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createEqual(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case NE:
		{
			AST __t2628 = _t;
			AST tmp44_AST = null;
			AST tmp44_AST_in = null;
			tmp44_AST = astFactory.create((AST)_t);
			tmp44_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp44_AST);
			ASTPair __currentAST2628 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,NE);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2628;
			_t = __t2628;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createNotEqual(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case GT:
		{
			AST __t2629 = _t;
			AST tmp45_AST = null;
			AST tmp45_AST_in = null;
			tmp45_AST = astFactory.create((AST)_t);
			tmp45_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp45_AST);
			ASTPair __currentAST2629 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,GT);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2629;
			_t = __t2629;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createGreaterThan(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case LT:
		{
			AST __t2630 = _t;
			AST tmp46_AST = null;
			AST tmp46_AST_in = null;
			tmp46_AST = astFactory.create((AST)_t);
			tmp46_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp46_AST);
			ASTPair __currentAST2630 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,LT);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2630;
			_t = __t2630;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createLessThan(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case GE:
		{
			AST __t2631 = _t;
			AST tmp47_AST = null;
			AST tmp47_AST_in = null;
			tmp47_AST = astFactory.create((AST)_t);
			tmp47_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp47_AST);
			ASTPair __currentAST2631 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,GE);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2631;
			_t = __t2631;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createGreaterOrEqual(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case LE:
		{
			AST __t2632 = _t;
			AST tmp48_AST = null;
			AST tmp48_AST_in = null;
			tmp48_AST = astFactory.create((AST)_t);
			tmp48_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp48_AST);
			ASTPair __currentAST2632 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,LE);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2632;
			_t = __t2632;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createLessOrEqual(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case GTU:
		{
			AST __t2633 = _t;
			AST tmp49_AST = null;
			AST tmp49_AST_in = null;
			tmp49_AST = astFactory.create((AST)_t);
			tmp49_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp49_AST);
			ASTPair __currentAST2633 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,GTU);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2633;
			_t = __t2633;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createUnsignedGreaterThan(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case LTU:
		{
			AST __t2634 = _t;
			AST tmp50_AST = null;
			AST tmp50_AST_in = null;
			tmp50_AST = astFactory.create((AST)_t);
			tmp50_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp50_AST);
			ASTPair __currentAST2634 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,LTU);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2634;
			_t = __t2634;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createUnsignedLessThan(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case GEU:
		{
			AST __t2635 = _t;
			AST tmp51_AST = null;
			AST tmp51_AST_in = null;
			tmp51_AST = astFactory.create((AST)_t);
			tmp51_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp51_AST);
			ASTPair __currentAST2635 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,GEU);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2635;
			_t = __t2635;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createUnsignedGreaterOrEqual(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case LEU:
		{
			AST __t2636 = _t;
			AST tmp52_AST = null;
			AST tmp52_AST_in = null;
			tmp52_AST = astFactory.create((AST)_t);
			tmp52_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp52_AST);
			ASTPair __currentAST2636 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,LEU);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2636;
			_t = __t2636;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createUnsignedLessOrEqual(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case PLUS:
		{
			AST __t2637 = _t;
			AST tmp53_AST = null;
			AST tmp53_AST_in = null;
			tmp53_AST = astFactory.create((AST)_t);
			tmp53_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp53_AST);
			ASTPair __currentAST2637 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,PLUS);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2637;
			_t = __t2637;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createPlus(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case PLUS_F:
		{
			AST __t2638 = _t;
			AST tmp54_AST = null;
			AST tmp54_AST_in = null;
			tmp54_AST = astFactory.create((AST)_t);
			tmp54_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp54_AST);
			ASTPair __currentAST2638 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,PLUS_F);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2638;
			_t = __t2638;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createPlus(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case PLUS_FD:
		{
			AST __t2639 = _t;
			AST tmp55_AST = null;
			AST tmp55_AST_in = null;
			tmp55_AST = astFactory.create((AST)_t);
			tmp55_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp55_AST);
			ASTPair __currentAST2639 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,PLUS_FD);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2639;
			_t = __t2639;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createPlus(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case PLUS_FQ:
		{
			AST __t2640 = _t;
			AST tmp56_AST = null;
			AST tmp56_AST_in = null;
			tmp56_AST = astFactory.create((AST)_t);
			tmp56_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp56_AST);
			ASTPair __currentAST2640 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,PLUS_FQ);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2640;
			_t = __t2640;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createPlus(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case MINUS:
		{
			AST __t2641 = _t;
			AST tmp57_AST = null;
			AST tmp57_AST_in = null;
			tmp57_AST = astFactory.create((AST)_t);
			tmp57_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp57_AST);
			ASTPair __currentAST2641 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,MINUS);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2641;
			_t = __t2641;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createMinus(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case MINUS_F:
		{
			AST __t2642 = _t;
			AST tmp58_AST = null;
			AST tmp58_AST_in = null;
			tmp58_AST = astFactory.create((AST)_t);
			tmp58_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp58_AST);
			ASTPair __currentAST2642 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,MINUS_F);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2642;
			_t = __t2642;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createMinus(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case MINUS_FD:
		{
			AST __t2643 = _t;
			AST tmp59_AST = null;
			AST tmp59_AST_in = null;
			tmp59_AST = astFactory.create((AST)_t);
			tmp59_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp59_AST);
			ASTPair __currentAST2643 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,MINUS_FD);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2643;
			_t = __t2643;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createMinus(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case MINUS_FQ:
		{
			AST __t2644 = _t;
			AST tmp60_AST = null;
			AST tmp60_AST_in = null;
			tmp60_AST = astFactory.create((AST)_t);
			tmp60_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp60_AST);
			ASTPair __currentAST2644 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,MINUS_FQ);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2644;
			_t = __t2644;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createMinus(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case MUL:
		{
			AST __t2645 = _t;
			AST tmp61_AST = null;
			AST tmp61_AST_in = null;
			tmp61_AST = astFactory.create((AST)_t);
			tmp61_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp61_AST);
			ASTPair __currentAST2645 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,MUL);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2645;
			_t = __t2645;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createMultiply(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case MUL_F:
		{
			AST __t2646 = _t;
			AST tmp62_AST = null;
			AST tmp62_AST_in = null;
			tmp62_AST = astFactory.create((AST)_t);
			tmp62_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp62_AST);
			ASTPair __currentAST2646 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,MUL_F);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2646;
			_t = __t2646;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createFloatMultiply(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case MUL_FD:
		{
			AST __t2647 = _t;
			AST tmp63_AST = null;
			AST tmp63_AST_in = null;
			tmp63_AST = astFactory.create((AST)_t);
			tmp63_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp63_AST);
			ASTPair __currentAST2647 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,MUL_FD);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2647;
			_t = __t2647;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createFloatMultiply(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case MUL_FQ:
		{
			AST __t2648 = _t;
			AST tmp64_AST = null;
			AST tmp64_AST_in = null;
			tmp64_AST = astFactory.create((AST)_t);
			tmp64_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp64_AST);
			ASTPair __currentAST2648 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,MUL_FQ);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2648;
			_t = __t2648;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createFloatMultiply(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case MUL_FSD:
		{
			AST __t2649 = _t;
			AST tmp65_AST = null;
			AST tmp65_AST_in = null;
			tmp65_AST = astFactory.create((AST)_t);
			tmp65_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp65_AST);
			ASTPair __currentAST2649 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,MUL_FSD);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2649;
			_t = __t2649;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createFloatMultiply(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case MUL_FDQ:
		{
			AST __t2650 = _t;
			AST tmp66_AST = null;
			AST tmp66_AST_in = null;
			tmp66_AST = astFactory.create((AST)_t);
			tmp66_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp66_AST);
			ASTPair __currentAST2650 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,MUL_FDQ);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2650;
			_t = __t2650;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createFloatMultiply(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case SMUL:
		{
			AST __t2651 = _t;
			AST tmp67_AST = null;
			AST tmp67_AST_in = null;
			tmp67_AST = astFactory.create((AST)_t);
			tmp67_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp67_AST);
			ASTPair __currentAST2651 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,SMUL);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2651;
			_t = __t2651;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createMultiply(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case DIV:
		{
			AST __t2652 = _t;
			AST tmp68_AST = null;
			AST tmp68_AST_in = null;
			tmp68_AST = astFactory.create((AST)_t);
			tmp68_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp68_AST);
			ASTPair __currentAST2652 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,DIV);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2652;
			_t = __t2652;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createDivide(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case DIV_F:
		{
			AST __t2653 = _t;
			AST tmp69_AST = null;
			AST tmp69_AST_in = null;
			tmp69_AST = astFactory.create((AST)_t);
			tmp69_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp69_AST);
			ASTPair __currentAST2653 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,DIV_F);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2653;
			_t = __t2653;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createFloatDivide(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case DIV_FD:
		{
			AST __t2654 = _t;
			AST tmp70_AST = null;
			AST tmp70_AST_in = null;
			tmp70_AST = astFactory.create((AST)_t);
			tmp70_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp70_AST);
			ASTPair __currentAST2654 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,DIV_FD);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2654;
			_t = __t2654;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createFloatDivide(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case DIV_FQ:
		{
			AST __t2655 = _t;
			AST tmp71_AST = null;
			AST tmp71_AST_in = null;
			tmp71_AST = astFactory.create((AST)_t);
			tmp71_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp71_AST);
			ASTPair __currentAST2655 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,DIV_FQ);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2655;
			_t = __t2655;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createFloatDivide(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case SDIV:
		{
			AST __t2656 = _t;
			AST tmp72_AST = null;
			AST tmp72_AST_in = null;
			tmp72_AST = astFactory.create((AST)_t);
			tmp72_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp72_AST);
			ASTPair __currentAST2656 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,SDIV);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2656;
			_t = __t2656;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createDivide(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case MOD:
		{
			AST __t2657 = _t;
			AST tmp73_AST = null;
			AST tmp73_AST_in = null;
			tmp73_AST = astFactory.create((AST)_t);
			tmp73_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp73_AST);
			ASTPair __currentAST2657 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,MOD);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2657;
			_t = __t2657;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createModulo(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case SMOD:
		{
			AST __t2658 = _t;
			AST tmp74_AST = null;
			AST tmp74_AST_in = null;
			tmp74_AST = astFactory.create((AST)_t);
			tmp74_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp74_AST);
			ASTPair __currentAST2658 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,SMOD);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2658;
			_t = __t2658;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createModulo(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_pow:
		{
			AST __t2659 = _t;
			AST tmp75_AST = null;
			AST tmp75_AST_in = null;
			tmp75_AST = astFactory.create((AST)_t);
			tmp75_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp75_AST);
			ASTPair __currentAST2659 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,LITERAL_pow);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2659;
			_t = __t2659;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createPowerOf(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case AND:
		{
			AST __t2660 = _t;
			AST tmp76_AST = null;
			AST tmp76_AST_in = null;
			tmp76_AST = astFactory.create((AST)_t);
			tmp76_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp76_AST);
			ASTPair __currentAST2660 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,AND);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2660;
			_t = __t2660;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createAnd(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case LAND:
		{
			AST __t2661 = _t;
			AST tmp77_AST = null;
			AST tmp77_AST_in = null;
			tmp77_AST = astFactory.create((AST)_t);
			tmp77_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp77_AST);
			ASTPair __currentAST2661 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,LAND);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2661;
			_t = __t2661;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createAnd(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case OR:
		{
			AST __t2662 = _t;
			AST tmp78_AST = null;
			AST tmp78_AST_in = null;
			tmp78_AST = astFactory.create((AST)_t);
			tmp78_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp78_AST);
			ASTPair __currentAST2662 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,OR);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2662;
			_t = __t2662;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createOr(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case LOR:
		{
			AST __t2663 = _t;
			AST tmp79_AST = null;
			AST tmp79_AST_in = null;
			tmp79_AST = astFactory.create((AST)_t);
			tmp79_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp79_AST);
			ASTPair __currentAST2663 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,LOR);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2663;
			_t = __t2663;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createOr(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case XOR:
		{
			AST __t2664 = _t;
			AST tmp80_AST = null;
			AST tmp80_AST_in = null;
			tmp80_AST = astFactory.create((AST)_t);
			tmp80_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp80_AST);
			ASTPair __currentAST2664 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,XOR);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2664;
			_t = __t2664;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createXor(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case ANDNOT:
		{
			AST __t2665 = _t;
			AST tmp81_AST = null;
			AST tmp81_AST_in = null;
			tmp81_AST = astFactory.create((AST)_t);
			tmp81_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp81_AST);
			ASTPair __currentAST2665 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,ANDNOT);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2665;
			_t = __t2665;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createAnd(e1, ExpressionFactory.createNot(e2));
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case ORNOT:
		{
			AST __t2666 = _t;
			AST tmp82_AST = null;
			AST tmp82_AST_in = null;
			tmp82_AST = astFactory.create((AST)_t);
			tmp82_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp82_AST);
			ASTPair __currentAST2666 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,ORNOT);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2666;
			_t = __t2666;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createOr(e1, ExpressionFactory.createNot(e2));
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case XORNOT:
		{
			AST __t2667 = _t;
			AST tmp83_AST = null;
			AST tmp83_AST_in = null;
			tmp83_AST = astFactory.create((AST)_t);
			tmp83_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp83_AST);
			ASTPair __currentAST2667 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,XORNOT);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2667;
			_t = __t2667;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createXor(e1, ExpressionFactory.createNot(e2));
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case NOT:
		{
			AST __t2668 = _t;
			AST tmp84_AST = null;
			AST tmp84_AST_in = null;
			tmp84_AST = astFactory.create((AST)_t);
			tmp84_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp84_AST);
			ASTPair __currentAST2668 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,NOT);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2668;
			_t = __t2668;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createNot(e1);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case LNOT:
		{
			AST __t2669 = _t;
			AST tmp85_AST = null;
			AST tmp85_AST_in = null;
			tmp85_AST = astFactory.create((AST)_t);
			tmp85_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp85_AST);
			ASTPair __currentAST2669 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,LNOT);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2669;
			_t = __t2669;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createNot(e1);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case FNEG:
		{
			AST __t2670 = _t;
			AST tmp86_AST = null;
			AST tmp86_AST_in = null;
			tmp86_AST = astFactory.create((AST)_t);
			tmp86_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp86_AST);
			ASTPair __currentAST2670 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,FNEG);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2670;
			_t = __t2670;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createNeg(e1);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_rlc:
		{
			AST __t2671 = _t;
			AST tmp87_AST = null;
			AST tmp87_AST_in = null;
			tmp87_AST = astFactory.create((AST)_t);
			tmp87_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp87_AST);
			ASTPair __currentAST2671 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,LITERAL_rlc);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2671;
			_t = __t2671;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createRotateLeftWithCarry(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_rrc:
		{
			AST __t2672 = _t;
			AST tmp88_AST = null;
			AST tmp88_AST_in = null;
			tmp88_AST = astFactory.create((AST)_t);
			tmp88_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp88_AST);
			ASTPair __currentAST2672 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,LITERAL_rrc);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2672;
			_t = __t2672;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createRotateRightWithCarry(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_rl:
		{
			AST __t2673 = _t;
			AST tmp89_AST = null;
			AST tmp89_AST_in = null;
			tmp89_AST = astFactory.create((AST)_t);
			tmp89_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp89_AST);
			ASTPair __currentAST2673 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,LITERAL_rl);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2673;
			_t = __t2673;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createRotateLeft(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_rr:
		{
			AST __t2674 = _t;
			AST tmp90_AST = null;
			AST tmp90_AST_in = null;
			tmp90_AST = astFactory.create((AST)_t);
			tmp90_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp90_AST);
			ASTPair __currentAST2674 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,LITERAL_rr);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2674;
			_t = __t2674;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createRotateRight(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case RSHIFT:
		{
			AST __t2675 = _t;
			AST tmp91_AST = null;
			AST tmp91_AST_in = null;
			tmp91_AST = astFactory.create((AST)_t);
			tmp91_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp91_AST);
			ASTPair __currentAST2675 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,RSHIFT);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2675;
			_t = __t2675;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createShiftRight(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case LSHIFT:
		{
			AST __t2676 = _t;
			AST tmp92_AST = null;
			AST tmp92_AST_in = null;
			tmp92_AST = astFactory.create((AST)_t);
			tmp92_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp92_AST);
			ASTPair __currentAST2676 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,LSHIFT);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2676;
			_t = __t2676;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createShiftLeft(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case RSHIFTA:
		{
			AST __t2677 = _t;
			AST tmp93_AST = null;
			AST tmp93_AST_in = null;
			tmp93_AST = astFactory.create((AST)_t);
			tmp93_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp93_AST);
			ASTPair __currentAST2677 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,RSHIFTA);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2677;
			_t = __t2677;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createShiftArithmeticRight(e1, e2);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case NAME:
		{
			vname = (AST)_t;
			AST vname_AST_in = null;
			vname_AST = astFactory.create(vname);
			astFactory.addASTChild(currentAST, vname_AST);
			match(_t,NAME);
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createRegisterVariable(vname.getText(), (bw>0 ? bw : RTLVariable.UNKNOWN_BITWIDTH));
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case REG_ID:
		{
			rname = (AST)_t;
			AST rname_AST_in = null;
			rname_AST = astFactory.create(rname);
			astFactory.addASTChild(currentAST, rname_AST);
			match(_t,REG_ID);
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createRegisterVariable(rname.getText(), (bw>0 ? bw : RTLVariable.UNKNOWN_BITWIDTH));
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case NUM:
		{
			n1=intValue(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			ret = ExpressionFactory.createNumber(n1, RTLVariable.UNKNOWN_BITWIDTH);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case FLOATNUM:
		{
			f1=floatValue(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			ret = ExpressionFactory.createNumber((long)f1, 80);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case MEM_IDX:
		{
			AST __t2678 = _t;
			AST tmp94_AST = null;
			AST tmp94_AST_in = null;
			tmp94_AST = astFactory.create((AST)_t);
			tmp94_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp94_AST);
			ASTPair __currentAST2678 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,MEM_IDX);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,-Math.abs(bw));
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2678;
			_t = __t2678;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createMemoryLocation(e1, (bw!=0 ? Math.abs(bw) : RTLVariable.UNKNOWN_BITWIDTH));
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case CAST:
		{
			AST __t2679 = _t;
			AST tmp95_AST = null;
			AST tmp95_AST_in = null;
			tmp95_AST = astFactory.create((AST)_t);
			tmp95_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp95_AST);
			ASTPair __currentAST2679 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,CAST);
			_t = _t.getFirstChild();
			n1=intValue(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e1=rtlExpr(_t,n1);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2679;
			_t = __t2679;
			_t = _t.getNextSibling();
			
						//ret = ExpressionFactory.createCast(e1, ExpressionFactory.createNumber(n1, RTLVariable.UNKNOWN_BITWIDTH));
						ret = e1;
						
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case AT:
		{
			AST __t2680 = _t;
			AST tmp96_AST = null;
			AST tmp96_AST_in = null;
			tmp96_AST = astFactory.create((AST)_t);
			tmp96_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp96_AST);
			ASTPair __currentAST2680 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,AT);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,0);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,0);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e3=rtlExpr(_t,0);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2680;
			_t = __t2680;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createBitRange(e1, e2, e3);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case QUEST:
		{
			AST __t2681 = _t;
			AST tmp97_AST = null;
			AST tmp97_AST_in = null;
			tmp97_AST = astFactory.create((AST)_t);
			tmp97_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp97_AST);
			ASTPair __currentAST2681 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,QUEST);
			_t = _t.getFirstChild();
			e1=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e2=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			e3=rtlExpr(_t,bw);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			currentAST = __currentAST2681;
			_t = __t2681;
			_t = _t.getNextSibling();
			ret = ExpressionFactory.createConditionalExpression(e1, e2, e3);
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		case BUILTIN:
		{
			AST __t2682 = _t;
			AST tmp98_AST = null;
			AST tmp98_AST_in = null;
			tmp98_AST = astFactory.create((AST)_t);
			tmp98_AST_in = (AST)_t;
			astFactory.addASTChild(currentAST, tmp98_AST);
			ASTPair __currentAST2682 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,BUILTIN);
			_t = _t.getFirstChild();
			str=nameValue(_t);
			_t = _retTree;
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop2684:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_tokenSet_2.member(_t.getType()))) {
					e1=rtlExpr(_t,bw);
					_t = _retTree;
					astFactory.addASTChild(currentAST, returnAST);
					exprList[i++] = e1;
				}
				else {
					break _loop2684;
				}
				
			} while (true);
			}
			currentAST = __currentAST2682;
			_t = __t2682;
			_t = _t.getNextSibling();
			
						  	if (str.equals("sgnex")) ret = ExpressionFactory.createSignExtend(exprList[0], exprList[1], exprList[2]);
						  	else if (str.equals("zfill")) ret = ExpressionFactory.createZeroFill(exprList[0], exprList[1], exprList[2]);
						  	else if (str.equals("fsize")) ret = ExpressionFactory.createFloatResize(exprList[0], exprList[1], exprList[2]);
						  	// temporary solution until real float support
						  	else if (str.equals("ftoi")) ret = ExpressionFactory.createFloatResize(exprList[0], exprList[1], exprList[2]);
						  	else if (str.equals("itof")) ret = ExpressionFactory.createFloatResize(exprList[0], exprList[1], exprList[2]);
							else ret = ExpressionFactory.createSpecialExpression(str, exprList); 
						
			rtlExpr_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		returnAST = rtlExpr_AST;
		_retTree = _t;
		return ret;
	}
	
	public final Map<RTLExpression,RTLExpression>  convertSimplificationTemplates(AST _t) throws RecognitionException {
		 Map<RTLExpression,RTLExpression> mapping = new HashMap<RTLExpression,RTLExpression>();
		
		AST convertSimplificationTemplates_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST convertSimplificationTemplates_AST = null;
		AST type = null;
		AST type_AST = null;
		
			RTLExpression lhs = null; 
			RTLExpression rhs = null;
			int bitWidth = -1;
			Map<RTLExpression,RTLExpression> subMap = null;
		
		
		if (_t==null) _t=ASTNULL;
		switch ( _t.getType()) {
		case RTL:
		{
			AST __t2622 = _t;
			AST tmp99_AST = null;
			AST tmp99_AST_in = null;
			tmp99_AST = astFactory.create((AST)_t);
			tmp99_AST_in = (AST)_t;
			ASTPair __currentAST2622 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,RTL);
			_t = _t.getFirstChild();
			{
			_loop2624:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==ASSIGNTYPE||_t.getType()==RTL)) {
					subMap=convertSimplificationTemplates(_t);
					_t = _retTree;
					mapping.putAll(subMap);
				}
				else {
					break _loop2624;
				}
				
			} while (true);
			}
			currentAST = __currentAST2622;
			_t = __t2622;
			_t = _t.getNextSibling();
			break;
		}
		case ASSIGNTYPE:
		{
			AST __t2625 = _t;
			type = _t==ASTNULL ? null :(AST)_t;
			AST type_AST_in = null;
			type_AST = astFactory.create(type);
			ASTPair __currentAST2625 = currentAST.copy();
			currentAST.root = currentAST.child;
			currentAST.child = null;
			match(_t,ASSIGNTYPE);
			_t = _t.getFirstChild();
			lhs=rtlExpr(_t,RTLVariable.UNKNOWN_BITWIDTH);
			_t = _retTree;
			rhs=rtlExpr(_t,RTLVariable.UNKNOWN_BITWIDTH);
			_t = _retTree;
			currentAST = __currentAST2625;
			_t = __t2625;
			_t = _t.getNextSibling();
			
					mapping.put(lhs, rhs);
				
			break;
		}
		default:
		{
			throw new NoViableAltException(_t);
		}
		}
		returnAST = convertSimplificationTemplates_AST;
		_retTree = _t;
		return mapping;
	}
	
	public final double  floatValue(AST _t) throws RecognitionException {
		 double value = -1; ;
		
		AST floatValue_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST floatValue_AST = null;
		AST number = null;
		AST number_AST = null;
		
		number = (AST)_t;
		AST number_AST_in = null;
		number_AST = astFactory.create(number);
		astFactory.addASTChild(currentAST, number_AST);
		match(_t,FLOATNUM);
		_t = _t.getNextSibling();
		value = Double.parseDouble(number.getText());
		floatValue_AST = (AST)currentAST.root;
		returnAST = floatValue_AST;
		_retTree = _t;
		return value;
	}
	
	public final String  nameValue(AST _t) throws RecognitionException {
		 String value = null; ;
		
		AST nameValue_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST nameValue_AST = null;
		AST str = null;
		AST str_AST = null;
		
		str = (AST)_t;
		AST str_AST_in = null;
		str_AST = astFactory.create(str);
		astFactory.addASTChild(currentAST, str_AST);
		match(_t,NAME);
		_t = _t.getNextSibling();
		value = str.getText();
		nameValue_AST = (AST)currentAST.root;
		returnAST = nameValue_AST;
		_retTree = _t;
		return value;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"SEMI",
		"NUM",
		"NAME",
		"EQUATE",
		"PLUS",
		"MINUS",
		"\"INTEGER\"",
		"\"FLOAT\"",
		"COMMA",
		"REG_ID",
		"INDEX",
		"LSQUARE",
		"RSQUARE",
		"\"COVERS\"",
		"TO",
		"\"SHARES\"",
		"AT",
		"\"OPERAND\"",
		"LCURLY",
		"RCURLY",
		"ASSIGNTYPE",
		"\"ENDIANNESS\"",
		"\"BIG\"",
		"\"LITTLE\"",
		"LPAREN",
		"RPAREN",
		"QUOTE",
		"DECOR",
		"MOD",
		"MUL",
		"DIV",
		"SMUL",
		"SDIV",
		"SMOD",
		"\"rlc\"",
		"\"rrc\"",
		"\"rl\"",
		"\"rr\"",
		"RSHIFT",
		"LSHIFT",
		"RSHIFTA",
		"OR",
		"ORNOT",
		"AND",
		"ANDNOT",
		"XOR",
		"XORNOT",
		"MUL_F",
		"MUL_FD",
		"MUL_FQ",
		"MUL_FSD",
		"MUL_FDQ",
		"DIV_F",
		"DIV_FD",
		"DIV_FQ",
		"PLUS_F",
		"PLUS_FD",
		"PLUS_FQ",
		"MINUS_F",
		"MINUS_FD",
		"MINUS_FQ",
		"\"pow\"",
		"EQ",
		"NE",
		"LT",
		"GT",
		"LE",
		"GE",
		"LTU",
		"GTU",
		"LEU",
		"GEU",
		"PRIME",
		"DOLLAR",
		"\"halt\"",
		"UNDERSCORE",
		"\"MEMSET\"",
		"\"MEMCPY\"",
		"\"r\"",
		"\"m\"",
		"COLON",
		"FLOATNUM",
		"QUEST",
		"S_E",
		"NOT",
		"FNEG",
		"LNOT",
		"\"and\"",
		"\"or\"",
		"\"FAST\"",
		"CONSTANT",
		"TABLE",
		"CROSSP",
		"FUNCTION",
		"INSTR",
		"INSTR_NAME",
		"LOOKUP_OP",
		"RTL",
		"BUILTIN",
		"CAST",
		"REGDECL",
		"WS",
		"COMMENT",
		"DIGITS",
		"HEXDIGITS",
		"FLOAT_OR_NUM",
		"ASSIGN",
		"THEN",
		"ASSIGNTYPE_OR_MUL",
		"DOT"
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 0L, 1128502657024L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 1077936192L, 4294967296L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = { -4293909664L, 825160634367L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	}
	
