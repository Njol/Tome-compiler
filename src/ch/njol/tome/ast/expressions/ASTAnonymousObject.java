package ch.njol.tome.ast.expressions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.ASTInterfaces.ASTGenericParameter;
import ch.njol.tome.ast.ASTInterfaces.ASTMember;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeDeclaration;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeUse;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.ast.expressions.ASTExpressions.ASTTypeExpressions;
import ch.njol.tome.ast.members.ASTMembers;
import ch.njol.tome.compiler.Token.WordToken;
import ch.njol.tome.ir.definitions.IRBrokkrClassDefinition;
import ch.njol.tome.ir.expressions.IRAnonymousObjectCreation;
import ch.njol.tome.ir.expressions.IRExpression;
import ch.njol.tome.ir.expressions.IRUnknownExpression;
import ch.njol.tome.ir.uses.IRSimpleTypeUse;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.ir.uses.IRUnknownTypeUse;
import ch.njol.tome.parser.Parser;
import ch.njol.tome.util.Cache;

public class ASTAnonymousObject extends AbstractASTElement implements ASTExpression {
	public @Nullable ASTAnonymousType type;
	
	@Override
	public int linkStart() {
		return type != null ? type.linkStart() : absoluteRegionStart();
	}
	
	@Override
	public int linkEnd() {
		return type != null ? type.linkEnd() : absoluteRegionEnd();
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
	public IRExpression getIR() {
		if (type != null)
			return new IRAnonymousObjectCreation(type.getIR());
		else
			return new IRUnknownExpression("Syntax error. Proper syntax: [create SomeType { ... }]", this);
	}
	
	public static class ASTAnonymousType extends AbstractASTElement implements ASTTypeDeclaration {
		public @Nullable ASTTypeUse type;
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
			return type != null ? type.absoluteRegionStart() : absoluteRegionStart();
		}
		
		@Override
		public int linkEnd() {
			return type != null ? type.absoluteRegionEnd() : absoluteRegionEnd();
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
		public List<? extends ASTGenericParameter> genericParameters() {
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
		
		private final Cache<IRBrokkrClassDefinition> ir = new Cache<>(() -> new IRBrokkrClassDefinition(this));
		
		@Override
		public IRBrokkrClassDefinition getIR() {
			return ir.get();
		}
	}
	
}
