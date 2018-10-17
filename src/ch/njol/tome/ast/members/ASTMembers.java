package ch.njol.tome.ast.members;

import ch.njol.tome.ast.ASTInterfaces.ASTMember;
import ch.njol.tome.ast.toplevel.ASTClassDeclaration;
import ch.njol.tome.ast.toplevel.ASTInterfaceDeclaration;
import ch.njol.tome.parser.Parser;

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
	
}
