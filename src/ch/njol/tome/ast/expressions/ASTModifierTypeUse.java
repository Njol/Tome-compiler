package ch.njol.tome.ast.expressions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeExpression;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.common.Borrowing;
import ch.njol.tome.common.Exclusiveness;
import ch.njol.tome.common.Modifiability;
import ch.njol.tome.common.Optionality;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.ir.uses.IRUnknownTypeUse;
import ch.njol.tome.parser.Parser;
import ch.njol.util.StringUtils;

/**
 * A type use with modifiers, e.g. 'mod exclusive C'.
 */
public class ASTModifierTypeUse extends AbstractASTElement implements ASTTypeExpression {
	
	public final List<ASTModifierTypeUseModifier> modifiers = new ArrayList<>();
	/**
	 * Either a {@link ASTSimpleTypeUse} or a {@link ASTTypeWithGenericArguments}
	 */
	public @Nullable ASTTypeExpression type;
	
	public ASTModifierTypeUse(final ASTModifierTypeUseModifier firstModifier) {
		modifiers.add(firstModifier);
	}
	
	@Override
	public String toString() {
		return (modifiers.size() == 0 ? "" : StringUtils.join(modifiers, " ") + " ") + type;
	}
	
	public static ASTTypeExpression parse(Parser parent) {
		Parser p = parent.start();
		final ASTModifierTypeUseModifier modifier = ASTModifierTypeUseModifier.tryParse(p);
		if (modifier != null)
			return finishParsing(p, modifier);
		p.cancel();
		return ASTTypeWithGenericArguments.parse(parent);
	}
	
	public static ASTTypeExpression finishParsing(final Parser p, final ASTModifierTypeUseModifier firstModifier) {
		final ASTModifierTypeUse ast = new ASTModifierTypeUse(firstModifier);
		do {
			final ASTModifierTypeUseModifier e = ASTModifierTypeUseModifier.tryParse(p);
			if (e != null) {
				ast.modifiers.add(e);
				continue;
			}
		} while (false);
		
		// TODO is 'modifier Self' possible?
		ast.type = ASTTypeWithGenericArguments.parse(p);
		
		return p.done(ast);
	}
	
	@Override
	public IRTypeUse getIR() {
		final IRTypeUse result = type != null ? type.getIR() : new IRUnknownTypeUse(getIRContext());
		// TODO
//			for (final ModifierTypeUseModifierElement mod : modifiers) {
//				if (mod.modifiability != null) {
//					result.setModifiability(mod.modifiability);
//				} else if (mod.exclusivity != null) {
//					result.setExclusivity(mod.exclusivity);
//				}
//			}
		return result;
	}
	
	/**
	 * a type use modifier like modifiability or exclusivity, optionally copied from an expression
	 */
	public static class ASTModifierTypeUseModifier extends AbstractASTElement {
		
		public @Nullable Modifiability modifiability;
		public @Nullable Exclusiveness exclusivity;
		public @Nullable Optionality optional;
		public @Nullable Borrowing borrowing;
		
		public @Nullable ASTExpression from;
		
		@Override
		public String toString() {
			return (modifiability != null ? modifiability : exclusivity != null ? exclusivity : optional)
					+ (from == null ? "" : "@" + (from instanceof ASTVariableOrUnqualifiedAttributeUse || from instanceof ASTThis ? from : "(" + from + ")"));
		}
		
		public static @Nullable ASTModifierTypeUseModifier tryParse(final Parser parent) {
			final Parser p = parent.start();
			final ASTModifierTypeUseModifier ast = new ASTModifierTypeUseModifier();
			final Modifiability modifiability = Modifiability.parse(p);
			if (modifiability != null) {
				ast.modifiability = modifiability;
				return finishParsingFrom(p, ast);
			}
			final Exclusiveness exclusivity = Exclusiveness.parse(p);
			if (exclusivity != null) {
				ast.exclusivity = exclusivity;
				return finishParsingFrom(p, ast);
			}
			final Optionality optional = Optionality.parse(p);
			if (optional != null) {
				ast.optional = optional;
				return finishParsingFrom(p, ast);
			}
			final Borrowing borrowing = Borrowing.parse(p);
			if (borrowing != null) {
				ast.borrowing = borrowing;
				return finishParsingFrom(p, ast);
			}
			p.cancel();
			return null;
		}
		
		private static ASTModifierTypeUseModifier finishParsingFrom(final Parser p, final ASTModifierTypeUseModifier ast) {
			if (p.try_('@')) {
				if (!p.tryGroup('(', () -> {
					ast.from = ASTExpressions.parse(p);
				}, ')')) {
					if (p.peekNext("this"))
						ast.from = ASTThis.parse(p);
					else
						ast.from = ASTVariableOrUnqualifiedAttributeUse.parse(p);
				}
			}
			return p.done(ast);
		}
	}
	
}
