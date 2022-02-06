import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class GrepexMain {
    public static final int BUFFER_SIZE = 1024 * 1024;
    private boolean unique;
    private boolean fullLine;
    private boolean sorted;
    private Pattern regexp;

    public static void main( String[] args ) {
        try {
            final ArgsSource argsSource = new ArgsSource( args );
            String regexp = null;
            boolean unique = false;
            boolean fullLine = false;
            boolean caseInsensitive = false;
            boolean sorted = false;
            InputStream input = System.in;
            PrintStream output = System.out;
            while ( argsSource.hasNext() ) {
                String arg = argsSource.getNext();
                if ( arg.startsWith( "--" ) ) {
                    switch ( arg.substring( 2 ) ) {
                        case "unique":
                            unique = true;
                            break;
                        case "line":
                            fullLine = true;
                            break;
                        case "ci":
                            caseInsensitive = true;
                            break;
                        case "sort":
                            sorted = true;
                            break;
                        case "in":
                            final String inputFileName = argsSource.getOrMissing( "Input file name" );
                            System.err.println( "Reading input from file " + inputFileName );
                            input = Files.newInputStream( Paths.get(
                                    inputFileName
                            ) );
                            break;
                    }
                } else {
                    System.err.println( "Using regexp " + arg );
                    regexp = arg;
                }
            }
            if ( regexp == null ) {
                throw new IllegalArgumentException( "Missing regexp" );
            }
            int flags = 0;
            if ( caseInsensitive ) {
                flags += Pattern.CASE_INSENSITIVE;
            }
            final GrepexMain processor = new GrepexMain(
                    unique,
                    fullLine,
                    sorted,
                    Pattern.compile( regexp, flags )
            );
            processor.process( input );
        } catch ( IllegalArgumentException e ) {
            System.err.println( "Wrong usage " + e.getMessage() );
        } catch ( Exception e ) {
            System.err.println( "Error happened " + e.getMessage() );
        }
    }

    public GrepexMain( boolean unique, boolean fullLine, boolean sorted, Pattern regexp ) {
        this.unique = unique;
        this.fullLine = fullLine;
        this.sorted = sorted;
        this.regexp = regexp;
    }

    private void process( InputStream input ) {
        Stream<String> stream = new BufferedReader( new InputStreamReader( input ), BUFFER_SIZE )
                .lines()
                .flatMap( this::filterLine );
        if ( unique ) {
            stream = stream.distinct();
        }
        if ( sorted ) {
            stream = stream.sorted();
        }
        stream.forEach( System.out::println );
    }

    private Stream<String> filterLine( String line ) {
        Matcher matcher = regexp.matcher( line );
        if ( fullLine ) {
            return matcher.find() ? Stream.of( line ) : Stream.of();
        } else {
            return matcher.results().map( MatchResult::group );
        }
    }
}
