package ch.njol.tome.ast.toplevel;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTDocument;
import ch.njol.tome.ast.ASTElement;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.ast.members.ASTCodeGenerationCallMember;
import ch.njol.tome.ast.members.ASTTemplate;
import ch.njol.tome.compiler.Modules;
import ch.njol.tome.compiler.Token;
import ch.njol.tome.compiler.TokenList;
import ch.njol.tome.ir.IRContext;
import ch.njol.tome.moduleast.ASTModule;
import ch.njol.tome.parser.Parser;
import ch.njol.tome.util.ASTTokenStream;
import ch.njol.tome.util.ModificationListener;
import ch.njol.tome.util.TokenListStream;
import ch.njol.tome.util.Watchable;

public class ASTSourceFile extends AbstractASTElement implements ModificationListener {
	public final String identifier;
	public final Modules modules;
	public @Nullable ASTModule module;
	
	public @Nullable ASTModuleDeclaration moduleDeclaration;
	public List<ASTElement> declarations = new ArrayList<>();
	
	public ASTSourceFile(final Modules modules, final String identifier) {
		this.modules = modules;
		this.identifier = identifier;
	}
	
	@Override
	public String toString() {
		return "file";
	}
	
	public final static ASTDocument<ASTSourceFile> parseFile(final Modules modules, final String identifier, final TokenList tokens) {
		final TokenListStream in = tokens.stream();
		final Parser documentParser = new Parser(in);
		final Parser p = documentParser.start();
		final ASTSourceFile ast = new ASTSourceFile(modules, identifier);
		ast.moduleDeclaration = ASTModuleDeclaration.parse(p);
		ast.updateModule();
		p.repeatUntilEnd(() -> {
			if (p.peekNext('$')) {
				ast.declarations.add(ASTCodeGenerationCallMember.parse(p));
				return;
			}
			final Parser declarationParser = p.start();
			final Parser modifierParser = declarationParser.start();
			final ASTTopLevelElementModifiers modifiers = ASTTopLevelElementModifiers.startParsing(modifierParser);
			ASTElement declaration;
			if (p.peekNext("interface"))
				declaration = ASTInterfaceDeclaration.finishParsing(declarationParser, modifiers.finishToMemberModifiers(modifierParser));
			else if (p.peekNext("class"))
				declaration = ASTClassDeclaration.finishParsing(declarationParser, modifiers.finishToMemberModifiers(modifierParser));
			//					else if (peekNext("enum"))
			//						declaration = one(new EnumDeclaration(modifiers));
			else if (p.peekNext("extension"))
				declaration = ASTExtensionDeclaration.finishParsing(declarationParser, modifiers.finish(modifierParser));
			else if (p.peekNext("alias"))
				declaration = ASTTypeAliasDeclaration.finishParsing(declarationParser, modifiers.finish(modifierParser));
			else if (p.peekNext("code") || p.peekNext("member") || p.peekNext("type"))
				declaration = ASTTemplate.finishParsing(declarationParser, modifiers.finishToMemberModifiers(modifierParser));
			else
				declaration = null;
			if (declaration != null) {
				ast.declarations.add(declaration);
			} else {
				modifiers.finish(modifierParser);
				declarationParser.doneAsChildren();
			}
			assert declaration == null || modifiers.parent() != ast : declaration;
		});
		p.done(ast);
		verifyTokenOrderInAST(ast, tokens);
		return documentParser.documentDone(ast);
	}
	
	private static void verifyTokenOrderInAST(final ASTElement ast, final TokenList tokens) {
		final ASTTokenStream astStream = new ASTTokenStream(ast);
		final TokenListStream listStream = tokens.stream();
		while (astStream.current() != null && !listStream.isAfterEnd()) {
			final Token fromAST = astStream.getAndMoveForward();
			final Token fromList = listStream.getAndMoveForward();
			assert fromAST == fromList;
		}
	}
	
	/**
	 * Updates the module link of this file and registers/unregister this file from the changed module(s), if any.
	 */
	public void updateModule() {
		final ASTModule oldModule = module;
		final ASTModuleDeclaration moduleDeclaration = this.moduleDeclaration;
		if (moduleDeclaration == null) {
			if (oldModule != null) {
				oldModule.clearFile(identifier);
				oldModule.removeModificationListener(this);
			}
			module = null;
			return;
		}
		final ASTModuleIdentifier moduleIdentifier = moduleDeclaration.module;
		final ASTModule newModule = module = moduleIdentifier == null ? null : modules.get(moduleIdentifier.identifier);
		if (oldModule == newModule)
			return;
		if (oldModule != null) {
			oldModule.clearFile(identifier);
			oldModule.removeModificationListener(this);
		}
		if (newModule != null) {
			newModule.registerFile(identifier, this);
			newModule.addModificationListener(this);
		}
	}
	
	@Override
	public void onModification(final Watchable source) {
		assert source instanceof ASTModule;
		updateModule(); // find new, valid module, and register this file again
		// no need to invalidate this whole file - any types or such taken from the module are depended on separately
	}
	
	@Override
	protected synchronized void modified() {
		super.modified();
		final ASTModule module = this.module;
		if (module != null) {
			module.clearFile(identifier);
			module.removeModificationListener(this);
		}
	}
	
	@Override
	public IRContext getIRContext() {
		return modules.irContext;
	}
	
//		@Override
//		public List<? extends TypeDeclaration> declaredTypes() {
//			return getDirectChildrenOfType(TypeDeclaration.class);
//		}
//
//		@SuppressWarnings("null")
//		@Override
//		public List<? extends HasTypes> parentHasTypes() {
//			return module == null ? Collections.EMPTY_LIST : Arrays.asList(module);
//		}
	
}
