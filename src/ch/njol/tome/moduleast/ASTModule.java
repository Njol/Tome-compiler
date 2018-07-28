package ch.njol.tome.moduleast;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTDocument;
import ch.njol.tome.ast.ASTElement;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeDeclaration;
import ch.njol.tome.ast.ASTLink;
import ch.njol.tome.ast.ASTTopLevelElements.ASTSourceFile;
import ch.njol.tome.common.ModuleIdentifier;
import ch.njol.tome.compiler.Modules;
import ch.njol.tome.compiler.Token.UppercaseWordToken;
import ch.njol.tome.compiler.Token.WordToken;
import ch.njol.tome.ir.IRContext;
import ch.njol.tome.ir.definitions.IRTypeDefinition;
import ch.njol.tome.parser.Parser;
import ch.njol.tome.util.TokenListStream;

/**
 * Brokkr code is divided into modules which can communicate using public interfaces.
 * 
 * @author Peter Güttinger
 */
public class ASTModule extends ASTModuleFileElement {
	
	public @Nullable ModuleIdentifier id;
	
	public final Modules modules;
	
	public ASTModule(final Modules modules, final ModuleIdentifier id) {
		this.modules = modules;
		this.id = id;
	}
	
	// for loading only
	private ASTModule(final Modules modules) {
		this.modules = modules;
		id = null;
	}
	
	@Override
	public IRContext getIRContext() {
		return modules.irContext;
	}
	
	public String version = "0.1";
	
	public String encoding = "UTF-8";
	// module files are always UTF-8 (and everything except possibly custom data is ASCII)
	
	public String language = "Brokkr-0.1"; // instantly backwards and forwards compatible! 
	
	// TODO more info?
	// TODO warn about missing values? leaving the default value might be good or bad...
	
	// TODO dependencies? or manage those differently?
	// TODO change to dependencies with: ID, version, imports, defaultImportPrefix (and Suffix?)
	public Map<ModuleIdentifier, List<ASTImport>> imports = new HashMap<>();
	
	public final static class ASTImport extends ASTModuleFileElement {
		public ASTLink<IRTypeDefinition> type = new ASTLink<IRTypeDefinition>(this) {
			@Override
			protected @Nullable IRTypeDefinition tryLink(final String name) {
				final ASTModule module = getParentOfType(ASTModule.class);
				assert module != null;
				for (final Entry<ModuleIdentifier, List<ASTImport>> e : module.imports.entrySet()) {
					if (!e.getValue().stream().anyMatch(l -> l == ASTImport.this)) // find module of this import // TODO improve this to directly access parent element
						continue;
					final ASTModule importedModule = module.modules.get(e.getKey());
					return importedModule == null ? null : importedModule.getDeclaredType(name);
				}
				return null;
			}
		};
		public String alias;
		
		@SuppressWarnings({"null", "unused"})
		private ASTImport() {}
		
		public ASTImport(final WordToken type, final @Nullable String alias) {
			this.type.setName(type);
			this.alias = alias == null ? type.word : alias;
		}
		
		@Override
		public String toString() {
			return type.getName() + (alias.equals(type.getName()) ? "" : " as " + alias);
		}
		
		@Override
		public ASTModuleFileElement parse(final Parser parent) {
			Parser p = parent.start();
			ASTImport ast = new ASTImport();
			final UppercaseWordToken typeName = p.oneTypeIdentifierToken();
			if (typeName == null)
				return p.done(ast);
			ast.type.setName(typeName);
			ast.alias = typeName.word; // make sure 'alias' not null even if the next lines fail
			if (p.try_("as")) {
				final String alias = p.oneTypeIdentifier();
				if (alias != null)
					ast.alias = alias;
			}
			return p.done(ast);
		}
	}
	
	@Override
	protected @Nullable String checkFieldValue(@NonNull final String field, @NonNull final Object value) {
		if (field.equals("encoding")) {
			if (!Charset.isSupported((String) value))
				return "Unsupported encoding (on this JVM)";
		}
		return null;
	}
	
	private final transient Map<String, ASTSourceFile> files = new HashMap<>();
	
	public void clearFiles() {
		for (final ASTSourceFile bf : files.values()) {
			bf.module = null;
			bf.invalidateSubtree();
		}
		files.clear();
	}
	
	public void clearFile(final String file) {
		final ASTSourceFile ast = files.remove(file);
		if (ast != null) {
			ast.module = null;
			ast.invalidateSubtree();
		}
	}
	
	public void registerFile(final String file, final ASTSourceFile ast) {
		files.put(file, ast);
		ast.module = this;
	}
	
	public List<IRTypeDefinition> findDeclaredTypes(final String name) {
		final List<IRTypeDefinition> r = new ArrayList<>();
		for (final ASTSourceFile ast : files.values()) {
			for (final ASTElement e : ast.declarations) {
				if (e instanceof ASTTypeDeclaration && name.equals(((ASTTypeDeclaration) e).name()))
					r.add(((ASTTypeDeclaration) e).getIR());
			}
		}
		return r;
	}
	
	public @Nullable IRTypeDefinition getDeclaredType(final String name) {
		for (final ASTSourceFile ast : files.values()) {
			for (final ASTElement e : ast.declarations) {
				if (e instanceof ASTTypeDeclaration && name.equals(((ASTTypeDeclaration) e).name()))
					return ((ASTTypeDeclaration) e).getIR();
			}
		}
		return null;
	}
	
	public @Nullable IRTypeDefinition getType(final String name) {
		final IRTypeDefinition declaredType = getDeclaredType(name);
		if (declaredType != null)
			return declaredType;
		for (final Entry<ModuleIdentifier, List<ASTImport>> e : imports.entrySet()) {
			for (final ASTImport i : e.getValue()) {
				if (i.alias.equals(name)) {
					final IRTypeDefinition t = i.type.get();
					if (t != null)
						registerInvalidateListener(t); // if this module is modified, any types accessed this way must be re-checked
					return t;
				}
			}
		}
		return null;
	}
	
	// TODO make this a code template instead
	private final static Map<String, String[]> defaultImports = new HashMap<>();
	static {
		defaultImports.put("lang", new String[] {"Any", "Object", "Function", "Procedure", "Type", "Tuple", "Boolean", "Kleenean", //
				"Copyable", "Comparable", "Orderable", "Relation", "Character", "String", //
				"Number", "Integer", "Int", "UInt", "Int8", "Int16", "Int32", "Int64", "UInt8", "UInt16", "UInt32", "UInt64", "Byte"});
		defaultImports.put("util", new String[] {"Vector", "Matrix"});
		defaultImports.put("util.collection", new String[] {"Collection", "Stream", "List", "Map", "Set", "Tree"});
	}
	
	/**
	 * Inserts defaults, for example a lot of standard library imports like Boolean and List.
	 * <p>
	 * These are not enforced, as e.g. replacing Number with a more general interface might be useful for a math application.
	 */
	public void addDefaults() {
		for (final Entry<String, @NonNull String[]> dis : defaultImports.entrySet()) {
			final List<ASTImport> is = imports.getOrDefault(dis.getKey(), new ArrayList<>());
			for (final String di : dis.getValue())
				is.add(new ASTImport(new UppercaseWordToken(di), null));
			imports.put(new ModuleIdentifier(dis.getKey()), is);
		}
	}
	
	public final static ASTDocument<ASTModule> load(final Modules modules, final TokenListStream tokens) {
		final ASTModule module = new ASTModule(modules);
		final Parser p = new Parser(tokens);
		module.parse(p);
		return p.documentDone(module);
	}
	
	public void save(final Writer out) throws IOException {
		for (final List<ASTImport> l : imports.values()) {
			l.sort((i1, i2) -> {
				final String n1 = i1.type.getName(), n2 = i2.type.getName();
				return n1 == null ? 0 : n1.compareTo(n2);
			});
		}
		save(this, out, null);
		out.flush();
	}
	
	@Override
	public String toString() {
		return "" + id;
	}
	
}
