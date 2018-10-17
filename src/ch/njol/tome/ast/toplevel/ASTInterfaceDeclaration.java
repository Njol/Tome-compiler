package ch.njol.tome.ast.toplevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTElement;
import ch.njol.tome.ast.ASTInterfaces.ASTGenericParameter;
import ch.njol.tome.ast.ASTInterfaces.ASTMember;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeDeclaration;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeUse;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.ast.expressions.ASTExpressions.ASTTypeExpressions;
import ch.njol.tome.ast.members.ASTMemberModifiers;
import ch.njol.tome.ast.members.ASTMembers;
import ch.njol.tome.common.ModuleIdentifier;
import ch.njol.tome.compiler.Token.WordToken;
import ch.njol.tome.ir.definitions.IRBrokkrInterfaceDefinition;
import ch.njol.tome.ir.definitions.IRMemberRedefinition;
import ch.njol.tome.ir.uses.IRAndTypeUse;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.parser.Parser;

// TODO make generics refer to an attribute instead (just a name, or an alias)
// then generics can easily be much more general, e.g. Matrix<rows, columns, NumberType> becomes possible
public class ASTInterfaceDeclaration extends AbstractASTElement implements ASTTypeDeclaration, ASTMember {
	public final ASTMemberModifiers modifiers;
	
	public @Nullable WordToken name;
	public List<ASTGenericParameterDeclaration> genericParameters = new ArrayList<>();
	public List<ASTTypeUse> parents = new ArrayList<>();
	public List<ASTMember> members = new ArrayList<>();
	
	public ASTInterfaceDeclaration(final ASTMemberModifiers modifiers) {
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
		return parentTypes(this, parents);
	}
	
	/**
	 * @param e
	 * @param parents
	 * @return The parent types of this type, or null if this represents the Any type which has no further parents
	 */
	public final static @Nullable IRTypeUse parentTypes(final ASTElement e, final List<? extends ASTTypeUse> parents) {
		if (parents.isEmpty()) {
			if (e instanceof ASTInterfaceDeclaration && "Any".equals(((ASTInterfaceDeclaration) e).name())) {
				final ASTSourceFile file = e.getParentOfType(ASTSourceFile.class);
				ASTModuleDeclaration md;
				if (file != null && (md = file.moduleDeclaration) != null && new ModuleIdentifier("lang").equals(md.module))
					return null; // only [lang.Any] has no parent
			}
			return e.getIRContext().getTypeUse("lang", "Any");
		}
		return parents.stream().map(t -> t.getIR()).reduce((t1, t2) -> IRAndTypeUse.makeNew(t1, t2)).get();
	}
	
	@Override
	public List<? extends ASTGenericParameter> genericParameters() {
		return genericParameters;
	}
	
	@Override
	public String toString() {
		return "" + name;
	}
	
	public static ASTInterfaceDeclaration finishParsing(final Parser p, final ASTMemberModifiers modifiers) {
		final ASTInterfaceDeclaration ast = new ASTInterfaceDeclaration(modifiers);
		p.one("interface");
		p.until(() -> {
			ast.name = p.oneTypeIdentifierToken();
			p.tryGroup('<', () -> {
				do {
					ast.genericParameters.add(ASTGenericParameterDeclaration.parse(p));
				} while (p.try_(','));
			}, '>');
			if (p.try_("extends")) {
				do {
					ast.parents.add(ASTTypeExpressions.parse(p, false, false, true));
					// TODO if base != null infer type from that one (e.g. in 'dynamic Collection extends addable')?
				} while (p.try_(','));
			}
		}, '{', false);
		p.repeatUntil(() -> {
			ast.members.add(ASTMembers.parse(p));
		}, '}', true);
		return p.done(ast);
	}
	
	private @Nullable IRBrokkrInterfaceDefinition ir = null;
	
	@Override
	public IRBrokkrInterfaceDefinition getIR() {
		if (ir != null)
			return ir;
		return ir = new IRBrokkrInterfaceDefinition(this);
	}
	
	@Override
	public List<? extends IRMemberRedefinition> getIRMembers() {
		return Collections.EMPTY_LIST;
	}
	
	@Override
	public boolean isInherited() {
		return false; // TODO allow to inherit non-private inner types?
	}
}
