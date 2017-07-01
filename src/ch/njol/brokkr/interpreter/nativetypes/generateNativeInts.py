import os, sys

script_path = os.path.dirname(os.path.realpath(sys.argv[0]))

bitsToJavaPrimitive = {
	'8': 'byte',
	'16': 'short',
	'32': 'int',
	'64': 'long',
}

for signed in  ['', 'U']:
	for bits in ['8', '16', '32', '64']:
		typeName = 'InterpretedNative'+signed+'Int'+bits
		primitive = bitsToJavaPrimitive[bits]
		with open(os.path.join(script_path, typeName+'.java'), encoding='utf-8', mode='w') as target_file:
			target_file.write(
'''package ch.njol.brokkr.interpreter.nativetypes;

import ch.njol.brokkr.interpreter.InterpretedNormalObject;
import ch.njol.brokkr.interpreter.Interpreter;

// GENERATED FILE - DO NOT MODIFY

public class '''+typeName+''' extends AbstractInterpretedSimpleNativeObject {
	
	public '''+primitive+''' value;

	public '''+typeName+'''('''+primitive+''' value) {
		this.value = value;
	}

	// native methods
	
	public '''+typeName+''' _add'''+bits+'''('''+typeName+''' other) {
		return new '''+typeName+'''(('''+primitive+''')(value + other.value));
	}
	
}
''')