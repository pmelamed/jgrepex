public class ArgsSource {
    private final String[] args;
    private int ptr;

    public ArgsSource( String[] args ) {
        this.args = args;
    }

    public boolean hasNext() {
        return ptr < args.length;
    }

    public String getNext() {
        if ( hasNext() ) {
            return args[ptr++];
        }
        throw new IllegalStateException( "Trying to read parameter after the end of list" );
    }

    public String getOrMissing( String paramName ) {
        if ( hasNext() ) {
            return args[ptr++];
        }
        throw new IllegalArgumentException( "Missing parameter for " + paramName );
    }
}
