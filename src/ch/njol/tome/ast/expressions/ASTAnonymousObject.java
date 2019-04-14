package ch.njol.tome.ast.expressions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.ASTInterfaces.ASTMember;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeDeclaration;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeUse;
import ch.njol.tome.ast.AbstractASTElementWithIR;
import ch.njol.tome.ast.expressions.ASTExpressions.ASTTypeExpressions;
import ch.njol.tome.ast.members.ASTMembers;
import ch.njol.tome.ast.toplevel.ASTGenericParameterDeclaration;
import ch.njol.tome.compiler.Token.WordToken;
import ch.njol.tome.ir.definitions.IRBrokkrClassDefinition;
import ch.njol.tome.ir.definitions.IRTypeDefinition;
import ch.njol.tome.ir.expressions.IRAnonymousObjectCreation;
import ch.njol.tome.ir.expressions.IRExpression;
import ch.njol.tome.ir.expressions.IRUnknownExpression;
import ch.njol.tome.ir.uses.IRSimpleTypeUse;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.ir.uses.IRUnknownTypeUse;
import ch.njol.tome.parser.Parser;

public class ASTAnonymousObject extends AbstractASTElementWithIR<IRExpression> implements ASTExpression<IRExpression> {
	
	public @Nullable ASTAnonymousType type;
	
	@Override
	public int linkStart() {
		return type != null ? type.linkStart() : absoluteRegionStart();
	}
	
	@Override
	public int linkLength() {
		return type != null ? type.linkLength() : regionLength();
	}
	
	@Override
	public String toString() {
		return "create " + type;
	}
	
	public static ASTAnonymousObject parse(final Parser parent) {
		return parent.one(p -> {
			final ASTAnonymousObject ast = new ASTAnonymousObject();
			p.one("create");
			ast.type = ASTAnonymousType.parse(p);
			return ast;
		});
	}
	
	@Override
	public IRTypeUse getIRType() {
		final ASTAnonymousType type = this.type;
		if (type == null)
			return new IRUnknownTypeUse(getIRContext());
		return new IRSimpleTypeUse(type.getIR());
	}
	
	@Override
	protected IRExpression calculateIR() {
		if (type != null)
			return new IRAnonymousObjectCreation((IRBrokkrClassDefinition) type.getIR()); // TODO maybe make AbstractASTElementWithIR have two generic parameters so that this cast can be removed?
		else
			return new IRUnknownExpression("Syntax error. Proper syntax: [create SomeType { ... }]", this);
	}
	
	public static class ASTAnonymousType extends AbstractASTElementWithIR<IRTypeDefinition> implements ASTTypeDeclaration<IRTypeDefinition> {
		
		public @Nullable ASTTypeUse<?> type;
		public List<ASTMember> members = new ArrayList<>();
		
		@Override
		public @Nullable String name() {
			return null;
		}
		
		@Override
		public @Nullable WordToken nameToken() {
			return null;
		}
		
		@Override
		public int linkStart() {
			return type != null ? type.linkStart() : absoluteRegionStart();
		}
		
		@Override
		public int linkLength() {
			return type != null ? type.linkLength() : regionLength();
		}
		
		@Override
		public List<? extends ASTMember> declaredMembers() {
			return members;
		}
		
		// always has a parent type
		@Override
		public @NonNull IRTypeUse parentTypes() {
			return type != null ? type.getIR() : new IRUnknownTypeUse(getIRContext());
		}
		
		@Override
		public List<? extends ASTGenericParameterDeclaration<?>> genericParameters() {
			return Collections.EMPTY_LIST;
		}
		
		@Override
		public String toString() {
			return type + " {...}";
		}
		
		public static ASTAnonymousType parse(final Parser parent) {
			return parent.one(p -> {
				final ASTAnonymousType ast = new ASTAnonymousType();
				ast.type = ASTTypeExpressions.parse(p, true, false);
				p.oneRepeatingGroup('{', () -> {
					ast.members.add(ASTMembers.parse(p));
				}, '}');
				return ast;
			});
		}
		
		@Override
		protected IRTypeDefinition calculateIR() {
			return new IRBrokkrClassDefinition(this);
		}
		
	}
	
}
