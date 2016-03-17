Building the annotation processor "symproc":
 cd src
 javac -cp /REPLACE_WITH_YOUR_JDK1.8.0_INSTALL_DIRECTORY/lib/tools.jar -d ../bin symprog/*.java

Building the examples using the "symproc" processor:
 javac -cp ../bin -processor symprog.SymProc -d ../bin examples/*.java

Running the examples:
 cd ../bin
 java examples.Example

Running the tests:
 cd ../src/tests
 jtreg -cpa:/REPLACE_WITH_YOUR_SYMPROC_DIRECTORY/bin -r:../../JTreport -w:../../JTwork .
