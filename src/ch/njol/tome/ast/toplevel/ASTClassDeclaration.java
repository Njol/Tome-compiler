package ch.njol.tome.ast.toplevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTGenericParameter;
import ch.njol.tome.ast.ASTInterfaces.ASTMember;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeDeclaration;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeUse;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.ast.expressions.ASTExpressions.ASTTypeExpressions;
import ch.njol.tome.ast.members.ASTMemberModifiers;
import ch.njol.tome.ast.members.ASTMembers;
import ch.njol.tome.compiler.Token.UppercaseWordToken;
import ch.njol.tome.compiler.Token.WordToken;
import ch.njol.tome.ir.definitions.IRBrokkrClassDefinition;
import ch.njol.tome.ir.definitions.IRMemberRedefinition;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.parser.Parser;

public class ASTClassDeclaration extends AbstractASTElement implements ASTTypeDeclaration, ASTMember {
	public final ASTMemberModifiers modifiers;
	
	public @Nullable UppercaseWordToken name;
	public List<ASTGenericParameterDeclaration> genericParameters = new ArrayList<>();
	public List<ASTTypeUse> parents = new ArrayList<>();
	public List<ASTMember> members = new ArrayList<>();
	
	public ASTClassDeclaration(final ASTMemberModifiers modifiers) {
		this.modifiers = modifiers;
	}
	
	@Override
	public @Nullable WordToken nameToken() {
		return name;
	}
	
	@Override
	public List<? extends ASTMember> declaredMembers() {
		return members;
	}
	
	@Override
	public @Nullable IRTypeUse parentTypes() {
		return ASTInterfaceDeclaration.parentTypes(this, parents);
	}
	
	@Override
	public List<? extends ASTGenericParameter> genericParameters() {
		return genericParameters;
	}
	
	@Override
	public String toString() {
		return "" + name;
	}
	
	public static ASTClassDeclaration finishParsing(final Parser p, final ASTMemberModifiers modifiers) {
		final ASTClassDeclaration ast = new ASTClassDeclaration(modifiers);
		p.one("class");
		p.until(() -> {
			ast.name = p.oneTypeIdentifierToken();
			p.tryGroup('<', () -> {
				do {
					ast.genericParameters.add(ASTGenericParameterDeclaration.parse(p));
				} while (p.try_(','));
			}, '>');
			if (p.try_("implements")) {
				do {
					ast.parents.add(ASTTypeExpressions.parse(p, false, false, true));
				} while (p.try_(','));
			}
		}, '{', false);
		p.repeatUntil(() -> {
			ast.members.add(ASTMembers.parse(p));
		}, '}', true);
		return p.done(ast);
	}
	
	private @Nullable IRBrokkrClassDefinition ir = null;
	
	@Override
	public IRBrokkrClassDefinition getIR() {
		if (ir != null)
			return ir;
		return ir = new IRBrokkrClassDefinition(this);
	}
	
	@Override
	public List<? extends IRMemberRedefinition> getIRMembers() {
		return Collections.EMPTY_LIST;
	}
	
	@Override
	public boolean isInherited() {
		return false;
	}
	
}
