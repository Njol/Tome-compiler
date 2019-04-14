package ch.njol.tome.ast.toplevel;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.common.Visibility;
import ch.njol.tome.compiler.Token.UppercaseWordToken;
import ch.njol.tome.parser.Parser;

public class ASTDecoratorDeclaration extends AbstractASTElement {
	
	public @Nullable Visibility visibility;
	public @Nullable UppercaseWordToken name;
//	public List<ASTDecoratorParameterDeclaration> parameters = new ArrayList<>();
	
	public static ASTDecoratorDeclaration parse(Parser parent) {
		return parent.one(p -> {
			ASTDecoratorDeclaration ast = new ASTDecoratorDeclaration();
			p.one("decorator");
			ast.name = p.oneTypeIdentifierToken();
			p.oneGroup('{', () -> {
				// TODO what?
				
				// TODO think about all kind of use cases for code generation:
				// - templates for classes (with interfaces, methods, etc. just like a normal class, basically abstract classes but better)
				// - adding stuff to classes (e.g. generating an equals or hashCode method from all fields)
				// - generating a bunch of types (e.g. API for a library or web service)
				
			}, '}');
			return ast;
		});
	}
	
	@Override
	public String toString() {
		return "@" + name;
	}
	
}
