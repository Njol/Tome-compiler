package ch.njol.tome.ast.expressions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTElement;
import ch.njol.tome.ast.ASTElementPart;
import ch.njol.tome.ast.ASTInterfaces.ASTElementWithVariables;
import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.ASTInterfaces.ASTMethodCall;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeDeclaration;
import ch.njol.tome.ast.ASTLink;
import ch.njol.tome.ast.AbstractASTElementWithIR;
import ch.njol.tome.ast.statements.ASTVariableDeclarations;
import ch.njol.tome.ast.statements.ASTVariableDeclarations.ASTVariableDeclarationsVariable;
import ch.njol.tome.common.ContentAssistProposal;
import ch.njol.tome.common.DebugString;
import ch.njol.tome.compiler.Token;
import ch.njol.tome.compiler.Token.LowercaseWordToken;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.definitions.IRMemberRedefinition;
import ch.njol.tome.ir.definitions.IRVariableOrAttributeRedefinition;
import ch.njol.tome.ir.definitions.IRVariableRedefinition;
import ch.njol.tome.ir.expressions.IRAttributeAccess;
import ch.njol.tome.ir.expressions.IRExpression;
import ch.njol.tome.ir.expressions.IRThis;
import ch.njol.tome.ir.expressions.IRUnknownExpression;
import ch.njol.tome.ir.expressions.IRVariableExpression;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.ir.uses.IRUnknownTypeUse;
import ch.njol.tome.parser.Parser;
import ch.njol.tome.util.StringMatcher;

/**
 * A variable of unqualified attribute. Since both are just a lowercase word, these cases cannot be distinguished before linking.
 * <p>
 * Also handles unqualified attribute calls, but not unqualified meta accesses (see {@link ASTUnqualifiedMetaAccess})
 */
public class ASTVariableOrUnqualifiedAttributeUse extends AbstractASTElementWithIR<IRExpression> implements ASTExpression<IRExpression>, ASTMethodCall, DebugString {
	
	public @Nullable ASTVariableOrUnqualifiedAttributeUseLink varOrAttributeLink;
	
	public static class ASTVariableOrUnqualifiedAttributeUseLink extends ASTLink<IRVariableOrAttributeRedefinition> {
		@Override
		protected @Nullable IRVariableOrAttributeRedefinition tryLink(final String name) {
			for (ASTElement p = parent(); p != null; p = p.parent()) {
				if (p instanceof ASTBlock) {
					// note: does not care about order of variable use and declaration - TODO either check this here or just let the semantic checker handle it
					for (final ASTVariableDeclarations vars : ((ASTBlock) p).getDirectChildrenOfType(ASTVariableDeclarations.class)) {
						for (final ASTVariableDeclarationsVariable var : vars.variables) {
							final LowercaseWordToken nameToken = var.nameToken;
							if (nameToken != null && name.equals(nameToken.word))
								return var.getIR();
						}
					}
				}
				if (p instanceof ASTElementWithVariables) {
					final IRVariableRedefinition var = ((ASTElementWithVariables) p).getVariableByName(name);
					if (var != null)
						return var;
				}
				if (p instanceof ASTTypeDeclaration) {
					final IRAttributeRedefinition attribute = ((ASTTypeDeclaration<?>) p).getIR().getAttributeByName(name);
					if (attribute != null)
						return attribute;
//						else
//							System.out.println(((ASTTypeDeclaration) p).getIR() + ": " + ((ASTTypeDeclaration) p).getIR().allInterfaces() + " :: " + ((ASTTypeDeclaration) p).getIR().members()); // FIXME debug
				}
			}
			// TODO semantic error; maybe set directly in Link: (copied from old code, so needs modification)
			//if (arguments.size() > 0)
			//error(m + " is not a method");
			return null;
		}
		
		private static ASTVariableOrUnqualifiedAttributeUseLink parse(final Parser parent) {
			return parseAsVariableIdentifier(new ASTVariableOrUnqualifiedAttributeUseLink(), parent);
		}
	}
	
	public List<ASTArgument> arguments = new ArrayList<>();
	
	public static ASTVariableOrUnqualifiedAttributeUse parse(final Parser parent) {
		return parent.one(p -> {
			final ASTVariableOrUnqualifiedAttributeUse ast = new ASTVariableOrUnqualifiedAttributeUse();
			ast.varOrAttributeLink = ASTVariableOrUnqualifiedAttributeUseLink.parse(p);
			p.tryGroup('(', () -> {
				int i = 0;
				do {
					ast.arguments.add(ASTArgument.parse(p, i));
					i++;
				} while (p.try_(','));
			}, ')');
			return ast;
		});
	}
	
	public @Nullable IRVariableOrAttributeRedefinition varOrAttribute() {
		return varOrAttributeLink != null ? varOrAttributeLink.get() : null;
	}
	
	@Override
	public String debug() {
		return "" + varOrAttribute();
	}
	
	@Override
	public String toString() {
		return "" + varOrAttributeLink;
	}
	
	@Override
	public @Nullable String hoverInfo(final Token token) {
		final IRVariableOrAttributeRedefinition varOrAttrib = varOrAttribute();
		if (varOrAttrib == null)
			return null;
		return varOrAttrib.hoverInfo();
	}
	
	@Override
	public IRTypeUse getIRType() {
		final IRVariableOrAttributeRedefinition variableOrAttributeRedefinition = varOrAttribute();
		return variableOrAttributeRedefinition == null ? new IRUnknownTypeUse(getIRContext()) : variableOrAttributeRedefinition.mainResultType();
	}
	
	@Override
	public @Nullable IRAttributeRedefinition attribute() {
		final IRVariableOrAttributeRedefinition varOrAttr = varOrAttribute();
		return varOrAttr instanceof IRAttributeRedefinition ? (IRAttributeRedefinition) varOrAttr : null;
	}
	
	@Override
	protected IRExpression calculateIR() {
		final IRVariableOrAttributeRedefinition varOrAttribute = varOrAttribute();
		if (varOrAttribute == null) {
			final ASTTypeDeclaration<?> selfAST = getParentOfType(ASTTypeDeclaration.class);
			ASTElementPart location = varOrAttributeLink != null ? varOrAttributeLink.getNameToken() : null;
			if (location == null)
				location = this;
			return new IRUnknownExpression("Cannot find an attribute with the name " + varOrAttributeLink + " in the type " + (selfAST == null ? "<unknown type>" : selfAST.name()), location);
		}
		if (varOrAttribute instanceof IRVariableRedefinition)
			return new IRVariableExpression((IRVariableRedefinition) varOrAttribute);
		else
			return new IRAttributeAccess(IRThis.makeNew(this), (IRAttributeRedefinition) varOrAttribute, ASTArgument.makeIRArgumentMap(((IRAttributeRedefinition) varOrAttribute).definition(), arguments),
					false, false, false);
	}
	
	@Override
	public @Nullable Stream<ContentAssistProposal> getContentAssistProposals(final Token token, final StringMatcher matcher) {
		final List<ContentAssistProposal> result = new ArrayList<>();
		for (ASTElement p = parent(); p != null; p = p.parent()) {
			if (p instanceof ASTBlock) {
				// note: does not care about order of variable use and declaration - TODO either check this here or just let the semantic checker handle it
				for (final ASTVariableDeclarations vars : ((ASTBlock) p).getDirectChildrenOfType(ASTVariableDeclarations.class)) {
					for (final ASTVariableDeclarationsVariable var : vars.variables) {
						final LowercaseWordToken nameToken = var.nameToken;
						if (nameToken != null && matcher.matches(nameToken.word))
							result.add(new ContentAssistProposal(var.getIR(), nameToken.word));
					}
				}
			}
			if (p instanceof ASTElementWithVariables) {
				for (final IRVariableRedefinition var : ((ASTElementWithVariables) p).allVariables()) {
					if (matcher.matches(var.name()))
						result.add(new ContentAssistProposal(var, var.name()));
				}
			}
			if (p instanceof ASTTypeDeclaration) {
				for (final IRMemberRedefinition member : ((ASTTypeDeclaration<?>) p).getIR().members()) {
					if (member instanceof IRAttributeRedefinition && matcher.matches(member.name())) {
						if (result.stream().anyMatch(cap -> cap.getElementToShow() instanceof IRVariableRedefinition && ((IRVariableRedefinition) cap.getElementToShow()).name().equals(member.name())))
							result.add(new ContentAssistProposal(member, "this." + member.name()));
						else
							result.add(new ContentAssistProposal(member, member.name()));
					}
				}
			}
		}
		return result.stream();
	}
}
