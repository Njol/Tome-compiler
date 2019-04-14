package ch.njol.tome.ast.members;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTAttribute;
import ch.njol.tome.ast.ASTInterfaces.ASTError;
import ch.njol.tome.ast.ASTInterfaces.ASTResult;
import ch.njol.tome.ast.AbstractASTElementWithIR;
import ch.njol.tome.ast.expressions.ASTBlock;
import ch.njol.tome.ast.statements.ASTReturn;
import ch.njol.tome.compiler.Token;
import ch.njol.tome.compiler.Token.WordToken;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.definitions.IRBrokkrAttributeDefinition;
import ch.njol.tome.ir.definitions.IRBrokkrAttributeDefinitionAndImplementation;
import ch.njol.tome.ir.definitions.IRBrokkrAttributeImplementation;
import ch.njol.tome.ir.definitions.IRBrokkrAttributeRedefinition;
import ch.njol.tome.ir.definitions.IRMemberRedefinition;
import ch.njol.tome.ir.definitions.IRVariableRedefinition;
import ch.njol.tome.parser.Parser;
import ch.njol.util.StringUtils;

public class ASTAttributeDeclaration extends AbstractASTElementWithIR<IRAttributeRedefinition> implements ASTAttribute {
	
	public final ASTMemberModifiers modifiers;
	
	public @Nullable WordToken name;
	public boolean hasParameterDefinitions;
	public boolean hasParameterDots;
	public List<ASTSimpleParameter> parameters = new ArrayList<>();
	public boolean hasResultDefinitions;
	public boolean hasResultDots;
	public List<ASTResult> results = new ArrayList<>();
	public List<ASTErrorDeclaration> errors = new ArrayList<>();
	public boolean isAbstract;
	public @Nullable ASTBlock body;
	public List<ASTPrecondition> preconditions = new ArrayList<>();
	public List<ASTPostcondition> postconditions = new ArrayList<>();
	
	public ASTAttributeDeclaration(final ASTMemberModifiers modifiers) {
		this.modifiers = modifiers;
	}
	
	@Override
	public boolean isInherited() {
		return true;
	}
	
	@Override
	public @Nullable WordToken nameToken() {
		return name;
	}
	
	@Override
	public ASTMemberModifiers modifiers() {
		return modifiers;
	}
	
//		@Override
//		public List<? extends TypeDeclaration> declaredTypes() {
//			return modifiers.genericParameters;
//		}
//
//		@SuppressWarnings("null")
//		@Override
//		public @NonNull List<? extends @NonNull HasTypes> parentHasTypes() {
//			final IRMember a = modifiers.overridden.get();
//			return a != null && a instanceof IRAttributeRedefinition ? Arrays.asList((IRAttributeRedefinition) a) : Collections.EMPTY_LIST;
//		}
	
	@Override
	public List<? extends IRVariableRedefinition> allVariables() {
		final IRAttributeRedefinition interpreted = getIR();
		final List<IRVariableRedefinition> result = new ArrayList<>();
		result.addAll(interpreted.parameters());
		result.addAll(interpreted.results());
		return result;
	}
	
	@Override
	public List<? extends ASTError<?>> declaredErrors() {
		return errors;
	}
	
	@Override
	public List<? extends ASTResult> declaredResults() {
		return results;
	}
	
	@Override
	public String toString() {
		return name
				+ (parameters.size() == 0 ? "" : "(" + StringUtils.join(parameters, ", ") + ")")
				+ ": " + (results.size() == 0 ? "[]" : String.join(", ", results.stream().map(r -> r.toString()).toArray(i -> new String[i])));
	}
	
	@Override
	public @Nullable String hoverInfo(final Token token) {
		return getIR().hoverInfo();
	}
	
	@Override
	public IRAttributeRedefinition calculateIR() {
		final IRMemberRedefinition overridden = modifiers().overridden();
		if (overridden instanceof IRAttributeRedefinition) {
			if (body != null)
				return new IRBrokkrAttributeImplementation(this, (IRAttributeRedefinition) overridden);
			else
				return new IRBrokkrAttributeRedefinition(this, (IRAttributeRedefinition) overridden);
		} else {
//				if (overridden != null)
			// TODO semantic error
			if (body != null)
				return new IRBrokkrAttributeDefinitionAndImplementation(this);
			else
				return new IRBrokkrAttributeDefinition(this);
		}
	}
	
	public static ASTAttributeDeclaration finishParsing(final Parser p, final ASTMemberModifiers modifiers) {
		final ASTAttributeDeclaration ast = new ASTAttributeDeclaration(modifiers);
		
		// name
		ast.name = p.oneVariableIdentifierToken();
		
		// parameters
		p.tryGroup('(', () -> {
			ast.hasParameterDefinitions = true;
			if (p.try_("..."))
				ast.hasParameterDots = true;
			if (!ast.hasParameterDots || p.try_(',')) {
				int i = 0;
				do {
					ast.parameters.add(ASTSimpleParameter.parse(p, i));
					i++;
				} while (p.try_(','));
			}
		}, ')');
		
		// results
		if (p.try_(':')) {
			ast.hasResultDefinitions = true;
			if (p.try_("..."))
				ast.hasResultDots = true;
			if (!ast.hasResultDots || p.try_(',')) {
				do {
					ast.results.add(ASTNormalResult.parse(p));
				} while (p.try_(','));
			}
		}
		
		// errors
		while (p.peekNext('#')) {
			ast.errors.add(ASTErrorDeclaration.parse(p));
		}
		
		// body
		if (p.peekNext('{')) {
			ast.body = ASTBlock.parse(p);
		} else if (p.try_(';')) { // abstract / single expression syntax
			while (p.peekNext("requires"))
				ast.preconditions.add(ASTPrecondition.parse(p));
			while (p.peekNext("ensures"))
				ast.postconditions.add(ASTPostcondition.parse(p));
		} else if (!ast.hasResultDefinitions && ast.errors.isEmpty()) { // 'attribute = value' syntax (nonexistent or overridden result)
			p.one("=");
			final Parser bodyParser = p.start();
			final ASTReturn returnStatement = ASTReturn.parse(bodyParser, false);
			ast.body = bodyParser.done(new ASTBlock(returnStatement));
			while (p.peekNext("requires"))
				ast.preconditions.add(ASTPrecondition.parse(p));
			while (p.peekNext("ensures"))
				ast.postconditions.add(ASTPostcondition.parse(p));
		}
		
		return p.done(ast);
	}
	
}
