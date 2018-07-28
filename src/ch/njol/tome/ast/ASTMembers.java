package ch.njol.tome.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTExpressions.ASTBlock;
import ch.njol.tome.ast.ASTExpressions.ASTTypeExpressions;
import ch.njol.tome.ast.ASTInterfaces.ASTAttribute;
import ch.njol.tome.ast.ASTInterfaces.ASTError;
import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.ASTInterfaces.ASTMember;
import ch.njol.tome.ast.ASTInterfaces.ASTParameter;
import ch.njol.tome.ast.ASTInterfaces.ASTResult;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeDeclaration;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeExpression;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeUse;
import ch.njol.tome.ast.ASTInterfaces.ASTVariable;
import ch.njol.tome.ast.ASTInterfaces.NamedASTElement;
import ch.njol.tome.ast.ASTStatements.ASTReturn;
import ch.njol.tome.ast.ASTStatements.ASTStatement;
import ch.njol.tome.ast.ASTTopLevelElements.ASTClassDeclaration;
import ch.njol.tome.ast.ASTTopLevelElements.ASTInterfaceDeclaration;
import ch.njol.tome.ast.ASTTopLevelElements.ASTSourceFile;
import ch.njol.tome.common.MethodModifiability;
import ch.njol.tome.common.Visibility;
import ch.njol.tome.compiler.Token;
import ch.njol.tome.compiler.Token.LowercaseWordToken;
import ch.njol.tome.compiler.Token.SymbolToken;
import ch.njol.tome.compiler.Token.UppercaseWordToken;
import ch.njol.tome.compiler.Token.WordOrSymbols;
import ch.njol.tome.compiler.Token.WordToken;
import ch.njol.tome.interpreter.InterpretedNormalObject;
import ch.njol.tome.interpreter.InterpretedObject;
import ch.njol.tome.interpreter.InterpreterContext;
import ch.njol.tome.interpreter.InterpreterException;
import ch.njol.tome.interpreter.nativetypes.InterpretedNativeCodeGenerationResult;
import ch.njol.tome.ir.IRBrokkrTemplate;
import ch.njol.tome.ir.IRError;
import ch.njol.tome.ir.IRNormalError;
import ch.njol.tome.ir.IRUnknownError;
import ch.njol.tome.ir.IRUnknownParameterDefinition;
import ch.njol.tome.ir.definitions.IRAttributeRedefinition;
import ch.njol.tome.ir.definitions.IRBrokkrAttributeDefinition;
import ch.njol.tome.ir.definitions.IRBrokkrAttributeDefinitionAndImplementation;
import ch.njol.tome.ir.definitions.IRBrokkrAttributeImplementation;
import ch.njol.tome.ir.definitions.IRBrokkrAttributeRedefinition;
import ch.njol.tome.ir.definitions.IRBrokkrConstructor;
import ch.njol.tome.ir.definitions.IRBrokkrConstructorFieldParameter;
import ch.njol.tome.ir.definitions.IRBrokkrNormalParameterDefinition;
import ch.njol.tome.ir.definitions.IRBrokkrNormalParameterRedefinition;
import ch.njol.tome.ir.definitions.IRBrokkrResultDefinition;
import ch.njol.tome.ir.definitions.IRBrokkrResultRedefinition;
import ch.njol.tome.ir.definitions.IRMemberRedefinition;
import ch.njol.tome.ir.definitions.IRParameterDefinition;
import ch.njol.tome.ir.definitions.IRParameterRedefinition;
import ch.njol.tome.ir.definitions.IRResultRedefinition;
import ch.njol.tome.ir.definitions.IRTypeDefinition;
import ch.njol.tome.ir.definitions.IRVariableRedefinition;
import ch.njol.tome.ir.expressions.IRAttributeAccess;
import ch.njol.tome.ir.nativetypes.IRTuple.IRTypeTuple;
import ch.njol.tome.ir.statements.IRPostcondition;
import ch.njol.tome.ir.statements.IRPrecondition;
import ch.njol.tome.ir.statements.IRStatement;
import ch.njol.tome.ir.statements.IRUnknownStatement;
import ch.njol.tome.ir.uses.IRMemberUse;
import ch.njol.tome.ir.uses.IRSimpleTypeUse;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.ir.uses.IRUnknownTypeUse;
import ch.njol.tome.moduleast.ASTModule;
import ch.njol.tome.parser.Parser;
import ch.njol.util.StringUtils;

// TODO sections (e.g. 'section nameHere { members here ... }')
// TODO annotations for time & memory use:
// time: uses some "unit time", where native methods, method calls, memory allocations etc. define the base values
// memory: annotation for max memory usage of a class (maybe?) as well as memory changes of methods (maybe defined for the 'this' object and all arguments separately)
public class ASTMembers {
	
	public static ASTMember parse(final Parser parent) {
		if (parent.peekNext('$') && parent.peekNext('=', 1, false))
			return ASTCodeGenerationCallMember.parse(parent);
		if (parent.peekNext("delegate"))
			return ASTDelegation.parse(parent);
//			if (parent.peekNext("element"))
//				return parent.one(EnumElement.class);
			
		final Parser p = parent.start();
		final ASTMemberModifiers modifiers = ASTMemberModifiers.parse(p);
//				if (!modifiers.genericParameters.isEmpty() && parent.peekNext(';'))
//					return parent.one(new GenericTypeParameter(modifiers));
		if (p.peekNext("interface"))
			return ASTInterfaceDeclaration.finishParsing(p, modifiers);
		if (p.peekNext("class"))
			return ASTClassDeclaration.finishParsing(p, modifiers);
		if (p.peekNext("constructor"))
			return ASTConstructor.finishParsing(p, modifiers);
		if (p.peekNext("invariant"))
			return ASTInvariant.finishParsing(p, modifiers);
//			if (parent.peekNext("type"))
//				return parent.one(new ASTGenericTypeDeclaration(modifiers));
		if (p.peekNext("code") || p.peekNext("member") || p.peekNext("type"))
			return ASTTemplate.finishParsing(p, modifiers);
		return ASTAttributeDeclaration.finishParsing(p, modifiers);
		// TODO constants? e.g. like 'constant Type name = value, name2 = value2;'
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
//		protected MemberRegion parse() {
//			// TODO Auto-generated method stub
//			return this;
//		}
//	}
	
	// TODO a correctness annotation that means that an attribute does not change (anymore), e.g. a field is "final" or a method always returns the same value, even if the object is modified.
	public static class ASTMemberModifiers extends AbstractASTElement {
		public final List<ASTGenericArgumentDeclaration> genericParameters = new ArrayList<>();
		public boolean override;
		public boolean partialOverride;
		public boolean hide;
		public boolean undefine;
		public final ASTLink<IRTypeDefinition> overriddenFromType = new ASTLink<IRTypeDefinition>(this) {
			@Override
			protected @Nullable IRTypeDefinition tryLink(final String name) {
				final ASTSourceFile file = getParentOfType(ASTSourceFile.class);
				if (file == null)
					return null;
				final ASTModule module = file.module;
				return module == null ? null : module.getType(name);
			}
		};
		public final ASTLink<IRMemberRedefinition> overridden = new ASTLink<IRMemberRedefinition>(this) {
			@Override
			protected @Nullable IRMemberRedefinition tryLink(String name) {
				final WordOrSymbols nameToken = getNameToken();
				assert nameToken != null;
				if (nameToken.isKeyword()) { // i.e. the 'override' keyword
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
				WordOrSymbols overrideToken;
				if ((overrideToken = p.try2("override")) != null) {
					ast.override = true;
					ast.hide = p.try_("hide");
					if (p.peekNext() instanceof UppercaseWordToken && p.peekNext('.', 1, true)) {
						ast.overriddenFromType.setName(p.oneTypeIdentifierToken());
						p.one('.');
						ast.overridden.setName(p.oneIdentifierToken());
						p.one("as");
					} else if (p.peekNext() instanceof WordToken && p.peekNext("as", 1, true)) {
						ast.overridden.setName(p.oneIdentifierToken());
						p.one("as");
					} else {
						ast.overridden.setName(overrideToken);
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
	
	// TODO is this even useful?
	public static class ASTGenericArgumentDeclaration extends AbstractASTElement {
		
		private @Nullable WordToken name;
		
		@Override
		public String toString() {
			return "" + name;
		}
		
		public static ASTGenericArgumentDeclaration parse(final Parser parent) {
			final Parser p = parent.start();
			final ASTGenericArgumentDeclaration ast = new ASTGenericArgumentDeclaration();
			ast.name = p.oneIdentifierToken();
			return p.done(ast);
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
//		protected GenericTypeParameter parse() {
//			one(';');
//			return this;
//		}
//	}
	
	public static class ASTAttributeDeclaration extends AbstractASTElement implements ASTAttribute {
		public final ASTMemberModifiers modifiers;
		
		public @Nullable WordToken name;
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
		
		public static ASTAttributeDeclaration finishParsing(final Parser p, final ASTMemberModifiers modifiers) {
			final ASTAttributeDeclaration ast = new ASTAttributeDeclaration(modifiers);
			
			// name
			ast.name = p.oneIdentifierToken();
			
			// parameters
			p.tryGroup('(', () -> {
				ast.hasParameterDefinitions = true;
				if (p.try_("..."))
					ast.hasParameterDots = true;
				if (!ast.hasParameterDots || p.try_(',')) {
					do {
						ast.parameters.add(ASTSimpleParameter.parse(p));
					} while (p.try_(','));
				}
			}, ')');
			
			// results
			if (p.try_(':')) {
				ast.hasResultDefinitions = true;
				if (p.try_("..."))
					ast.hasResultDots = true;
				if (!ast.hasResultDots || p.try_(',')) {
					do {
						ast.results.add(ASTNormalResult.parse(p));
					} while (p.try_(','));
				}
			}
			
			// errors
			while (p.peekNext('#')) {
				ast.errors.add(ASTErrorDeclaration.parse(p));
			}
			
			// body
			if (p.peekNext('{')) {
				ast.body = ASTBlock.parse(p);
			} else if (p.try_(';')) { // abstract / single expression syntax
				while (p.peekNext("requires"))
					ast.preconditions.add(ASTPrecondition.parse(p));
				while (p.peekNext("ensures"))
					ast.postconditions.add(ASTPostcondition.parse(p));
			} else if (!ast.hasResultDefinitions && ast.errors.isEmpty()) { // 'attribute = value' syntax (nonexistent or overridden result)
				p.one("=");
				final Parser bodyParser = p.start();
				final ASTReturn returnStatement = ASTReturn.parse(bodyParser, false);
				ast.body = bodyParser.done(new ASTBlock(returnStatement));
				while (p.peekNext("requires"))
					ast.preconditions.add(ASTPrecondition.parse(p));
				while (p.peekNext("ensures"))
					ast.postconditions.add(ASTPostcondition.parse(p));
			}
			
			return p.done(ast);
		}
	}
	
	public static class ASTSimpleParameter extends AbstractASTElement implements ASTParameter {
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
		
		@Override
		public @Nullable WordToken nameToken() {
			return name;
		}
		
		public @Nullable ASTAttribute attribute() {
			final ASTElement parent = this.parent;
			if (parent instanceof ASTAttribute)
				return (ASTAttribute) parent;
			return null;
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
		
		public static ASTSimpleParameter parse(final Parser parent) {
			final Parser p = parent.start();
			final ASTSimpleParameter ast = new ASTSimpleParameter();
			ast.visibility = Visibility.parse(p);
			ast.override = p.try_("override");
			if (ast.override) {
				if (p.peekNext() instanceof WordToken && p.peekNext("as", 1, true)) {
					ast.overridden.setName(p.oneIdentifierToken());
					p.next(); // skip 'as'
				}
				if (ast.overridden.getNameToken() == null // if not renamed, the only thing left to change is the type, so require it
						|| !(p.peekNext() instanceof WordToken && (p.peekNext(',', 1, true) || p.peekNext(')', 1, true)))) // allows overriding the name without changing the type
					ast.type = ASTTypeExpressions.parse(p, true, true);
			} else {
				ast.type = ASTTypeExpressions.parse(p, true, true);
			}
			ast.name = p.oneIdentifierToken();
			if (ast.override && ast.overridden.getNameToken() == null) // overridden, but not renamed - use same name to look up parent parameter
				ast.overridden.setName(ast.name);
			if (p.try_('='))
				ast.defaultValue = ASTExpressions.parse(p);
			return p.done(ast);
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
			final ASTAttribute attribute = attribute();
			assert attribute != null;
			return ir = (parent != null ? new IRBrokkrNormalParameterRedefinition(this, parent, attribute.getIR())
					: new IRBrokkrNormalParameterDefinition(this, attribute.getIR()));
		}
	}
	
	public static class ASTNormalResult extends AbstractASTElement implements ASTVariable, ASTResult {
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
		
		@Override
		public @Nullable WordToken nameToken() {
			return name;
		}
		
		@Override
		public @NonNull String name() {
			final WordToken wordToken = name;
			return wordToken != null ? wordToken.word : "result";
		}
		
		public @Nullable ASTAttribute attribute() {
			final ASTElement parent = this.parent;
			if (parent instanceof ASTAttribute)
				return (ASTAttribute) parent;
			return null;
		}
		
		@Override
		public String toString() {
			return (type == null ? "<unresolvable type>" : type) + " " + (name == null ? "result" : "" + name);
		}
		
		@Override
		public @Nullable String hoverInfo(final Token token) {
			return getIR().hoverInfo();
		}
		
		public static ASTNormalResult parse(final Parser parent) {
			final Parser p = parent.start();
			final ASTNormalResult ast = new ASTNormalResult();
			if (p.try_("override")) {
				ast.overridden.setName(p.oneVariableIdentifierToken());
				p.one("as");
			}
			boolean parseType = true;
			if (p.peekNext() instanceof LowercaseWordToken) {
				final Token nextNext = p.peekNext(1, true);
				if (nextNext instanceof SymbolToken && "=,#{".indexOf(((SymbolToken) nextNext).symbol) >= 0)
					parseType = false;
			}
			if (parseType)
				ast.type = ASTTypeExpressions.parse(p, true, true);
			ast.name = p.tryVariableIdentifierToken();
//			if (name == null)
//				name = "result";
//			if (peekNext("=>")) // method '=> results' syntax
//				return this;
			if (p.try_('='))
				ast.defaultValue = ASTExpressions.parse(p);
			return p.done(ast);
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
			final ASTAttribute attribute = attribute();
			assert attribute != null;
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
		protected ParameterResult parse() {
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
	
	public static class ASTErrorDeclaration extends AbstractASTElement implements ASTError {
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
		
		public static ASTErrorDeclaration parse(final Parser parent) {
			final Parser p = parent.start();
			final ASTErrorDeclaration ast = new ASTErrorDeclaration();
			p.one('#');
			ast.name = p.oneVariableIdentifierToken();
			p.tryGroup('(', () -> {
				do {
					// FIXME
//					parameters.add(one(new ASTSimpleParameter(this)));
				} while (p.try_(','));
			}, ')');
			return p.done(ast);
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
//		protected EnumElement parse() {
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
	
	public static class ASTConstructor extends AbstractASTElement implements ASTAttribute {
		public final ASTMemberModifiers modifiers;
		
		public @Nullable LowercaseWordToken name;
		public List<ASTParameter> parameters = new ArrayList<>();
		public @Nullable ASTBlock body;
		public final List<ASTPostcondition> postconditions = new ArrayList<>();
		
		public ASTConstructor(final ASTMemberModifiers modifiers) {
			this.modifiers = modifiers;
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
		
		public static ASTConstructor finishParsing(final Parser p, final ASTMemberModifiers modifiers) {
			final ASTConstructor ast = new ASTConstructor(modifiers);
			p.one("constructor");
			ast.name = p.oneVariableIdentifierToken();
			p.oneGroup('(', () -> {
				do {
					if (p.peekNext('=', 1, true) || p.peekNext(',', 1, true) || p.peekNext(')', 1, true))
						ast.parameters.add(ASTConstructorFieldParameter.parse(p));
					else
						ast.parameters.add(ASTSimpleParameter.parse(p));
				} while (p.try_(','));
			}, ')');
			if (!p.try_(';')) // field params syntax
				ast.body = ASTBlock.parse(p);
			return p.done(ast);
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
	
	public static class ASTConstructorFieldParameter extends AbstractASTElement implements ASTParameter {
		public final ASTLink<IRAttributeRedefinition> attribute = new ASTLink<IRAttributeRedefinition>(this) {
			@Override
			protected @Nullable IRAttributeRedefinition tryLink(final String name) {
				final ASTTypeDeclaration mc = ASTConstructorFieldParameter.this.getParentOfType(ASTTypeDeclaration.class);
				assert mc != null;
				return mc.getIR().getAttributeByName(name);
			}
		};
		public @Nullable ASTExpression defaultValue;
		
		public @Nullable ASTConstructor constructor() {
			final ASTElement parent = this.parent;
			if (parent instanceof ASTConstructor)
				return (ASTConstructor) parent;
			return null;
		}
		
		@Override
		public @Nullable WordOrSymbols nameToken() {
			return attribute.getNameToken();
		}
		
		public static ASTConstructorFieldParameter parse(final Parser parent) {
			final Parser p = parent.start();
			final ASTConstructorFieldParameter ast = new ASTConstructorFieldParameter();
			ast.attribute.setName(p.oneVariableIdentifierToken());
			if (p.try_('='))
				ast.defaultValue = ASTExpressions.parse(p);
			return p.done(ast);
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
			final ASTConstructor constructor = constructor();
			assert constructor != null;
			if (attr == null || attr.results().size() != 1 || !attr.results().get(0).name().equals("result") || !attr.isVariable())
				return new IRUnknownParameterDefinition(attr != null ? attr.name() : "<unknown name>", new IRUnknownTypeUse(getIRContext()), constructor.getIR(), "Constructor field parameter '" + (attr != null ? attr.name() : "<unknown name>") + "' does not reference a field", this);
			return ir = new IRBrokkrConstructorFieldParameter(this, attr, constructor.getIR());
		}
	}
	
	public static class ASTTemplate extends AbstractASTElement implements ASTAttribute {
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
		
		public static ASTTemplate finishParsing(final Parser p, final ASTMemberModifiers modifiers) {
			final ASTTemplate ast = new ASTTemplate(modifiers);
			final String templateType = p.oneOf("code", "member", "type");
			ast.templateType = templateType == null ? null : TemplateType.valueOf(templateType.toUpperCase(Locale.ENGLISH));
			p.one("template");
			ast.name = p.oneVariableIdentifierToken();
			p.tryGroup('(', () -> {
				do {
					ast.parameters.add(ASTSimpleParameter.parse(p));
				} while (p.try_(','));
			}, ')');
			ast.body = ASTBlock.parse(p);
			return p.done(ast);
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
	public static class ASTDelegation extends AbstractASTElement implements ASTMember {
		public List<ASTLink<IRAttributeRedefinition>> methods = new ArrayList<>();
		public List<ASTTypeExpression> types = new ArrayList<>();
		public List<ASTExpression> expressions = new ArrayList<>();
		public @Nullable ASTExpression joinWith;
		
		private static class MethodLink extends ASTLink<IRAttributeRedefinition> {
			public MethodLink(final ASTDelegation parent, @Nullable final WordToken name) {
				super(parent, name);
			}
			
			@Override
			protected @Nullable IRAttributeRedefinition tryLink(final String name) {
				final ASTTypeDeclaration type = parentElement.getParentOfType(ASTTypeDeclaration.class);
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
		
		public static ASTDelegation parse(final Parser parent) {
			final Parser p = parent.start();
			final ASTDelegation ast = new ASTDelegation();
			p.one("delegate");
			p.until(() -> {
				do {
					if (!ast.methods.isEmpty() || ast.types.isEmpty() && p.peekNext() instanceof LowercaseWordToken && (p.peekNext(',', 1, true) || p.peekNext("to", 1, true)))
						ast.methods.add(new MethodLink(ast, p.oneVariableIdentifierToken()));
					else
						ast.types.add(ASTTypeExpressions.parse(p, true, true));
				} while (p.try_(','));
				p.one("to");
				do {
					ast.expressions.add(ASTExpressions.parse(p));
				} while (p.try_(','));
				if (p.try_("join")) {
					p.one("with");
					ast.joinWith = ASTExpressions.parse(p);
				}
			}, ';', false);
			return p.done(ast);
		}
		
		@Override
		public List<? extends IRMemberRedefinition> getIRMembers() {
			return Collections.EMPTY_LIST; // TODO
		}
	}
	
	public static class ASTInvariant extends AbstractASTElement implements ASTMember, NamedASTElement {
		public final ASTMemberModifiers modifiers;
		
		public boolean negated;
		public @Nullable LowercaseWordToken name;
		public @Nullable ASTExpression expression;
		
		public ASTInvariant(final ASTMemberModifiers modifiers) {
			this.modifiers = modifiers;
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
		
		public static ASTInvariant finishParsing(final Parser p, final ASTMemberModifiers modifiers) {
			final ASTInvariant ast = new ASTInvariant(modifiers);
			p.one("invariant");
			p.until(() -> {
				ast.negated = p.try_('!');
				ast.name = p.oneVariableIdentifierToken();
				p.one(':');
				ast.expression = ASTExpressions.parse(p); // TODO allow some statements?
			}, ';', false);
			return p.done(ast);
		}
		
		@Override
		public List<? extends IRMemberRedefinition> getIRMembers() {
			return Collections.EMPTY_LIST; // TODO
		}
	}
	
	public static class ASTPrecondition extends AbstractASTElement implements ASTStatement, ASTError {
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
		
		public static ASTPrecondition parse(final Parser parent) {
			final Parser p = parent.start();
			final ASTPrecondition ast = new ASTPrecondition();
			p.until(() -> {
				p.one("requires");
				//genericRestrictions=GenericParameters? // FIXME wrong
				ast.negated = p.peekNext('!');
				Token t;
				if ((t = p.peekNext(ast.negated ? 1 : 0, true)) instanceof LowercaseWordToken && p.peekNext(';', ast.negated ? 2 : 1, true)) {
					ast.name = (LowercaseWordToken) t;
					ast.expression = ASTExpressions.parse(p);
				} else {
					ast.negated = p.try_('!');
					ast.name = p.oneVariableIdentifierToken();
					if (p.try_(':'))
						ast.expression = ASTExpressions.parse(p); // TODO allow some statements?
				}
			}, ';', false);
			return p.done(ast);
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
	
	public static class ASTPostcondition extends AbstractASTElement implements ASTStatement, NamedASTElement {
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
		
		public static ASTPostcondition parse(final Parser parent) {
			final Parser p = parent.start();
			final ASTPostcondition ast = new ASTPostcondition();
			p.until(() -> {
				p.one("ensures");
				//genericRestrictions=GenericParameters? // FIXME wrong
				final boolean negated = p.peekNext('!');
				if (p.peekNext(negated ? 1 : 0, true) instanceof LowercaseWordToken && p.peekNext(':', negated ? 2 : 1, true)) {
					ast.negated = negated;
					if (negated)
						p.next(); // skip '!';
					ast.name = p.oneVariableIdentifierToken();
					p.next(); // skip ':'
				}
				ast.expression = ASTExpressions.parse(p); // TODO allow some statements?
			}, ';', false);
			return p.done(ast);
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
	
	public static class ASTCodeGenerationCallMember extends AbstractASTElement implements ASTMember {
		public @Nullable ASTExpression code;
		
		@Override
		public boolean isInherited() {
			return false; // only generates code at the current location
		}
		
		@Override
		public String toString() {
			return "$= " + code + ";";
		}
		
		public static ASTCodeGenerationCallMember parse(final Parser parent) {
			final Parser p = parent.start();
			final ASTCodeGenerationCallMember ast = new ASTCodeGenerationCallMember();
			p.one("$=");
			p.until(() -> {
				ast.code = ASTExpressions.parse(p);
			}, ';', false);
			return p.done(ast);
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
