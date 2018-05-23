package ch.njol.brokkr.compiler;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ast.ASTElement;
import ch.njol.brokkr.ast.ASTTopLevelElements.ASTModuleIdentifier;
import ch.njol.brokkr.ast.AbstractASTElement;
import ch.njol.brokkr.common.ModuleIdentifier;
import ch.njol.brokkr.compiler.Token.LowercaseWordToken;
import ch.njol.brokkr.compiler.Token.NumberToken;
import ch.njol.brokkr.compiler.Token.StringToken;

public abstract class ASTModuleFileElement extends AbstractASTElement<ASTModuleFileElement> {
	
	@Override
	protected ASTModuleFileElement parse() throws ParseException {
		final VoidProcessor x = () -> {
			do {
				final LowercaseWordToken key = oneVariableIdentifierToken();
				one(':');
				Field f;
				try {
					f = getClass().getDeclaredField(key.word);
				} catch (NoSuchFieldException | SecurityException e) {
					errorFatal("Invalid key '" + key + "'", key.regionStart(), key.regionLength());
					throw new ParseException();
				}
				if (!isValidField(f)) {
					errorFatal("Invalid key '" + key + "'", key.regionStart(), key.regionLength());
					throw new ParseException();
				}
				try {
					f.setAccessible(true);
					final Type genericType = f.getGenericType();
					assert genericType != null;
					final Object value = parse("" + f.getName(), genericType);
					final String error = checkFieldValue("" + f.getName(), value);
					if (error != null) {
						errorFatal(error, key.regionStart(), key.regionLength());
						throw new ParseException();
					}
					f.set(this, value);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			} while (try_(','));
		};
		if (getClass() == ASTModule.class)
			untilEnd(x);
		else
			oneGroup('{', x, '}');
		return this;
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
		private final ParameterizedType genericType;
		public final Map<Object, Object> value = new HashMap<>();
		
		public MapElement(final String name, final ParameterizedType genericType) {
			this.name = name;
			this.genericType = genericType;
		}
		
		@Override
		protected MapElement parse() throws ParseException {
			final Type keyType = genericType.getActualTypeArguments()[0],
					valueType = genericType.getActualTypeArguments()[1];
			assert keyType != null && valueType != null;
			oneGroup('{', () -> {
				do {
					final Object key = parse("", keyType);
					one(':');
					final Object value = parse("" + key, valueType);
					this.value.put(key, value);
				} while (try_(','));
			}, '}');
			return this;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	public final static class ListElement extends ASTModuleFileElement {
		public final String name;
		private final ParameterizedType genericType;
		public final List<Object> value = new ArrayList<>();
		
		public ListElement(final String name, final ParameterizedType genericType) {
			this.name = name;
			this.genericType = genericType;
		}
		
		@Override
		protected ListElement parse() throws ParseException {
			final Type valueType = genericType.getActualTypeArguments()[0];
			assert valueType != null;
			final int[] i = {0};
			oneGroup('[', () -> {
				do {
					value.add(parse("[" + (i[0]++) + "]", valueType));
				} while (try_(','));
			}, ']');
			return this;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	public final static boolean isValidField(final @Nullable Field f) {
		return f != null && (f.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT | Modifier.FINAL)) == 0;
	}
	
	@SuppressWarnings({"null", "unchecked", "rawtypes"})
	protected Object parse(final String name, final Type genericType) throws ParseException {
		final Class<?> rawType = getRawType(genericType);
		if (AbstractASTElement.class.isAssignableFrom(rawType)) {
			return one((Class) rawType); // Eclipse Oxygen cannot deal with generics here
		} else if (ModuleIdentifier.class.isAssignableFrom(rawType)) {
			return one(ASTModuleIdentifier.class).identifier;
		} else if (Map.class.isAssignableFrom(rawType)) {
			return ((MapElement) one(new MapElement(name, (ParameterizedType) genericType))).value;
		} else if (List.class.isAssignableFrom(rawType)) {
			return ((ListElement) one(new ListElement(name, (ParameterizedType) genericType))).value;
		} else {
			assert rawType == String.class;
			final Token t = next();
			if (t instanceof StringToken)
				return ((StringToken) t).value;
			if (t instanceof NumberToken) {
				String r = "" + ((NumberToken) t).value.toPlainString();
				while (try_('.')) {
					final Token t2 = next();
					if (!(t2 instanceof NumberToken)) {
						expectedFatal("a number", t2);
						throw new ParseException();
					}
					r += "." + ((NumberToken) t2).value.toPlainString();
				}
				return r;
			}
			expectedFatal("a string", t);
			throw new ParseException();
		}
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
