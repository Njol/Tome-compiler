package ch.njol.brokkr.interpreter.nativetypes;

import java.util.ArrayList;
import java.util.List;

import ch.njol.brokkr.ast.ASTMembers.ASTCodeGenerationCallMember;
import ch.njol.brokkr.ast.ASTStatements.ASTCodeGenerationCallStatement;
import ch.njol.brokkr.ir.IRContext;
import ch.njol.brokkr.ir.definitions.IRMemberRedefinition;
import ch.njol.brokkr.ir.statements.IRStatement;

/**
 * Used to store resulting lines in code generation methods.
 * TODO define how this object is created and stored/accessed (e.g. implicit fist parameter of code templates? special value on the stack? ...)
 */
public class InterpretedNativeCodeGenerationResult extends AbstractInterpretedSimpleNativeObject {
	
	public InterpretedNativeCodeGenerationResult(final IRContext irContext) {
		super(irContext);
	}
	
	private final List<String> lines = new ArrayList<>();
	
	public void addLine(final String line) {
		lines.add(line);
	}
	
	@SuppressWarnings("null")
	public IRStatement parseStatements(final ASTCodeGenerationCallStatement astCall) {
		// TODO parse these lines as if they were where the call is actually at, and return the resulting IR statements
		// this mostly requires making a way to parse AST elements without actually adding anything to the existing AST (not even temporarily!)
		return null;
	}
	
	@SuppressWarnings("null")
	public List<IRMemberRedefinition> parseMembers(final ASTCodeGenerationCallMember astCall) {
		// TODO parse members similar to above
		return null;
	}
	
}
