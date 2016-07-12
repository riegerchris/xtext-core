/*
 * generated by Xtext
 */
grammar DebugInternalBug289515TestLanguage;

// Rule Model
ruleModel:
	(
		'1'
		'%'
		    |
		'2'
		'%'
		    |
		'3'
		'\\%'
		    |
		'4'
		'\\%'
		    |
		'5'
		'%%'
		    |
		'6'
		'%%'
	)
;

RULE_ID : '^'? ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'_'|'0'..'9')*;

RULE_INT : ('0'..'9')+;

RULE_STRING : ('"' ('\\' .|~(('\\'|'"')))* '"'|'\'' ('\\' .|~(('\\'|'\'')))* '\'');

RULE_ML_COMMENT : '/*' ( options {greedy=false;} : . )*'*/' {skip();};

RULE_SL_COMMENT : '//' ~(('\n'|'\r'))* ('\r'? '\n')? {skip();};

RULE_WS : (' '|'\t'|'\r'|'\n')+ {skip();};

RULE_ANY_OTHER : .;
