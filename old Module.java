package ch.njol.brokkr.compiler;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.compiler.ast.Element;
import ch.njol.brokkr.compiler.ast.TopLevelElements.BrokkrFile;
import ch.njol.brokkr.compiler.ast.TopLevelElements.TypeDeclaration;

/**
 * Brokkr code is divided into modules which can communicate using public interfaces.
 * 
 * @author Peter Güttinger
 */
public class Module {
	
	/**
	 * The id of this module in dots syntax (e.g. 'util.collections') - this is also the base package of all files in the module.
	 */
	public String id;
	
	public Module(final String id) {
		this.id = id;
	}
	
	// for loading only
	@SuppressWarnings({"null", "unused"})
	private Module() {
		id = null;
	}
	
	public String version = "0.1";
	
	// TODO dependencies? or manage those differently?
	// TODO more info?
	
	public Map<String, List<Import>> imports = new HashMap<>();
	
	public final static class Import implements SerializeAsString {
		public String type;
		public String alias;
		
		public Import(final String type, final @Nullable String alias) {
			this.type = type;
			this.alias = alias == null ? type : alias;
		}
		
		@Override
		public String serialize() {
			return type + (alias.equals(type) ? "" : " as " + alias);
		}
		
		@SuppressWarnings("null")
		public static Import deserialize(final String s) {
			final String[] ss = s.split("\\sas\\s", 2);
			return new Import(ss[0], ss.length == 1 ? null : ss[1]);
		}
	}
	
	private final transient Map<String, Module> modules = new HashMap<>();
	
	public void registerAvailableModule(final Module mod) {
		modules.put(mod.id, mod);
	}
	
	private final transient Map<String, BrokkrFile> files = new HashMap<>();
	
	public void clearFiles() {
		files.clear();
	}
	
	public void clearFile(final String file) {
		@SuppressWarnings("null")
		final BrokkrFile ast = files.remove(file);
		ast.module = null;
	}
	
	public void registerFile(final String file, final BrokkrFile ast) {
		files.put(file, ast);
		ast.module = this;
	}
	
	public List<TypeDeclaration> findDeclaredTypes(final String name) {
		final List<TypeDeclaration> r = new ArrayList<>();
		for (final BrokkrFile ast : files.values()) {
			for (final Element e : ast.declarations) {
				if (e instanceof TypeDeclaration && name.equals(((TypeDeclaration) e).name()))
					r.add((TypeDeclaration) e);
			}
		}
		return r;
	}
	
	public @Nullable TypeDeclaration getDeclaredType(final String name) {
		for (final BrokkrFile ast : files.values()) {
			for (final Element e : ast.declarations) {
				if (e instanceof TypeDeclaration && name.equals(((TypeDeclaration) e).name()))
					return (TypeDeclaration) e;
			}
		}
		return null;
	}
	
	@SuppressWarnings({"null", "unused"})
	public @Nullable TypeDeclaration getType(final String name) {
		for (final Entry<String, List<Import>> e : imports.entrySet()) {
			for (final Import i : e.getValue()) {
				if (i.alias.equals(name)) {
					final @Nullable Module mod = modules.get(e.getKey());
					if (mod != null)
						return mod.getDeclaredType(name);
					return null;
				}
			}
		}
		return getDeclaredType(name);
	}
	
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
	@SuppressWarnings("null")
	public void addDefaults() {
		for (final Entry<String, String[]> dis : defaultImports.entrySet()) {
			final List<Import> is = imports.getOrDefault(dis.getKey(), new ArrayList<>());
			for (final String di : dis.getValue())
				is.add(new Import(di, null));
			imports.put(dis.getKey(), is);
		}
	}
	
	public void save(final Writer out) throws IOException {
		for (final List<Import> l : imports.values())
			l.sort((i1, i2) -> i1.type.compareTo(i2.type));
		save(this, out, null);
		out.flush();
	}
	
	@SuppressWarnings({"null", "unused"})
	public final static Module load(final BrokkrReader in) throws IOException {
		final Object o = load_(in);
		try {
			final Module result = convert(o, Module.class, Module.class, "<root>");
			if (result.id == null)
				throw new IOException("missing id");
			return result;
		} catch (final ConvertException e) {
			throw new IOException(e.getMessage(), e);
		}
	}
	
	static interface SerializeAsString {
		String serialize();
		
		static Object deserialize(final String s) {
			return s;
		}
	}
	
	@SuppressWarnings("null")
	private final static Pattern nonQuoted = Pattern.compile("[a-zA-Z0-9_./]*");
	
	// whitespace: this method starts writing directly, and thus sub-calls must indent properly before the call. No whitespace is added at the end either.
	private void save(Object val, final Writer out, final @Nullable String indentation) throws IOException {
		final String nextIndentation = indentation == null ? "" : indentation + "\t";
		if (val instanceof SerializeAsString)
			val = ((SerializeAsString) val).serialize();
		if (val instanceof String) {
			if (nonQuoted.matcher((String) val).matches())
				out.write("" + val);
			else
				out.write("\'" + ((String) val).replace("\\", "\\\\").replace("'", "\\'") + "\'");
		} else if (val instanceof List) {
			out.write("[\n");
			for (final Object v : (List<?>) val) {
				assert v != null : val;
				out.write(nextIndentation);
				save(v, out, nextIndentation);
				out.write(",\n");
			}
			out.write(indentation + "]");
		} else if (val instanceof Map) {
			if (indentation != null)
				out.write("{\n");
			for (final Entry<?, ?> e : ((Map<?, ?>) val).entrySet()) {
				final Object v = e.getValue();
				assert v != null : val;
				out.write(nextIndentation + e.getKey() + ": ");
				save(v, out, nextIndentation);
				out.write(",\n");
			}
			if (indentation != null)
				out.write(indentation + "}");
		} else {
			if (indentation != null)
				out.write("{\n");
			for (final Field f : val.getClass().getDeclaredFields()) {
				try {
					if ((f.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT | Modifier.FINAL)) != 0)
						continue;
					f.setAccessible(true);
					final Object o = f.get(val);
					assert o != null : f;
					out.write(nextIndentation + f.getName() + ": ");
					save(o, out, nextIndentation);
					out.write(",\n");
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			if (indentation != null)
				out.write(indentation + "}");
		}
	}
	
	private final static class ConvertException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		
		public ConvertException(final String message) {
			super(message);
		}
	}
	
	@SuppressWarnings("null")
	private final static Class<?> getRawType(Type t) {
		if (t instanceof Class) return (Class<?>) t;
		if (t instanceof ParameterizedType) return getRawType(((ParameterizedType) t).getRawType());
		if (t instanceof GenericArrayType) return getRawType(((GenericArrayType) t).getGenericComponentType());
		assert false: t;
		return null;
	}
	
	@SuppressWarnings({"unchecked", "null"})
	private final static <T> T convert(final Object o, final Class<T> rawType, final Type genericType, final String what) throws ConvertException {
		assert genericType instanceof ParameterizedType ? ((ParameterizedType) genericType).getRawType() == rawType : genericType == rawType : rawType + " / " + genericType;
		if (Map.class.isAssignableFrom(rawType)) {
			if (!(o instanceof Map))
				throw new ConvertException(what + ": Expected a map");
			assert genericType instanceof ParameterizedType && ((ParameterizedType) genericType).getActualTypeArguments()[0] == String.class;
			final Type valueType = ((ParameterizedType) genericType).getActualTypeArguments()[1];
			((Map<String, Object>) o).replaceAll((key, value) -> convert(value, getRawType(valueType), valueType, what + "." + key));
			return (T) o;
		} else if (List.class.isAssignableFrom(rawType)) {
			if (!(o instanceof List))
				throw new ConvertException(what + ": Expected a list");
			assert genericType instanceof ParameterizedType;
			final Type valueType = ((ParameterizedType) genericType).getActualTypeArguments()[0];
			final int[] i = {0};
			((List<Object>) o).replaceAll(val -> convert(val, getRawType(valueType), valueType, what + "[" + (i[0]++) + "]"));
			return (T) o;
		} else if (rawType == String.class) {
			if (!(o instanceof String))
				throw new ConvertException(what + ": Expected a string");
			return (T) o;
		} else {
			if (SerializeAsString.class.isAssignableFrom(rawType)) {
				if (!(o instanceof String))
					throw new ConvertException(what + ": Expected a " + rawType.getSimpleName());
				try {
					return (T) rawType.getMethod("deserialize", String.class).invoke(null, (String) o);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
					throw new RuntimeException(e);
				}
			}
			if (!(o instanceof Map))
				throw new ConvertException(what + ": Expected a " + rawType.getSimpleName());
			final Map<String, Object> map = (Map<String, Object>) o;
			try {
				final Constructor<T> c = rawType.getDeclaredConstructor();
				c.setAccessible(true);
				final T t = c.newInstance();
				for (final Entry<String, Object> e : map.entrySet()) {
					try {
						final Field f = rawType.getDeclaredField(e.getKey());
						if ((f.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT | Modifier.FINAL)) != 0)
							throw new ConvertException(what + ": Invalid key " + e.getKey());
						f.set(t, convert(e.getValue(), f.getType(), f.getGenericType(), what + "." + f.getName()));
					} catch (final NoSuchFieldException ex) {
						throw new ConvertException(what + ": Invalid key " + e.getKey());
					}
				}
				return t;
			} catch (final InstantiationException e) {
				throw new ConvertException(what + ": Expected a " + rawType.getSimpleName());
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	// whitespace: this method skips whitespace at the start, but not the end
	private final static Object load_(final BrokkrReader in) throws IOException {
		final boolean start = in.isBeforeStart();
		in.skipWhitespace();
		final int next = start ? '{' : in.next();
		if (next < 0)
			throw new IOException("unexpected end of file");
		if (next == '[') {
			final List<Object> list = new ArrayList<>();
			while (true) {
				in.skipWhitespace();
				if (in.peekNext() == ']') {
					in.next();
					return list;
				}
				list.add(load_(in));
				in.skipWhitespace();
				final int x = in.next();
				if (x == ',')
					continue;
				else if (x == ']')
					return list;
				else
					throw new IOException("missing ']' to end list");
			}
		} else if (next == '{') {
			final Map<Object, Object> map = new HashMap<>();
			while (true) {
				in.skipWhitespace();
				int x = in.peekNext();
				if (x == '}' || x == -1 && start) {
					in.next();
					return map;
				}
				final StringBuilder key = new StringBuilder();
				int y;
				while ((y = in.next()) != ':') {
					if (y == -1)
						throw new IOException("unexpected end of file");
					key.append((char) y);
				}
				map.put("" + key.toString().trim(), load_(in));
				in.skipWhitespace();
				x = in.next();
				if (x == ',')
					continue;
				else if (x == '}' || x == -1 && start)
					return map;
				else
					throw new IOException("missing '}' to end map");
			}
		} else if (next == '\'') {
			final StringBuilder b = new StringBuilder();
			int x;
			while ((x = in.next()) != '\'') {
				if (x == -1)
					throw new IOException("unclosed string");
				b.append((char) x);
				if (x == '\\') {
					final int y = in.next();
					if (y == -1)
						throw new IOException("unclosed string");
					b.append((char) y);
				}
			}
			return "" + b;
		} else {
			final StringBuilder b = new StringBuilder();
			b.append((char) next);
			while (true) {
				final int x = in.next();
				if (nonQuoted.matcher("" + (char) x).matches()) {
					b.append((char) x);
				} else {
					in.back();
					return "" + b;
				}
			}
		}
	}
	
}
