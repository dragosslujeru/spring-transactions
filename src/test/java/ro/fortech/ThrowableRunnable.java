package ro.fortech;

@FunctionalInterface
public interface ThrowableRunnable<E extends Throwable> {

  void run() throws E;

  static <E extends Throwable> Runnable unchecked(ThrowableRunnable<E> f) {
    return () -> {
      try {
        f.run();
      } catch (RuntimeException e) {
        throw e;
      } catch (Throwable e) {
        throw new RuntimeException(e);
      }
    };
  }
}
