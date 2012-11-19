# Error recovery for MicroXML

This specification defines a way to parse any sequence of characters into a generalization of MicroXML data model. The generalization is that there are no restrictions on names and data characters.

Parsing is divided in two consecutive phases: tokenization and tree building.

Information passed from the tokenization phase to the tree building phase as a sequence of  _abstract tokens_. Abstract tokens are named in CamelCase and each token may have associated data.  The following abstract tokens are defined:

+ DataChar - associated data is a code point
+ StartTagOpen - associated data is a string (the name of the element)
+ StartTagClose
+ EmptyElementTagClose 
+ EndTag - associated data is a string (the name of the element)
+ AttributeOpen - associated data is a string (the name of the attribute)
+ AttributeClose

## Tokenization

The input to the tokenization phase is a sequence of characters. The output of the tokenization phase is a sequence of abstract tokens that matches the following regular expression:

    ((StartTagOpen (AttributeOpen DataChar* AttributeClose)* (EmptyElementTagClose|StartTagClose))
     | DataChar
     | EndTag)*
     

### Lexical tokens

The tokenization phase works by dividing up the input into _lexical tokens_. Each lexical token has an associated regular grammar and may also have associated data. Lexical tokens are named in UPPER_CASE. The following lexical tokens are defined:

    DATA_CHAR = [#x0-#x10FFFF]
    COMMENT_OPEN = "<!--"
    COMMENT_CLOSE = "-->"
    SIMPLE_START_TAG = START_TAG_OPEN S* START_TAG_CLOSE
    SIMPLE_EMPTY_ELEMENT_TAG = START_TAG_OPEN S* EMPTY_ELEMENT_TAG_CLOSE
    START_TAG_OPEN_ATTRIBUTE_OPEN = START_TAG_OPEN S+ NAME S* "="
    START_TAG_CLOSE = ">"
    EMPTY_ELEMENT_TAG_CLOSE = "/>"
    END_TAG = "</" NAME S* ">"
    START_TAG_OPEN = "<" NAME
    NAME = NAME_START_CHAR NAME_CHAR*
    NAME_START_CHAR = [A-Za-z_:$] | [#x80-#x10FFFF]
    NAME_CHAR = NAME_START_CHAR | [0-9] | "-" | "."
    NAMED_CHAR_REF = "&" NAME ";"
    NUMERIC_CHAR_REF = "&#x" HEX_NUMBER ";"
    HEX_NUMBER = [0-9a-fA-F]+
    S = #x9 | #xA | #xC | #x20

The associated data for lexical tokens is as follows is as follows:

+ START_TAG_OPEN, END_TAG and NAMED_CHAR_REF have an string (which is a NAME)
+ NUMERIC_CHAR_REF has a non-negative integer
+ DATA_CHAR has a code-point (a non-negative integer in the range 0 to #x10FFFF)

There are a number of different tokenization modes.  Each tokenization mode specifies

+ a set of lexical tokens that are recognized in that mode,
+ rules for mapping each lexical token to zero or more abstract tokens, and
+ rules for when to change to another tokenization mode.

### Main tokenization mode

In the Main tokenization mode, tokens are recognized as follows:

+ DATA_CHAR - emit a DataChar token
+ NAMED_CHAR_REF - if the associated string is a valid character name emit a single DataChar, otherwise emit a DataChar for each character in the NAMED_CHAR_REF 
+ NUMERIC_CHAR_REF - if the associated number is <= #x10FFFF emit a single DataChar, otherwise emit a DataChar for each
+ COMMENT_OPEN - switch to Comment tokenization mode
+ SIMPLE_START_TAG - emit a StartTagOpen token followed by a StartTagClose token
+ SIMPLE_EMPTY_ELEMENT_TAG - emit a StartTagOpen token followed by a EmptyElementTagClose token
+ START_TAG_OPEN_ATTRIBUTE_OPEN - emit a StartTagOpen token followed by an AttributeOpen token and switch to AttributeValue mode

### Comment tokenization mode

In Comment tokenization mode, tokens are recognized as follows:

+ DATA_CHAR - do nothing
+ COMMENT_CLOSE - switch to Main tokenization mode



## Tree building

The tree building phase turns a sequence of abstract tokens into the MicroXML data model.

There are two main tasks:
+ making start-tags and end-tags match
+ ensuring that the sequence corresponds to a single element




