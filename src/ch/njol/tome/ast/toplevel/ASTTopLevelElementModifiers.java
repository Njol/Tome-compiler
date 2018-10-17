package ch.njol.tome.ast.toplevel;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.ast.members.ASTMemberModifiers;
import ch.njol.tome.common.Visibility;
import ch.njol.tome.parser.Parser;

public class ASTTopLevelElementModifiers extends AbstractASTElement {
//		public final List<ASTGenericTypeDeclaration> genericParameters = new ArrayList<>();
	public @Nullable Visibility visibility;
	public boolean isNative;
	public boolean isTemplate;
	
	@Override
	public @NonNull String toString() {
		return /*(genericParameters.isEmpty() ? "" : "<" + StringUtils.join(genericParameters, ", ") + "> ")
				+*/ (visibility == null ? "" : visibility + " ") + (isNative ? "native " : "") + (isNative ? "template " : "");
	}
	
	public static ASTTopLevelElementModifiers startParsing(final Parser p) {
		final ASTTopLevelElementModifiers ast = new ASTTopLevelElementModifiers();
		p.unordered(/*() -> {
					tryGroup('<', () -> {
					do {
					genericParameters.add(one(new ASTGenericTypeDeclaration(null)));
					} while (try_(','));
					}, '>');
					}, */() -> {
			ast.isNative = p.try_("native");
		}, () -> {
			ast.visibility = Visibility.parse(p);
		}, () -> {
			ast.isTemplate = p.try_("template");
		});
		return ast;
	}
	
	public ASTTopLevelElementModifiers finish(final Parser p) {
		p.done(this);
		return this;
	}
	
	public ASTMemberModifiers finishToMemberModifiers(final Parser p) {
		final ASTMemberModifiers ast = new ASTMemberModifiers();
//			ast.genericParameters.addAll(genericParameters);
		ast.visibility = visibility;
		ast.isNative = isNative;
		ast.template = isTemplate;
		return p.done(ast);
	}
}
