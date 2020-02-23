# 异常

### 遇见过哪些运行时异常？异常处理机制知道哪些？

Java 中 Throwable 是所有异常和错误的基类，Throwable 子类有 Exception 和 Error。

Exception 是所有异常的基类，应用可以对异常进行捕捉。  
Error 代表严重的错误发生，比如系统崩溃、虚拟机出错误，应用通常无法处理这些错误，也不应该捕捉。

#### 受检异常和非受检异常

通常，Java的异常\(包括Exception和Error\)分为受检异常（checked exceptions）和非受检异常（unchecked exceptions）。 受检异常（编译器要求必须处置的异常）：正确的程序在运行中，很容易出现的、情理可容的异常状况。受检异常虽然是异常状况，但在一定程度上它的发生是可以预计的，而且一旦发生这种异常状况，就必须采取某种方式进行处理。除了RuntimeException及其子类以外，其他的 Exception 类及其子类都属于可查异常。这种异常的特点是Java编译器会检查它，也就是说，当程序中可能出现这类异常，要么用try-catch语句捕获它，要么用throws子句声明抛出它，否则编译不会通过。

非受检异常\(编译器不要求强制处置的异常\):包括运行时异常（RuntimeException与其子类）和错误（Error）。

#### 运行时异常和非运行时异常

运行时异常都是`RuntimeException`类及其子类异常，如 `NullPointerException`、`IndexOutOfBoundsException`、`ClassCastException`、`IllegalArgumentException`、`IllegalStateException`等，这些异常是不检查异常，程序中可以选择捕获处理，也可以不处理。这些异常一般是由程序逻辑错误引起的，程序应该从逻辑角度尽可能避免这类异常的发生。

当出现RuntimeException的时候，我们可以不处理。当出现这样的异常时，总是由虚拟机接管。比如：我们从来没有人去处理过NullPointerException异常，它就是运行时异常，并且这种异常还是最常见的异常之一。 出现运行时异常后，如果没有捕获处理这个异常（即没有catch），系统会把异常一直往上层抛，一直到最上层，如果是多线程就由Thread.run\(\)抛出，如果是单线程就被main\(\)抛出。抛出之后，如果是线程，这个线程也就退出了。如果是主程序抛出的异常，那么这整个程序也就退出了。运行时异常是Exception的子类，也有一般异常的特点，是可以被catch块处理的。只不过往往我们不对他处理罢了。也就是说，你如果不对运行时异常进行处理，那么出现运行时异常之后，要么是线程中止，要么是主程序终止。 如果不想终止，则必须捕获所有的运行时异常，决不让这个处理线程退出。队列里面出现异常数据了，正常的处理应该是把异常数据舍弃，然后记录日志。不应该由于异常数据而影响下面对正常数据的处理。

非运行时异常是RuntimeException以外的异常，类型上都属于Exception类及其子类。如IOException、SQLException等以及用户自定义的Exception异常。对于这种异常，JAVA编译器强制要求我们必需对出现的这些异常进行catch并处理，否则程序就不能编译通过。

#### 异常处理

抛出异常，throw 或方法声明时使用throws声明可能的异常

处理异常 try catch finally

