package ch.njol.tome.ast.toplevel;

public class ASTTopLevelElements {
	
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

}
