package ch.njol.tome.moduleast;

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

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTElement;
import ch.njol.tome.ast.ASTTopLevelElements.ASTModuleIdentifier;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.common.ModuleIdentifier;
import ch.njol.tome.compiler.Token;
import ch.njol.tome.compiler.Token.LowercaseWordToken;
import ch.njol.tome.compiler.Token.NumberToken;
import ch.njol.tome.compiler.Token.StringToken;
import ch.njol.tome.parser.Parser;
import ch.njol.tome.parser.Parser.VoidProcessor;

public abstract class ASTModuleFileElement extends AbstractASTElement {
	
	public ASTModuleFileElement parse(final Parser parent) {
		Parser p = parent.start();
		final VoidProcessor x = () -> {
			do {
				final LowercaseWordToken key = p.oneVariableIdentifierToken();
				if (key == null)
					return;
				p.one(':');
				Field f;
				try {
					f = getClass().getDeclaredField(key.word);
				} catch (NoSuchFieldException | SecurityException e) {
					p.errorFatal("Invalid key '" + key + "'", key.absoluteRegionStart(), key.regionLength());
					return;
				}
				if (!isValidField(f)) {
					p.errorFatal("Invalid key '" + key + "'", key.absoluteRegionStart(), key.regionLength());
					return;
				}
				try {
					f.setAccessible(true);
					final Type genericType = f.getGenericType();
					assert genericType != null;
					final Object value = parse(p, "" + f.getName(), genericType);
					final String error = checkFieldValue("" + f.getName(), value);
					if (error != null) {
						p.errorFatal(error, key.absoluteRegionStart(), key.regionLength());
						return;
					}
					f.set(this, value);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			} while (p.try_(','));
		};
		if (getClass() == ASTModule.class)
			p.untilEnd(x);
		else
			p.oneGroup('{', x, '}');
		return p.done(this);
	}
	
	protected static Object parse(final Parser parent, final String name, final Type genericType) {
		final Class<?> rawType = getRawType(genericType);
		if (ASTModuleFileElement.class.isAssignableFrom(rawType)) {
			try {
				final Constructor<?> constructor = rawType.getDeclaredConstructor();
				constructor.setAccessible(true);
				return ((ASTModuleFileElement) constructor.newInstance()).parse(parent);
			} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException("" + rawType, e);
			}
		} else if (ModuleIdentifier.class.isAssignableFrom(rawType)) {
			final ASTModuleIdentifier moduleIdentifier = ASTModuleIdentifier.tryParse(parent);
			return moduleIdentifier == null ? new ModuleIdentifier() : moduleIdentifier.identifier;
		} else if (Map.class.isAssignableFrom(rawType)) {
			return MapElement.parseMap(parent, name, (ParameterizedType) genericType).value;
		} else if (List.class.isAssignableFrom(rawType)) {
			return ListElement.parseList(parent, name, (ParameterizedType) genericType).value;
		} else {
			assert rawType == String.class;
			final Token t = parent.next();
			if (t instanceof StringToken)
				return ((StringToken) t).value;
			if (t instanceof NumberToken) {
				String r = "" + ((NumberToken) t).value.toPlainString();
				while (parent.try_('.')) {
					final Token t2 = parent.next();
					if (!(t2 instanceof NumberToken)) {
						parent.expectedFatal("a number", t2);
						return r;
					}
					r += "." + ((NumberToken) t2).value.toPlainString();
				}
				return r;
			}
			parent.expectedFatal("a string", t);
			return "";
		}
	}
	
	/**
	 * Check whether the given value is fine for the given field.
	 * 
	 * @param field the name of the field to set
	 * @param value the parsed value (which is of the correct type for the given field)
	 * @return The error message if the value is invalid, or null if it is valid.
	 */
	protected @Nullable String checkFieldValue(final String field, final Object value) {
		return null;
	}
	
	public final static class MapElement extends ASTModuleFileElement {
		public final String name;
		public final Map<Object, Object> value = new HashMap<>();
		
		public MapElement(final String name) {
			this.name = name;
		}
		
		public static MapElement parseMap(final Parser parent, final String name, final ParameterizedType genericType) {
			Parser p = parent.start();
			final MapElement ast = new MapElement(name);
			final Type keyType = genericType.getActualTypeArguments()[0],
					valueType = genericType.getActualTypeArguments()[1];
			assert keyType != null && valueType != null;
			p.oneGroup('{', () -> {
				do {
					final Object key = parse(p, "", keyType);
					p.one(':');
					final Object value = parse(p, "" + key, valueType);
					ast.value.put(key, value);
				} while (p.try_(','));
			}, '}');
			return p.done(ast);
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	public final static class ListElement extends ASTModuleFileElement {
		public final String name;
		public final List<Object> value = new ArrayList<>();
		
		public ListElement(final String name) {
			this.name = name;
		}
		
		public static ListElement parseList(final Parser parent, final String name, final ParameterizedType genericType) {
			Parser p = parent.start();
			final ListElement ast = new ListElement(name);
			final Type valueType = genericType.getActualTypeArguments()[0];
			assert valueType != null;
			final int[] i = {0};
			p.oneGroup('[', () -> {
				do {
					ast.value.add(parse(p, "[" + (i[0]++) + "]", valueType));
				} while (p.try_(','));
			}, ']');
			return p.done(ast);
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	public final static boolean isValidField(final @Nullable Field f) {
		return f != null && (f.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT | Modifier.FINAL)) == 0;
	}
	
	@SuppressWarnings("null")
	private final static Class<?> getRawType(final Type t) {
		if (t instanceof Class)
			return (Class<?>) t;
		if (t instanceof ParameterizedType)
			return getRawType(((ParameterizedType) t).getRawType());
		if (t instanceof GenericArrayType)
			return getRawType(((GenericArrayType) t).getGenericComponentType());
		assert false : t;
		return null;
	}
	
	/**
	 * whitespace: this method starts writing directly, and thus sub-calls must indent properly before the call. No whitespace is added at the end either.
	 * <p>
	 * The indentation parameters must be null if and only if the current value is the top level element (i.e. a {@link ASTModule} usually)
	 * 
	 * @param val
	 * @param out
	 * @param indentation
	 * @throws IOException
	 */
	public void save(final Object val, final Writer out, final @Nullable String indentation) throws IOException {
		final String nextIndentation = indentation == null ? "" : indentation + "\t";
		if (val instanceof String) {
			out.write("\'" + ((String) val).replace("\\", "\\\\").replace("'", "\\'") + "\'");
		} else if (val instanceof List) {
			out.write("[\n");
			boolean first = true;
			for (final Object v : (List<?>) val) {
				if (!first)
					out.write(",\n");
				first = false;
				assert v != null : val;
				out.write(nextIndentation);
				save(v, out, nextIndentation);
			}
			if (!first) // not empty
				out.write("\n");
			out.write(indentation + "]");
		} else if (val instanceof Map) {
			if (indentation != null)
				out.write("{\n");
			boolean first = true;
			for (final Entry<?, ?> e : ((Map<?, ?>) val).entrySet()) {
				if (!first)
					out.write(",\n");
				first = false;
				final Object v = e.getValue();
				assert v != null : val;
				out.write(nextIndentation + e.getKey() + ": ");
				save(v, out, nextIndentation);
			}
			if (indentation != null) {
				if (first) // empty
					out.write("}");
				else
					out.write("\n" + indentation + "}");
			}
		} else if (val instanceof ASTModuleFileElement) {
			if (indentation != null) // not top level element
				out.write("{\n");
			boolean first = true;
			for (final Field f : val.getClass().getDeclaredFields()) {
				try {
					if (!ASTModuleFileElement.isValidField(f))
						continue;
					f.setAccessible(true);
					final Object o = f.get(val);
					assert o != null : f;
					if (!first)
						out.write(",\n");
					first = false;
					out.write(nextIndentation + f.getName() + ": ");
					save(o, out, nextIndentation);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			if (indentation != null) {
				if (first) // empty
					out.write("}");
				else
					out.write("\n" + indentation + "}");
			}
		} else {
			assert val instanceof ASTElement : val;
			out.write(val.toString());
		}
	}
	
}
