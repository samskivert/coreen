// a Java file on which we test our name resolving processor
public class Test
{
    public static class A {
        public int value;
    }

    public static class B {
        public void noop () {
        }
    }

    public static void main (String[] args) {
        int av = new A().value;
        B b = new B();
        b.noop();
    }
}
