package ch.njol.brokkr.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ast.ASTExpressions.ASTBlock;
import ch.njol.brokkr.ast.ASTExpressions.ASTTypeExpressions;
import ch.njol.brokkr.ast.ASTInterfaces.ASTAttribute;
import ch.njol.brokkr.ast.ASTInterfaces.ASTError;
import ch.njol.brokkr.ast.ASTInterfaces.ASTExpression;
import ch.njol.brokkr.ast.ASTInterfaces.ASTMember;
import ch.njol.brokkr.ast.ASTInterfaces.ASTParameter;
import ch.njol.brokkr.ast.ASTInterfaces.ASTResult;
import ch.njol.brokkr.ast.ASTInterfaces.ASTTypeDeclaration;
import ch.njol.brokkr.ast.ASTInterfaces.ASTTypeExpression;
import ch.njol.brokkr.ast.ASTInterfaces.ASTTypeUse;
import ch.njol.brokkr.ast.ASTInterfaces.ASTVariable;
import ch.njol.brokkr.ast.ASTInterfaces.NamedASTElement;
import ch.njol.brokkr.ast.ASTStatements.ASTReturn;
import ch.njol.brokkr.ast.ASTStatements.ASTStatement;
import ch.njol.brokkr.ast.ASTTopLevelElements.ASTBrokkrFile;
import ch.njol.brokkr.ast.ASTTopLevelElements.ASTClassDeclaration;
import ch.njol.brokkr.ast.ASTTopLevelElements.ASTInterfaceDeclaration;
import ch.njol.brokkr.common.MethodModifiability;
import ch.njol.brokkr.common.Visibility;
import ch.njol.brokkr.compiler.ASTModule;
import ch.njol.brokkr.compiler.ParseException;
import ch.njol.brokkr.compiler.Token;
import ch.njol.brokkr.compiler.Token.LowercaseWordToken;
import ch.njol.brokkr.compiler.Token.SymbolToken;
import ch.njol.brokkr.compiler.Token.UppercaseWordToken;
import ch.njol.brokkr.compiler.Token.WordToken;
import ch.njol.brokkr.interpreter.InterpretedNormalObject;
import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeCodeGenerationResult;
import ch.njol.brokkr.ir.IRBrokkrTemplate;
import ch.njol.brokkr.ir.IRError;
import ch.njol.brokkr.ir.IRNormalError;
import ch.njol.brokkr.ir.IRUnknownError;
import ch.njol.brokkr.ir.IRUnknownParameterDefinition;
import ch.njol.brokkr.ir.definitions.IRAttributeRedefinition;
import ch.njol.brokkr.ir.definitions.IRBrokkrAttributeDefinition;
import ch.njol.brokkr.ir.definitions.IRBrokkrAttributeDefinitionAndImplementation;
import ch.njol.brokkr.ir.definitions.IRBrokkrAttributeImplementation;
import ch.njol.brokkr.ir.definitions.IRBrokkrAttributeRedefinition;
import ch.njol.brokkr.ir.definitions.IRBrokkrConstructor;
import ch.njol.brokkr.ir.definitions.IRBrokkrConstructorFieldParameter;
import ch.njol.brokkr.ir.definitions.IRBrokkrGenericTypeDefinition;
import ch.njol.brokkr.ir.definitions.IRBrokkrGenericTypeRedefinition;
import ch.njol.brokkr.ir.definitions.IRBrokkrNormalParameterDefinition;
import ch.njol.brokkr.ir.definitions.IRBrokkrNormalParameterRedefinition;
import ch.njol.brokkr.ir.definitions.IRBrokkrResultDefinition;
import ch.njol.brokkr.ir.definitions.IRBrokkrResultRedefinition;
import ch.njol.brokkr.ir.definitions.IRGenericTypeRedefinition;
import ch.njol.brokkr.ir.definitions.IRMemberRedefinition;
import ch.njol.brokkr.ir.definitions.IRParameterDefinition;
import ch.njol.brokkr.ir.definitions.IRParameterRedefinition;
import ch.njol.brokkr.ir.definitions.IRResultRedefinition;
import ch.njol.brokkr.ir.definitions.IRTypeDefinition;
import ch.njol.brokkr.ir.definitions.IRVariableRedefinition;
import ch.njol.brokkr.ir.expressions.IRAttributeAccess;
import ch.njol.brokkr.ir.nativetypes.IRTuple.IRTypeTuple;
import ch.njol.brokkr.ir.statements.IRPostcondition;
import ch.njol.brokkr.ir.statements.IRPrecondition;
import ch.njol.brokkr.ir.statements.IRStatement;
import ch.njol.brokkr.ir.statements.IRUnknownStatement;
import ch.njol.brokkr.ir.uses.IRMemberUse;
import ch.njol.brokkr.ir.uses.IRSimpleTypeUse;
import ch.njol.brokkr.ir.uses.IRTypeUse;
import ch.njol.brokkr.ir.uses.IRUnknownTypeUse;
import ch.njol.util.StringUtils;

// TODO sections (e.g. 'section nameHere { members here ... }')
// TODO annotations for time & memory use:
// time: uses some "unit time", where native methods, method calls, memory allocations etc. define the base values
// memory: annotation for max memory usage of a class (maybe?) as well as memory changes of methods (maybe defined for the 'this' object and all arguments separately)
public class ASTMembers {
	
	public static ASTMember parse(final AbstractASTElement<?> parent) throws ParseException {
		if (parent.peekNext('$') && parent.peekNext('=', 1, false))
			return parent.one(ASTCodeGenerationCallMember.class);
		if (parent.peekNext("delegate"))
			return parent.one(ASTDelegation.class);
//			if (parent.peekNext("element"))
//				return parent.one(EnumElement.class);
		
		final ASTMemberModifiers modifiers = parent.one(ASTMemberModifiers.class);
		try {
//				if (!modifiers.genericParameters.isEmpty() && parent.peekNext(';'))
//					return parent.one(new GenericTypeParameter(modifiers));
			if (parent.peekNext("interface"))
				return parent.one(new ASTInterfaceDeclaration(modifiers));
			if (parent.peekNext("class"))
				return parent.one(new ASTClassDeclaration(modifiers));
			if (parent.peekNext("constructor"))
				return parent.one(new ASTConstructor(modifiers));
			if (parent.peekNext("invariant"))
				return parent.one(new ASTInvariant(modifiers));
			if (parent.peekNext("type"))
				return parent.one(new ASTGenericTypeDeclaration(modifiers));
			if (parent.peekNext("code") || parent.peekNext("member") || parent.peekNext("type"))
				return parent.one(new ASTTemplate(modifiers));
			return parent.one(new ASTAttributeDeclaration(modifiers));
			// TODO constants? e.g. like 'constant Type name = value, name2 = value2;'
		} finally {
			assert modifiers.parent() != parent;
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
	public static class ASTMemberModifiers extends AbstractASTElement<ASTMemberModifiers> {
		public final List<ASTGenericTypeDeclaration> genericParameters = new ArrayList<>();
		public boolean override;
		public boolean partialOverride;
		public boolean hide;
		public boolean undefine;
		public final ASTLink<IRTypeDefinition> overriddenFromType = new ASTLink<IRTypeDefinition>(this) {
			@Override
			protected @Nullable IRTypeDefinition tryLink(final String name) {
				final ASTBrokkrFile file = getParentOfType(ASTBrokkrFile.class);
				if (file == null)
					return null;
				final ASTModule module = file.module;
				return module == null ? null : module.getType(name);
			}
		};
		public final ASTLink<IRMemberRedefinition> overridden = new ASTLink<IRMemberRedefinition>(this) {
			@Override
			protected @Nullable IRMemberRedefinition tryLink(String name) {
				final WordToken nameToken = getNameToken();
				assert nameToken != null;
				if (nameToken.keyword) { // i.e. the 'override' keyword
					if (parent == null || !(parent instanceof NamedASTElement)) {
						System.out.println("Warning: parent <" + parent + "> of 'overridden' link is not a NamedASTElement");
						return null;
					}
					@SuppressWarnings("null")
					final String n = ((NamedASTElement) parent).name();
					if (n == null)
						return null;
					name = n;
				}
				final IRTypeDefinition fromType = overriddenFromType.getNameToken() == null ? null : overriddenFromType.get();
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
		};
		public @Nullable Visibility visibility;
		public boolean isNative;
		public boolean isStatic;
		public @Nullable MethodModifiability modifiability;
		public boolean context;
		public boolean recursive;
		public boolean var;
		
		@Override
		public String toString() {
			return (isNative ? "native " : "") + (isStatic ? "static " : "") + (visibility != null ? visibility + " " : "")
					+ (modifiability != null ? modifiability + " " : "") + (context ? "context " : "") + (recursive ? "recursive " : "") + (var ? "var " : "")
					+ (override ? "override " : partialOverride ? "partialOverride " : "")
					+ (hide ? "hide " : "") + (undefine ? "undefine " : "")
					+ (overriddenFromType.getNameToken() != null ? overriddenFromType.getName() + "." : "")
					+ (overridden.getNameToken() != null ? overridden.getName() + " as " : "");
		}
		
		@Override
		public @Nullable String hoverInfo(final Token token) {
			return null;
		}
		
		@Override
		protected ASTMemberModifiers parse() throws ParseException {
			// modifiers
			unordered(() -> {
				tryGroup('<', () -> {
					do {
						genericParameters.add(one(new ASTGenericTypeDeclaration(null)));
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
	
	public static class ASTAttributeDeclaration extends AbstractASTElement<ASTAttributeDeclaration> implements ASTAttribute {
		public final ASTMemberModifiers modifiers;
		
		public @Nullable LowercaseWordToken name;
		public boolean hasParameterDefinitions;
		public boolean hasParameterDots;
		public List<ASTSimpleParameter> parameters = new ArrayList<>();
		public boolean hasResultDefinitions;
		public boolean hasResultDots;
		public List<ASTResult> results = new ArrayList<>();
		public List<ASTErrorDeclaration> errors = new ArrayList<>();
		public boolean isAbstract;
		public @Nullable ASTBlock body;
		public List<ASTPrecondition> preconditions = new ArrayList<>();
		public List<ASTPostcondition> postconditions = new ArrayList<>();
		
		public ASTAttributeDeclaration(final ASTMemberModifiers modifiers) {
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
		public ASTMemberModifiers modifiers() {
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
//			final IRMember a = modifiers.overridden.get();
//			return a != null && a instanceof IRAttributeRedefinition ? Arrays.asList((IRAttributeRedefinition) a) : Collections.EMPTY_LIST;
//		}
		
		@Override
		public List<? extends IRVariableRedefinition> allVariables() {
			final IRAttributeRedefinition interpreted = getIR();
			final List<IRVariableRedefinition> result = new ArrayList<>();
			result.addAll(interpreted.parameters());
			result.addAll(interpreted.results());
			return result;
		}
		
		@Override
		public List<? extends ASTError> declaredErrors() {
			return errors;
		}
		
		@Override
		public List<? extends ASTResult> declaredResults() {
			return results;
		}
		
		@Override
		public String toString() {
			return name
					+ (parameters.size() == 0 ? "" : "(" + StringUtils.join(parameters, ", ") + ")")
					+ ": " + (results.size() == 0 ? "[]" : String.join(", ", results.stream().map(r -> r.toString()).toArray(i -> new String[i])));
		}
		
		@Override
		public @Nullable String hoverInfo(final Token token) {
			return getIR().hoverInfo();
		}
		
		private @Nullable IRAttributeRedefinition ir;
		
		@Override
		public IRAttributeRedefinition getIR() {
			if (ir != null)
				return ir;
			final IRMemberRedefinition overridden = modifiers.overridden.get();
			if (overridden instanceof IRAttributeRedefinition) {
				if (body != null)
					return ir = new IRBrokkrAttributeImplementation(this, (IRAttributeRedefinition) overridden);
				else
					return ir = new IRBrokkrAttributeRedefinition(this, (IRAttributeRedefinition) overridden);
			} else {
//				if (overridden != null)
				// TODO semantic error
				if (body != null)
					return ir = new IRBrokkrAttributeDefinitionAndImplementation(this);
				else
					return ir = new IRBrokkrAttributeDefinition(this);
			}
		}
		
		@Override
		protected ASTAttributeDeclaration parse() throws ParseException {
			
			// name
			name = oneVariableIdentifierToken();
			
			// parameters
			tryGroup('(', () -> {
				hasParameterDefinitions = true;
				if (try_("..."))
					hasParameterDots = true;
				if (!hasParameterDots || try_(',')) {
					do {
						parameters.add(one(new ASTSimpleParameter(this, parameters.size())));
					} while (try_(','));
				}
			}, ')');
			
			// results
			if (try_(':')) {
				hasResultDefinitions = true;
				if (try_("..."))
					hasResultDots = true;
				if (!hasResultDots || try_(',')) {
					do {
						results.add(one(new ASTNormalResult(this)));
					} while (try_(','));
				}
			}
			
			// errors
			while (peekNext('#')) {
				errors.add(one(ASTErrorDeclaration.class));
			}
			
			// body
			if (peekNext('{')) {
				body = one(ASTBlock.class);
			} else if (try_(';')) { // abstract / single expression syntax
				while (peekNext("requires"))
					preconditions.add(one(ASTPrecondition.class));
				while (peekNext("ensures"))
					postconditions.add(one(ASTPostcondition.class));
			} else if (!hasResultDefinitions && errors.isEmpty()) { // 'attribute = value' syntax (nonexistent or overridden result)
				one("=");
				body = new ASTBlock(one(new ASTReturn(false)));
				while (peekNext("requires"))
					preconditions.add(one(ASTPrecondition.class));
				while (peekNext("ensures"))
					postconditions.add(one(ASTPostcondition.class));
			}
			
			return this;
		}
	}
	
	public static class ASTSimpleParameter extends AbstractASTElement<ASTSimpleParameter> implements ASTParameter {
		public ASTAttribute attribute;
		private final int index;
		public @Nullable Visibility visibility;
		public boolean override;
		public final ASTLink<IRParameterRedefinition> overridden = new ASTLink<IRParameterRedefinition>(this) {
			@Override
			protected @Nullable IRParameterRedefinition tryLink(final String name) {
				// TODO parameter named like this link, or parameter with same position as this parameter (either from left or right, depending on where the dots are (if any)).
				// for the position, this first requires to set this link to 'override' or such (no name = no linking!) [see MemberModifiers.overridden for how]
				final ASTAttributeDeclaration attribute = getParentOfType(ASTAttributeDeclaration.class);
				if (attribute == null)
					return null;
				final IRMemberRedefinition parent = attribute.modifiers.overridden.get();
				if (parent == null || !(parent instanceof IRAttributeRedefinition))
					return null;
				return ((IRAttributeRedefinition) parent).getParameterByName(name);
			}
		};
		public @Nullable ASTTypeUse type;
		public @Nullable WordToken name;
		public @Nullable ASTExpression defaultValue;
		
		public ASTSimpleParameter(final ASTAttribute attribute, final int index) {
			this.attribute = attribute;
			this.index = index;
		}
		
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
		public @Nullable String hoverInfo(final Token token) {
			return getIR().hoverInfo();
		}
		
		@Override
		protected ASTSimpleParameter parse() throws ParseException {
			visibility = Visibility.parse(this);
			override = try_("override");
			if (override) {
				if (peekNext() instanceof WordToken && peekNext("as", 1, true)) {
					overridden.setName(oneIdentifierToken());
					next(); // skip 'as'
				}
				if (overridden.getNameToken() == null // if not renamed, the only thing left to change is the type, so require it
						|| !(peekNext() instanceof WordToken && (peekNext(',', 1, true) || peekNext(')', 1, true)))) // allows overriding the name without changing the type
					type = ASTTypeExpressions.parse(this, true, true);
			} else {
				type = ASTTypeExpressions.parse(this, true, true);
			}
			name = oneIdentifierToken();
			if (override && overridden.getNameToken() == null) // overridden, but not renamed - use same name to look up parent parameter
				overridden.setName(name);
			if (try_('='))
				defaultValue = ASTExpressions.parse(this);
			return this;
		}
		
		@Override
		public IRTypeUse getIRType() {
			if (type != null)
				return type.getIR();
			final IRParameterRedefinition param = overridden.get();
			if (param != null)
				return param.type();
			return new IRUnknownTypeUse(getIRContext());
		}
		
		private @Nullable IRParameterRedefinition ir;
		
		@Override
		public IRParameterRedefinition getIR() {
			if (ir != null)
				return ir;
			final IRParameterRedefinition parent = override ? overridden.get() : null;
			return ir = (parent != null ? new IRBrokkrNormalParameterRedefinition(this, parent, attribute.getIR())
					: new IRBrokkrNormalParameterDefinition(this, attribute.getIR()));
		}
	}
	
	public static class ASTNormalResult extends AbstractASTElement<ASTNormalResult> implements ASTVariable, ASTResult {
		public final ASTAttribute attribute;
		public @Nullable ASTTypeUse type;
		public @Nullable LowercaseWordToken name;
		public @Nullable ASTExpression defaultValue;
		public ASTLink<IRResultRedefinition> overridden = new ASTLink<IRResultRedefinition>(this) {
			@Override
			protected @Nullable IRResultRedefinition tryLink(final String name) {
				@SuppressWarnings("null")
				final IRAttributeRedefinition parentAttr = getParentOfType(ASTAttribute.class).getIR().parentRedefinition();
				if (parentAttr == null)
					return null;
				return parentAttr.getResultByName(name);
			}
		};
		
		public ASTNormalResult(final ASTAttribute attribute) {
			this.attribute = attribute;
		}
		
		@Override
		public @Nullable WordToken nameToken() {
			return name;
		}
		
		@Override
		public @NonNull String name() {
			final WordToken wordToken = name;
			return wordToken != null ? wordToken.word : "result";
		}
		
		@Override
		public String toString() {
			return (type == null ? "<unresolvable type>" : type) + " " + (name == null ? "result" : "" + name);
		}
		
		@Override
		public @Nullable String hoverInfo(final Token token) {
			return getIR().hoverInfo();
		}
		
		@Override
		protected ASTNormalResult parse() throws ParseException {
			if (try_("override")) {
				overridden.setName(oneVariableIdentifierToken());
				one("as");
			}
			boolean parseType = true;
			if (peekNext() instanceof LowercaseWordToken) {
				final Token nextNext = peekNext(1, true);
				if (nextNext instanceof SymbolToken && "=,#{".indexOf(((SymbolToken) nextNext).symbol) >= 0)
					parseType = false;
			}
			if (parseType)
				type = ASTTypeExpressions.parse(this, true, true);
			name = tryVariableIdentifierToken();
//			if (name == null)
//				name = "result";
//			if (peekNext("=>")) // method '=> results' syntax
//				return this;
			if (try_('='))
				defaultValue = ASTExpressions.parse(this);
			return this;
		}
		
		@Override
		public IRTypeUse getIRType() {
			final ASTTypeUse type = this.type;
			if (type != null)
				return type.getIR();
			final IRResultRedefinition parent = overridden.get();
			if (parent == null)
				return new IRUnknownTypeUse(getIRContext());
			return parent.type();
		}
		
		private @Nullable IRResultRedefinition ir;
		
		@Override
		public IRResultRedefinition getIR() {
			if (ir != null)
				return ir;
			final IRResultRedefinition parent = overridden.get();
			return ir = parent == null ? new IRBrokkrResultDefinition(this, attribute.getIR()) : new IRBrokkrResultRedefinition(this, parent, attribute.getIR());
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
		public IRResult interpreted() {
			// TODO pass along the link to the parameter too (or do this differently)
			return new IRResult(getParentOfType(FormalAttribute.class).interpreted(), parameter.getName(), parameter.get().interpreted().nativeType());
		}
	}
	*/
	
	public static class ASTErrorDeclaration extends AbstractASTElement<ASTErrorDeclaration> implements ASTError {
		public @Nullable LowercaseWordToken name;
		public List<ASTParameter> parameters = new ArrayList<>();
		
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
//			final @Nullable IRMemberRedefinition parent = ((FormalAttribute) parent()).modifiers().overridden.get();
//			return parent == null || name == null || !(parent instanceof IRAttributeRedefinition) ? null //
//					: ((IRAttributeRedefinition) parent).getErrorByName(name.word);
//		}
		
		@Override
		protected @NonNull ASTErrorDeclaration parse() throws ParseException {
			one('#');
			name = oneVariableIdentifierToken();
			tryGroup('(', () -> {
				do {
					// FIXME
//					parameters.add(one(new ASTSimpleParameter(this)));
				} while (try_(','));
			}, ')');
			return this;
		}
		
		private @Nullable IRNormalError ir;
		
		@Override
		public IRError getIRError() {
			if (ir != null)
				return ir;
			final ASTAttributeDeclaration attribute = getParentOfType(ASTAttributeDeclaration.class);
			if (attribute == null)
				return new IRUnknownError("" + name(), "internal compiler error", this);
			return ir = new IRNormalError("" + name(), parameters.stream().map(p -> p.getIR()).collect(Collectors.toList()), attribute.getIR());
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
	
	public static class ASTConstructor extends AbstractASTElement<ASTConstructor> implements ASTAttribute {
		public final ASTMemberModifiers modifiers;
		
		public @Nullable LowercaseWordToken name;
		public List<ASTParameter> parameters = new ArrayList<>();
		public @Nullable ASTBlock body;
		public final List<ASTPostcondition> postconditions = new ArrayList<>();
		
		public ASTConstructor(final ASTMemberModifiers modifiers) {
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
		public ASTMemberModifiers modifiers() {
			return modifiers;
		}
		
		@Override
		public List<? extends IRVariableRedefinition> allVariables() {
			return getIR().parameters();
		}
		
		@Override
		public List<ASTError> declaredErrors() {
			return Collections.EMPTY_LIST; // FIXME preconditions are errors too!
		}
		
		@Override
		public List<? extends ASTResult> declaredResults() {
			return Collections.EMPTY_LIST; // FIXME
		}
		
		@Override
		public String toString() {
			return "" + name + (parameters.size() == 0 ? "" : "(" + StringUtils.join(parameters, ", ") + ")");
		}
		
		@Override
		public @Nullable String hoverInfo(final Token token) {
			return getIR().hoverInfo();
		}
		
		@Override
		protected ASTConstructor parse() throws ParseException {
			one("constructor");
			name = oneVariableIdentifierToken();
			oneGroup('(', () -> {
				do {
					if (peekNext('=', 1, true) || peekNext(',', 1, true) || peekNext(')', 1, true))
						parameters.add(one(new ASTConstructorFieldParameter(this)));
					else
						parameters.add(one(new ASTSimpleParameter(this, parameters.size())));
				} while (try_(','));
			}, ')');
			if (!try_(';')) // field params syntax
				body = one(ASTBlock.class);
			return this;
		}
		
		@SuppressWarnings("null")
		@Override
		public IRTypeUse getIRType() {
			return new IRSimpleTypeUse(getParentOfType(ASTTypeDeclaration.class).getIR());
		}
		
		private @Nullable IRBrokkrConstructor ir;
		
		@Override
		public IRBrokkrConstructor getIR() {
			if (ir != null)
				return ir;
			return ir = new IRBrokkrConstructor(this);
		}
	}
	
	public static class ASTConstructorFieldParameter extends AbstractASTElement<ASTConstructorFieldParameter> implements ASTParameter {
		public final ASTLink<IRAttributeRedefinition> attribute = new ASTLink<IRAttributeRedefinition>(this) {
			@Override
			protected @Nullable IRAttributeRedefinition tryLink(final String name) {
				final ASTTypeDeclaration mc = ASTConstructorFieldParameter.this.getParentOfType(ASTTypeDeclaration.class);
				assert mc != null;
				return mc.getIR().getAttributeByName(name);
			}
		};
		public @Nullable ASTExpression defaultValue;
		
		public final ASTConstructor constructor;
		
		public ASTConstructorFieldParameter(final ASTConstructor constructor) {
			this.constructor = constructor;
		}
		
		@Override
		public @Nullable WordToken nameToken() {
			return attribute.getNameToken();
		}
		
		@Override
		protected ASTConstructorFieldParameter parse() throws ParseException {
			attribute.setName(oneVariableIdentifierToken());
			if (try_('='))
				defaultValue = ASTExpressions.parse(this);
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
		
		@Override
		public @Nullable String hoverInfo(final Token token) {
			return getIR().hoverInfo();
		}
		
		@SuppressWarnings("null")
		@Override
		public IRTypeUse getIRType() {
			final IRAttributeRedefinition f = attribute.get();
			return f == null ? null : f.mainResultType();
		}
		
		private @Nullable IRParameterDefinition ir;
		
		@Override
		public IRParameterDefinition getIR() {
			if (ir != null)
				return ir;
			final IRAttributeRedefinition attr = attribute.get();
			if (attr == null || attr.results().size() != 1 || !attr.results().get(0).name().equals("result") || !attr.isVariable())
				return new IRUnknownParameterDefinition(attr != null ? attr.name() : "<unknown name>", new IRUnknownTypeUse(getIRContext()), constructor.getIR(), "Constructor field parameter '" + (attr != null ? attr.name() : "<unknown name>") + "' does not reference a field", this);
			return ir = new IRBrokkrConstructorFieldParameter(this, attr, constructor.getIR());
		}
	}
	
	public static class ASTGenericTypeDeclaration extends AbstractASTElement<ASTGenericTypeDeclaration> implements ASTMember, NamedASTElement {
		/**
		 * null if and only if this is an implicit generic type (i.e. is part of a MemberModifiers itself)
		 */
		public final @Nullable ASTMemberModifiers modifiers;
		
		public @Nullable UppercaseWordToken name;
		public @Nullable ASTTypeUse superType, extendedType;
		public @Nullable ASTTypeUse defaultType;
		
		public ASTGenericTypeDeclaration(final @Nullable ASTMemberModifiers modifiers) {
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
		public @Nullable WordToken nameToken() {
			return name;
		}
		
		@Override
		protected ASTGenericTypeDeclaration parse() throws ParseException {
			final VoidProcessor simple = () -> {
				name = oneTypeIdentifierToken();
				unordered(() -> {
					if (try_("super")) {
						superType = ASTTypeExpressions.parse(this, true, true);
					}
				}, () -> {
					if (try_("extends")) {
						extendedType = ASTTypeExpressions.parse(this, true, true);
					}
				});
				if (try_('=')) {
					defaultType = ASTTypeExpressions.parse(this, true, true);
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
		
		private @Nullable IRGenericTypeRedefinition ir;
		
		public IRGenericTypeRedefinition getIR() {
			if (ir != null)
				return ir;
			final ASTMemberModifiers modifiers = this.modifiers;
			final IRMemberRedefinition parent = modifiers != null ? modifiers.overridden.get() : null;
			return ir = (parent == null || !(parent instanceof IRGenericTypeRedefinition)
					? new IRBrokkrGenericTypeDefinition(this)
					: new IRBrokkrGenericTypeRedefinition(this, (IRGenericTypeRedefinition) parent));
		}
		
		@Override
		public List<IRGenericTypeRedefinition> getIRMembers() {
			return Collections.singletonList(getIR());
		}
	}
	
	public static class ASTTemplate extends AbstractASTElement<ASTTemplate> implements ASTAttribute {
		public final ASTMemberModifiers modifiers;
		
		public @Nullable TemplateType templateType;
		public @Nullable LowercaseWordToken name;
		public List<ASTParameter> parameters = new ArrayList<>();
		public @Nullable ASTBlock body;
		
		enum TemplateType {
			TYPE, MEMBER, CODE;
		}
		
		public ASTTemplate(final ASTMemberModifiers modifiers) {
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
		public ASTMemberModifiers modifiers() {
			return modifiers;
		}
		
		@Override
		public List<? extends IRVariableRedefinition> allVariables() {
			return getIR().parameters();
		}
		
		@Override
		public List<ASTError> declaredErrors() {
			return Collections.EMPTY_LIST; // FIXME preconditions are errors too!
		}
		
		@Override
		public List<? extends ASTResult> declaredResults() {
			return Collections.EMPTY_LIST;
		}
		
		@Override
		public String toString() {
			return "" + name;
		}
		
		@Override
		protected ASTTemplate parse() throws ParseException {
			templateType = TemplateType.valueOf(oneOf("code", "member", "type").toUpperCase(Locale.ENGLISH));
			one("template");
			name = oneVariableIdentifierToken();
			tryGroup('(', () -> {
				do {
					parameters.add(one(new ASTSimpleParameter(this, parameters.size())));
				} while (try_(','));
			}, ')');
			body = one(ASTBlock.class);
			return this;
		}
		
		@Override
		public IRTypeUse getIRType() {
			return IRTypeTuple.emptyTuple(getIRContext());
		}
		
		@Override
		public IRAttributeRedefinition getIR() {
			return new IRBrokkrTemplate(this);
		}
	}
	
	// TODO remove this? can be done with code generation, assuming it can work with incomplete types (what incomplete types?)
	public static class ASTDelegation extends AbstractASTElement<ASTDelegation> implements ASTMember {
		public List<ASTLink<IRAttributeRedefinition>> methods = new ArrayList<>();
		public List<ASTTypeExpression> types = new ArrayList<>();
		public List<ASTExpression> expressions = new ArrayList<>();
		public @Nullable ASTExpression joinWith;
		
		private class MethodLink extends ASTLink<IRAttributeRedefinition> {
			public MethodLink(@Nullable final WordToken name) {
				super(ASTDelegation.this, name);
			}
			
			@Override
			protected @Nullable IRAttributeRedefinition tryLink(final String name) {
				final ASTTypeDeclaration type = getParentOfType(ASTTypeDeclaration.class);
				if (type == null)
					return null;
				return type.getIR().getAttributeByName(name);
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
		protected ASTDelegation parse() throws ParseException {
			until(() -> {
				one("delegate");
				do {
					if (!methods.isEmpty() || types.isEmpty() && peekNext() instanceof LowercaseWordToken && (peekNext(',', 1, true) || peekNext("to", 1, true)))
						methods.add(new MethodLink(oneVariableIdentifierToken()));
					else
						types.add(ASTTypeExpressions.parse(this, true, true));
				} while (try_(','));
				one("to");
				do {
					expressions.add(ASTExpressions.parse(this));
				} while (try_(','));
				if (try_("join")) {
					one("with");
					joinWith = ASTExpressions.parse(this);
				}
			}, ';', false);
			return this;
		}
		
		@Override
		public List<? extends IRMemberRedefinition> getIRMembers() {
			return Collections.EMPTY_LIST; // TODO
		}
	}
	
	public static class ASTInvariant extends AbstractASTElement<ASTInvariant> implements ASTMember, NamedASTElement {
		public final ASTMemberModifiers modifiers;
		
		public boolean negated;
		public @Nullable LowercaseWordToken name;
		public @Nullable ASTExpression expression;
		
		public ASTInvariant(final ASTMemberModifiers modifiers) {
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
		protected ASTInvariant parse() throws ParseException {
			until(() -> {
				one("invariant");
				negated = try_('!');
				name = oneVariableIdentifierToken();
				one(':');
				expression = ASTExpressions.parse(this); // TODO allow some statements?
			}, ';', false);
			return this;
		}
		
		@Override
		public List<? extends IRMemberRedefinition> getIRMembers() {
			return Collections.EMPTY_LIST; // TODO
		}
	}
	
	public static class ASTPrecondition extends AbstractASTElement<ASTPrecondition> implements ASTStatement, ASTError {
		public boolean negated;
		public @Nullable LowercaseWordToken name;
		public @Nullable ASTExpression expression;
		
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
//		public List<? extends IRVariableRedefinition> declaredVariables() {
//			return Collections.EMPTY_LIST; // preconditions have no parameters
//		}
//
//		@Override
//		public @Nullable HasVariables parentHasVariables() {
//			return null; // TODO return overridden precondition?
//		}
		
		@Override
		protected ASTPrecondition parse() throws ParseException {
			until(() -> {
				one("requires");
				//genericRestrictions=GenericParameters? // FIXME wrong
				negated = peekNext('!');
				Token t;
				if ((t = peekNext(negated ? 1 : 0, true)) instanceof LowercaseWordToken && peekNext(';', negated ? 2 : 1, true)) {
					name = (LowercaseWordToken) t;
					expression = ASTExpressions.parse(this);
				} else {
					negated = try_('!');
					name = oneVariableIdentifierToken();
					if (try_(':'))
						expression = ASTExpressions.parse(this); // TODO allow some statements?
				}
			}, ';', false);
			return this;
		}
		
		@Override
		public IRStatement getIR() {
			final String name = this.name != null ? this.name.word : "<unknown name>";
			final ASTAttributeDeclaration attribute = getParentOfType(ASTAttributeDeclaration.class);
			if (attribute == null)
				return new IRUnknownError(name, "Internal compiler error (precondition not in attribute)", this);
			final ASTExpression expression = this.expression;
			if (expression == null)
				return new IRUnknownError(name, "Syntax error. Proper syntax: [ensures some_expression;] or [ensures name: some_expression;] or [ensures !name: some_expression;]", this);
			final IRAttributeRedefinition negatedAttribute = getIRContext().getTypeDefinition("lang", "Boolean").getAttributeByName("negated");
			if (negatedAttribute == null)
				return new IRUnknownError(name, "Cannot find attribute lang.Boolean.negated", this);
			return new IRPrecondition(attribute.getIR(), name,
					negated ? new IRAttributeAccess(expression.getIR(), negatedAttribute, Collections.EMPTY_MAP, false, false, false) : expression.getIR());
		}
		
		@Override
		public IRError getIRError() {
			return (IRError) getIR();
		}
	}
	
	public static class ASTPostcondition extends AbstractASTElement<ASTPostcondition> implements ASTStatement, NamedASTElement {
		public boolean negated;
		public @Nullable LowercaseWordToken name;
		public @Nullable ASTExpression expression;
		
		@Override
		public @Nullable WordToken nameToken() {
			return name;
		}
		
		@Override
		public String toString() {
			return "ensures " + (name == null ? "..." : name);
		}
		
		@Override
		public @Nullable String hoverInfo(final Token token) {
			return null;
		}
		
		@Override
		protected ASTPostcondition parse() throws ParseException {
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
				expression = ASTExpressions.parse(this); // TODO allow some statements?
			}, ';', false);
			return this;
		}
		
		@Override
		public IRStatement getIR() {
			final ASTAttributeDeclaration attribute = getParentOfType(ASTAttributeDeclaration.class);
			if (attribute == null)
				return new IRUnknownStatement("Internal compiler error", this);
			final WordToken name = this.name;
			final ASTExpression expression = this.expression;
			if (expression == null)
				return new IRUnknownStatement("Syntax error. Proper syntax: [ensures some_expression;] or [ensures name: some_expression;] or [ensures !name: some_expression;]", this);
			final IRAttributeRedefinition negatedAttribute = getIRContext().getTypeDefinition("lang", "Boolean").getAttributeByName("negated");
			if (negatedAttribute == null)
				return new IRUnknownStatement("Cannot find attribute lang.Boolean.negated", this);
			return new IRPostcondition(attribute.getIR(), name != null ? name.word : null,
					negated ? new IRAttributeAccess(expression.getIR(), negatedAttribute, Collections.EMPTY_MAP, false, false, false) : expression.getIR());
		}
	}
	
	public static class ASTCodeGenerationCallMember extends AbstractASTElement<ASTCodeGenerationCallMember> implements ASTMember {
		public @Nullable ASTExpression code;
		
		@Override
		public boolean isInherited() {
			return false; // only generates code at the current location
		}
		
		@Override
		public String toString() {
			return "$= " + code + ";";
		}
		
		@Override
		protected ASTCodeGenerationCallMember parse() throws ParseException {
			one("$=");
			until(() -> {
				code = ASTExpressions.parse(this);
			}, ';', false);
			return this;
		}
		
		// TODO make sure to prevent infinite recursion with other types!
		@Override
		public List<IRMemberRedefinition> getIRMembers() {
			final ASTExpression code = this.code;
			if (code == null)
				return Arrays.asList();
			try {
				final InterpretedObject result = code.getIR().interpret(new InterpreterContext(getIRContext(), (InterpretedNormalObject) null));
				if (!(result instanceof InterpretedNativeCodeGenerationResult))
					return Collections.EMPTY_LIST; // Collections.singletonList(new IRUnknownMember("Must call a code generation template", this));
				return ((InterpretedNativeCodeGenerationResult) result).parseMembers(this);
			} catch (final InterpreterException e) {
				return Collections.EMPTY_LIST; // Collections.singletonList(new IRUnknownMember("" + e.getMessage(), this));
			}
		}
	}
	
}
