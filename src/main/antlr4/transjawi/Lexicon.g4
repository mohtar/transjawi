grammar Lexicon;

expr: list | structure | STRING | NUMBER;
list: '[' (item (',' item)*)? ']';
item: (STRING ':')? expr;
structure: IDENTIFIER '(' (expr (',' expr)*)? ')';

IDENTIFIER: [a-zA-Z]+;
STRING: '"' ~["]* '"';
NUMBER: [0-9]+;

COMMENT: '//' ~[\r\n]* '\r'? '\n' -> skip;
WS: [ \n\t\r]+ -> skip;
