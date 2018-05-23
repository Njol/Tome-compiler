package ch.njol.brokkr.ir;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ast.ASTElementPart;
import ch.njol.brokkr.ast.ASTInterfaces.ASTTypeDeclaration;
import ch.njol.brokkr.ast.ASTMembers.ASTTemplate;
import ch.njol.brokkr.ir.definitions.IRAttributeDefinition;
import ch.njol.brokkr.ir.definitions.IRParameterRedefinition;
import ch.njol.brokkr.ir.definitions.IRResultRedefinition;
import ch.njol.brokkr.ir.definitions.IRTypeDefinition;
import ch.njol.brokkr.ir.definitions.IRUnknownTypeDefinition;

public class IRBrokkrTemplate extends AbstractIRElement implements IRAttributeDefinition {
	
	private final ASTTemplate ast;
	private final IRTypeDefinition declaringType;
	
	public IRBrokkrTemplate(final ASTTemplate ast) {
		this.ast = registerDependency(ast);
		final ASTTypeDeclaration type = ast.getParentOfType(ASTTypeDeclaration.class);
		declaringType = registerDependency(type != null ? type.getIR() : new IRUnknownTypeDefinition(getIRContext(), "Internal compiler error (template not in type: " + this + ")", ast));
	}
	
	@Override
	public String name() {
		return "" + ast.name;
	}
	
	@Override
	public boolean isStatic() {
		return true;
	}
	
	@Override
	public IRTypeDefinition declaringType() {
		return declaringType;
	}
	
	@Override
	public @Nullable ASTElementPart getLinked() {
		return ast;
	}
	
	@Override
	public String hoverInfo() {
		return "template " + name();
	}
	
	@Override
	public List<IRParameterRedefinition> parameters() {
		return ast.parameters.stream().map(p -> p.getIR()).collect(Collectors.toList());
	}
	
	@Override
	public List<IRResultRedefinition> results() {
		return Collections.EMPTY_LIST;
	}
	
	@Override
	public List<IRError> errors() {
		return Collections.EMPTY_LIST;
	}
	
	@Override
	public boolean isModifying() {
		return false;
	}
	
	@Override
	public boolean isVariable() {
		return false;
	}
	
	@Override
	public IRContext getIRContext() {
		return ast.getIRContext();
	}
	
}
