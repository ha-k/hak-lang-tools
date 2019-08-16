//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

/**
 * This is a tool that beautifies the presentation of Java code in html.
 * It also recognizes javadoc comments formatted using html elements and
 * uses the information that they provide. It is meant as a complement
 * to the <b>javadoc</b> tool (which builds hyperlinked indexes and
 * cross-references to java code documentation) by also providing links
 * to the full (highlighted) code as well.
 *
 * @version     Last modified on Fri Aug 03 04:31:49 2018 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

package hlt.language.tools;

import java.io.*;                      // Needed, obviously...
import java.util.HashMap;              // For the keywords
import java.util.Properties;           // For the configuration
import java.util.Iterator;             // For table iteration
import java.util.Date;                 // For file stamping

import hlt.language.io.FileTools;      // For file names
import hlt.language.util.ArrayList;    // For the javadoc tag definitions

public class Hilite
{
  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\\

  /**
   * Input file name.
   */
  String inputFileName;

  /**
   * Input file.
   */
  File inputFile;

  /**
   * Input stream filter to allow unreading.
   */
  PushbackReader input;         

  /**
   * Output file name.
   */
  String outputFileName;

  /**
   * Target directory.
   */
  String outputDir;

  /**
   * File output writer.
   */
  BufferedWriter output;                

  /**
   * Constructs a Hilite object with specified input file name, configuration
   * file name, output file name, etc...
   * @param input name of file to highlight
   * @param configuration name of configuration file (properties)
   * @param output name of highlighted file
   * @param dir directory name where to write the highlighted file
   * @param clobber if <tt>true</tt>, overwrite existing files
   */
  public Hilite (String input, String configuration, String stylefile, String output, String dir, boolean clobber)
    {
      inputFileName = input;                    // set input
      configurationFileName = configuration;    // set configuration
      styleFileName = stylefile;		// set css style file
      outputFileName = output;                  // set output
      outputDir = dir;                          // set target directory
      hilite(clobber);                          // do the work
    }

  /**
   * Constructs a Hilite object with specified input file name.
   * @param input name of file to highlight
   * @param clobber if <tt>true</tt>, overwrite existing files
   */
  public Hilite (String input, boolean clobber)
    {
      inputFileName = input;                    // set input
      outputFileName = FileTools.prefix(input)+".html"; // define output
      hilite(clobber);                          // do the work
    }

  /**
   * Constructs a Hilite object with specified input file name
   * asking for confirmation before when clobbering existing files.
   * @param input name of file to highlight
   */
  public Hilite (String input)
    {
      inputFileName = input;                    // set input
      outputFileName = FileTools.prefix(input)+".html"; // define output
      hilite(false);                            // do the work
    }

  /**
   * Returns the target directory.
   */
  String dir ()
    {
      if (outputDir == null)  return "";

      return outputDir;
    }

  /**
   * Initiates the highlighting of the input file.
   */
  void hilite (boolean clobber)
    {
      inputFile = new File(inputFileName);      // create the file
      if (inputFile.exists())                   // check if the input file exists
        {
          try
            {
              // if so, attach it to a buffered/pushback stream
              input = new PushbackReader(new BufferedReader(new FileReader(inputFile)));
              // create output file stream
              File outputFile = new File(dir()+outputFileName);

              /*start*/
              if (!clobber && outputFile.exists()) // confirm if output file exists
                {
                  if (!Misc.askYesNo("*** File "+dir()+outputFileName+
                                     " already exists; overwrite anyway"))
                    throw new IOException("Aborting hiliting file "+inputFileName);
                }
              /*end*/

              // NOTE: This following phony try/catch wrapper has no business being
              // here, BUT... with no apparent reason, the call to new FileWriter(...)
              // throws an IOException - which it doesn't if the above lines between
              // /*start...end*/ are commented out. In other words, issuing the above
              // call to outputFile.exists() (whether the output file exists or not),
              // makes the following call to new FileWriter(...) fail. If omitted,
              // Java does not complain! So this try/catch wrapper is a workaround,
              // although admittedlly as weird: it traps the exception and re-issues
              // the exact same call! Go figure... This all looks like a bona fide
              // Java 1.2 bug to me. [Tue Mar 16 1999 -hak]
              try
                { 
                  output = new BufferedWriter(new FileWriter(outputFile));
                }
              catch (IOException e)
                {
                  output = new BufferedWriter(new FileWriter(outputFile));
                }
              configure();      // set colors and styles
              hiliteFile();     // process the file
            }             
          catch (IOException e)
            {
              System.err.println("*** "+e.getMessage());
            }
        }
      else    // the input file is not there: beep and complain
        {
          Misc.beep();
          System.err.println("*** File "+inputFileName+" not found!");
        }
    }      

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  /**
   * Static data definitions: keyword types and strings.
   */
  final static String MODIFIER = "modifier";
  final static String TYPE     = "type";
  final static String CONTROL  = "control";
  final static String DECLARE  = "declare";
  final static String LITERAL  = "literal"; 
  final static String OTHER    = "other"; 

  /**
   * Storage for Java keywords (keyword -> keyword type).
   */
  final static HashMap javaWords = new HashMap();   

  /**
   * Stores a modifier word.
   */
  final static void jkw_m (String w)
    {
      javaWords.put(w,MODIFIER);
    }
  /**
   * Stores a type word.
   */
  final static void jkw_t (String w)
    {
      javaWords.put(w,TYPE);
    }
  /**
   * Stores a control word.
   */
  final static void jkw_c (String w)
    {
      javaWords.put(w,CONTROL);
    }
  /**
   * Stores a declaration word.
   */
  final static void jkw_d (String w)
    {
      javaWords.put(w,DECLARE);
    }
  /**
   * Stores a literal.
   */
  final static void jkw_l (String w)
    {
      javaWords.put(w,LITERAL);
    }
  /**
   * Stores a `noneOfTheAbove'.
   */
  final static void jkw_o (String w)
    {
      javaWords.put(w,OTHER);
    }    

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  /**
   * Color and style parameters to be set to the configuration values or the default.
   */
  String FONT_SIZE, BACKGROUND_COLOR, JAVADOC_BG_COLOR, JAVADOC_TEXT_COLOR,
         COMMENT_COLOR, BRACKET_COLOR, KEYWORD_COLOR, MODIFIER_COLOR, TYPE_COLOR,
         CONTROL_COLOR, DECLARE_COLOR, LITERAL_COLOR, OTHER_COLOR, CLASS_COLOR,
         CONSTANT_COLOR, NUMBER_COLOR, STRING_COLOR, TEXT_COLOR, ANNOTATE_COLOR,
         ANNOTATE_TAG_COLOR, COMMENT_STYLE, BRACKET_STYLE, KEYWORD_STYLE,
         MODIFIER_STYLE, TYPE_STYLE, CONTROL_STYLE, DECLARE_STYLE, LITERAL_STYLE,
         OTHER_STYLE, CLASS_STYLE, CONSTANT_STYLE, NUMBER_STYLE, STRING_STYLE,
         PLAIN_STYLE, ANNOTATE_STYLE, ANNOTATE_TAG;

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  /**
   * Default style file name.
   */
  String styleFileName = "style.css";

  /**
   * Default configuration file name.
   */
  String configurationFileName = "Hilite.Configuration";

  /**
   * Properties loaded from the configuration.
   */
  Properties configuration = new Properties();  

  /**
   * Gets the specified attribute from the configuration's properties.
   * @param attribute the property's name
   */
  final String configure (String attribute)
    {
      return configuration.getProperty(attribute);      
    }

  /**
   * Gets the specified attribute to the value of configuration's
   * properties if there is one (returning <tt>null</tt> when it is
   * equal to <tt>\*</tt>). If there is not, returns the specified
   * default value.
   * @param attribute the property's name
   * @param defaultValue the default value
   */
  final String configure (String attribute, String defaultValue)
    {
      String value = configuration.getProperty(attribute);
      return (value == null) ? defaultValue : (value.equals("*") ? null : value);
    }

  /**
   * Initiates the configuration of the highlighter by looking for a
   * configuration file. If one is found, it loads it into the configuration
   * properties.
   */
  final void configure ()
    {
      File configurationFile = new File(configurationFileName);
      //      System.out.println("*** Configuration file:\t"+configurationFileName);
      if (configurationFile.exists())
        {
          try
            {
              configuration.load(new FileInputStream(configurationFile));
            } catch (IOException e)
              {
                System.err.println(e+configurationFileName);
              }
        }
      else
        System.err.println("*** File "+configurationFileName+" not found (using defaults)");
      configureValues();
    }

  /**
   * Configure the color and style parameters.
   */
  final void configureValues ()
    {
      FONT_SIZE          = configure("FONT_SIZE");

      BACKGROUND_COLOR   = configure("BACKGROUND_COLOR","#CCCFF");
      JAVADOC_BG_COLOR   = configure("JAVADOC_BG_COLOR","WHITE");
      JAVADOC_TEXT_COLOR = configure("JAVADOC_TEXT_COLOR","BLACK");
      COMMENT_COLOR      = configure("COMMENT_COLOR","#777777");
      BRACKET_COLOR      = configure("BRACKET_COLOR","GRAY");
      KEYWORD_COLOR      = configure("KEYWORD_COLOR","BLUE");
      MODIFIER_COLOR     = configure("MODIFIER_COLOR","PURPLE");
      TYPE_COLOR         = configure("TYPE_COLOR","BLUE");
      CONTROL_COLOR      = configure("CONTROL_COLOR","BROWN");
      DECLARE_COLOR      = configure("DECLARE_COLOR","RED");
      LITERAL_COLOR      = configure("LITERAL_COLOR","GREEN");
      OTHER_COLOR        = configure("OTHER_COLOR",KEYWORD_COLOR);
      CLASS_COLOR        = configure("CLASS_COLOR","BLUE");
      CONSTANT_COLOR     = configure("CONSTANT_COLOR","#009900");      // leaf green
      NUMBER_COLOR       = configure("NUMBER_COLOR",LITERAL_COLOR);
      STRING_COLOR       = configure("STRING_COLOR",LITERAL_COLOR);
      TEXT_COLOR         = configure("TEXT_COLOR");
      ANNOTATE_COLOR     = configure("ANNOTATE_COLOR","YELLOW");
      ANNOTATE_TAG_COLOR = configure("ANNOTATE_TAG_COLOR","RED");

      COMMENT_STYLE      = configure("COMMENT_STYLE","EM");
      BRACKET_STYLE      = configure("BRACKET_STYLE");
      KEYWORD_STYLE      = configure("KEYWORD_STYLE","STRONG");
      MODIFIER_STYLE     = configure("MODIFIER_STYLE",KEYWORD_STYLE);
      TYPE_STYLE         = configure("TYPE_STYLE",KEYWORD_STYLE);
      CONTROL_STYLE      = configure("CONTROL_STYLE",KEYWORD_STYLE);
      DECLARE_STYLE      = configure("DECLARE_STYLE",KEYWORD_STYLE);
      LITERAL_STYLE      = configure("LITERAL_STYLE");
      OTHER_STYLE        = configure("OTHER_STYLE",KEYWORD_STYLE);
      CLASS_STYLE        = configure("CLASS_STYLE",KEYWORD_STYLE);
      CONSTANT_STYLE     = configure("CONSTANT_STYLE");
      NUMBER_STYLE       = configure("NUMBER_STYLE");
      STRING_STYLE       = configure("STRING_STYLE");
      PLAIN_STYLE        = configure("PLAIN_STYLE");
      ANNOTATE_STYLE     = configure("ANNOTATE_STYLE");
      ANNOTATE_TAG       = configure("ANNOTATE_TAG","PLEASE READ");

      annotateTag        = "<BLINK><SPAN STYLE=\"COLOR:"+ANNOTATE_TAG_COLOR+
                           "\" SIZE=-1><B>"+ANNOTATE_TAG+"</B></SPAN></BLINK>/";
    }

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  /**
   * Returns a keyword's highlight color as per its type.
   */
  final String keywordColor ()
    {
      if (keywordType == MODIFIER) return MODIFIER_COLOR;
      if (keywordType == TYPE)     return TYPE_COLOR;
      if (keywordType == CONTROL)  return CONTROL_COLOR;
      if (keywordType == DECLARE)  return DECLARE_COLOR;
      if (keywordType == LITERAL)  return LITERAL_COLOR;
      if (keywordType == OTHER)    return OTHER_COLOR;
      return KEYWORD_COLOR;
    }    

  /**
   * Returns a keyword's highlight style as per its type.
   */
  final String keywordStyle ()
    {
      if (keywordType == MODIFIER) return MODIFIER_STYLE;
      if (keywordType == TYPE)     return TYPE_STYLE;
      if (keywordType == CONTROL)  return CONTROL_STYLE;
      if (keywordType == DECLARE)  return DECLARE_STYLE;
      if (keywordType == LITERAL)  return LITERAL_STYLE;
      if (keywordType == OTHER)    return OTHER_STYLE;
      return KEYWORD_STYLE;
    }    

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  /**
   * Constants to identify the type of tokens read from the input.
   */
  final static int EOF       = -1;
  final static int PLAINCHAR = 0;
  final static int BRACKET   = 1;
  final static int COMMENT   = 2;
  final static int NUMBER    = 3;
  final static int STRING    = 4;
  final static int CLASS     = 5;
  final static int CONSTANT  = 6;
  final static int KEYWORD   = 7;
  final static int PLAINTEXT = 8;
  final static int JAVADOC   = 9;

  /**
   * True iff the current token is a class' name.
   */
  boolean isClassIdentifier;
  /**
   * True iff the current token is a constant's name.
   */
  boolean isConstantIdentifier; 

  /**
   * The current token word.
   */
  StringBuilder tokenWord;
  /**
   * The keyword type, if the current token word is a keyword.
   */
  String keywordType;
  /**
   * The current token character.
   */
  int tokenChar;
  /**
   * The current token type.
   */
  int tokenType;        

  /**
   * Used to indicate that the comment is an annotation.
   */
  int annotateChar = '$';
  /**
   * True whenever reading an annotation comment.
   */
  boolean annotateMode = false;
  /**
   * String indicating that the comment is an annotation.
   */
  String annotateTag;

  /**
   * If true, format javadoc comments.
   */
  boolean formatJavadocComments = true; 

  /**
   * Contains javadoc tag definitions.
   */
  HashMap javadocTags;

  /**
   * Javadoc tag labels.
   */
  static HashMap javadocTagsLabels = new HashMap(); 

  /**
   * Static initializations.
   */
  static 
    {
      // Filling the javaWords table:

      jkw_m("abstract"); jkw_t("boolean");    jkw_c("break");        jkw_t("byte");
      jkw_c("case");     jkw_c("catch");      jkw_t("char");         jkw_d("class");
      jkw_d("const");    jkw_c("continue");   jkw_c("default");      jkw_c("do");
      jkw_t("double");   jkw_c("else");       jkw_d("extends");      jkw_m("final");
      jkw_c("finally");  jkw_t("float");      jkw_c("for");          jkw_c("goto");
      jkw_c("if");       jkw_d("implements"); jkw_d("import");       jkw_o("instanceof");
      jkw_t("int");      jkw_d("interface");  jkw_t("long");         jkw_m("native");
      jkw_o("new");      jkw_d("package");    jkw_m("private");      jkw_m("protected");
      jkw_m("public");   jkw_c("return");     jkw_t("short");        jkw_m("static");
      jkw_o("super");    jkw_c("switch");     jkw_m("synchronized"); jkw_o("this");
      jkw_c("throw");    jkw_d("throws");     jkw_m("transient");    jkw_c("try");
      jkw_t("void");     jkw_m("volatile");   jkw_c("while");        jkw_l("null");
      jkw_l("true");     jkw_l("false");

      // Filling the javadocTagsLabels table:

      javadocTagsLabels.put("deprecated","<BLINK>Deprecated!</BLINK>");
      javadocTagsLabels.put("exception","Throws:");
      javadocTagsLabels.put("param","Parameters:");
      javadocTagsLabels.put("return","Returns:");
      javadocTagsLabels.put("see","See also:");
      javadocTagsLabels.put("since","Since:");
      javadocTagsLabels.put("version","Version:");
    }      

  /**
   * Returns true if the token character is for an annotation.
   */
  final boolean isAnnotateChar() 
    {
      return (tokenChar == annotateChar);
    }

  /**
   * Returns true iff the given character is a round, curly, or square bracket.
   */
  final static boolean isBracket(int c)
    {
      switch (c)
        {
        case '{': case '}':
        case '(': case ')':
        case '[': case ']':
          return true;
        }
      return false;
    }

  /**
   * Returns true iff the given character is a single or double quote.
   */
  final static boolean isQuote(int c)
    {
      switch (c)
        {
        case '\'': case '"':
          return true;
        }
      return false;
    }

  /**
   * Returns true iff the given string is a Java keyword.
   */
  final boolean isKeyword ()
    {
      keywordType = (String)javaWords.get(tokenWord.toString());
      return (keywordType != null);
    }

  /**
   * Returns true iff the given character is a legal start for a Java identifier.
   */
  final boolean isIdentifierStart (int c)
    {
      return Character.isJavaIdentifierStart((char)c);
    }

  /**
   * Returns true iff the given character is a legal part of a Java identifier.
   */
  final boolean isIdentifierPart (int c)
    {
      return Character.isJavaIdentifierPart((char)c);
    }

  /**
   * Returns true iff the given character is a numerical digit.
   */
  final boolean isDigit (int c)
    {
      return Character.isDigit((char)c);
    }

  /**
   * Returns true iff the given character is an alphabetical character.
   */
  final boolean isLetter (int c)
    {
      return Character.isLetter((char)c);
    }

  /**
   * Returns true iff the given character is an uppercase letter.
   */
  final boolean isUpperCase (int c)
    {
      return Character.isUpperCase((char)c);
    }

  /**
   * Returns true iff the given character is a lowercase letter.
   */
  final boolean isLowerCase (int c)
    {
      return Character.isLowerCase((char)c);
    }

  /**
   * Returns the HTML encoding of the given character as a string.
   */
  final String htmlCodeString (int c)
    {
      switch (c)
        {
        case '<': return "&lt;";
        case '>': return "&gt;";
        case '&': return "&amp;";
        }
      return String.valueOf((char)c);
    }

  /**
   * Writes the current token character's HTML encoding to the output file.
   */
  final void writeTokenChar () throws IOException
    {
      output.write(htmlCodeString(tokenChar));
    }

  /**
   * Returns the next character in the input stream.
   */
  final int nextChar () throws EOFException, IOException
    {
      int c = input.read();
//       System.err.print(java.lang.Character.toString((char)c));
      if (c == -1)
	throw new EOFException();
      return c;
    }

  /**
   * Reads an identifier off the input stream into the current token word.
   * Determines what kind of identifier that is (class name, constant name,
   * or other).
   */
  final void readIdentifier () throws IOException
    {
      tokenWord = new StringBuilder(String.valueOf((char)tokenChar));
      isClassIdentifier = isUpperCase(tokenChar);
      isConstantIdentifier = isClassIdentifier;

      for (;;)
        {
          tokenChar = nextChar();
          
          if (isIdentifierPart(tokenChar))
            {
              tokenWord.append((char)tokenChar);
              isConstantIdentifier &=
                (!isLetter(tokenChar) || !isLowerCase(tokenChar));
            }
          else
            {
              input.unread(tokenChar);
              return;
            }
        }
    }

  /**
   * Reads a maximal string of numerical digits off the input stream into the
   * current token word.
   */
  final void readNumber () throws IOException
    {
      tokenWord = new StringBuilder(String.valueOf((char)tokenChar));

      for (;;)
        {
          tokenChar = nextChar();
          
          if (isDigit(tokenChar))
            {
              tokenWord.append((char)tokenChar);
            }
          else
            {
              input.unread(tokenChar);
              return;
            }
        }
    }

  /**
   * Turns on annotation mode if such is the case.
   */
  final void checkAnnotate () throws IOException
    {
      tokenChar = nextChar();
      annotateMode = isAnnotateChar();
      if (annotateMode)
	tokenWord.append(annotateTag);
      else input.unread(tokenChar);
    }

  /**
   * Reads a C++-style comment into the current token word.
   */
  final void readSlashSlashComment () throws IOException
    {
      tokenWord = new StringBuilder("//");

      checkAnnotate();

      for (;;)
        {
	  try
	    {
	      tokenChar = nextChar();
	    }
	  catch (EOFException e)
	    {
	      return;
	    }
          tokenWord.append(htmlCodeString(tokenChar));    
          if (tokenChar == '\n')
	    return;
        }      
    }

  /**
   * Reads a C-style comment into the current token word.
   * If the switch <tt>formatJavadocComments</tt> is on
   * and this is a javadoc comment (<i>i.e.</i>, there
   * are two stars after the slash) then it is formatted.
   * Otherwise, it is treated as a normal C-style comment.
   */
  final void readSlashStarComment () throws IOException
    {
      if (formatJavadocComments && isJavadocComment())
        {
          formatJavadocComment();
          return;
        }

      tokenWord = new StringBuilder("/"+"*");

      checkAnnotate();

      int followingChar;

      for (;;)
        {
          tokenChar = nextChar();
          tokenWord.append(htmlCodeString(tokenChar));
          
          if (tokenChar == '*')
            {
              followingChar = nextChar();
              if (followingChar == '/')
                {
                  tokenWord.append("/");
                  return;
                }
              else
                tokenWord.append(htmlCodeString(followingChar));
              continue;
            }
        }
    }

  /**
   * Returns true if this is a javadoc comments.
   */
  final boolean isJavadocComment () throws IOException
    {
      tokenChar = nextChar();

      if (tokenChar != '*')     // this is not a javadoc comment
        {
          input.unread(tokenChar);
          return false;
        }
      
      tokenType = JAVADOC;
      return true;
    }

  /**
   * This translates the javadoc comment. It turns off the preformatted
   * mode so that HTML formatting becomes effective. Any star (\*) is ignored
   * unless escaped with a backslash (i.e., \\\*) or followed by a / (to end
   * the comment).
   */
  final void formatJavadocComment () throws IOException
    {
      javadocTags = new HashMap();

      output.write("\n</PRE>\n<HR>\n<CENTER>\n<TABLE BGCOLOR=\""+JAVADOC_BG_COLOR+
                   "\" WIDTH=90% BORDER=1 CELLPADDING=10>\n<TR><TD><SPAN STYLE=\"COLOR:"+
                   JAVADOC_TEXT_COLOR+"\">\n");
    out:
      for (;;)
        {
          tokenChar = nextChar();

          while (tokenChar == '*')
            {
              tokenChar = nextChar();
              if (tokenChar == '/')	// this ends the comment
		break out;		// exit
            }

          if (tokenChar == '\\')        // the next character is escaped
            tokenChar = nextChar();     // process it as a non-special one

	  boolean endOfComment = false;

          if (tokenChar == '@')         // start of a javadoc tag definition line
            endOfComment = recordJavadocTag();  // record the definition for the tag
						// return true is also end of comment
          else
            output.write(tokenChar);    // otherwise, just output the character

	  if (endOfComment)
	    break out;
        }

      if (!javadocTags.isEmpty())       // end with the javadoc tags if any
        formatJavadocTags();

      output.write("\n</SPAN></TD></TR>\n</TABLE>\n</CENTER>\n<P>\n<PRE>\n");
    }

  /**
   * Reads and records a javadoc tag and its definition in a table.
   * Returns true iff this is also the end of the javadoc comment.
   */  
  final boolean recordJavadocTag () throws IOException
    {
      boolean endOfComment = false;
      
      // If the char following the '@' is not a letter
      // just output "@" followed by the char and exit
      if (!isLetter(tokenChar = nextChar()))
        {
          output.write('@');
          output.write(tokenChar);
          return endOfComment;
        }

      // This is a bona fide tag; read it and process it
      tokenWord = new StringBuilder();

      do tokenWord.append((char)tokenChar);
      while (isLetter(tokenChar = nextChar()));

      ArrayList tagDefs = (ArrayList)javadocTags.get(tokenWord.toString());
      if (tagDefs == null)
        javadocTags.put(tokenWord.toString(),(tagDefs = new ArrayList()));

      // If there is nothing else on this line, exit
      if (tokenChar == '\n')
	return endOfComment;

      // Otherwise read to the end of line, or to the end of comment if it on this line

      tokenWord = new StringBuilder();

      tokenChar = nextChar();
      while (tokenChar != '\n')
	{
	  if (tokenChar == '*')			// is this the end of the comment?
	    {
	      int followingChar = nextChar();
	      if (followingChar == '/')		// this is the end of the comment
		{				// we're done reading the tag's value
		  endOfComment = true;
		  break;
		}

	      // this is not the end of the comment: append the two chars
	      tokenWord.append((char)tokenChar);
	      tokenWord.append((char)followingChar);
	    }
	  else	// this is a non-special char - just append it
	    tokenWord.append((char)tokenChar);

	  tokenChar = nextChar();
	}

      tagDefs.add(tokenWord.toString().trim());

      return endOfComment;
    }      

  /**
   * Formats the recorded javadoc tags and their definitions.
   */  
  final void formatJavadocTags() throws IOException
    {
      String tag;
      
      output.write("\n<P><TABLE>");

      for (Iterator k = javadocTags.keySet().iterator(); k.hasNext();)
        {
          tag = (String)k.next();
      
          output.write("\n<TR><TD VALIGN=TOP><STRONG>"+javadocTagFormat(tag)+"</STRONG></TD>");
          output.write("<TD>&nbsp;</TD>");
          
          ArrayList tagDefs = (ArrayList)javadocTags.get(tag);

          if (!tagDefs.isEmpty())
            {
              boolean first = true;
              boolean paramTag = tag.equalsIgnoreCase("param");
              
              for (Iterator e = tagDefs.iterator(); e.hasNext();)
                {
                  if (first)
                    {
                      output.write("\n<TD>");
                      first = false;
                      if (paramTag) output.write("<TABLE>\n");
                    }
                  else output.write(paramTag ? "\n" : ", ");

                  output.write(javadocTagDefFormat(tag,(String)e.next()));
                }
              if (paramTag) output.write("\n</TABLE>");
	      output.write("\n</TD></tr>");
            }
        }

      output.write("\n</TABLE>\n");
    }

  /**
   * Returns the format label for the given tag.
   */
  final String javadocTagFormat (String tag)
    {
      String label = (String)javadocTagsLabels.get(tag);
      if (label != null) return label;

      // If not a standard tag, the label is the capitalized tag followed with ':'
      label = String.valueOf(Character.toUpperCase(tag.charAt(0)))+
              tag.toLowerCase().substring(1)+":";
      return label;
    }

  /**
   * Returns the formatted definition as per the given tag. The <tt>String def</tt>
   * contains the definition (the term and the definition) separated by either a space
   * or a tab character.
   */
  final String javadocTagDefFormat (String tag, String def)
    {
      if (tag.equalsIgnoreCase("param"))
        {
          int i = def.indexOf(' ');
          int j = def.indexOf('\t');
          if (i>= 0 && j>= 0)       
            i = Math.min(i,j);
          else  // (i < 0 || j < 0)
            i = Math.max(i,j);
          
          return "<TR><TD VALIGN=BASELINE><SPAN STYLE=\"COLOR:"+JAVADOC_TEXT_COLOR+
                 "\"><TT>"+(i >= 0?def.substring(0,i):def)+
                 "&nbsp;</TT></SPAN></TD><TD><SPAN STYLE=\"COLOR:"+
                 JAVADOC_TEXT_COLOR+"\">- "+(i >= 0?def.substring(i):def)+
                 "</SPAN></TD></TR>";
        }

      if (!tag.equalsIgnoreCase("see") || def.regionMatches(true,0,"<A HREF",0,7))
        return def;

      int i = def.lastIndexOf('#');
      String refBase = (i<0) ? def : def.substring(0,i);
      String refTag = (i<0) ? "" : def.substring(i);

      return "<A HREF=\""+refBase+(refBase.length()>0?".html":"")+refTag+"\">"+
             ((i<0) ? refBase.substring(refBase.lastIndexOf('.')+1)
                    : refTag.substring(1))
             +"</A>";
    }

  /**
   * Reads a quoted string into the current token word.
   */
  final void readQuotedWord () throws IOException
    {
      tokenWord = new StringBuilder(String.valueOf((char)tokenChar));
      int quote = tokenChar;      

      int prevChar = 0;
      int prevPrevChar;

      for (;;)
        {
          prevPrevChar = prevChar;
          prevChar = tokenChar;
          tokenChar = nextChar();
          tokenWord.append(htmlCodeString(tokenChar));
          if (tokenChar == quote)
            {
              if (prevChar == '\\')
                {
                  if (prevPrevChar == '\\') return;
                  continue;
                }
              return;
            }
        }
    }

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\\  

  /**
   * Reads the next token off the input stream.
   */
  final int nextToken () throws IOException
    {
      tokenChar = input.read();         // read a character

      if (tokenChar == -1)              // this is the end of the file
        return (tokenType = EOF);       // set the token type to EOF and exit

      if (isIdentifierStart(tokenChar)) // this is the start Java identifier
        {
          readIdentifier();             // read the identifier

          if (isConstantIdentifier)     // this is a constant's name
            return (tokenType = CONSTANT);      // set the token type to CONSTANT and exit

          if (isClassIdentifier)        // this is a class' name
            return (tokenType = CLASS);         // set the token type to CLASS and exit

          if (isKeyword())              // this is a Java keyword
            return (tokenType = KEYWORD);       // set the token type to KEYWORD and exit
          
          return (tokenType = PLAINTEXT);       // set the token type to PLAINTEXT and exit
        }

      if (isDigit(tokenChar))           // this is a number
        {
          readNumber();                 // read the number
          return (tokenType = NUMBER);          // set the token type to NUMBER and exit
        }

      if (tokenChar == '/')             // this may be the start of a comment
        {
          int followingChar = nextChar();       // get the next character
          if ((followingChar == '/') || (followingChar == '*')) // this is a comment
            {
              tokenType = COMMENT;              // set the token type to COMMENT
              if (followingChar == '/')         // this a C++-style comment
                {
                  readSlashSlashComment();      // read the C++-style comment
                  return tokenType;             // and exit
                }                               // this is C-style comment
              readSlashStarComment();           // read the C-style comment
              return tokenType;                 // and exit
            }
          output.write(tokenChar);              // write out the current token character
          input.unread(followingChar);          // and push back the following character
          return nextToken();                   // return the next token
        }

      if (isQuote(tokenChar))           // this is the start of a quoted word
        {
          readQuotedWord();             // read the quoted word
          return (tokenType = STRING);  // set the token type to STRING and exit
        }

      if (isBracket(tokenChar))         // this is a bracket
        {
          tokenWord =                   // set the token word to the bracket
            new StringBuilder(String.valueOf((char)tokenChar));
          return (tokenType = BRACKET); // set the token type to BRACKET and exit
        }

      return (tokenType = PLAINCHAR);   // set the token type to PLANCHAR and exit
    }

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\\  

  /**
   * This is the main method. It loops reading through the input
   * file and processes the tokens as appropriate.
   */ 
  final void hiliteFile () throws IOException
    {
      System.out.println("*** Hiliting file:\t"+FileTools.simpleName(inputFileName));
      preamble();
    out:
      for (;;)
        {
          switch (nextToken())
            {
            case EOF:
              input.close();
              break out;
            case PLAINCHAR:
              writeTokenChar();
              break;
            case PLAINTEXT:
              output.write(tokenWord.toString());
              break;
            case JAVADOC:
              break;
            default:
              hiliteTokenWord();
            }
        }
      postamble();
    }


  /**
   * This method outputs whatever goes in the output file's preamble.
   */
  final void preamble () throws IOException
    {
      String bc = (BACKGROUND_COLOR == null)
        ? ""
        : " BGCOLOR=\""+BACKGROUND_COLOR+"\"";
      String tc = (TEXT_COLOR == null)
        ? ""
        : " TEXT=\""+TEXT_COLOR+"\"";
      String fs = (FONT_SIZE == null)
        ? ""
        : " SIZE=\""+FONT_SIZE+"\"";
      
      output.write("<HTML>\n<HEAD>\n<TITLE>\n"+outputFileName+"\n</TITLE>"+
		   "\n<LINK REL=\"STYLESHEET\" TYPE=\"text/css\" HREF=\""+styleFileName+"\">"+
                   "\n</HEAD>\n<BODY"+bc+tc+fs+">"+
                   "\n<CENTER>\n<TABLE BGCOLOR=white WIDTH=50% BORDER=5 CELLPADDING=20>"+
                   "\n<TR><TD ALIGN=CENTER>\n<SPAN STYLE=\"FONT-SIZE:XX-LARGE\"><TT><B>"+
                   FileTools.simpleName(inputFileName)+"</B></TT></SPAN>\n<P>\n</TD></TR>"+
                   "\n</TABLE>\n</CENTER>\n<PRE>\n");
    }

  /**
   * This method outputs whatever goes in the output file's ending.
   */
  final void postamble () throws IOException
    {
      output.write("\n</PRE>\n<P>\n<HR>\n<P ALIGN=\"RIGHT\">"+
                   "<SPAN STYLE=\"COLOR:#F07070\"><EM>\n"+
                   "This file was generated on "+(new Date())+
                   " from file <SPAN STYLE=\"COLOR:BROWN\"><KBD>"+
                   FileTools.simpleName(inputFileName)+"</KBD></SPAN><BR>"+
                   "by the <SPAN STYLE=\"COLOR:BROWN\"><KBD>"+
                   "hlt.language.tools.Hilite</KBD></SPAN> Java tool"+
                   " written by <A HREF=\"http://hassan-ait-kaci.net\">"+
                   "Hassan A&iuml;t-Kaci</A></EM></SPAN>\n"+
                   "<P>\n<HR>\n</BODY>\n</HTML>");
      output.close();
      System.out.println("*** Wrote hilited file:\t"+outputFileName);
    }

  /**
   * Outputs a token word highlighted in color and style according to its type.
   */
  final void hiliteTokenWord () throws IOException
    {
      String color = null;
      String style = null;

      switch (tokenType)
        {
        case BRACKET:
          color = BRACKET_COLOR;
          style = BRACKET_STYLE;
          break;
        case COMMENT:
          color = annotateMode ? ANNOTATE_COLOR : COMMENT_COLOR;
          style = annotateMode ? ANNOTATE_STYLE : COMMENT_STYLE;
          break;
        case NUMBER:
          color = NUMBER_COLOR;
          style = NUMBER_STYLE;
          break;
        case STRING:
          color = STRING_COLOR;
          style = STRING_STYLE;
          break;
        case KEYWORD:
          color = keywordColor();
          style = keywordStyle();
          break;
        case CLASS:
          color = CLASS_COLOR;
          style = CLASS_STYLE;
          break;
        case CONSTANT:
          color = CONSTANT_COLOR;
          style = CONSTANT_STYLE;
          break;
        }

      if (style != null)
        tokenWord.insert(0,"<"+style+">").append("</"+style+">");
      if (color != null)
        tokenWord.insert(0,"<SPAN STYLE=\"COLOR:"+color+"\">").append("</SPAN>");        
      output.write(tokenWord.toString());
    }

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
}
