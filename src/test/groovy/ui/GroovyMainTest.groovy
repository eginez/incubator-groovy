package groovy.ui

class GroovyMainTest extends GroovyTestCase {
    private baos = new ByteArrayOutputStream()
    private ps = new PrintStream(baos)

    void testHelp() {
        String[] args = ['-h']
        GroovyMain.processArgs(args, ps)
        def out = baos.toString()
        assert out.contains('usage: groovy')
        ['-a', '-c', '-d', '-e', '-h', '-i', '-l', '-n', '-p', '-v'].each{
            assert out.contains(it)
        }
    }

    void testVersion() {
        String[] args = ['-v']
        GroovyMain.processArgs(args, ps)
        def out = baos.toString()
        assert out.contains('Groovy Version:')
        assert out.contains('JVM:')
    }

    void testNoArgs() {
        String[] args = []
        GroovyMain.processArgs(args, ps)
        def out = baos.toString()
        assert out.contains('error: neither -e or filename provided')
    }

    void testAttemptToRunJavaFile() {
        String[] args = ['abc.java']
        GroovyMain.processArgs(args, ps)
        def out = baos.toString()
        assert out.contains('error: error: cannot compile file with .java extension: abc.java')
    }

    /**
     * GROOVY-1512: Add support for begin() and end() methods when processing files by line with -a -ne
     */
    void testAandNEparametersWithBeginEndFunctions() {
        def originalErr = System.err
        System.setErr(ps)
        def tempFile = null
        try {
            tempFile = File.createTempFile("groovy-ui-GroovyMainTest-testAandNEparametersWithBeginEndFunctions", "txt")
            tempFile.text = "dummy text\n" * 10
            String[] args = ['-a', '-ne', 'def begin() { nb = 0 }; def end() { System.err.println nb }; nb++', tempFile.absolutePath]
            GroovyMain.main(args)
            def out = baos.toString()
            assert out.contains('10')
        } finally {
            System.setErr(originalErr)
            tempFile?.delete()
        }
    }

    /**
     * GROOVY-6561 : Correct handling of scripts from a URI.
     */
    void testURISource() {
        def tempFile = File.createTempFile("groovy-ui-GroovyMainTest-testURISource", ".groovy")
        // TODO: For GROOVY-6561 we need to actually test that we got a URI to the compiler.
        tempFile.text = """
@groovy.transform.ScriptURI def myURI

assert myURI instanceof java.net.URI

print myURI
"""
        tempFile.deleteOnExit()

        def oldOut = System.out
        System.setOut(ps)
        try {
            String[] args = [tempFile.toURI().toString()]
            GroovyMain.main(args)
            def out = baos.toString()
            assert out == tempFile.toURI()
        } finally {
            System.setOut(oldOut)
        }
    }
}
