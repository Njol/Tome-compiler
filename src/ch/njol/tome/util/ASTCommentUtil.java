package ch.njol.tome.util;

import ch.njol.tome.ast.ASTElement;
import ch.njol.tome.compiler.Token;
import ch.njol.tome.compiler.Token.CommentToken;
import ch.njol.tome.compiler.Token.WhitespaceOrCommentToken;

public final class ASTCommentUtil {
	private ASTCommentUtil() {}
	
	/**
	 * @param ast {@link ASTElement} to start looking from.
	 * @return A string containing the text of any comment(s) before the given AST element, in a useful format.
	 */
	public static String getCommentBefore(final ASTElement ast) {
		final StringBuilder result = new StringBuilder();
		final ASTTokenStream stream = new ASTTokenStream(ast);
		stream.moveBackward(); // move to the first token before the element
		// combine comments as long as we only see comment and whitespace tokens
		Token t = stream.current();
		while (t instanceof WhitespaceOrCommentToken) {
			if (t instanceof CommentToken) {
				if (result.length() != 0)
					result.insert(0, " ");
				result.insert(0, ((CommentToken) t).toPrettyString());
			}
			t = stream.moveBackwardAndGet();
		}
		return result.toString().trim();
	}
	
}
