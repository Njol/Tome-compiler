package ch.njol.brokkr.interpreter.definitions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.compiler.SourceCodeLinkable;
import ch.njol.brokkr.compiler.ast.Element;
import ch.njol.brokkr.compiler.ast.ElementPart;
import ch.njol.brokkr.compiler.ast.Interfaces.TypeDeclaration;
import ch.njol.brokkr.compiler.ast.Interfaces.TypeUse;
import ch.njol.brokkr.compiler.ast.Members.AttributeDeclaration;
import ch.njol.brokkr.compiler.ast.Members.Member;
import ch.njol.brokkr.compiler.ast.TopLevelElements.BrokkrFile;
import ch.njol.brokkr.compiler.ast.TopLevelElements.ModuleIdentifier;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeBrokkrTypeDefinition;

public abstract class AbstractInterpretedNativeBrokkrTypeDefinition implements InterpretedNativeBrokkrTypeDefinition, SourceCodeLinkable {
	
	protected final TypeDeclaration declaration;

	protected final List<InterpretedMemberRedefinition> members = new ArrayList<>();
	
	public AbstractInterpretedNativeBrokkrTypeDefinition(final TypeDeclaration declaration) {
		this.declaration = declaration;
		
		for (final Member m : declaration.declaredMembers()) {
			members.add(m.interpreted());
		}
	}
	
	public String name() {
		return "" + declaration.name();
	}
	
	@SuppressWarnings("null")
	@Override
	public ElementPart getLinked() {
		return declaration.nameToken();
	}

	@Override
	public List<InterpretedMemberRedefinition> members() {
		return members;
	}
	
	@Override
	public boolean equalsType(InterpretedNativeTypeDefinition other) {
		return this.getClass() == other.getClass() && this.declaration == ((AbstractInterpretedNativeBrokkrTypeDefinition) other).declaration;
	}

	@Override
	public boolean isSubtypeOfOrEqual(InterpretedNativeTypeDefinition other) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSupertypeOfOrEqual(InterpretedNativeTypeDefinition other) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
