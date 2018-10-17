package ch.njol.tome.ast.members;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTError;
import ch.njol.tome.ast.ASTInterfaces.ASTParameter;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.compiler.Token.LowercaseWordToken;
import ch.njol.tome.compiler.Token.WordToken;
import ch.njol.tome.ir.IRError;
import ch.njol.tome.ir.IRNormalError;
import ch.njol.tome.ir.IRUnknownError;
import ch.njol.tome.parser.Parser;

public class ASTErrorDeclaration extends AbstractASTElement implements ASTError {
	public @Nullable LowercaseWordToken name;
	public List<ASTParameter> parameters = new ArrayList<>();
	
	@Override
	public @Nullable WordToken nameToken() {
		return name;
	}
	
//		@Override
//		public List<? extends FormalParameter> declaredParameters() {
//			return parameters;
//		}
	
	@Override
	public String toString() {
		return "#" + name;
	}
	
//		@SuppressWarnings("null")
//		@Override
//		public @Nullable HasParameters parentParameters() {
//			final @Nullable IRMemberRedefinition parent = ((FormalAttribute) parent()).modifiers().overridden.get();
//			return parent == null || name == null || !(parent instanceof IRAttributeRedefinition) ? null //
//					: ((IRAttributeRedefinition) parent).getErrorByName(name.word);
//		}
	
	public static ASTErrorDeclaration parse(final Parser parent) {
		final Parser p = parent.start();
		final ASTErrorDeclaration ast = new ASTErrorDeclaration();
		p.one('#');
		ast.name = p.oneVariableIdentifierToken();
		p.tryGroup('(', () -> {
			do {
				// FIXME
//					parameters.add(one(new ASTSimpleParameter(this)));
			} while (p.try_(','));
		}, ')');
		return p.done(ast);
	}
	
	private @Nullable IRNormalError ir;
	
	@Override
	public IRError getIRError() {
		if (ir != null)
			return ir;
		final ASTAttributeDeclaration attribute = getParentOfType(ASTAttributeDeclaration.class);
		if (attribute == null)
			return new IRUnknownError("" + name(), "internal compiler error", this);
		return ir = new IRNormalError("" + name(), parameters.stream().map(p -> p.getIR()).collect(Collectors.toList()), attribute.getIR());
	}
	
}
