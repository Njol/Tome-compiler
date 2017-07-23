package ch.njol.brokkr.compiler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ast.ASTElement;
import ch.njol.brokkr.ast.ASTElementPart;
import ch.njol.brokkr.ast.ASTTopLevelElements.ASTBrokkrFile;

public class Compiler {
	
	public final static void main(final String[] args) throws IOException {
		if (args.length == 0) {
			System.out.println("Missing argument <file>");
			return;
		}
		final Path p = Paths.get(args[0]);
		if (!p.toFile().exists() || p.toFile().isDirectory()) {
			System.out.println("Expected a valid file as input");
			return;
		}
		final String file = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
		System.out.println("Started parsing " + p.toFile().getName() + "...");
		final long startTime = System.nanoTime();
		final BrokkrReader in = new StringReader(file);
		final Lexer lexer = new Lexer(in);
		final TokenStream tokens = lexer.newStream();
		final Modules modules = new Modules();
		final ASTBrokkrFile result = ASTBrokkrFile.parseFile(modules, "<only one file>", tokens);
		in.reset();
		final List<ParseError> errors = result.fatalParseErrors();
		if (!tokens.isAfterEnd())
			errors.add(new ParseError("Unexpected data at end of document (or unable to parse due to previous errors)", tokens.getTextOffset(), 1));
		
//		for (Token t : tokens.tokens)
//			System.out.print(t + " ");
		assert System.out != null;
		result.print(System.out, "| ");
		System.out.println();
		
		if (errors.isEmpty()) {
			System.out.println("Parsing successful (" + (System.nanoTime() - startTime) / 1_000_000.0 + " ms)");
		} else {
			System.out.println("Parsing failed (" + (System.nanoTime() - startTime) / 1_000_000.0 + " ms)");
			// the errors that were able to parse the farthest seem like the best bet
//			final List<ParseError> es = new ArrayList<>();
//			ParseError ex = errors;
//			es.add(ex);
//			while ((ex = ex.other) != null) {
//				if (ex.start > es.get(0).start) {
//					es.clear();
//					es.add(ex);
//				} else if (ex.start == es.get(0).start) {
//					es.add(ex);
//				}
//			}
//			String m = "Expected ";
//			int end = 0;
//			for (final ParseError e : es) {
//				if (es.size() > 1 && e == es.get(es.size() - 1))
//					m += ", or ";
//				else if (e != es.get(0))
//					m += ", ";
//				m += e.expected;
//				if (e.end > end)
//					end = e.end;
//			}
//			final ParseError combined = new ParseError(m, es.get(0).start, end);
			
			final Map<Integer, @Nullable Set<String>> expected = new LinkedHashMap<>();
			for (final ParseError e : errors) {
				Set<String> s = expected.get(e.start);
				if (s != null)
					s.add(e.message);
				else
					s = new HashSet<>(Arrays.asList(e.message));
				expected.put(e.start, s);
			}
			
			for (final Entry<Integer, @Nullable Set<String>> e : expected.entrySet()) {
				final int start = e.getKey();
				final int column = in.getColumn(start);
				final List<String> l = new ArrayList<>(e.getValue());
				Collections.sort(l);
				final String message = "" + l;
				System.err.println(message + " at line " + (in.getLine(start) + 1) + ", column " + column);// + " (end: line " + (in.getLine(e.start + e.length) + 1) + ", column " + in.getColumn(e.start + e.length) + ")");
				System.err.println(">> " + in.getLineTextAtOffset(start).replace('\t', ' ').replace("\r", ""));
				System.err.print("   ");
				for (int i = 0; i < column; i++)
					System.err.print(' ');
				System.err.println("^");
//				e.printStackTrace();
				printASTLine(result, "" + in.getLineTextAtOffset(start).replace('\t', ' ').replace("\r", ""), in.getLineStart(start), in.getLineEnd(start));
			}
		}
	}
	
	public final static void printASTLine(final ASTElement top, final String line, final int lineStart, final int lineEnd) {
		if (top.regionEnd() < lineStart || top.regionStart() > lineEnd)
			return;
		System.out.println(line);
		printASTLine_(top, lineStart, lineEnd);
		for (@NonNull
		final ASTElementPart e : top.parts()) {
			if (e instanceof ASTElement)
				printASTLine((ASTElement) e, line, lineStart, lineEnd);
		}
	}
	
	private final static void printASTLine_(final ASTElement element, final int lineStart, final int lineEnd) {
		if (element.regionStart() < lineStart) {
			System.out.print("-");
		} else {
			for (int i = lineStart; i < element.regionStart(); i++)
				System.out.print(" ");
			System.out.print("^");
		}
		for (int i = Math.max(element.regionStart(), lineStart) + 1; i < Math.min(element.regionStart() + element.regionLength(), lineEnd) - 1; i++)
			System.out.print("-");
		if (element.regionEnd() <= lineEnd)
			System.out.print("^");
		else
			System.out.print("-");
		System.out.print(" :: ");
		System.out.println(element.getClass().getSimpleName());
	}
	
}
