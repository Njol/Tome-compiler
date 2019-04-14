package ch.njol.tome.ast.members;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTTypeDeclaration;
import ch.njol.tome.ast.ASTInterfaces.NamedASTElement;
import ch.njol.tome.ast.ASTLink;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.ast.toplevel.ASTSourceFile;
import ch.njol.tome.common.MethodModifiability;
import ch.njol.tome.common.Visibility;
import ch.njol.tome.compiler.SourceCodeLinkable;
import ch.njol.tome.compiler.Token;
import ch.njol.tome.compiler.Token.LowercaseWordToken;
import ch.njol.tome.compiler.Token.UppercaseWordToken;
import ch.njol.tome.compiler.Token.WordToken;
import ch.njol.tome.ir.definitions.IRMemberRedefinition;
import ch.njol.tome.ir.definitions.IRTypeDefinition;
import ch.njol.tome.ir.uses.IRMemberUse;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.moduleast.ASTModule;
import ch.njol.tome.parser.Parser;

// TODO a correctness annotation that means that an attribute does not change (anymore), e.g. a field is "final" or a method always returns the same value, even if the object is modified.
public class ASTMemberModifiers extends AbstractASTElement {
	public final List<ASTGenericArgumentDeclaration> genericParameters = new ArrayList<>();
	public boolean override;
	public boolean partialOverride;
	public boolean hide;
	public boolean undefine;
	private @Nullable OverriddenFromTypeLink overriddenFromType;
	private @Nullable OverriddenLink overridden;
	public @Nullable Visibility visibility;
	public boolean isNative;
	public boolean isStatic;
	public boolean template;
	public @Nullable MethodModifiability modifiability;
	public boolean context;
	public boolean recursive;
	public boolean var;
	
	private static class OverriddenFromTypeLink extends ASTLink<IRTypeDefinition> {
		@Override
		protected @Nullable IRTypeDefinition tryLink(final String name) {
			final ASTSourceFile file = getParentOfType(ASTSourceFile.class);
			if (file == null)
				return null;
			final ASTModule module = file.module;
			return module == null ? null : module.getType(name);
		}
		
		private static OverriddenFromTypeLink parse(final Parser parent) {
			return parseAsTypeIdentifier(new OverriddenFromTypeLink(), parent);
		}
	}
	
	private static class OverriddenLink extends ASTLink<IRMemberRedefinition> {
		@Override
		protected @Nullable IRMemberRedefinition tryLink(final String name) {
			final ASTMemberModifiers mm = getParentOfType(ASTMemberModifiers.class);
			if (mm == null)
				return null;
			return mm.getOverriddenByName(name);
		}
		
		private static OverriddenLink parse(final Parser parent) {
			return parseAsAnyIdentifier(new OverriddenLink(), parent);
		}
	}
	
	@Override
	public String toString() {
		return (isNative ? "native " : "") + (isStatic ? "static " : "") + (visibility != null ? visibility + " " : "")
				+ (template ? "template " : "")
				+ (modifiability != null ? modifiability + " " : "") + (context ? "context " : "") + (recursive ? "recursive " : "") + (var ? "var " : "")
				+ (override ? "override " : partialOverride ? "partialOverride " : "")
				+ (hide ? "hide " : "") + (undefine ? "undefine " : "")
				+ (overriddenFromType != null ? overriddenFromType + "." : "")
				+ (overridden != null ? overridden + " as " : "");
	}
	
	@Override
	public @Nullable String hoverInfo(final Token token) {
		return null;
	}
	
	public @Nullable IRMemberRedefinition overridden() {
		if (overridden != null)
			return overridden.get();
		if (parent == null || !(parent instanceof NamedASTElement)) {
			System.out.println("Warning: parent <" + parent + "> of 'overridden' link is not a NamedASTElement");
			return null;
		}
		@SuppressWarnings("null")
		final String name = ((NamedASTElement) parent).name();
		if (name == null)
			return null;
		return getOverriddenByName(name);
	}
	
	private @Nullable IRMemberRedefinition getOverriddenByName(final String name) {
		final IRTypeDefinition fromType = overriddenFromType != null ? overriddenFromType.get() : null;
		if (fromType != null) {
			// TODO check if actually subtyped (and maybe check interfaces in-between, e.g. A.a overrides C.a, but A extends B extends C and B also defines a)
			return fromType.getMemberByName(name);
		} else {
			final ASTTypeDeclaration t = getParentOfType(ASTTypeDeclaration.class);
			if (t == null)
				return null;
//					// only check parents of the containing type (otherwise this method itself would be found)
			final IRTypeUse parent = t.parentTypes();
			if (parent == null)
				return null;
			final IRMemberUse member = parent.getMemberByName(name);
			return member != null ? member.redefinition() : null;
		}
	}
	
	@Override
	public @Nullable SourceCodeLinkable getLinked(final Token t) {
		if (t instanceof LowercaseWordToken && ((LowercaseWordToken) t).isKeyword() && ((LowercaseWordToken) t).word.equals("override"))
			return overridden();
		return super.getLinked(t);
	}
	
	public static ASTMemberModifiers parse(final Parser parent) {
		final Parser p = parent.start();
		final ASTMemberModifiers ast = new ASTMemberModifiers();
		// modifiers
		p.unordered(() -> {
			p.tryGroup('<', () -> {
				do {
					ast.genericParameters.add(ASTGenericArgumentDeclaration.parse(p));
				} while (p.try_(','));
			}, '>');
		}, () -> {
			ast.isStatic = p.try_("static");
		}, () -> {
			ast.template = p.try_("template");
		}, () -> {
			if (p.try_("override")) {
				ast.override = true;
				ast.hide = p.try_("hide");
				if (p.peekNext() instanceof UppercaseWordToken && p.peekNext('.', 1, true)) {
					ast.overriddenFromType = OverriddenFromTypeLink.parse(p);
					p.one('.');
					ast.overridden = OverriddenLink.parse(p);
					p.one("as");
				} else if (p.peekNext() instanceof WordToken && p.peekNext("as", 1, true)) {
					ast.overridden = OverriddenLink.parse(p);
					p.one("as");
				}
			} else if (p.try_("partialOverride")) { // TODO partial overrides should always be together (i.e. near each other in code), should I enforce this? (probably not)
				ast.partialOverride = true;
			}
			if (!ast.partialOverride)
				ast.undefine = p.try_("undefine");
		}, () -> {
			ast.isNative = p.try_("native");
		}, () -> {
			ast.visibility = Visibility.parse(p);
		}, () -> {
			// TODO modifiability for fields
			ast.modifiability = MethodModifiability.parse(p);
		}, () -> {
			ast.context = p.try_("context");
		}, () -> {
			ast.recursive = p.try_("recursive");
		}, () -> {
			ast.var = p.try_("var");
		});
		return p.done(ast);
	}
}
