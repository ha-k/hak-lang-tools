//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.tools;

/**
 * This class implements a simple driver for the <tt>GenericHilite</tt> class.
 * It subclasses <tt>Command</tt> which supports UNIX-style command line
 * options. It defines the following options
 * <ul>
 * <li><tt><b>!</b></tt>&nbsp;&nbsp; clobber existing files
 * <li><tt><b>d</b></tt>&nbsp;&nbsp; target directory for HTML file(s)
 * <li><tt><b>c</b></tt>&nbsp;&nbsp; configuration file
 * <li><tt><b>o</b></tt>&nbsp;&nbsp; HTML output file
 * <li><tt><b>p</b></tt>&nbsp;&nbsp; package or directory (if more than one file)
 * <li><tt><b>s</b></tt>&nbsp;&nbsp; file separator character
 * </ul>
 *
 * @see         GenericHilite
 *
 * @version     Last modified on Fri Aug 03 04:31:28 2018 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Date;

import hlt.language.io.FileTools;

public class GenericHiliteCommand extends Command
{
  private static void helpAndExit ()
    {
      printHelp();
      System.exit(1);
    }

  static BufferedWriter index;      

  final static void w (String s) throws IOException
    {
      index.write(s);
    }

  final static void wl (String s) throws IOException
    {
      index.write(s+"\n");
    }

  public static void main (String args[])
    {
      String defaultExtension = ".java";

      defineOption("c",
                   "Hilite.Configuration",
                   "configuration file");
      defineOption("d",
                   ".",
                   "target directory");
      defineOption("o",
                   "output.html",
                   "HTML output file");
      defineOption("!",
                   "",
                   "overwrite existing files");
      defineOption("s",
                   File.separator,
                   "file separator character");
      try
        {
          defineOption("p",
                       new File("").getCanonicalPath(),
                       "package or directory");
        }
      catch (IOException e)
        {
          System.err.println("*** Can't access current directory");
        }

      setUsage("\nUsage: hl [options] sourcefile\n");

      if (parseCommandLine(args))
        {
          FileTools.setSeparator(getOption("s"));

          try
            {           
              if (!argumentIsPresent())
		helpAndExit();

              String[] arguments = getArguments();
              String targetDir
		= (optionIsPresent("d") || fileNameDir(arguments[0]).length()==0)
                ? getOption("d")+getOption("s")
                : fileNameDir(arguments[0])+getOption("s");

              boolean manyFiles = (arguments.length > 1);

              if (manyFiles)
                {
                  String title = "Source files in "+getOption("p");

                  index = new BufferedWriter(new FileWriter(targetDir+"index.html"));

                  wl("<HTML>");
                  wl("<HEAD>");
                  wl("<TITLE>");
                  wl(title);
                  wl("</TITLE>");
                  wl("</HEAD>");
                  wl("<BODY BGCOLOR=\"#DDDDDD\">");
                  wl("<CENTER>");
                  wl("<TABLE BGCOLOR=\"WHITE\" WIDTH=50% BORDER=5 CELLPADDING=20>");
                  wl("<TR><TD ALIGN=CENTER>");
                  wl("<SPAN STYLE=\"FONT-SIZE=\"X-LARGE\"><B>"+title+"</B></SPAN>");
                  wl("<P>");
                  wl("</TD></TR>");
                  wl("</TABLE>");
                  wl("</CENTER>");
                  wl("<P>");
                  wl("<OL>");
                }

              for (int i=0; i<arguments.length; i++)
                {
                  String arg = arguments[i];
                  String dir = fileNameDir(arg);
                  String pre = fileNamePrefix(arg);
                  String suf = fileNameSuffix(arg);
                  String nam = (dir.length()>0?(dir+getOption("s")):"")+pre;
                  String con = getOption("c");
                  String out = getOption("o");

                  if (suf.length() == 0)
                    {
                      if (!(new File(nam)).exists()) suf = defaultExtension;
                    }
                  else suf = "." + suf;

                  if (!optionIsPresent("o")) out = pre;

                  if (fileNameSuffix(out).length() == 0)
                    out += ".html";

                  new GenericHilite(nam+suf,con,out,targetDir,optionIsPresent("!"));

                  if (manyFiles)
                    {
                      w("<LI><A HREF=\""+out+"\">");
                      wl("<TT><B>"+pre+suf+"</B></TT></A>");
                    }
                }

              if (manyFiles)
                {
                  wl("</OL>");
                  wl("<P>");
                  wl("<HR>");
                  wl("<P ALIGN=\"RIGHT\">");
                  wl("<SPAN STYLE=\"COLOR:#F07070\"><EM>\n"+
                     "This file was generated on "+
                     (new Date())+"<BR>"+
                     "by the <SPAN STYLE=\"COLOR:BROWN\"><TT>"+
                     "hlt.language.tools.GenericHiliteCommand</TT></SPAN> Java tool"+
                     " written by <A HREF=\"http://hassan-ait-kaci.net\">"+
                     "Hassan A&iuml;t-Kaci</A></EM></SPAN>");
                  wl("<P>");
                  wl("<HR>");
                  wl("</BODY>");
                  wl("</HTML>");

                  index.close();

                  System.out.println("*** Wrote file index.html in "+targetDir);
                }
            }
          catch (IOException e)
            {
              System.err.println(e);
              System.err.println("\n*** Couldn't create HTML files");
            }
        }
    }
}
