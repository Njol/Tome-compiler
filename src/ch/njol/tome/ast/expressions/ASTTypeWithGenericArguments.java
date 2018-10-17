package ch.njol.tome.ast.expressions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.njol.tome.ast.ASTInterfaces.ASTTypeExpression;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.ir.IRGenericArgument;
import ch.njol.tome.ir.definitions.IRAttributeDefinition;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.parser.Parser;
import ch.njol.util.StringUtils;

/**
 * A type use with generic arguments, e.g. 'A<B, C: D>'
 */
public class ASTTypeWithGenericArguments extends AbstractASTElement implements ASTTypeExpression {
	
	public final ASTSimpleTypeUse baseType;
	
	public ASTTypeWithGenericArguments(final ASTSimpleTypeUse baseType) {
		this.baseType = baseType;
	}
	
	public final List<ASTGenericArgument> genericArguments = new ArrayList<>();
	
	@Override
	public String toString() {
		return baseType + "<" + StringUtils.join(genericArguments, ",") + ">";
	}
	
	public static ASTTypeExpression finishParsingWithModifiers(final Parser p, final ASTTypeExpression withModifiers) {
		if (withModifiers instanceof ASTSimpleTypeUse)
			return finishParsing(p, (ASTSimpleTypeUse) withModifiers);
		p.unparse(withModifiers);
		final ASTModifierTypeUse modifierTypeUse = (ASTModifierTypeUse) withModifiers;
		final ASTSimpleTypeUse typeUseElement = (ASTSimpleTypeUse) modifierTypeUse.type; // cast is valid, as the other case is constructed here
		assert typeUseElement != null; // shouldn't get here if this is null
		final ASTTypeWithGenericArguments generic = finishParsing(p, typeUseElement);
		modifierTypeUse.type = generic;
		modifierTypeUse.addChild(generic); // FIXME
		return p.done(modifierTypeUse);
	}
	
	public static ASTTypeExpression parse(Parser parent) {
		Parser p = parent.start();
		ASTSimpleTypeUse baseType = ASTSimpleTypeUse.parse(p);
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
	public IRTypeUse getIR() {
		final Map<IRAttributeDefinition, IRGenericArgument> genericArguments = new HashMap<>();
		final IRTypeUse baseTypeIR = baseType.getIR();
		for (int i = 0; i < this.genericArguments.size(); i++) {
			final ASTGenericArgument ga = this.genericArguments.get(i);
			final IRAttributeRedefinition attrib = ga.attribute(baseTypeIR, i);
			final ASTGenericArgumentValue value = ga.value;
			if (attrib == null || value == null)
				continue;
			genericArguments.put(attrib.definition(), value.getIR());
		}
		return baseTypeIR.getGenericUse(genericArguments);
	}
}
