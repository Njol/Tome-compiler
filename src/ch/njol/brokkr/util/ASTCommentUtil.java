package ch.njol.brokkr.util;

import ch.njol.brokkr.ast.ASTDocument;
import ch.njol.brokkr.ast.ASTElement;
import ch.njol.brokkr.compiler.Token;
import ch.njol.brokkr.compiler.Token.CommentToken;
import ch.njol.brokkr.compiler.Token.WhitespaceToken;
import ch.njol.brokkr.compiler.TokenStream;

public final class ASTCommentUtil {
	private ASTCommentUtil() {}
	
	/**
	 * @param ast {@link ASTElement} to start looking from.
	 * @return A string containing the text of any comment(s) before the given AST element, in a useful format.
	 */
	public static String getCommentBefore(final ASTElement ast) {
		final StringBuilder result = new StringBuilder();
		ASTDocument<?> document = ast.document();
		if (document == null)return "";
		TokenStream stream = document.tokens().stream();
		stream.setTextOffset(ast.regionStart());
		stream.backward(); // move to the first token before
		// combine comments as long as we only see comment and whitespace tokens
		Token t = stream.current();
		while ((t instanceof WhitespaceToken) || (t instanceof CommentToken)) {
			if (t instanceof CommentToken) {
				if (result.length() != 0)
					result.insert(0, " ");
				result.insert(0, ((CommentToken) t).toPrettyString());
			}
			stream.backward();
			t = stream.current();
		}
		return result.toString().trim();
	}
	
}
