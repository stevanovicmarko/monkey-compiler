# Monkey Interpreter and Compiler
* Interpreter for Monkey language written in Kotlin.
* Inspired by the books: *Writing An Interpreter In Go*, *Crafting Interpreters* and *Writing A Compiler In Go*.

## Installation and usage
* Install JDK 11 and maven.
* Use `mvn install` to generate standalone jar.
* Go to `target` folder.
* Run some examples in the `examples` folder:
 ```
   java -jar monkey.interpreter-1.0-SNAPSHOT-jar-with-dependencies.jar ../examples/fibonacci.monkey 
 ```
 
 #### Fibbonaci example:
 
```
let fibonacci = fn(x) {
    if (x == 0) {
        return 0;
    } else {
        if (x == 1) {
            return 1;
        } else {
            fibonacci(x - 1) + fibonacci(x - 2);
        }
    }
};
fibonacci(15); // prints 610
```
