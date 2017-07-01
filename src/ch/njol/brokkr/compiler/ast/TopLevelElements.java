package ch.njol.brokkr.compiler.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.compiler.Module;
import ch.njol.brokkr.compiler.Modules;
import ch.njol.brokkr.compiler.ParseException;
import ch.njol.brokkr.compiler.Token;
import ch.njol.brokkr.compiler.Token.LowercaseWordToken;
import ch.njol.brokkr.compiler.Token.UppercaseWordToken;
import ch.njol.brokkr.compiler.Token.WordToken;
import ch.njol.brokkr.compiler.TokenStream;
import ch.njol.brokkr.compiler.ast.Expressions.TypeExpressions;
import ch.njol.brokkr.compiler.ast.Interfaces.GenericParameter;
import ch.njol.brokkr.compiler.ast.Interfaces.TypeDeclaration;
import ch.njol.brokkr.compiler.ast.Interfaces.TypeExpression;
import ch.njol.brokkr.compiler.ast.Interfaces.TypeUse;
import ch.njol.brokkr.compiler.ast.Members.GenericTypeDeclaration;
import ch.njol.brokkr.compiler.ast.Members.Member;
import ch.njol.brokkr.compiler.ast.Members.MemberModifiers;
import ch.njol.brokkr.compiler.ast.Members.Template;
import ch.njol.brokkr.compiler.ast.Statements.CodeGenerationCall;
import ch.njol.brokkr.data.Visibility;
import ch.njol.brokkr.interpreter.Interpreter;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.interpreter.definitions.InterpretedGenericTypeRedefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedMemberRedefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedNativeTypeDefinition;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeBrokkrClass;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeBrokkrInterface;
import ch.njol.brokkr.interpreter.uses.InterpretedAndTypeUse;
import ch.njol.brokkr.interpreter.uses.InterpretedSimpleTypeUse;
import ch.njol.brokkr.interpreter.uses.InterpretedTypeUse;

public class TopLevelElements {
	
	public static class BrokkrFile extends AbstractElement<BrokkrFile> {
		public final String identifier;
		public final Modules modules;
		public @Nullable Module module;
		
		public @Nullable ModuleDeclaration moduleDeclaration;
		public List<Element> declarations = new ArrayList<>();
		
		public BrokkrFile(final Modules modules, final String identifier) {
			this.modules = modules;
			this.identifier = identifier;
		}
		
		@Override
		public String toString() {
			return "file";
		}
		
		@SuppressWarnings("null")
		@Override
		protected BrokkrFile parse() throws ParseException {
			moduleDeclaration = one(ModuleDeclaration.class);
			module = moduleDeclaration.module == null ? null : modules.get(moduleDeclaration.module);
			if (module != null)
				module.registerFile(identifier, this);
			repeatUntilEnd(() -> {
				if (peekNext('$')) {
					declarations.add(one(CodeGenerationCall.class));
					return;
				}
				final TopLevelElementModifiers modifiers = one(TopLevelElementModifiers.class);
				Element declaration;
				if (peekNext("interface"))
					declaration = one(new InterfaceDeclaration(modifiers.toMemberModifiers()));
				else if (peekNext("class"))
					declaration = one(new ClassDeclaration(modifiers.toMemberModifiers()));
//				else if (peekNext("enum"))
//					declaration = one(new EnumDeclaration(modifiers));
				else if (peekNext("extension"))
					declaration = one(new ExtensionDeclaration(modifiers));
				else if (peekNext("alias"))
					declaration = one(new TypeAliasDeclaration(modifiers));
				else if (peekNext("code") || peekNext("member") || peekNext("type"))
					declaration = one(new Template(modifiers.toMemberModifiers()));
				else
					declaration = null;
				if (declaration != null)
					declarations.add(declaration);
				assert declaration == null || modifiers.parent() != this : declaration;
			});
			return this;
		}
		
		public final static BrokkrFile parseFile(final Modules modules, final String identifier, final TokenStream in) {
			final BrokkrFile r = new BrokkrFile(modules, identifier);
			try {
				r.parse(in);
			} catch (final ParseException e) {}
			r.compactFatalParseErrors();
			return r;
		}
		
		public final static @Nullable InterpretedNativeTypeDefinition getStandardType(final Element context, final String module, final String type) {
			final Modules modules = getModules(context);
			if (modules == null)
				return null;
			final Module mod = modules.get(new ModuleIdentifier(module));
			if (mod == null)
				return null;
			return mod.getDeclaredType(type);
		}
		
		public final static @Nullable Modules getModules(final Element context) {
			final BrokkrFile file = context.getParentOfType(BrokkrFile.class);
			if (file == null)
				return null;
			return file.modules;
		}
		
		public final static @Nullable Module getModule(final Element context) {
			final BrokkrFile file = context.getParentOfType(BrokkrFile.class);
			if (file == null)
				return null;
			return file.module;
		}
		
		public final static Interpreter getInterpreter(final Element context) {
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
	
	public static class ModuleIdentifier extends AbstractElement<ModuleIdentifier> {
		public List<String> parts = new ArrayList<>();
		
		public ModuleIdentifier() {}
		
		@SuppressWarnings("null")
		public ModuleIdentifier(final String parts) {
			this.parts.addAll(Arrays.asList(parts.split("\\.")));
		}
		
		@Override
		public String toString() {
			return "" + String.join(".", parts);
		}
		
		@Override
		protected ModuleIdentifier parse() throws ParseException {
			parts.add(oneVariableIdentifier());
			while (true) {
				if (!peekNext('.'))
					break;
				final Token t = peekNext(1, true);
				if (t instanceof LowercaseWordToken) {
					next(); // skip '.'
					next(); // skip package name
					parts.add(((LowercaseWordToken) t).word);
				} else {
					break;
				}
			}
			return this;
		}
		
		@Override
		public int hashCode() {
			return parts.hashCode();
		}
		
		@Override
		public boolean equals(@Nullable final Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final ModuleIdentifier other = (ModuleIdentifier) obj;
			return parts.equals(other.parts);
		}
	}
	
	public static class ModuleDeclaration extends AbstractElement<ModuleDeclaration> {
		public @Nullable ModuleIdentifier module;
		
		@Override
		public String toString() {
			return "" + module;
		}
		
		@Override
		protected ModuleDeclaration parse() throws ParseException {
			one("module");
			until(() -> {
				module = one(ModuleIdentifier.class);
			}, ';', false);
			return this;
		}
	}
	
	public static class TopLevelElementModifiers extends AbstractElement<TopLevelElementModifiers> {
		public final List<GenericTypeDeclaration> genericParameters = new ArrayList<>();
		public @Nullable Visibility visibility;
		public boolean isNative;
		
		@Override
		protected TopLevelElementModifiers parse() throws ParseException {
			// modifiers
			unordered(() -> {
				tryGroup('<', () -> {
					do {
						genericParameters.add(one(new GenericTypeDeclaration(null)));
					} while (try_(','));
				}, '>');
			}, () -> {
				isNative = try_("native");
			}, () -> {
				visibility = Visibility.parse(this);
			});
			return this;
		}
		
		public MemberModifiers toMemberModifiers() {
			final MemberModifiers r = new MemberModifiers();
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
	public static class InterfaceDeclaration extends AbstractElement<InterfaceDeclaration> implements TypeDeclaration {
		public final MemberModifiers modifiers;
		
		public @Nullable WordToken name;
		public Link<InterpretedNativeTypeDefinition> base = new Link<InterpretedNativeTypeDefinition>(this) {
			@Override
			protected @Nullable InterpretedNativeTypeDefinition tryLink(final String name) {
				@SuppressWarnings("null")
				final Module module = getParentOfType(BrokkrFile.class).module;
				return module == null ? null : module.getType(name);
			}
		};
		public List<GenericParameterDeclaration> genericParameters = new ArrayList<>();
		public List<TypeUse> parents = new ArrayList<>();
		public List<Member> members = new ArrayList<>();
		
		public InterfaceDeclaration(final MemberModifiers modifiers) {
			this.modifiers = modifiers;
			modifiers.setParent(this);
		}
		
		@Override
		public @Nullable WordToken nameToken() {
			return name;
		}
		
		@Override
		public List<? extends Member> declaredMembers() {
			return members;
		}
		
		@SuppressWarnings("null")
		@Override
		public InterpretedTypeUse parentTypes() {
			InterpretedTypeUse parents = parentTypes(this, this.parents);
			if (base.get() != null)
				parents = new InterpretedAndTypeUse(new InterpretedSimpleTypeUse(base.get()), parents);
			return parents;
		}
		
		public final static InterpretedTypeUse parentTypes(final Element e, final List<? extends TypeUse> parents) {
			if (parents.isEmpty()) {
				final InterpretedTypeUse any = BrokkrFile.getInterpreter(e).getTypeUse("lang", "Any");
				if (e != any)
					return any;
			}
			return parents.stream().map(t -> t.staticallyKnownType()).reduce((t1, t2) -> new InterpretedAndTypeUse(t1, t2)).get();
		}
		
		@Override
		public List<? extends GenericParameter> genericParameters() {
			return genericParameters;
		}
		
		@Override
		public String toString() {
			return "" + name + (base.getName() == null ? "" : " " + base.getName());
		}
		
		@SuppressWarnings("null")
		@Override
		public int linkEnd() {
			return base.getNameToken() != null ? base.getNameToken().regionEnd() : name != null ? name.regionEnd() : regionEnd(); // link to whole 'mod Base', not just to 'mod'
		}
		
		@Override
		protected InterfaceDeclaration parse() throws ParseException {
			one("interface");
			until(() -> {
				name = tryVariableIdentifierToken();
				if (name == null)
					name = oneTypeIdentifierToken();
				else
					base.setName(oneTypeIdentifierToken());
				tryGroup('<', () -> {
					do {
						genericParameters.add(one(GenericParameterDeclaration.class));
					} while (try_(','));
				}, '>');
				if (try_("extends")) {
					do {
						parents.add(TypeExpressions.parse(this, false, false, true));
						// TODO if base != null infer type from that one (e.g. in 'dynamic Collection extends addable')?
					} while (try_(','));
				}
			}, '{', false);
			repeatUntil(() -> {
				members.add(Member.parse(this));
			}, '}', true);
			return this;
		}
		
		@Override
		public InterpretedNativeBrokkrInterface interpreted() {
			return new InterpretedNativeBrokkrInterface(this);
		}
	}
	
	public static class ClassDeclaration extends AbstractElement<ClassDeclaration> implements TypeDeclaration {
		public final MemberModifiers modifiers;
		
		public @Nullable UppercaseWordToken name;
		public List<GenericParameterDeclaration> genericParameters = new ArrayList<>();
		public List<TypeUse> parents = new ArrayList<>();
		public List<Member> members = new ArrayList<>();
		
		public ClassDeclaration(final MemberModifiers modifiers) {
			this.modifiers = modifiers;
			modifiers.setParent(this);
		}
		
		@Override
		public @Nullable WordToken nameToken() {
			return name;
		}
		
		@Override
		public List<? extends Member> declaredMembers() {
			return members;
		}
		
		@Override
		public InterpretedTypeUse parentTypes() {
			return InterfaceDeclaration.parentTypes(this, parents);
		}
		
		@Override
		public List<? extends GenericParameter> genericParameters() {
			return genericParameters;
		}
		
		@Override
		public String toString() {
			return "" + name;
		}
		
		@Override
		protected ClassDeclaration parse() throws ParseException {
			one("class");
			until(() -> {
				name = oneTypeIdentifierToken();
				tryGroup('<', () -> {
					do {
						genericParameters.add(one(GenericParameterDeclaration.class));
					} while (try_(','));
				}, '>');
				if (try_("implements")) {
					do {
						parents.add(TypeExpressions.parse(this, false, false, true));
					} while (try_(','));
				}
			}, '{', false);
			repeatUntil(() -> {
				members.add(Member.parse(this));
			}, '}', true);
			return this;
		}
		
		@Override
		public InterpretedNativeBrokkrClass interpreted() {
			return new InterpretedNativeBrokkrClass(this);
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
	
	public static class ExtensionDeclaration extends AbstractElement<ExtensionDeclaration> implements TypeDeclaration {
		public final TopLevelElementModifiers modifiers;
		
		public @Nullable UppercaseWordToken name;
		public List<TypeUse> parents = new ArrayList<>();
		public List<Member> members = new ArrayList<>();
		public @Nullable TypeExpression extended;
		
		public ExtensionDeclaration(final TopLevelElementModifiers modifiers) {
			this.modifiers = modifiers;
			modifiers.setParent(this);
		}
		
		@Override
		public @Nullable WordToken nameToken() {
			return name;
		}
		
		@Override
		public @NonNull List<? extends Member> declaredMembers() {
			return members;
		}
		
		@Override
		public InterpretedTypeUse parentTypes() {
			return InterfaceDeclaration.parentTypes(this, parents);
		}
		
		@Override
		public List<? extends GenericParameter> genericParameters() {
			return Collections.EMPTY_LIST;
		}
		
		@Override
		public String toString() {
			return "" + name;
		}
		
		@Override
		protected ExtensionDeclaration parse() throws ParseException {
			one("extension");
			name = oneTypeIdentifierToken();
			//generics=GenericParameters? // deriving the generics of an extension is non-trivial (e.g. 'extension WeirdList<X> extends List<T extends Comparable<X> & Collection<X>>')
			one("extends");
			extended = TypeExpressions.parse(this, true, false);
			if (try_("implements")) {
				do {
					parents.add(TypeExpressions.parse(this, false, false, true));
				} while (try_(','));
			}
			oneRepeatingGroup('{', () -> {
				members.add(Member.parse(this));
			}, '}');
			return this;
		}
		
		@Override
		public InterpretedNativeTypeDefinition interpreted() {
			throw new InterpreterException("not implemented");
		}
	}
	
	// TODO remove this?
	public static class TypeAliasDeclaration extends AbstractElement<TypeAliasDeclaration> implements TypeDeclaration {
		public final TopLevelElementModifiers modifiers;
		
		public @Nullable WordToken name;
		public @Nullable String base;
		public @Nullable TypeExpression aliasOf;
		
		public TypeAliasDeclaration(final TopLevelElementModifiers modifiers) {
			this.modifiers = modifiers;
			modifiers.setParent(this);
		}
		
		@Override
		public @Nullable WordToken nameToken() {
			return name;
		}
		
		@Override
		public List<? extends Member> declaredMembers() {
			return Collections.EMPTY_LIST;
		}
		
		@Override
		public InterpretedTypeUse parentTypes() {
			throw new InterpreterException("");
		}
		
		@Override
		public List<GenericParameter> genericParameters() {
			return Collections.EMPTY_LIST; // TODO aliases should allow generic params too (e.g. 'alias Generator<T> = Procedure<[], T>')
		}
		
		@Override
		public String toString() {
			return "alias " + name;
		}
		
		@Override
		protected TypeAliasDeclaration parse() throws ParseException {
			one("alias");
			until(() -> {
				name = tryVariableIdentifierToken();
				if (name == null)
					name = oneTypeIdentifierToken();
				else
					base = oneTypeIdentifier();
//				tryGroup('<', () -> {
//					// TODO generic params
//				}, '>');
				one('=');
				aliasOf = TypeExpressions.parse(this, false, false);
			}, ';', false);
			return this;
		}
		
		@Override
		public InterpretedNativeTypeDefinition interpreted() {
			throw new InterpreterException("not implemented");
//			return aliasOf.interpret(new InterpreterContext(null));
		}
	}
	
	/**
	 * A declaration of a generic parameter of a type, i.e. a generic type that can be defined by position without a name after a type, e.g. 'List&lt;T>'.
	 */
	public static class GenericParameterDeclaration extends AbstractElement<GenericParameterDeclaration> implements GenericParameter {
		public Link<InterpretedGenericTypeRedefinition> definition = new Link<InterpretedGenericTypeRedefinition>(this) {
			@Override
			protected @Nullable InterpretedGenericTypeRedefinition tryLink(@NonNull final String name) {
				final TypeDeclaration type = getParentOfType(TypeDeclaration.class);
				if (type == null)
					return null;
				final InterpretedMemberRedefinition m = type.interpreted().getMemberByName(name);
				return m instanceof InterpretedGenericTypeRedefinition ? (InterpretedGenericTypeRedefinition) m : null;
			}
		};
		
		@Override
		public String toString() {
			return "" + definition.getName();
		}
		
		@Override
		protected GenericParameterDeclaration parse() throws ParseException {
			definition.setName(oneTypeIdentifierToken());
			return this;
		}
		
		@Override
		public Variance variance() {
			return Variance.INVARIANT; // FIXME calculate variance from positions in attributes (arguments and results) // TODO what about uses as generics arguments to other types? (e.g. if List<T> has addAll(Collection<T>))
		}
		
		@Override
		public @Nullable InterpretedGenericTypeRedefinition declaration() {
			return definition.get();
		}
		
	}
	
}
