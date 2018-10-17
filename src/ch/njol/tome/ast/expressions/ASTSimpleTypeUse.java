package ch.njol.tome.ast.expressions;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTElement;
import ch.njol.tome.ast.ASTInterfaces.ASTAttribute;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeDeclaration;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeExpression;
import ch.njol.tome.ast.ASTLink;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.ast.toplevel.ASTSourceFile;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.definitions.IRTypeDefinition;
import ch.njol.tome.ir.uses.IRGenericTypeAccess;
import ch.njol.tome.ir.uses.IRSelfTypeUse;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.ir.uses.IRUnknownTypeUse;
import ch.njol.tome.moduleast.ASTModule;
import ch.njol.tome.parser.Parser;

/**
 * A type use that is a single word, which can be either a normal type or a generic parameter.
 */
public class ASTSimpleTypeUse extends AbstractASTElement implements ASTTypeExpression {
	
	public @Nullable ASTSimpleTypeUseLink link;
	
	private static class ASTSimpleTypeUseLink extends ASTLink<IRTypeUse> {
		
		@Override
		protected @Nullable IRTypeUse tryLink(String name) {
			// A type is either defined in the same file, or imported.
			// From types in the same file only siblings and siblings of parents are valid candidates.
			ASTElement start = parent();
			if (start instanceof ASTTypeDeclaration) { // i.e. this type is in the declaration of a type as a parent or such // TODO improve this (documentation + implementation - currently depends on no elements between the type decl and this type)
				if (name.equals(((ASTTypeDeclaration) start).name()))
					return ((ASTTypeDeclaration) start).getIR().getUse();
				start = start.parent(); // prevent looking at members of the implementing/extending type (also prevents an infinite loop)
			}
			for (ASTElement p = start; p != null; p = p.parent()) {
				if (p instanceof ASTTypeDeclaration) {
					final IRTypeDefinition type = ((ASTTypeDeclaration) p).getIR();
					// generic types
					final IRAttributeRedefinition attribute = type.getAttributeByName(name);
					if (attribute != null) {
						final IRSelfTypeUse self = IRSelfTypeUse.makeNew(p);
						return new IRGenericTypeAccess(self, attribute.getUse(self));
					}
					// inner classes and interfaces // TODO make part of interpreted or not?
					final List<ASTTypeDeclaration> declarations = ((ASTTypeDeclaration) p).getDirectChildrenOfType(ASTTypeDeclaration.class);
					for (final ASTTypeDeclaration d : declarations) {
						if (name.equals(d.name())) {
							return d.getIR().getUse();
						}
					}
				} else if (p instanceof ASTSourceFile) {
					final ASTModule m = ((ASTSourceFile) p).module;
					if (m == null)
						return null;
					final IRTypeDefinition typeDefinition = m.getType(name);
					return typeDefinition == null ? null : typeDefinition.getUse();
				} else if (p instanceof ASTAttribute) {
					if (name.equals(((ASTAttribute) p).name())) {
						final IRSelfTypeUse self = IRSelfTypeUse.makeNew(p);
						return new IRGenericTypeAccess(self, ((ASTAttribute) p).getIR().getUse(self));
					}
//							for (final ASTGenericTypeDeclaration gp : ((ASTAttribute) p).modifiers().genericParameters) {
//								if (name.equals(gp.name()))
//									return gp.getIR();
//							}
				}
			}
			return null;
		}
		
		static ASTSimpleTypeUseLink parse(Parser parent) {
			return parseAsTypeIdentifier(new ASTSimpleTypeUseLink(), parent);
		}
		
	}
	
	@Override
	public String toString() {
		return "" + link;
	}
	
	public static ASTSimpleTypeUse parse(final Parser parent) {
		return parent.one(p -> {
			final ASTSimpleTypeUse ast = new ASTSimpleTypeUse();
			ast.link = ASTSimpleTypeUseLink.parse(p);
			return ast;
		});
	}
	
	@Override
	public IRTypeUse getIR() {
		final IRTypeUse type = link != null ? link.get() : null;
		return type == null ? new IRUnknownTypeUse(getIRContext()) : type;
	}
	
}
