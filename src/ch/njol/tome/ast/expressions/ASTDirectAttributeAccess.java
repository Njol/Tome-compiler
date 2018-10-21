package ch.njol.tome.ast.expressions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTMethodCall;
import ch.njol.tome.ast.ASTInterfaces.ASTTargettedExpression;
import ch.njol.tome.ast.ASTLink;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.ast.expressions.ASTExpressions.ASTAccess;
import ch.njol.tome.ast.statements.ASTLambdaMethodCall;
import ch.njol.tome.common.ContentAssistProposal;
import ch.njol.tome.compiler.Token;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.uses.IRAttributeUse;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.ir.uses.IRUnknownTypeUse;
import ch.njol.tome.parser.Parser;
import ch.njol.tome.util.StringMatcher;

public class ASTDirectAttributeAccess extends AbstractASTElement implements ASTAccess, ASTMethodCall, ASTTargettedExpression {
	
//		public boolean negated;
	public boolean allResults;
	
	public final List<ASTArgument> arguments = new ArrayList<>();
	
	/**
	 * This link is also used by {@link ASTAttributeAssignment} and {@link ASTLambdaMethodCall}
	 */
	public @Nullable ASTLink<IRAttributeRedefinition> attributeLink;
	
	private static class ASTDirectAttributeAccessLink extends ASTLink<IRAttributeRedefinition> {
		@Override
		protected @Nullable IRAttributeRedefinition tryLink(String name) {
			final ASTTargettedExpression accessExpression = getParentOfType(ASTTargettedExpression.class);
			if (accessExpression == null)
				return null;
			final IRTypeUse type = accessExpression.targetType();
			if (type == null)
				return null;
			final IRAttributeUse a = type.getAttributeByName(name);
//					if (a == null && name.endsWith("_"))
//						a = type.getAttributeByName("" + name.substring(0, name.length() - 1));
			return a == null ? null : a.redefinition();
		}
		
		@Override
		protected @Nullable String errorMessage(String name) {
			final ASTTargettedExpression accessExpression = getParentOfType(ASTTargettedExpression.class);
			if (accessExpression == null)
				return null;
			final IRTypeUse type = accessExpression.targetType();
			if (type == null)
				return null;
			return "Cannot find " + name + " in " + type;
		}
		
		private static ASTDirectAttributeAccessLink parse(Parser parent) {
			return parseAsVariableIdentifier(new ASTDirectAttributeAccessLink(), parent);
		}
	}
	
	@Override
	public String toString() {
		return /*(negated ? "!" : "") +*/ attributeLink + (arguments.size() == 0 ? "" : "(...)");
	}
	
	public static ASTDirectAttributeAccess parse(final Parser parent) {
		return parent.one(p -> {
			final ASTDirectAttributeAccess ast = new ASTDirectAttributeAccess();
//			negated = try_('!');
			ast.attributeLink = ASTDirectAttributeAccessLink.parse(p);
//			ast.allResults = name != null && name.word.endsWith("_"); //try_('!'); // FIXME other symbol - possible: !´`'¬@¦§°%_$£\     // maybe a combination of symbols? e.g. []
			final int[] i = {0};
			p.tryGroup('(', () -> {
				do {
					ast.arguments.add(ASTArgument.parse(p, i[0]));
					i[0]++;
				} while (p.try_(','));
			}, ')');
			return ast;
		});
	}
	
	@Override
	public @Nullable IRTypeUse targetType() {
		ASTAccessExpression accessExpression = getParentOfType(ASTAccessExpression.class);
		return accessExpression != null ? accessExpression.target.getIRType() : null;
	}
	
	@Override
	public @Nullable IRAttributeRedefinition attribute() {
		return attributeLink != null ? attributeLink.get() : null;
	}
	
	@Override
	public IRTypeUse getIRType() {
		final IRAttributeRedefinition attributeRedefinition = attribute();
		if (attributeRedefinition == null)
			return new IRUnknownTypeUse(getIRContext());
		return allResults ? attributeRedefinition.allResultTypes() : attributeRedefinition.mainResultType();
	}
	
	@Override
	public @Nullable Stream<ContentAssistProposal> getContentAssistProposals(final Token token,
			final StringMatcher matcher) {
		return parent != null ? parent.getContentAssistProposals(token, matcher) : null;
	}
	
}
