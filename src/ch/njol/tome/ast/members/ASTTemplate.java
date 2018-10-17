package ch.njol.tome.ast.members;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTAttribute;
import ch.njol.tome.ast.ASTInterfaces.ASTError;
import ch.njol.tome.ast.ASTInterfaces.ASTParameter;
import ch.njol.tome.ast.ASTInterfaces.ASTResult;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.ast.expressions.ASTBlock;
import ch.njol.tome.compiler.Token.LowercaseWordToken;
import ch.njol.tome.compiler.Token.WordToken;
import ch.njol.tome.ir.IRBrokkrTemplate;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.definitions.IRVariableRedefinition;
import ch.njol.tome.ir.nativetypes.IRTuple.IRTypeTuple;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.parser.Parser;

public class ASTTemplate extends AbstractASTElement implements ASTAttribute {
	public final ASTMemberModifiers modifiers;
	
	public @Nullable TemplateType templateType;
	public @Nullable LowercaseWordToken name;
	public List<ASTParameter> parameters = new ArrayList<>();
	public @Nullable ASTBlock body;
	
	enum TemplateType {
		TYPE, MEMBER, CODE;
	}
	
	public ASTTemplate(final ASTMemberModifiers modifiers) {
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
	
	@Override
	public List<? extends IRVariableRedefinition> allVariables() {
		return getIR().parameters();
	}
	
	@Override
	public List<ASTError> declaredErrors() {
		return Collections.EMPTY_LIST; // FIXME preconditions are errors too!
	}
	
	@Override
	public List<? extends ASTResult> declaredResults() {
		return Collections.EMPTY_LIST;
	}
	
	@Override
	public String toString() {
		return "" + name;
	}
	
	public static ASTTemplate finishParsing(final Parser p, final ASTMemberModifiers modifiers) {
		final ASTTemplate ast = new ASTTemplate(modifiers);
		final String templateType = p.oneOf("code", "member", "type");
		ast.templateType = templateType == null ? null : TemplateType.valueOf(templateType.toUpperCase(Locale.ENGLISH));
		p.one("template");
		ast.name = p.oneVariableIdentifierToken();
		p.tryGroup('(', () -> {
			int i = 0;
			do {
				ast.parameters.add(ASTSimpleParameter.parse(p, i));
				i++;
			} while (p.try_(','));
		}, ')');
		ast.body = ASTBlock.parse(p);
		return p.done(ast);
	}
	
	@Override
	public IRTypeUse getIRType() {
		return IRTypeTuple.emptyTuple(getIRContext());
	}
	
	@Override
	public IRAttributeRedefinition getIR() {
		return new IRBrokkrTemplate(this);
	}
}
