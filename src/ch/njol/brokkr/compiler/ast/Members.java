package ch.njol.brokkr.compiler.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.compiler.Module;
import ch.njol.brokkr.compiler.ParseException;
import ch.njol.brokkr.compiler.Token;
import ch.njol.brokkr.compiler.Token.LowercaseWordToken;
import ch.njol.brokkr.compiler.Token.SymbolToken;
import ch.njol.brokkr.compiler.Token.UppercaseWordToken;
import ch.njol.brokkr.compiler.Token.WordToken;
import ch.njol.brokkr.compiler.ast.Expressions.Block;
import ch.njol.brokkr.compiler.ast.Expressions.TypeExpressions;
import ch.njol.brokkr.compiler.ast.Interfaces.Expression;
import ch.njol.brokkr.compiler.ast.Interfaces.FormalAttribute;
import ch.njol.brokkr.compiler.ast.Interfaces.FormalError;
import ch.njol.brokkr.compiler.ast.Interfaces.FormalParameter;
import ch.njol.brokkr.compiler.ast.Interfaces.FormalResult;
import ch.njol.brokkr.compiler.ast.Interfaces.FormalVariable;
import ch.njol.brokkr.compiler.ast.Interfaces.NamedElement;
import ch.njol.brokkr.compiler.ast.Interfaces.TypeDeclaration;
import ch.njol.brokkr.compiler.ast.Interfaces.TypeExpression;
import ch.njol.brokkr.compiler.ast.Interfaces.TypeUse;
import ch.njol.brokkr.compiler.ast.Statements.CodeGenerationCall;
import ch.njol.brokkr.compiler.ast.Statements.Return;
import ch.njol.brokkr.compiler.ast.Statements.Statement;
import ch.njol.brokkr.compiler.ast.TopLevelElements.BrokkrFile;
import ch.njol.brokkr.data.MethodModifiability;
import ch.njol.brokkr.data.Visibility;
import ch.njol.brokkr.interpreter.InterpretedError;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.interpreter.definitions.InterpretedAttributeRedefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedBrokkrAttributeDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedBrokkrAttributeDefinitionAndImplementation;
import ch.njol.brokkr.interpreter.definitions.InterpretedBrokkrAttributeImplementation;
import ch.njol.brokkr.interpreter.definitions.InterpretedBrokkrAttributeRedefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedBrokkrConstructor;
import ch.njol.brokkr.interpreter.definitions.InterpretedBrokkrConstructorFieldParameter;
import ch.njol.brokkr.interpreter.definitions.InterpretedBrokkrGenericTypeDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedBrokkrGenericTypeRedefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedBrokkrNormalParameterDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedBrokkrNormalParameterRedefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedBrokkrResultDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedBrokkrResultRedefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedGenericTypeRedefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedMemberRedefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedNativeTypeDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedParameterDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedParameterRedefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedResultRedefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedVariableRedefinition;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeBoolean;
import ch.njol.brokkr.interpreter.uses.InterpretedMemberUse;
import ch.njol.brokkr.interpreter.uses.InterpretedSimpleTypeUse;
import ch.njol.brokkr.interpreter.uses.InterpretedTypeUse;
import ch.njol.util.StringUtils;

// TODO sections (e.g. 'section nameHere { members here ... }')
public class Members {
	
	public static interface Member {
		boolean isInherited();
		
		InterpretedMemberRedefinition interpreted();
		
		public static Member parse(final AbstractElement<?> parent) throws ParseException {
			if (parent.peekNext('$') && parent.peekNext('=', 1, false))
				return parent.one(CodeGenerationCall.class);
			if (parent.peekNext("delegate"))
				return parent.one(Delegation.class);
//			if (parent.peekNext("element"))
//				return parent.one(EnumElement.class);
			
			final MemberModifiers modifiers = parent.one(MemberModifiers.class);
			try {
//				if (!modifiers.genericParameters.isEmpty() && parent.peekNext(';'))
//					return parent.one(new GenericTypeParameter(modifiers));
//				if (parent.peekNext("interface"))
//					return parent.one(new InterfaceDeclaration(modifiers));
//				if (parent.peekNext("class"))
//					return parent.one(new ClassDeclaration(modifiers));
				if (parent.peekNext("constructor"))
					return parent.one(new Constructor(modifiers));
				if (parent.peekNext("invariant"))
					return parent.one(new Invariant(modifiers));
				if (parent.peekNext("type"))
					return parent.one(new GenericTypeDeclaration(modifiers));
				if (parent.peekNext("code") || parent.peekNext("member") || parent.peekNext("type"))
					return parent.one(new Template(modifiers));
				return parent.one(new AttributeDeclaration(modifiers));
				// TODO constants? e.g. like 'constant Type name = value, name2 = value2;'
			} finally {
				assert modifiers.parent() != parent;
			}
		}
	}
	
//	// TODO
//	public static class MemberRegion extends AbstractElement<MemberRegion> implements MemberContainer {
//		public final List<? extends Member> members = new ArrayList<>();
//
//		@Override
//		public List<? extends Member> declaredMembers() {
//			return members;
//		}
//
//		@SuppressWarnings("null")
//		@Override
//		public List<? extends MemberContainer> parentMemberContainers() {
//			return Arrays.asList((MemberContainer) parent);
//		}
//
//		@Override
//		protected MemberRegion parse() throws ParseException {
//			// TODO Auto-generated method stub
//			return this;
//		}
//	}
	
	// TODO a correctness annotation that means that an attribute does not change (anymore), e.g. a field is "final" or a method always returns the same value, even if the object is modified.
	public static class MemberModifiers extends AbstractElement<MemberModifiers> {
		public final List<GenericTypeDeclaration> genericParameters = new ArrayList<>();
		public boolean override;
		public boolean partialOverride;
		public boolean hide;
		public boolean undefine;
		public final Link<InterpretedNativeTypeDefinition> overriddenFromType = new Link<InterpretedNativeTypeDefinition>(this) {
			@Override
			protected @Nullable InterpretedNativeTypeDefinition tryLink(final String name) {
				final BrokkrFile file = getParentOfType(BrokkrFile.class);
				if (file == null)
					return null;
				final Module module = file.module;
				return module == null ? null : module.getType(name);
			}
		};
		public final Link<InterpretedMemberRedefinition> overridden = new Link<InterpretedMemberRedefinition>(this) {
			@Override
			protected @Nullable InterpretedMemberRedefinition tryLink(String name) {
				final WordToken nameToken = getNameToken();
				assert nameToken != null;
				if (nameToken.keyword) { // i.e. the 'override' keyword
					if (parent == null || !(parent instanceof NamedElement))
						return null;
					@SuppressWarnings("null")
					final String n = ((NamedElement) parent).name();
					if (n == null)
						return null;
					name = n;
				}
				final InterpretedNativeTypeDefinition fromType = overriddenFromType.getNameToken() == null ? null : overriddenFromType.get();
				if (fromType != null) {
					// TODO check if actually subtyped (and maybe check interfaces in-between, e.g. A.a overrides C.a, but A extends B extends C and B also defines a)
					return fromType.getMemberByName(name);
				} else {
					final TypeDeclaration t = getParentOfType(TypeDeclaration.class);
					if (t == null)
						return null;
//					// only check parents of the containing type (otherwise this method itself would be found)
					final InterpretedTypeUse parent = t.parentTypes();
					final InterpretedMemberUse member = parent.getMemberByName(name);
					return member != null ? member.redefinition() : null;
				}
			}
		};
		public @Nullable Visibility visibility;
		public boolean isNative;
		public boolean isStatic;
		public @Nullable MethodModifiability modifiability;
		public boolean alias;
		public boolean context;
		public boolean recursive;
		public boolean var;
		
		@Override
		protected MemberModifiers parse() throws ParseException {
			// modifiers
			unordered(() -> {
				tryGroup('<', () -> {
					do {
						genericParameters.add(one(new GenericTypeDeclaration(null)));
					} while (try_(','));
				}, '>');
			}, () -> {
				isStatic = try_("static");
			}, () -> {
				WordToken overrideToken;
				if ((overrideToken = try2("override")) != null) {
					override = true;
					hide = try_("hide");
					if (peekNext() instanceof UppercaseWordToken && peekNext('.', 1, true)) {
						overriddenFromType.setName(oneTypeIdentifierToken());
						one('.');
						overridden.setName(oneIdentifierToken());
						one("as");
					} else if (peekNext() instanceof WordToken && peekNext("as", 1, true)) {
						overridden.setName(oneIdentifierToken());
						one("as");
					} else {
						overridden.setName(overrideToken);
					}
				} else if (try_("partialOverride")) { // TODO partial overrides should always be together (i.e. near each other in code), should I enforce this? (probably not)
					partialOverride = true;
				}
				if (!partialOverride)
					undefine = try_("undefine");
			}, () -> {
				isNative = try_("native");
			}, () -> {
				alias = try_("alias");
			}, () -> {
				visibility = Visibility.parse(this);
			}, () -> {
				// TODO modifiability for fields
				modifiability = MethodModifiability.parse(this);
			}, () -> {
				context = try_("context");
			}, () -> {
				recursive = try_("recursive");
			}, () -> {
				var = try_("var");
			});
			return this;
		}
	}
	
//	public static class GenericTypeParameter extends AbstractElement<GenericTypeParameter> implements Member, TypeDeclaration {
//		public final MemberModifiers modifiers;
//		public final GenericParameter parameter;
//
//		@Override
//		public @Nullable WordToken nameToken() {
//			return parameter.name;
//		}
//
//		// a generic parameter does not declare any additional members - all are inherited
//		@SuppressWarnings("null")
//		@Override
//		public List<? extends Member> declaredMembers() {
//			return Collections.EMPTY_LIST;
//		}
//
//		@Override
//		public List<? extends TypeDeclaration> parentMemberContainers() {
//			return parameter.parentMemberContainers();
//		}
//
//		@SuppressWarnings("null")
//		public GenericTypeParameter(final MemberModifiers modifiers) {
//			assert !modifiers.genericParameters.isEmpty();
//			this.modifiers = modifiers;
//			modifiers.setParent(this);
//			parameter = modifiers.genericParameters.get(0);
//			modifiers.genericParameters.clear();
//			parameter.setParent(this);
//		}
//
//		@Override
//		public String toString() {
//			return "<" + parameter + ">;";
//		}
//
//		@Override
//		protected GenericTypeParameter parse() throws ParseException {
//			one(';');
//			return this;
//		}
//	}
	
	public static class AttributeDeclaration extends AbstractElement<AttributeDeclaration> implements Member, FormalAttribute {
		public final MemberModifiers modifiers;
		
		public @Nullable LowercaseWordToken name;
		public boolean hasParameterDots;
		public List<SimpleParameter> parameters = new ArrayList<>();
		public boolean hasResultDots;
		public List<FormalResult> results = new ArrayList<>();
		public List<ErrorDeclaration> errors = new ArrayList<>();
		public boolean isAbstract;
		public @Nullable Block body;
		public List<Precondition> preconditions = new ArrayList<>();
		public List<Postcondition> postconditions = new ArrayList<>();
		
		public AttributeDeclaration(final MemberModifiers modifiers) {
			this.modifiers = modifiers;
			modifiers.setParent(this);
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
		public MemberModifiers modifiers() {
			return modifiers;
		}
		
//		@Override
//		public List<? extends TypeDeclaration> declaredTypes() {
//			return modifiers.genericParameters;
//		}
//
//		@SuppressWarnings("null")
//		@Override
//		public @NonNull List<? extends @NonNull HasTypes> parentHasTypes() {
//			final InterpretedMember a = modifiers.overridden.get();
//			return a != null && a instanceof InterpretedAttributeRedefinition ? Arrays.asList((InterpretedAttributeRedefinition) a) : Collections.EMPTY_LIST;
//		}
		
		@Override
		public List<? extends InterpretedVariableRedefinition> allVariables() {
			final InterpretedAttributeRedefinition interpreted = interpreted();
			final List<InterpretedVariableRedefinition> result = new ArrayList<>();
			result.addAll(interpreted.parameters());
			result.addAll(interpreted.results());
			return result;
		}
		
		@Override
		public List<? extends FormalError> declaredErrors() {
			return errors;
		}
		
		@Override
		public List<? extends FormalResult> declaredResults() {
			return results;
		}
		
		@Override
		public String toString() {
			return name
					+ (parameters.size() == 0 ? "" : "(" + StringUtils.join(parameters, ", ") + ")")
					+ ": " + (results.size() == 0 ? "[]" : String.join(", ", results.stream().map(r -> r.toString()).toArray(i -> new String[i])));
		}
		
		@Override
		public InterpretedAttributeRedefinition interpreted() {
			if (modifiers.overridden.getNameToken() != null) {
				if (body != null)
					return new InterpretedBrokkrAttributeImplementation(this);
				else
					return new InterpretedBrokkrAttributeRedefinition(this);
			} else {
				if (body != null)
					return new InterpretedBrokkrAttributeDefinitionAndImplementation(this);
				else
					return new InterpretedBrokkrAttributeDefinition(this);
			}
		}
		
		@Override
		protected AttributeDeclaration parse() throws ParseException {
			
			// name
			name = oneVariableIdentifierToken();
			
			// parameters
			tryGroup('(', () -> {
				if (try_("..."))
					hasParameterDots = true;
				if (!hasParameterDots || try_(',')) {
					do {
						parameters.add(one(SimpleParameter.class));
					} while (try_(','));
				}
			}, ')');
			
			// results
			boolean hasResults = false;
			if (try_(':')) {
				hasResults = true;
				if (try_("..."))
					hasResultDots = true;
				if (!hasResultDots || try_(',')) {
					do {
						results.add(one(NormalResult.class));
					} while (try_(','));
				}
			}
			
			// errors
			while (peekNext('#')) {
				hasResults = true;
				errors.add(one(ErrorDeclaration.class));
			}
			
			// body
			if (peekNext('{')) {
				body = one(Block.class);
			} else if (try_(';')) { // abstract / single expression syntax
				while (peekNext("requires"))
					preconditions.add(one(Precondition.class));
				while (peekNext("ensures"))
					postconditions.add(one(Postcondition.class));
			} else if (!hasResults) { // 'attribute = value' syntax (nonexistent or overridden result)
				one("=");
				body = new Block(one(new Return(false)));
				while (peekNext("requires"))
					preconditions.add(one(Precondition.class));
				while (peekNext("ensures"))
					postconditions.add(one(Postcondition.class));
			}
			
			return this;
		}
	}
	
	public static class SimpleParameter extends AbstractElement<SimpleParameter> implements FormalParameter {
		public @Nullable Visibility visibility;
		public boolean override;
		private @Nullable WordToken overrideToken;
		public final Link<InterpretedParameterRedefinition> overridden = new Link<InterpretedParameterRedefinition>(this) {
			@Override
			protected @Nullable InterpretedParameterRedefinition tryLink(final String name) {
				// TODO parameter named like this link, or parameter with same position as this parameter (either from left or right, depending on where the dots are (if any)).
				// for the position, this first requires to set this link to 'override' or such (no name = no linking!) [see MemberModifiers.overridden for how]
				final AttributeDeclaration attribute = getParentOfType(AttributeDeclaration.class);
				if (attribute == null)
					return null;
				final InterpretedMemberRedefinition parent = attribute.modifiers.overridden.get();
				if (parent == null || !(parent instanceof InterpretedAttributeRedefinition))
					return null;
//				if (getNameToken() == overrideToken)
//					return ((InterpretedAttributeRedefinition) parent).getParameterByPosition(...); // TODO
//				else
				return ((InterpretedAttributeRedefinition) parent).getParameterByName(name);
			}
		};
		public @Nullable TypeUse type;
		public @Nullable WordToken name;
		public @Nullable Expression defaultValue;
		
		@Override
		public @Nullable WordToken nameToken() {
			return name;
		}
		
//		@Override
//		public @Nullable FormalParameter overridden() {
//			return overridden.get();
//		}
		
		@Override
		public String toString() {
			return type + " " + name;
		}
		
		@Override
		protected SimpleParameter parse() throws ParseException {
			visibility = Visibility.parse(this);
			overrideToken = try2("override");
			override = overrideToken != null;
			if (override) {
				if (peekNext() instanceof WordToken && peekNext("as", 1, true)) {
					overridden.setName(oneIdentifierToken());
					next(); // skip 'as'
				} else {
					overridden.setName(overrideToken);
				}
				if (overridden.getNameToken() == overrideToken //
						|| !(peekNext() instanceof WordToken && (peekNext(',', 1, true) || peekNext(')', 1, true)))) // allows overriding the name without changing the type
					type = TypeExpressions.parse(this, true, true);
			} else {
				type = TypeExpressions.parse(this, true, true);
			}
			name = oneIdentifierToken();
			if (try_('='))
				defaultValue = Expressions.parse(this);
			return this;
		}
		
		@Override
		public InterpretedTypeUse interpretedType() {
			if (type != null)
				return type.staticallyKnownType();
			final InterpretedParameterRedefinition param = overridden.get();
			if (param != null)
				return param.type();
			throw new InterpreterException("");
		}
		
		@Override
		public InterpretedParameterRedefinition interpreted(final InterpretedAttributeRedefinition attribute) {
			final InterpretedParameterRedefinition parent = override ? overridden.get() : null;
			return parent != null ? new InterpretedBrokkrNormalParameterRedefinition(this, parent, attribute) : new InterpretedBrokkrNormalParameterDefinition(this, attribute);
		}
	}
	
	public static class NormalResult extends AbstractElement<NormalResult> implements FormalVariable, FormalResult {
		public @Nullable TypeUse type;
		public @Nullable LowercaseWordToken name;
		public @Nullable Expression defaultValue;
		public Link<InterpretedResultRedefinition> overridden = new Link<InterpretedResultRedefinition>(this) {
			@Override
			protected @Nullable InterpretedResultRedefinition tryLink(final String name) {
				@SuppressWarnings("null")
				final InterpretedAttributeRedefinition parentAttr = getParentOfType(FormalAttribute.class).interpreted().parentRedefinition();
				if (parentAttr == null)
					return null;
				return parentAttr.getResultByName(name);
			}
		};
		
		@Override
		public @Nullable WordToken nameToken() {
			return name;
		}
		
		@Override
		public InterpretedTypeUse interpretedType() {
			final TypeUse type = this.type;
			if (type == null) {
				final InterpretedResultRedefinition parent = overridden.get();
				if (parent == null)
					throw new InterpreterException("Missing parent of result " + this);
				return parent.type();
			}
			return type.staticallyKnownType();
		}
		
		@Override
		public String toString() {
			return type + (name == null ? "" : " " + name);
		}
		
		@Override
		protected NormalResult parse() throws ParseException {
			if (try_("override")) {
				overridden.setName(oneVariableIdentifierToken());
				one("as");
			}
			boolean parseType = true;
			if (peekNext() instanceof LowercaseWordToken) {
				final Token nextNext = peekNext(2, true);
				if (nextNext instanceof SymbolToken && "=,#{".indexOf(((SymbolToken) nextNext).symbol) >= 0)
					parseType = false;
			}
			if (parseType)
				type = TypeExpressions.parse(this, true, true);
			name = tryVariableIdentifierToken();
//			if (name == null)
//				name = "result";
//			if (peekNext("=>")) // method '=> results' syntax
//				return this;
			if (try_('='))
				defaultValue = Expressions.parse(this);
			return this;
		}
		
		@Override
		public InterpretedResultRedefinition interpreted(final InterpretedAttributeRedefinition attribute) {
			final InterpretedResultRedefinition parent = overridden.get();
			return parent == null ? new InterpretedBrokkrResultDefinition(this, attribute) : new InterpretedBrokkrResultRedefinition(this, parent, attribute);
		}
	}
	
	// TODO remove this?
	/*
	public static class ParameterResult extends AbstractElement<ParameterResult> implements FormalResult {
		Link<FormalParameter> parameter = new Link<FormalParameter>(this) {
			@Override
			protected @Nullable FormalParameter tryLink(final String name) {
				final HasParameters hp = ParameterResult.this.getParentOfType(HasParameters.class);
				assert hp != null;
				return hp.getParameter(name);
			}
		};
		
		@Override
		public @Nullable TypeUse type() {
			final FormalParameter p = parameter.get();
			return p == null ? null : p.type();
		}
		
		@Override
		public @Nullable WordToken nameToken() {
			return parameter.getNameToken();
		}
		
		@Override
		public String toString() {
			return "" + parameter.getName();
		}
		
		@Override
		protected ParameterResult parse() throws ParseException {
			parameter.setName(oneVariableIdentifierToken());
			return this;
		}
		
		@SuppressWarnings("null")
		@Override
		public InterpretedResult interpreted() {
			// TODO pass along the link to the parameter too (or do this differently)
			return new InterpretedResult(getParentOfType(FormalAttribute.class).interpreted(), parameter.getName(), parameter.get().interpreted().nativeType());
		}
	}
	*/
	
	public static class ErrorDeclaration extends AbstractElement<ErrorDeclaration> implements FormalError {
		public @Nullable LowercaseWordToken name;
		public List<FormalParameter> parameters = new ArrayList<>();
		
		@Override
		public @Nullable WordToken nameToken() {
			return name;
		}
		
//		@Override
//		public List<? extends FormalParameter> declaredParameters() {
//			return parameters;
//		}
		
		@Override
		public String toString() {
			return "#" + name;
		}
		
//		@SuppressWarnings("null")
//		@Override
//		public @Nullable HasParameters parentParameters() {
//			final @Nullable InterpretedMemberRedefinition parent = ((FormalAttribute) parent()).modifiers().overridden.get();
//			return parent == null || name == null || !(parent instanceof InterpretedAttributeRedefinition) ? null //
//					: ((InterpretedAttributeRedefinition) parent).getErrorByName(name.word);
//		}
		
		@Override
		protected @NonNull ErrorDeclaration parse() throws ParseException {
			one('#');
			name = oneVariableIdentifierToken();
			tryGroup('(', () -> {
				do {
					parameters.add(one(SimpleParameter.class));
				} while (try_(','));
			}, ')');
			return this;
		}
		
		@Override
		public InterpretedError interpreted(final InterpretedAttributeRedefinition attribute) {
			return new InterpretedError("" + name(), parameters.stream().map(p -> p.interpreted(attribute)).collect(Collectors.toList()), attribute);
		}
		
	}
	
//	public static class EnumElement extends AbstractElement<EnumElement> implements TypeDeclaration, Member {
//		public @Nullable UppercaseWordToken name;
//		public List<ActualSimpleType> parents = new ArrayList<>();
//		public List<Member> members = new ArrayList<>();
//
//		@Override
//		public boolean isInherited() {
//			return true;
//		}
//
//		@Override
//		public @Nullable WordToken nameToken() {
//			return name;
//		}
//
//		@Override
//		public List<? extends Member> declaredMembers() {
//			return members;
//		}
//
//		@SuppressWarnings("null")
//		@Override
//		public List<? extends TypeDeclaration> parentTypes() {
//			final List<TypeDeclaration> parents = new ArrayList<>();
//			parents.add((EnumDeclaration) parent);
//			parents.addAll(InterfaceDeclaration.parentTypes(this, this.parents));
//			return parents;
//		}
//
//		@Override
//		public String toString() {
//			return "" + name;
//		}
//
//		@Override
//		protected EnumElement parse() throws ParseException {
//			one("element");
//			name = oneTypeIdentifierToken();
//			if (try_("extends")) {
//				do {
//					parents.add(one(ActualSimpleType.class)); // TODO if base != null infer type from that one (e.g. in 'dynamic Collection extends addable')
//				} while (try_(','));
//			}
//			if (!try_(';')) {
//				oneRepeatingGroup('{', () -> {
//					members.add(Member.parse(this));
//				}, '}');
//			}
//			return this;
//		}
//	}
	
	public static class Constructor extends AbstractElement<Constructor> implements Member, FormalAttribute {
		public final MemberModifiers modifiers;
		
		public @Nullable LowercaseWordToken name;
		public List<FormalParameter> parameters = new ArrayList<>();
		public @Nullable Block body;
		public final List<Postcondition> postconditions = new ArrayList<>();
		
		public Constructor(final MemberModifiers modifiers) {
			this.modifiers = modifiers;
			modifiers.setParent(this);
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
		public MemberModifiers modifiers() {
			return modifiers;
		}
		
		@Override
		public List<? extends InterpretedVariableRedefinition> allVariables() {
			return interpreted().parameters();
		}
		
		@Override
		public List<FormalError> declaredErrors() {
			return Collections.EMPTY_LIST; // FIXME preconditions are errors too!
		}
		
		@Override
		public List<? extends FormalResult> declaredResults() {
			return Collections.EMPTY_LIST; // FIXME
		}
		
		@Override
		public String toString() {
			return "" + name + (parameters.size() == 0 ? "" : "(" + StringUtils.join(parameters, ", ") + ")");
		}
		
		@Override
		protected Constructor parse() throws ParseException {
			one("constructor");
			name = oneVariableIdentifierToken();
			oneGroup('(', () -> {
				do {
					if (peekNext('=', 1, true) || peekNext(',', 1, true) || peekNext(')', 1, true))
						parameters.add(one(ConstructorFieldParameter.class));
					else
						parameters.add(one(SimpleParameter.class));
				} while (try_(','));
			}, ')');
			if (!try_(';')) // field params syntax
				body = one(Block.class);
			return this;
		}
		
		@SuppressWarnings("null")
		@Override
		public InterpretedTypeUse interpretedType() {
			return new InterpretedSimpleTypeUse(getParentOfType(TypeDeclaration.class).interpreted());
		}
		
		@Override
		public InterpretedBrokkrConstructor interpreted() {
			return new InterpretedBrokkrConstructor(this);
		}
	}
	
	public static class ConstructorFieldParameter extends AbstractElement<ConstructorFieldParameter> implements FormalParameter {
		public final Link<InterpretedAttributeRedefinition> attribute = new Link<InterpretedAttributeRedefinition>(this) {
			@Override
			protected @Nullable InterpretedAttributeRedefinition tryLink(final String name) {
				final TypeDeclaration mc = ConstructorFieldParameter.this.getParentOfType(TypeDeclaration.class);
				assert mc != null;
				return mc.interpreted().getAttributeByName(name);
			}
		};
		public @Nullable Expression defaultValue;
		
		@Override
		public @Nullable WordToken nameToken() {
			return attribute.getNameToken();
		}
		
		@Override
		protected ConstructorFieldParameter parse() throws ParseException {
			attribute.setName(oneVariableIdentifierToken());
			if (try_('='))
				defaultValue = Expressions.parse(this);
			return this;
		}
		
//		@Override
//		public @Nullable FormalParameter overridden() {
//			return null;
//		}
		
		@Override
		public String toString() {
			return "" + attribute.getName();
		}
		
		@SuppressWarnings("null")
		@Override
		public InterpretedTypeUse interpretedType() {
			final InterpretedAttributeRedefinition f = attribute.get();
			return f == null ? null : f.mainResultType();
		}
		
		@SuppressWarnings("null")
		@Override
		public InterpretedParameterDefinition interpreted(final InterpretedAttributeRedefinition constructor) {
			final InterpretedAttributeRedefinition attr = attribute.get();
			if (attr.results().size() != 1 || !attr.results().get(0).name().equals("result") || !attr.isVariable())
				throw new InterpreterException("Constructor field parameter '" + attr.name() + "' does not reference a field");
			return new InterpretedBrokkrConstructorFieldParameter(this, attr, constructor);
		}
	}
	
	public static class GenericTypeDeclaration extends AbstractElement<GenericTypeDeclaration> implements Member {
		/**
		 * null if and only if this is an implicit generic type (i.e. is part of a MemberModifiers itself)
		 */
		public final @Nullable MemberModifiers modifiers;
		
		public @Nullable UppercaseWordToken name;
		public @Nullable TypeUse superType, extendedType;
		public @Nullable TypeUse defaultType;
		
		public GenericTypeDeclaration(final @Nullable MemberModifiers modifiers) {
			this.modifiers = modifiers;
			if (modifiers != null)
				modifiers.setParent(this);
		}
		
		@Override
		public boolean isInherited() {
			return true; // TODO make some not inheritable?
		}
		
		@Override
		public String toString() {
			return "" + name;
		}
		
		@Override
		protected GenericTypeDeclaration parse() throws ParseException {
			final VoidProcessor simple = () -> {
				name = oneTypeIdentifierToken();
				unordered(() -> {
					if (try_("super")) {
						superType = TypeExpressions.parse(this, true, true);
					}
				}, () -> {
					if (try_("extends")) {
						extendedType = TypeExpressions.parse(this, true, true);
					}
				});
				if (try_('=')) {
					defaultType = TypeExpressions.parse(this, true, true);
				}
			};
			if (modifiers == null) {
				simple.process();
			} else {
				until(() -> {
					one("type");
					simple.process();
				}, ';', false);
			}
			return this;
		}
		
		@Override
		public InterpretedGenericTypeRedefinition interpreted() {
			@SuppressWarnings("null")
			final InterpretedMemberRedefinition parent = modifiers != null ? modifiers.overridden.get() : null;
			return parent == null || !(parent instanceof InterpretedGenericTypeRedefinition)
					? new InterpretedBrokkrGenericTypeDefinition(this)
					: new InterpretedBrokkrGenericTypeRedefinition(this, (InterpretedGenericTypeRedefinition) parent);
		}
	}
	
	public static class Template extends AbstractElement<Template> implements Member, FormalAttribute {
		public final MemberModifiers modifiers;
		
		public @Nullable TemplateType templateType;
		public @Nullable LowercaseWordToken name;
		public List<FormalParameter> parameters = new ArrayList<>();
		public @Nullable Block body;
		
		enum TemplateType {
			TYPE, MEMBER, CODE;
		}
		
		public Template(final MemberModifiers modifiers) {
			this.modifiers = modifiers;
			modifiers.setParent(this);
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
		public MemberModifiers modifiers() {
			return modifiers;
		}
		
		@Override
		public List<? extends InterpretedVariableRedefinition> allVariables() {
			return interpreted().parameters();
		}
		
		@Override
		public List<FormalError> declaredErrors() {
			return Collections.EMPTY_LIST; // FIXME preconditions are errors too!
		}
		
		@Override
		public List<? extends FormalResult> declaredResults() {
			return Collections.EMPTY_LIST;
		}
		
		@Override
		public String toString() {
			return "" + name;
		}
		
		@Override
		protected Template parse() throws ParseException {
			templateType = TemplateType.valueOf("" + oneOf("code", "member", "type").toUpperCase(Locale.ENGLISH));
			one("template");
			name = oneVariableIdentifierToken();
			oneGroup('(', () -> {
				do {
					parameters.add(one(SimpleParameter.class));
				} while (try_(','));
			}, ')');
			body = one(Block.class);
			return this;
		}
		
		@Override
		public @Nullable InterpretedTypeUse interpretedType() {
			return null; // templates never return anything // TODO "void" return type?
		}
		
		@Override
		public InterpretedAttributeRedefinition interpreted() {
			throw new InterpreterException("not implemented");
		}
	}
	
	public static class Delegation extends AbstractElement<Delegation> implements Member {
		public List<Link<InterpretedAttributeRedefinition>> methods = new ArrayList<>();
		public List<TypeExpression> types = new ArrayList<>();
		public List<Expression> expressions = new ArrayList<>();
		public @Nullable Expression joinWith;
		
		private class MethodLink extends Link<InterpretedAttributeRedefinition> {
			public MethodLink(@Nullable final WordToken name) {
				super(Delegation.this, name);
			}
			
			@Override
			protected @Nullable InterpretedAttributeRedefinition tryLink(final String name) {
				final TypeDeclaration type = getParentOfType(TypeDeclaration.class);
				if (type == null)
					return null;
				return type.interpreted().getAttributeByName(name);
			}
		}
		
		@Override
		public boolean isInherited() {
			return true;
		}
		
		@Override
		public String toString() {
			return "delegate " + StringUtils.join(methods, ", ") + StringUtils.join(types, ", ") + " to ...";
		}
		
		@Override
		protected Delegation parse() throws ParseException {
			until(() -> {
				one("delegate");
				do {
					if (!methods.isEmpty() || types.isEmpty() && peekNext() instanceof LowercaseWordToken && (peekNext(',', 1, true) || peekNext("to", 1, true)))
						methods.add(new MethodLink(oneVariableIdentifierToken()));
					else
						types.add(TypeExpressions.parse(this, true, true));
				} while (try_(','));
				one("to");
				do {
					expressions.add(Expressions.parse(this));
				} while (try_(','));
				if (try_("join")) {
					one("with");
					joinWith = Expressions.parse(this);
				}
			}, ';', false);
			return this;
		}
		
		@Override
		public @NonNull InterpretedMemberRedefinition interpreted() {
			// TODO should return multiple members
			throw new InterpreterException("not implemented");
		}
	}
	
	public static class Invariant extends AbstractElement<Invariant> implements Member, NamedElement {
		public final MemberModifiers modifiers;
		
		public boolean negated;
		public @Nullable LowercaseWordToken name;
		public @Nullable Expression expression;
		
		public Invariant(final MemberModifiers modifiers) {
			this.modifiers = modifiers;
			modifiers.setParent(this);
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
		public String toString() {
			return "invariant " + name;
		}
		
		@Override
		protected Invariant parse() throws ParseException {
			until(() -> {
				one("invariant");
				negated = try_('!');
				name = oneVariableIdentifierToken();
				one(':');
				expression = Expressions.parse(this); // TODO allow some statements?
			}, ';', false);
			return this;
		}
		
		@Override
		public @NonNull InterpretedMemberRedefinition interpreted() {
			throw new InterpreterException("not implemented");
		}
	}
	
	public static class Precondition extends AbstractElement<Precondition> implements Statement, FormalError {
		public boolean negated;
		public @Nullable LowercaseWordToken name;
		public @Nullable Expression expression;
		
		@Override
		public @Nullable WordToken nameToken() {
			return name;
		}
		
		@Override
		public String toString() {
			return "requires " + name;
		}
		
//		@SuppressWarnings("null")
//		@Override
//		public List<? extends InterpretedVariableRedefinition> declaredVariables() {
//			return Collections.EMPTY_LIST; // preconditions have no parameters
//		}
//
//		@Override
//		public @Nullable HasVariables parentHasVariables() {
//			return null; // TODO return overridden precondition?
//		}
		
		@Override
		protected Precondition parse() throws ParseException {
			until(() -> {
				one("requires");
				//genericRestrictions=GenericParameters? // FIXME wrong
				negated = peekNext('!');
				Token t;
				if ((t = peekNext(negated ? 1 : 0, true)) instanceof LowercaseWordToken && peekNext(';', negated ? 2 : 1, true)) {
					name = (LowercaseWordToken) t;
					expression = Expressions.parse(this);
				} else {
					negated = try_('!');
					name = oneVariableIdentifierToken();
					if (try_(':'))
						expression = Expressions.parse(this); // TODO allow some statements?
				}
			}, ';', false);
			return this;
		}
		
		@SuppressWarnings("null")
		@Override
		public void interpret(final InterpreterContext context) {
			boolean value = ((InterpretedNativeBoolean) expression.interpret(context)).value;
			if (negated)
				value = !value;
			if (!value)
				throw new InterpreterException("Failed precondition " + name + " in " + getParentOfType(FormalAttribute.class).name());
		}
		
		@Override
		public InterpretedError interpreted(final InterpretedAttributeRedefinition attribute) {
			// TODO
			return new InterpretedError("" + name, Collections.EMPTY_LIST, attribute);
		}
	}
	
	public static class Postcondition extends AbstractElement<Postcondition> implements Statement, NamedElement {
		public boolean negated;
		public @Nullable LowercaseWordToken name;
		public @Nullable Expression expression;
		
		@Override
		public @Nullable WordToken nameToken() {
			return name;
		}
		
		@Override
		public String toString() {
			return "ensures " + (name == null ? "..." : name);
		}
		
		@Override
		protected Postcondition parse() throws ParseException {
			until(() -> {
				one("ensures");
				//genericRestrictions=GenericParameters? // FIXME wrong
				final boolean negated = peekNext('!');
				if (peekNext(negated ? 1 : 0, true) instanceof LowercaseWordToken && peekNext(':', negated ? 2 : 1, true)) {
					this.negated = negated;
					if (negated)
						next(); // skip '!';
					name = oneVariableIdentifierToken();
					next(); // skip ':'
				}
				expression = Expressions.parse(this); // TODO allow some statements?
			}, ';', false);
			return this;
		}
		
		@SuppressWarnings("null")
		@Override
		public void interpret(final InterpreterContext context) {
			boolean value = ((InterpretedNativeBoolean) expression.interpret(context)).value;
			if (negated)
				value = !value;
			if (!value)
				throw new InterpreterException("Failed postcondition " + name + " in " + getParentOfType(FormalAttribute.class).name());
		}
	}
	
}
