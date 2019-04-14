package ch.njol.tome.ast.members;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTAttribute;
import ch.njol.tome.ast.ASTInterfaces.ASTError;
import ch.njol.tome.ast.ASTInterfaces.ASTParameter;
import ch.njol.tome.ast.ASTInterfaces.ASTResult;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeDeclaration;
import ch.njol.tome.ast.AbstractASTElementWithIR;
import ch.njol.tome.ast.expressions.ASTBlock;
import ch.njol.tome.compiler.Token;
import ch.njol.tome.compiler.Token.LowercaseWordToken;
import ch.njol.tome.compiler.Token.WordToken;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.definitions.IRBrokkrConstructor;
import ch.njol.tome.ir.definitions.IRVariableRedefinition;
import ch.njol.tome.ir.uses.IRSimpleTypeUse;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.parser.Parser;
import ch.njol.util.StringUtils;

//TODO make more specific, e.g. AbstractASTElementWithIR<IRAttributeRedefinition, IRBrokkrConstructor>
public class ASTConstructor extends AbstractASTElementWithIR<IRAttributeRedefinition> implements ASTAttribute {
	
	public final ASTMemberModifiers modifiers;
	
	public @Nullable LowercaseWordToken name;
	public List<ASTParameter> parameters = new ArrayList<>();
	public @Nullable ASTBlock body;
	public final List<ASTPostcondition> postconditions = new ArrayList<>();
	
	public ASTConstructor(final ASTMemberModifiers modifiers) {
		this.modifiers = modifiers;
	}
	
	@Override
	public boolean isInherited() {
		return true;
	}
	
	@Override
	public @Nullable WordToken nameToken() {
		return name;
	}
	
	@Override
	public ASTMemberModifiers modifiers() {
		return modifiers;
	}
	
	@Override
	public List<? extends IRVariableRedefinition> allVariables() {
		return getIR().parameters();
	}
	
	@Override
	public List<ASTError<?>> declaredErrors() {
		return Collections.EMPTY_LIST; // FIXME preconditions are errors too!
	}
	
	@Override
	public List<? extends ASTResult> declaredResults() {
		return Collections.EMPTY_LIST; // FIXME
	}
	
	@Override
	public String toString() {
		return "" + name + (parameters.size() == 0 ? "" : "(" + StringUtils.join(parameters, ", ") + ")");
	}
	
	@Override
	public @Nullable String hoverInfo(final Token token) {
		return getIR().hoverInfo();
	}
	
	public static ASTConstructor finishParsing(final Parser p, final ASTMemberModifiers modifiers) {
		final ASTConstructor ast = new ASTConstructor(modifiers);
		p.one("constructor");
		ast.name = p.oneVariableIdentifierToken();
		p.oneGroup('(', () -> {
			int i = 0;
			do {
				if (p.peekNext('=', 1, true) || p.peekNext(',', 1, true) || p.peekNext(')', 1, true))
					ast.parameters.add(ASTConstructorFieldParameter.parse(p));
				else
					ast.parameters.add(ASTSimpleParameter.parse(p, i));
				i++;
			} while (p.try_(','));
		}, ')');
		if (!p.try_(';')) // field params syntax
			ast.body = ASTBlock.parse(p);
		return p.done(ast);
	}
	
	@SuppressWarnings("null")
	@Override
	public IRTypeUse getIRType() {
		return new IRSimpleTypeUse(getParentOfType(ASTTypeDeclaration.class).getIR());
	}
	
	@Override
	public IRAttributeRedefinition calculateIR() {
		return new IRBrokkrConstructor(this);
	}
	
}
