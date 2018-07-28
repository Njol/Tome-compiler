package ch.njol.tome.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTExpressions.ASTTypeExpressions;
import ch.njol.tome.ast.ASTInterfaces.ASTGenericParameter;
import ch.njol.tome.ast.ASTInterfaces.ASTMember;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeDeclaration;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeExpression;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeUse;
import ch.njol.tome.ast.ASTMembers.ASTCodeGenerationCallMember;
import ch.njol.tome.ast.ASTMembers.ASTMemberModifiers;
import ch.njol.tome.ast.ASTMembers.ASTTemplate;
import ch.njol.tome.common.Invalidatable;
import ch.njol.tome.common.InvalidateListener;
import ch.njol.tome.common.ModuleIdentifier;
import ch.njol.tome.common.Visibility;
import ch.njol.tome.compiler.Modules;
import ch.njol.tome.compiler.Token;
import ch.njol.tome.compiler.Token.LowercaseWordToken;
import ch.njol.tome.compiler.Token.UppercaseWordToken;
import ch.njol.tome.compiler.Token.WordToken;
import ch.njol.tome.compiler.TokenList;
import ch.njol.tome.ir.IRContext;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.definitions.IRBrokkrClassDefinition;
import ch.njol.tome.ir.definitions.IRBrokkrInterfaceDefinition;
import ch.njol.tome.ir.definitions.IRMemberRedefinition;
import ch.njol.tome.ir.definitions.IRTypeDefinition;
import ch.njol.tome.ir.definitions.IRUnknownTypeDefinition;
import ch.njol.tome.ir.uses.IRAndTypeUse;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.moduleast.ASTModule;
import ch.njol.tome.parser.Parser;
import ch.njol.tome.util.ASTTokenStream;
import ch.njol.tome.util.TokenListStream;

public class ASTTopLevelElements {
	
	public static class ASTSourceFile extends AbstractASTElement implements InvalidateListener {
		public final String identifier;
		public final Modules modules;
		public @Nullable ASTModule module;
		
		public @Nullable ASTModuleDeclaration moduleDeclaration;
		public List<ASTElement> declarations = new ArrayList<>();
		
		public ASTSourceFile(final Modules modules, final String identifier) {
			this.modules = modules;
			this.identifier = identifier;
		}
		
		@Override
		public String toString() {
			return "file";
		}
		
		public final static ASTDocument<ASTSourceFile> parseFile(final Modules modules, final String identifier, final TokenList tokens) {
			final TokenListStream in = tokens.stream();
			final Parser documentParser = new Parser(in);
			final Parser p = documentParser.start();
			final ASTSourceFile ast = new ASTSourceFile(modules, identifier);
			ast.moduleDeclaration = ASTModuleDeclaration.parse(p);
			ast.updateModule();
			p.repeatUntilEnd(() -> {
				if (p.peekNext('$')) {
					ast.declarations.add(ASTCodeGenerationCallMember.parse(p));
					return;
				}
				final Parser declarationParser = p.start();
				final Parser modifierParser = declarationParser.start();
				final ASTTopLevelElementModifiers modifiers = ASTTopLevelElementModifiers.startParsing(modifierParser);
				ASTElement declaration;
				if (p.peekNext("interface"))
					declaration = ASTInterfaceDeclaration.finishParsing(declarationParser, modifiers.finishToMemberModifiers(modifierParser));
				else if (p.peekNext("class"))
					declaration = ASTClassDeclaration.finishParsing(declarationParser, modifiers.finishToMemberModifiers(modifierParser));
				//					else if (peekNext("enum"))
				//						declaration = one(new EnumDeclaration(modifiers));
				else if (p.peekNext("extension"))
					declaration = ASTExtensionDeclaration.finishParsing(declarationParser, modifiers.finish(modifierParser));
				else if (p.peekNext("alias"))
					declaration = ASTTypeAliasDeclaration.finishParsing(declarationParser, modifiers.finish(modifierParser));
				else if (p.peekNext("code") || p.peekNext("member") || p.peekNext("type"))
					declaration = ASTTemplate.finishParsing(declarationParser, modifiers.finishToMemberModifiers(modifierParser));
				else
					declaration = null;
				if (declaration != null) {
					ast.declarations.add(declaration);
				} else {
					modifiers.finish(modifierParser);
					declarationParser.doneAsChildren();
				}
				assert declaration == null || modifiers.parent() != ast : declaration;
			});
			p.done(ast);
			verifyTokenOrderInAST(ast, tokens);
			return documentParser.documentDone(ast);
		}
		
		private static void verifyTokenOrderInAST(final ASTElement ast, final TokenList tokens) {
			final ASTTokenStream astStream = new ASTTokenStream(ast);
			final TokenListStream listStream = tokens.stream();
			while (astStream.current() != null && !listStream.isAfterEnd()) {
				final Token fromAST = astStream.getAndMoveForward();
				final Token fromList = listStream.getAndMoveForward();
				assert fromAST == fromList;
			}
		}
		
		/**
		 * Updates the module link of this file and registers/unregister this file from the changed module(s), if any.
		 */
		public void updateModule() {
			final ASTModule oldModule = module;
			final ASTModuleDeclaration moduleDeclaration = this.moduleDeclaration;
			if (moduleDeclaration == null) {
				if (oldModule != null) {
					oldModule.clearFile(identifier);
					oldModule.removeInvalidateListener(this);
				}
				module = null;
				return;
			}
			final ASTModuleIdentifier moduleIdentifier = moduleDeclaration.module;
			final ASTModule newModule = module = moduleIdentifier == null ? null : modules.get(moduleIdentifier.identifier);
			if (oldModule == newModule)
				return;
			if (oldModule != null) {
				oldModule.clearFile(identifier);
				oldModule.removeInvalidateListener(this);
			}
			if (newModule != null) {
				newModule.registerFile(identifier, this);
				newModule.registerInvalidateListener(this);
			}
		}
		
		@Override
		public void onInvalidate(final Invalidatable source) {
			assert source instanceof ASTModule;
			updateModule(); // find new, valid module, and register this file again
			// no need to invalidate this whole file - any types or such taken from the module are depended on separately
		}
		
		@Override
		protected synchronized void invalidate() {
			super.invalidate();
			final ASTModule module = this.module;
			if (module != null) {
				module.clearFile(identifier);
				module.removeInvalidateListener(this);
			}
		}
		
		@Override
		public IRContext getIRContext() {
			return modules.irContext;
		}
		
//		@Override
//		public List<? extends TypeDeclaration> declaredTypes() {
//			return getDirectChildrenOfType(TypeDeclaration.class);
//		}
//
//		@SuppressWarnings("null")
//		@Override
//		public List<? extends HasTypes> parentHasTypes() {
//			return module == null ? Collections.EMPTY_LIST : Arrays.asList(module);
//		}
		
	}
	
	public static class ASTModuleIdentifier extends AbstractASTElement {
		public ModuleIdentifier identifier = new ModuleIdentifier();
		
		public ASTModuleIdentifier() {}
		
		@Override
		public String toString() {
			return "" + identifier;
		}
		
		public static @Nullable ASTModuleIdentifier tryParse(final Parser parent) {
			final Parser p = parent.start();
			final ASTModuleIdentifier ast = new ASTModuleIdentifier();
			final String firstPart = p.oneVariableIdentifier();
			if (firstPart == null) {
				p.expectedFatal("a module identifier");
				p.cancel();
				return null;
			}
			ast.identifier.parts.add(firstPart);
			while (true) {
				if (!p.peekNext('.'))
					break;
				final Token t = p.peekNext(1, true);
				if (t instanceof LowercaseWordToken) {
					p.next(); // skip '.'
					p.next(); // skip package name
					ast.identifier.parts.add(((LowercaseWordToken) t).word);
				} else {
					break;
				}
			}
			return p.done(ast);
		}
	}
	
	public static class ASTModuleDeclaration extends AbstractASTElement {
		public @Nullable ASTModuleIdentifier module;
		
		@Override
		public String toString() {
			return "" + module;
		}
		
		public static ASTModuleDeclaration parse(final Parser parent) {
			final Parser p = parent.start();
			final ASTModuleDeclaration ast = new ASTModuleDeclaration();
			p.one("module");
			p.until(() -> {
				ast.module = ASTModuleIdentifier.tryParse(p);
			}, ';', false);
			return p.done(ast);
		}
	}
	
	public static class ASTTopLevelElementModifiers extends AbstractASTElement {
//		public final List<ASTGenericTypeDeclaration> genericParameters = new ArrayList<>();
		public @Nullable Visibility visibility;
		public boolean isNative;
		
		@Override
		public @NonNull String toString() {
			return /*(genericParameters.isEmpty() ? "" : "<" + StringUtils.join(genericParameters, ", ") + "> ")
					+*/ (isNative ? "native " : "") + (visibility == null ? "" : visibility + " ");
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
			return p.done(ast);
		}
	}
	
	// TODO make generics refer to an attribute instead (just a name, or an alias)
	// then generics can easily be much more general, e.g. Matrix<rows, columns, NumberType> becomes possible
	public static class ASTInterfaceDeclaration extends AbstractASTElement implements ASTTypeDeclaration, ASTMember {
		public final ASTMemberModifiers modifiers;
		
		public @Nullable WordToken name;
		public List<ASTGenericParameterDeclaration> genericParameters = new ArrayList<>();
		public List<ASTTypeUse> parents = new ArrayList<>();
		public List<ASTMember> members = new ArrayList<>();
		
		public ASTInterfaceDeclaration(final ASTMemberModifiers modifiers) {
			this.modifiers = modifiers;
		}
		
		@Override
		public @Nullable WordToken nameToken() {
			return name;
		}
		
		@Override
		public List<? extends ASTMember> declaredMembers() {
			return members;
		}
		
		@Override
		public @Nullable IRTypeUse parentTypes() {
			return parentTypes(this, parents);
		}
		
		/**
		 * @param e
		 * @param parents
		 * @return The parent types of this type, or null if this represents the Any type which has no further parents
		 */
		public final static @Nullable IRTypeUse parentTypes(final ASTElement e, final List<? extends ASTTypeUse> parents) {
			if (parents.isEmpty()) {
				if (e instanceof ASTInterfaceDeclaration && "Any".equals(((ASTInterfaceDeclaration) e).name())) {
					final ASTSourceFile file = e.getParentOfType(ASTSourceFile.class);
					ASTModuleDeclaration md;
					if (file != null && (md = file.moduleDeclaration) != null && new ModuleIdentifier("lang").equals(md.module))
						return null; // only [lang.Any] has no parent
				}
				return e.getIRContext().getTypeUse("lang", "Any");
			}
			return parents.stream().map(t -> t.getIR()).reduce((t1, t2) -> IRAndTypeUse.makeNew(t1, t2)).get();
		}
		
		@Override
		public List<? extends ASTGenericParameter> genericParameters() {
			return genericParameters;
		}
		
		@Override
		public String toString() {
			return "" + name;
		}
		
		public static ASTInterfaceDeclaration finishParsing(final Parser p, final ASTMemberModifiers modifiers) {
			final ASTInterfaceDeclaration ast = new ASTInterfaceDeclaration(modifiers);
			p.one("interface");
			p.until(() -> {
				ast.name = p.oneTypeIdentifierToken();
				p.tryGroup('<', () -> {
					do {
						ast.genericParameters.add(ASTGenericParameterDeclaration.parse(p));
					} while (p.try_(','));
				}, '>');
				if (p.try_("extends")) {
					do {
						ast.parents.add(ASTTypeExpressions.parse(p, false, false, true));
						// TODO if base != null infer type from that one (e.g. in 'dynamic Collection extends addable')?
					} while (p.try_(','));
				}
			}, '{', false);
			p.repeatUntil(() -> {
				ast.members.add(ASTMembers.parse(p));
			}, '}', true);
			return p.done(ast);
		}
		
		private @Nullable IRBrokkrInterfaceDefinition ir = null;
		
		@Override
		public IRBrokkrInterfaceDefinition getIR() {
			if (ir != null)
				return ir;
			return ir = new IRBrokkrInterfaceDefinition(this);
		}
		
		@Override
		public List<? extends IRMemberRedefinition> getIRMembers() {
			return Collections.EMPTY_LIST;
		}
		
		@Override
		public boolean isInherited() {
			return false; // TODO allow to inherit non-private inner types?
		}
	}
	
	public static class ASTClassDeclaration extends AbstractASTElement implements ASTTypeDeclaration, ASTMember {
		public final ASTMemberModifiers modifiers;
		
		public @Nullable UppercaseWordToken name;
		public List<ASTGenericParameterDeclaration> genericParameters = new ArrayList<>();
		public List<ASTTypeUse> parents = new ArrayList<>();
		public List<ASTMember> members = new ArrayList<>();
		
		public ASTClassDeclaration(final ASTMemberModifiers modifiers) {
			this.modifiers = modifiers;
		}
		
		@Override
		public @Nullable WordToken nameToken() {
			return name;
		}
		
		@Override
		public List<? extends ASTMember> declaredMembers() {
			return members;
		}
		
		@Override
		public @Nullable IRTypeUse parentTypes() {
			return ASTInterfaceDeclaration.parentTypes(this, parents);
		}
		
		@Override
		public List<? extends ASTGenericParameter> genericParameters() {
			return genericParameters;
		}
		
		@Override
		public String toString() {
			return "" + name;
		}
		
		public static ASTClassDeclaration finishParsing(final Parser p, final ASTMemberModifiers modifiers) {
			final ASTClassDeclaration ast = new ASTClassDeclaration(modifiers);
			p.one("class");
			p.until(() -> {
				ast.name = p.oneTypeIdentifierToken();
				p.tryGroup('<', () -> {
					do {
						ast.genericParameters.add(ASTGenericParameterDeclaration.parse(p));
					} while (p.try_(','));
				}, '>');
				if (p.try_("implements")) {
					do {
						ast.parents.add(ASTTypeExpressions.parse(p, false, false, true));
					} while (p.try_(','));
				}
			}, '{', false);
			p.repeatUntil(() -> {
				ast.members.add(ASTMembers.parse(p));
			}, '}', true);
			return p.done(ast);
		}
		
		private @Nullable IRBrokkrClassDefinition ir = null;
		
		@Override
		public IRBrokkrClassDefinition getIR() {
			if (ir != null)
				return ir;
			return ir = new IRBrokkrClassDefinition(this);
		}
		
		@Override
		public List<? extends IRMemberRedefinition> getIRMembers() {
			return Collections.EMPTY_LIST;
		}
		
		@Override
		public boolean isInherited() {
			return false;
		}
		
	}
	
//	public static class EnumDeclaration extends AbstractElement<EnumDeclaration> implements TypeDeclaration {
//		public final TopLevelElementModifiers modifiers;
//
//		public @Nullable UppercaseWordToken name;
//		public final List<ActualSimpleType> parents = new ArrayList<>();
//		public final List<Member> members = new ArrayList<>();
//
//		public EnumDeclaration(final TopLevelElementModifiers modifiers) {
//			this.modifiers = modifiers;
//			modifiers.setParent(this);
//		}
//
//		@Override
//		public @Nullable WordToken nameToken() {
//			return name;
//		}
//
//		// enum constants are like static fields, but not really.
//		@Override
//		public List<? extends Member> declaredMembers() {
//			return members;
//		}
//
//		@Override
//		public List<? extends TypeDeclaration> parentTypes() {
//			return InterfaceDeclaration.parentTypes(this, parents);
//		}
//
//		@Override
//		public boolean equalsType(Type other) {
//			return other == this;
//		}
//
//		@Override
//		public boolean isSubTypeOf(Type other) {
//			for (Type p : parents) {
//				if (p.isSubTypeOfOrEqual(other))
//					return true;
//			}
//			// FIXME 'Z extends X, Y' is a subtype of 'X & Y'!
//			return false;
//		}
//
//		@Override
//		public String toString() {
//			return "" + name;
//		}
//
//		@Override
//		protected EnumDeclaration parse() {
//			one("enum");
//			until(() -> {
//				name = oneTypeIdentifierToken();
//				if (try_("extends")) {
//					do {
//						parents.add(one(ActualSimpleType.class));
//					} while (try_(','));
//				}
//			}, '{', false);
//			repeatUntil(() -> {
//				members.add(Member.parse(this));
//			}, '}', true);
//			return this;
//		}
//	}
	
	public static class ASTExtensionDeclaration extends AbstractASTElement implements ASTTypeDeclaration {
		public final ASTTopLevelElementModifiers modifiers;
		
		public @Nullable UppercaseWordToken name;
		public List<ASTTypeUse> parents = new ArrayList<>();
		public List<ASTMember> members = new ArrayList<>();
		public @Nullable ASTTypeExpression extended;
		
		public ASTExtensionDeclaration(final ASTTopLevelElementModifiers modifiers) {
			this.modifiers = modifiers;
		}
		
		@Override
		public @Nullable WordToken nameToken() {
			return name;
		}
		
		@Override
		public @NonNull List<? extends ASTMember> declaredMembers() {
			return members;
		}
		
		@Override
		public @Nullable IRTypeUse parentTypes() {
			return ASTInterfaceDeclaration.parentTypes(this, parents);
		}
		
		@Override
		public List<? extends ASTGenericParameter> genericParameters() {
			return Collections.EMPTY_LIST;
		}
		
		@Override
		public String toString() {
			return "" + name;
		}
		
		public static ASTExtensionDeclaration finishParsing(final Parser p, final ASTTopLevelElementModifiers modifiers) {
			final ASTExtensionDeclaration ast = new ASTExtensionDeclaration(modifiers);
			p.one("extension");
			ast.name = p.oneTypeIdentifierToken();
			//generics=GenericParameters? // deriving the generics of an extension is non-trivial (e.g. 'extension WeirdList<X> extends List<T extends Comparable<X> & Collection<X>>')
			// could just make an error if it cannot be inferred, and allow it for the normal, simple cases
			p.one("extends");
			ast.extended = ASTTypeExpressions.parse(p, true, false);
			if (p.try_("implements")) {
				do {
					ast.parents.add(ASTTypeExpressions.parse(p, false, false, true));
				} while (p.try_(','));
			}
			p.oneRepeatingGroup('{', () -> {
				ast.members.add(ASTMembers.parse(p));
			}, '}');
			return p.done(ast);
		}
		
		@Override
		public IRTypeDefinition getIR() {
			return new IRUnknownTypeDefinition(getIRContext(), "not implemented", this);
		}
	}
	
	// TODO remove this? (can use a static variable instead)
	public static class ASTTypeAliasDeclaration extends AbstractASTElement implements ASTTypeDeclaration {
		public final ASTTopLevelElementModifiers modifiers;
		
		public @Nullable WordToken name;
		public @Nullable ASTTypeExpression aliasOf;
		
		public ASTTypeAliasDeclaration(final ASTTopLevelElementModifiers modifiers) {
			this.modifiers = modifiers;
		}
		
		@Override
		public @Nullable WordToken nameToken() {
			return name;
		}
		
		@Override
		public List<? extends ASTMember> declaredMembers() {
			return Collections.EMPTY_LIST;
		}
		
		@Override
		public @Nullable IRTypeUse parentTypes() {
			return null;
		}
		
		@Override
		public List<ASTGenericParameter> genericParameters() {
			return Collections.EMPTY_LIST; // TODO aliases should allow generic params too (e.g. 'alias Generator<T> = Procedure<[], T>')
		}
		
		@Override
		public String toString() {
			return "alias " + name;
		}
		
		public static ASTTypeAliasDeclaration finishParsing(final Parser p, final ASTTopLevelElementModifiers modifiers) {
			final ASTTypeAliasDeclaration ast = new ASTTypeAliasDeclaration(modifiers);
			p.one("alias");
			p.until(() -> {
				ast.name = p.oneTypeIdentifierToken();
//				tryGroup('<', () -> {
//					// TODO generic params
//				}, '>');
				p.one('=');
				ast.aliasOf = ASTTypeExpressions.parse(p, false, false);
			}, ';', false);
			return p.done(ast);
		}
		
		@Override
		public IRTypeDefinition getIR() {
			return new IRUnknownTypeDefinition(getIRContext(), "not implemented", this);
//			return aliasOf.interpret(new InterpreterContext(null));
		}
	}
	
	/**
	 * A declaration of a generic parameter of a type, i.e. a generic type that can be defined by position without a name after a type, e.g. 'List&lt;T>'.
	 */
	public static class ASTGenericParameterDeclaration extends AbstractASTElement implements ASTGenericParameter {
		public ASTLink<IRAttributeRedefinition> definition = new ASTLink<IRAttributeRedefinition>(this) {
			@Override
			protected @Nullable IRAttributeRedefinition tryLink(@NonNull final String name) {
				final ASTTypeDeclaration type = getParentOfType(ASTTypeDeclaration.class);
				if (type == null)
					return null;
				return type.getIR().getAttributeByName(name);
			}
		};
		
		@Override
		public String toString() {
			return "" + definition.getName();
		}
		
		public static ASTGenericParameterDeclaration parse(final Parser parent) {
			final Parser p = parent.start();
			final ASTGenericParameterDeclaration ast = new ASTGenericParameterDeclaration();
			ast.definition.setName(p.oneTypeIdentifierToken());
			return p.done(ast);
		}
		
		@Override
		public Variance variance() {
			return Variance.INVARIANT; // FIXME calculate variance from positions in attributes (arguments and results)
			// TODO what about uses as generic arguments in other types? (e.g. if List<T> has addAll(Collection<T>))
		}
		
		@Override
		public @Nullable IRAttributeRedefinition declaration() {
			return definition.get();
		}
		
	}
	
}
