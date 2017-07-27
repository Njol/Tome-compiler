package ch.njol.brokkr.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ast.ASTExpressions.ASTTypeExpressions;
import ch.njol.brokkr.ast.ASTInterfaces.ASTGenericParameter;
import ch.njol.brokkr.ast.ASTInterfaces.ASTTypeDeclaration;
import ch.njol.brokkr.ast.ASTInterfaces.ASTTypeExpression;
import ch.njol.brokkr.ast.ASTInterfaces.ASTTypeUse;
import ch.njol.brokkr.ast.ASTMembers.ASTGenericTypeDeclaration;
import ch.njol.brokkr.ast.ASTMembers.ASTMember;
import ch.njol.brokkr.ast.ASTMembers.ASTMemberModifiers;
import ch.njol.brokkr.ast.ASTMembers.ASTTemplate;
import ch.njol.brokkr.ast.ASTStatements.ASTCodeGenerationCall;
import ch.njol.brokkr.common.ModuleIdentifier;
import ch.njol.brokkr.common.Visibility;
import ch.njol.brokkr.compiler.Module;
import ch.njol.brokkr.compiler.Modules;
import ch.njol.brokkr.compiler.ParseException;
import ch.njol.brokkr.compiler.Token;
import ch.njol.brokkr.compiler.Token.LowercaseWordToken;
import ch.njol.brokkr.compiler.Token.UppercaseWordToken;
import ch.njol.brokkr.compiler.Token.WordToken;
import ch.njol.brokkr.compiler.TokenStream;
import ch.njol.brokkr.interpreter.Interpreter;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.ir.definitions.IRBrokkrClass;
import ch.njol.brokkr.ir.definitions.IRBrokkrInterface;
import ch.njol.brokkr.ir.definitions.IRGenericTypeRedefinition;
import ch.njol.brokkr.ir.definitions.IRMemberRedefinition;
import ch.njol.brokkr.ir.definitions.IRTypeDefinition;
import ch.njol.brokkr.ir.uses.IRAndTypeUse;
import ch.njol.brokkr.ir.uses.IRTypeUse;

public class ASTTopLevelElements {
	
	public static class ASTBrokkrFile extends AbstractASTElement<ASTBrokkrFile> {
		public final String identifier;
		public final Modules modules;
		public @Nullable Module module;
		
		public @Nullable ASTModuleDeclaration moduleDeclaration;
		public List<ASTElement> declarations = new ArrayList<>();
		
		public ASTBrokkrFile(final Modules modules, final String identifier) {
			this.modules = modules;
			this.identifier = identifier;
		}
		
		@Override
		public String toString() {
			return "file";
		}
		
		@SuppressWarnings("null")
		@Override
		protected ASTBrokkrFile parse() throws ParseException {
			moduleDeclaration = one(ASTModuleDeclaration.class);
			module = moduleDeclaration.module == null ? null : modules.get(moduleDeclaration.module.identifier);
			if (module != null)
				module.registerFile(identifier, this);
			repeatUntilEnd(() -> {
				if (peekNext('$')) {
					declarations.add(one(ASTCodeGenerationCall.class));
					return;
				}
				final ASTTopLevelElementModifiers modifiers = one(ASTTopLevelElementModifiers.class);
				ASTElement declaration;
				if (peekNext("interface"))
					declaration = one(new ASTInterfaceDeclaration(modifiers.toMemberModifiers()));
				else if (peekNext("class"))
					declaration = one(new ASTClassDeclaration(modifiers.toMemberModifiers()));
//				else if (peekNext("enum"))
//					declaration = one(new EnumDeclaration(modifiers));
				else if (peekNext("extension"))
					declaration = one(new ASTExtensionDeclaration(modifiers));
				else if (peekNext("alias"))
					declaration = one(new ASTTypeAliasDeclaration(modifiers));
				else if (peekNext("code") || peekNext("member") || peekNext("type"))
					declaration = one(new ASTTemplate(modifiers.toMemberModifiers()));
				else
					declaration = null;
				if (declaration != null)
					declarations.add(declaration);
				assert declaration == null || modifiers.parent() != this : declaration;
			});
			return this;
		}
		
		public final static ASTBrokkrFile parseFile(final Modules modules, final String identifier, final TokenStream in) {
			final ASTBrokkrFile r = new ASTBrokkrFile(modules, identifier);
			try {
				r.parse(in);
			} catch (final ParseException e) {}
			r.compactFatalParseErrors();
			return r;
		}
		
		public final static @Nullable IRTypeDefinition getStandardType(final ASTElement context, final String module, final String type) {
			final Modules modules = getModules(context);
			if (modules == null)
				return null;
			final Module mod = modules.get(new ModuleIdentifier(module));
			if (mod == null)
				return null;
			return mod.getDeclaredType(type);
		}
		
		public final static @Nullable Modules getModules(final ASTElement context) {
			final ASTBrokkrFile file = context.getParentOfType(ASTBrokkrFile.class);
			if (file == null)
				return null;
			return file.modules;
		}
		
		public final static @Nullable Module getModule(final ASTElement context) {
			final ASTBrokkrFile file = context.getParentOfType(ASTBrokkrFile.class);
			if (file == null)
				return null;
			return file.module;
		}
		
		public final static Interpreter getInterpreter(final ASTElement context) {
			final Modules modules = getModules(context);
			if (modules == null)
				throw new InterpreterException("missing module");
			return modules.interpreter;
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
	
	public static class ASTModuleIdentifier extends AbstractASTElement<ASTModuleIdentifier> {
		public ModuleIdentifier identifier = new ModuleIdentifier();
		
		public ASTModuleIdentifier() {}
		
		@Override
		public String toString() {
			return "" + identifier;
		}
		
		@Override
		protected ASTModuleIdentifier parse() throws ParseException {
			identifier.parts.add(oneVariableIdentifier());
			while (true) {
				if (!peekNext('.'))
					break;
				final Token t = peekNext(1, true);
				if (t instanceof LowercaseWordToken) {
					next(); // skip '.'
					next(); // skip package name
					identifier.parts.add(((LowercaseWordToken) t).word);
				} else {
					break;
				}
			}
			return this;
		}
	}
	
	public static class ASTModuleDeclaration extends AbstractASTElement<ASTModuleDeclaration> {
		public @Nullable ASTModuleIdentifier module;
		
		@Override
		public String toString() {
			return "" + module;
		}
		
		@Override
		protected ASTModuleDeclaration parse() throws ParseException {
			one("module");
			until(() -> {
				module = one(ASTModuleIdentifier.class);
			}, ';', false);
			return this;
		}
	}
	
	public static class ASTTopLevelElementModifiers extends AbstractASTElement<ASTTopLevelElementModifiers> {
		public final List<ASTGenericTypeDeclaration> genericParameters = new ArrayList<>();
		public @Nullable Visibility visibility;
		public boolean isNative;
		
		@Override
		protected ASTTopLevelElementModifiers parse() throws ParseException {
			// modifiers
			unordered(() -> {
				tryGroup('<', () -> {
					do {
						genericParameters.add(one(new ASTGenericTypeDeclaration(null)));
					} while (try_(','));
				}, '>');
			}, () -> {
				isNative = try_("native");
			}, () -> {
				visibility = Visibility.parse(this);
			});
			return this;
		}
		
		public ASTMemberModifiers toMemberModifiers() {
			final ASTMemberModifiers r = new ASTMemberModifiers();
			r.genericParameters.addAll(genericParameters);
			r.visibility = visibility;
			r.isNative = isNative;
//			r.parts().addAll(parts());
			setParent(r);
			return r;
		}
	}
	
	// TODO make generics refer to an attribute instead (just a name, or an alias)
	// then generics can easily be much more general, e.g. Matrix<rows, columns, NumberType> becomes possible
	public static class ASTInterfaceDeclaration extends AbstractASTElement<ASTInterfaceDeclaration> implements ASTTypeDeclaration, ASTMember {
		public final ASTMemberModifiers modifiers;
		
		public @Nullable WordToken name;
		public List<ASTGenericParameterDeclaration> genericParameters = new ArrayList<>();
		public List<ASTTypeUse> parents = new ArrayList<>();
		public List<ASTMember> members = new ArrayList<>();
		
		public ASTInterfaceDeclaration(final ASTMemberModifiers modifiers) {
			this.modifiers = modifiers;
			modifiers.setParent(this);
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
					final Module module = ASTBrokkrFile.getModule(e);
					if (module != null && new ModuleIdentifier("lang").equals(module.id))
						return null;
				}
				return ASTBrokkrFile.getInterpreter(e).getTypeUse("lang", "Any");
			}
			return parents.stream().map(t -> t.staticallyKnownType()).reduce((t1, t2) -> new IRAndTypeUse(t1, t2)).get();
		}
		
		@Override
		public List<? extends ASTGenericParameter> genericParameters() {
			return genericParameters;
		}
		
		@Override
		public String toString() {
			return "" + name;
		}
		
		@Override
		protected ASTInterfaceDeclaration parse() throws ParseException {
			one("interface");
			until(() -> {
				name = oneTypeIdentifierToken();
				tryGroup('<', () -> {
					do {
						genericParameters.add(one(ASTGenericParameterDeclaration.class));
					} while (try_(','));
				}, '>');
				if (try_("extends")) {
					do {
						parents.add(ASTTypeExpressions.parse(this, false, false, true));
						// TODO if base != null infer type from that one (e.g. in 'dynamic Collection extends addable')?
					} while (try_(','));
				}
			}, '{', false);
			repeatUntil(() -> {
				members.add(ASTMember.parse(this));
			}, '}', true);
			return this;
		}
		
		private @Nullable IRBrokkrInterface interpreted = null;
		
		@Override
		public IRBrokkrInterface getIR() {
			if (interpreted != null)
				return interpreted;
			return interpreted = new IRBrokkrInterface(this);
		}
		
		@Override
		public boolean isInherited() {
			return false; // TODO allow to inherit non-private inner types?
		}
	}
	
	public static class ASTClassDeclaration extends AbstractASTElement<ASTClassDeclaration> implements ASTTypeDeclaration, ASTMember {
		public final ASTMemberModifiers modifiers;
		
		public @Nullable UppercaseWordToken name;
		public List<ASTGenericParameterDeclaration> genericParameters = new ArrayList<>();
		public List<ASTTypeUse> parents = new ArrayList<>();
		public List<ASTMember> members = new ArrayList<>();
		
		public ASTClassDeclaration(final ASTMemberModifiers modifiers) {
			this.modifiers = modifiers;
			modifiers.setParent(this);
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
		
		@Override
		protected ASTClassDeclaration parse() throws ParseException {
			one("class");
			until(() -> {
				name = oneTypeIdentifierToken();
				tryGroup('<', () -> {
					do {
						genericParameters.add(one(ASTGenericParameterDeclaration.class));
					} while (try_(','));
				}, '>');
				if (try_("implements")) {
					do {
						parents.add(ASTTypeExpressions.parse(this, false, false, true));
					} while (try_(','));
				}
			}, '{', false);
			repeatUntil(() -> {
				members.add(ASTMember.parse(this));
			}, '}', true);
			return this;
		}
		
		private @Nullable IRBrokkrClass interpreted = null;
		
		@Override
		public IRBrokkrClass getIR() {
			if (interpreted != null)
				return interpreted;
			return interpreted = new IRBrokkrClass(this);
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
//		protected EnumDeclaration parse() throws ParseException {
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
	
	public static class ASTExtensionDeclaration extends AbstractASTElement<ASTExtensionDeclaration> implements ASTTypeDeclaration {
		public final ASTTopLevelElementModifiers modifiers;
		
		public @Nullable UppercaseWordToken name;
		public List<ASTTypeUse> parents = new ArrayList<>();
		public List<ASTMember> members = new ArrayList<>();
		public @Nullable ASTTypeExpression extended;
		
		public ASTExtensionDeclaration(final ASTTopLevelElementModifiers modifiers) {
			this.modifiers = modifiers;
			modifiers.setParent(this);
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
		
		@Override
		protected ASTExtensionDeclaration parse() throws ParseException {
			one("extension");
			name = oneTypeIdentifierToken();
			//generics=GenericParameters? // deriving the generics of an extension is non-trivial (e.g. 'extension WeirdList<X> extends List<T extends Comparable<X> & Collection<X>>')
			one("extends");
			extended = ASTTypeExpressions.parse(this, true, false);
			if (try_("implements")) {
				do {
					parents.add(ASTTypeExpressions.parse(this, false, false, true));
				} while (try_(','));
			}
			oneRepeatingGroup('{', () -> {
				members.add(ASTMember.parse(this));
			}, '}');
			return this;
		}
		
		@Override
		public IRTypeDefinition getIR() {
			throw new InterpreterException("not implemented");
		}
	}
	
	// TODO remove this?
	public static class ASTTypeAliasDeclaration extends AbstractASTElement<ASTTypeAliasDeclaration> implements ASTTypeDeclaration {
		public final ASTTopLevelElementModifiers modifiers;
		
		public @Nullable WordToken name;
		public @Nullable ASTTypeExpression aliasOf;
		
		public ASTTypeAliasDeclaration(final ASTTopLevelElementModifiers modifiers) {
			this.modifiers = modifiers;
			modifiers.setParent(this);
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
			throw new InterpreterException("");
		}
		
		@Override
		public List<ASTGenericParameter> genericParameters() {
			return Collections.EMPTY_LIST; // TODO aliases should allow generic params too (e.g. 'alias Generator<T> = Procedure<[], T>')
		}
		
		@Override
		public String toString() {
			return "alias " + name;
		}
		
		@Override
		protected ASTTypeAliasDeclaration parse() throws ParseException {
			one("alias");
			until(() -> {
				name = oneTypeIdentifierToken();
//				tryGroup('<', () -> {
//					// TODO generic params
//				}, '>');
				one('=');
				aliasOf = ASTTypeExpressions.parse(this, false, false);
			}, ';', false);
			return this;
		}
		
		@Override
		public IRTypeDefinition getIR() {
			throw new InterpreterException("not implemented");
//			return aliasOf.interpret(new InterpreterContext(null));
		}
	}
	
	/**
	 * A declaration of a generic parameter of a type, i.e. a generic type that can be defined by position without a name after a type, e.g. 'List&lt;T>'.
	 */
	public static class ASTGenericParameterDeclaration extends AbstractASTElement<ASTGenericParameterDeclaration> implements ASTGenericParameter {
		public ASTLink<IRGenericTypeRedefinition> definition = new ASTLink<IRGenericTypeRedefinition>(this) {
			@Override
			protected @Nullable IRGenericTypeRedefinition tryLink(@NonNull final String name) {
				final ASTTypeDeclaration type = getParentOfType(ASTTypeDeclaration.class);
				if (type == null)
					return null;
				final IRMemberRedefinition m = type.getIR().getMemberByName(name);
				return m instanceof IRGenericTypeRedefinition ? (IRGenericTypeRedefinition) m : null;
			}
		};
		
		@Override
		public String toString() {
			return "" + definition.getName();
		}
		
		@Override
		protected ASTGenericParameterDeclaration parse() throws ParseException {
			definition.setName(oneTypeIdentifierToken());
			return this;
		}
		
		@Override
		public Variance variance() {
			return Variance.INVARIANT; // FIXME calculate variance from positions in attributes (arguments and results)
			// TODO what about uses as generic arguments in other types? (e.g. if List<T> has addAll(Collection<T>))
		}
		
		@Override
		public @Nullable IRGenericTypeRedefinition declaration() {
			return definition.get();
		}
		
	}
	
}
