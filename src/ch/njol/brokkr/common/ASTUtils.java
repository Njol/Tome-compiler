package ch.njol.brokkr.common;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ast.ASTElement;
import ch.njol.brokkr.ast.ASTElementPart;
import ch.njol.brokkr.compiler.Token.CommentToken;
import ch.njol.brokkr.compiler.Token.MultiLineCommentToken;
import ch.njol.brokkr.compiler.Token.SingleLineCommentToken;
import ch.njol.brokkr.compiler.Token.WhitespaceToken;

public final class ASTUtils {
	private ASTUtils() {}
	
	public static @Nullable String getDocComment(final ASTElement ast) {
		ASTElement parent = ast, c;
		do {
			c = parent;
			parent = parent.parent();
		} while (parent != null && parent.regionStart() == ast.regionStart());
		if (parent == null)
			return null;
		final List<ASTElementPart> parts = parent.parts();
		final int elementIndex = parts.indexOf(c);
		StringBuilder b = null;
		for (int i = elementIndex - 1; i >= 0; i--) {
			final ASTElementPart p = parts.get(i);
			if (p instanceof WhitespaceToken) {
				continue;
			} else if (p instanceof SingleLineCommentToken) {
				if (b == null)
				b = new StringBuilder();
				b.insert(0, ((SingleLineCommentToken)p).comment+"\n");
			} else if (p instanceof MultiLineCommentToken) {
				return ((MultiLineCommentToken) p).comment; // TODO remove leading whitespace + *
			} else {
				break;
			}
		}
		if (b != null)
			return b.toString();
		return null;
	}
}
