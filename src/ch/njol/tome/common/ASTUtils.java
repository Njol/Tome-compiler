package ch.njol.tome.common;

public final class ASTUtils {
	private ASTUtils() {}
	
//	public static @Nullable String getDocComment(final ASTElement ast) {
//		ASTElement c = ast, parent = ast.parent();
//		while (parent != null && ast.relativeRegionStart() == 0) {
//			c = parent;
//			parent = c.parent();
//		}
//		if (parent == null)
//			return null;
//		final List<? extends ASTElementPart> parts = parent.parts();
//		final int elementIndex = parts.indexOf(c);
//		StringBuilder b = null;
//		for (int i = elementIndex - 1; i >= 0; i--) {
//			final ASTElementPart p = parts.get(i);
//			if (p instanceof WhitespaceToken) {
//				continue;
//			} else if (p instanceof SingleLineCommentToken) {
//				if (b == null)
//				b = new StringBuilder();
//				b.insert(0, ((SingleLineCommentToken)p).comment+"\n");
//			} else if (p instanceof MultiLineCommentToken) {
//				return ((MultiLineCommentToken) p).comment; // TODO remove leading whitespace + *
//			} else {
//				break;
//			}
//		}
//		if (b != null)
//			return b.toString();
//		return null;
//	}
}
