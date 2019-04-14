package ch.njol.tome.ast.members;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTMember;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeExpression;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeUse;
import ch.njol.tome.ast.ASTInterfaces.NamedASTElement;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.ast.expressions.ASTTypeUseWithOperators;
import ch.njol.tome.compiler.Token.UppercaseWordToken;
import ch.njol.tome.compiler.Token.WordOrSymbols;
import ch.njol.tome.ir.definitions.IRBrokkrGenericTypeDefinition;
import ch.njol.tome.ir.definitions.IRMemberRedefinition;
import ch.njol.tome.parser.Parser;

public class ASTGenericTypeDeclaration extends AbstractASTElement implements ASTMember, NamedASTElement {
	
	public final ASTMemberModifiers modifiers;
	
	public @Nullable UppercaseWordToken nameToken;
	
	public @Nullable ASTTypeUse<?> extends_, super_;
	
	public @Nullable ASTTypeExpression<?> defaultValue;
	
	public ASTGenericTypeDeclaration(final ASTMemberModifiers modifiers) {
		this.modifiers = modifiers;
	}
	
	@Override
	public @Nullable WordOrSymbols nameToken() {
		return nameToken;
	}
	
	@Override
	public String toString() {
		return "type " + nameToken + (extends_ == null ? "" : " extends " + extends_) + (super_ == null ? "" : " super " + super_);
	}
	
	public static ASTGenericTypeDeclaration finishParsing(final Parser p, final ASTMemberModifiers modifiers) {
		final ASTGenericTypeDeclaration ast = new ASTGenericTypeDeclaration(modifiers);
		p.one("type");
		ast.nameToken = p.oneTypeIdentifierToken();
		p.unordered(() -> {
			if (p.try_("extends"))
				ast.extends_ = ASTTypeUseWithOperators.parse(p);
		}, () -> {
			if (p.try_("super"))
				ast.super_ = ASTTypeUseWithOperators.parse(p);
		});
		if (p.try_('=')) {
			// TODO this syntax is not intuitive - 'type T = Int8' looks like T is always Int8, not just by default...
			ast.defaultValue = ASTTypeUseWithOperators.parse(p);
		}
		p.one(';');
		return p.done(ast);
	}
	
	@Override
	public boolean isInherited() {
		return false;
	}
	
	@Override
	public List<? extends IRMemberRedefinition> getIRMembers() {
		return Arrays.asList(new IRBrokkrGenericTypeDefinition(this));
	}
	
}
