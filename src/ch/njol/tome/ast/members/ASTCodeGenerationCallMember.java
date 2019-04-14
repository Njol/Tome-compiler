package ch.njol.tome.ast.members;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.ASTInterfaces.ASTMember;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.ast.expressions.ASTExpressions;
import ch.njol.tome.interpreter.InterpretedNormalObject;
import ch.njol.tome.interpreter.InterpretedObject;
import ch.njol.tome.interpreter.InterpreterContext;
import ch.njol.tome.interpreter.InterpreterException;
import ch.njol.tome.interpreter.nativetypes.InterpretedNativeCodeGenerationResult;
import ch.njol.tome.ir.definitions.IRMemberRedefinition;
import ch.njol.tome.parser.Parser;

public class ASTCodeGenerationCallMember extends AbstractASTElement implements ASTMember {
	
	public @Nullable ASTExpression<?> code;
	
	@Override
	public boolean isInherited() {
		return false; // only generates code at the current location
	}
	
	@Override
	public String toString() {
		return "$= " + code + ";";
	}
	
	public static ASTCodeGenerationCallMember parse(final Parser parent) {
		final Parser p = parent.start();
		final ASTCodeGenerationCallMember ast = new ASTCodeGenerationCallMember();
		p.one("$=");
		p.until(() -> {
			ast.code = ASTExpressions.parse(p);
		}, ';', false);
		return p.done(ast);
	}
	
	// TODO make sure to prevent infinite recursion with other types!
	@Override
	public List<IRMemberRedefinition> getIRMembers() {
		final ASTExpression<?> code = this.code;
		if (code == null)
			return Arrays.asList();
		try {
			final InterpretedObject result = code.getIR().interpret(new InterpreterContext(getIRContext(), (InterpretedNormalObject) null));
			if (!(result instanceof InterpretedNativeCodeGenerationResult))
				return Collections.EMPTY_LIST; // Collections.singletonList(new IRUnknownMember("Must call a code generation template", this));
			return ((InterpretedNativeCodeGenerationResult) result).parseMembers(this);
		} catch (final InterpreterException e) {
			return Collections.EMPTY_LIST; // Collections.singletonList(new IRUnknownMember("" + e.getMessage(), this));
		}
	}
	
}
