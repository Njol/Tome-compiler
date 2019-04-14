package ch.njol.tome.ast.expressions;

import java.util.ArrayList;
import java.util.List;

import ch.njol.tome.ast.ASTInterfaces.ASTTypeExpression;
import ch.njol.tome.ast.AbstractASTElementWithIR;
import ch.njol.tome.ir.IRGenericArgument;
import ch.njol.tome.ir.IRGenericArguments;
import ch.njol.tome.ir.IRPredicateGenericArgument;
import ch.njol.tome.ir.IRTypeBoundGenericArgument;
import ch.njol.tome.ir.IRValueGenericArgument;
import ch.njol.tome.ir.definitions.IRGenericTypeParameter;
import ch.njol.tome.ir.definitions.IRMemberRedefinition;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.parser.Parser;
import ch.njol.util.StringUtils;

/**
 * A type use with generic arguments, e.g. 'A&lt;B, C: D>'
 */
public class ASTTypeWithGenericArguments extends AbstractASTElementWithIR<IRTypeUse> implements ASTTypeExpression<IRTypeUse> {
	
	public final ASTSimpleTypeUse baseType;
	
	public ASTTypeWithGenericArguments(final ASTSimpleTypeUse baseType) {
		this.baseType = baseType;
	}
	
	public final List<ASTGenericArgument> genericArguments = new ArrayList<>();
	
	@Override
	public String toString() {
		return baseType + "<" + StringUtils.join(genericArguments, ",") + ">";
	}
	
	public static ASTTypeExpression<?> parse(final Parser parent) {
		final Parser p = parent.start();
		final ASTSimpleTypeUse baseType = ASTSimpleTypeUse.parse(p);
		if (p.peekNext('<'))
			return finishParsing(p, baseType);
		p.doneAsChildren();
		return baseType;
	}
	
	public static ASTTypeWithGenericArguments finishParsing(final Parser p, final ASTSimpleTypeUse baseType) {
		final ASTTypeWithGenericArguments ast = new ASTTypeWithGenericArguments(baseType);
		p.oneGroup('<', () -> {
			do {
				ast.genericArguments.add(ASTGenericArgument.parse(p));
			} while (p.try_(','));
		}, '>');
		return p.done(ast);
	}
	
	@Override
	protected IRTypeUse calculateIR() {
		final IRGenericArguments genericArguments = new IRGenericArguments(getIRContext());
		final IRTypeUse baseTypeIR = baseType.getIR();
		for (int i = 0; i < this.genericArguments.size(); i++) {
			final ASTGenericArgument ga = this.genericArguments.get(i);
			final IRGenericArgument value = ga.value != null ? ga.value.getIR() : null;
			if (value == null)
				continue;
			if (value instanceof IRPredicateGenericArgument) {
				assert !ga.hasParameter();
				genericArguments.addPredicateArgument((IRPredicateGenericArgument) value);
			} else {
				final IRMemberRedefinition member = ga.getDefinition(baseTypeIR, i);
				if (member == null)
					continue;
				if (value instanceof IRTypeBoundGenericArgument) {
					if (member instanceof IRGenericTypeParameter)
						genericArguments.addTypeBoundArgument((IRGenericTypeParameter) member, (IRTypeBoundGenericArgument) value);
					// else error // TODO
				} else {
					assert value instanceof IRValueGenericArgument;
					genericArguments.addValueArgument(member, (IRValueGenericArgument) value);
				}
			}
		}
		return baseTypeIR.getGenericUse(genericArguments);
	}
	
}
