package ch.njol.tome.ast.toplevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTMember;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeDeclaration;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeExpression;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeUse;
import ch.njol.tome.ast.AbstractASTElementWithIR;
import ch.njol.tome.ast.expressions.ASTExpressions.ASTTypeExpressions;
import ch.njol.tome.ast.members.ASTMembers;
import ch.njol.tome.compiler.Token.UppercaseWordToken;
import ch.njol.tome.compiler.Token.WordToken;
import ch.njol.tome.ir.definitions.IRTypeDefinition;
import ch.njol.tome.ir.definitions.IRUnknownTypeDefinition;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.parser.Parser;

public class ASTExtensionDeclaration extends AbstractASTElementWithIR<IRTypeDefinition> implements ASTTypeDeclaration<IRTypeDefinition> {
	
	public final ASTTopLevelElementModifiers modifiers;
	
	public @Nullable UppercaseWordToken name;
	public List<ASTTypeUse<?>> parents = new ArrayList<>();
	public List<ASTMember> members = new ArrayList<>();
	public @Nullable ASTTypeExpression<?> extended;
	
	public ASTExtensionDeclaration(final ASTTopLevelElementModifiers modifiers) {
		this.modifiers = modifiers;
	}
	
	@Override
	public @Nullable WordToken nameToken() {
		return name;
	}
	
	@Override
	public @NonNull List<? extends ASTMember> declaredMembers() {
		return members;
	}
	
	@Override
	public @Nullable IRTypeUse parentTypes() {
		return ASTInterfaceDeclaration.parentTypes(this, parents);
	}
	
	@Override
	public List<? extends ASTGenericParameterDeclaration<?>> genericParameters() {
		return Collections.EMPTY_LIST;
	}
	
	@Override
	public String toString() {
		return "" + name;
	}
	
	public static ASTExtensionDeclaration finishParsing(final Parser p, final ASTTopLevelElementModifiers modifiers) {
		final ASTExtensionDeclaration ast = new ASTExtensionDeclaration(modifiers);
		p.one("extension");
		ast.name = p.oneTypeIdentifierToken();
		//generics=GenericParameters? // deriving the generics of an extension is non-trivial (e.g. 'extension WeirdList<X> extends List<T extends Comparable<X> & Collection<X>>')
		// could just make an error if it cannot be inferred, and allow it for the normal, simple cases
		p.one("extends");
		ast.extended = ASTTypeExpressions.parse(p, true, false);
		if (p.try_("implements")) {
			do {
				ast.parents.add(ASTTypeExpressions.parse(p, false, false, true));
			} while (p.try_(','));
		}
		p.oneRepeatingGroup('{', () -> {
			ast.members.add(ASTMembers.parse(p));
		}, '}');
		return p.done(ast);
	}
	
	@Override
	protected IRTypeDefinition calculateIR() {
		return new IRUnknownTypeDefinition(getIRContext(), "not implemented", this);
	}
	
}
