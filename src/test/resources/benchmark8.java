// Check Influence Flow Graph
/*
For tests that check the influence flow graph, we find all the changes, apply them,
and compare to the programAfter to see how our tool suggests changes to all
the declarations that influence each other in one suggestion.

You can run the plugin (refer to the user test for instructions) and paste the content of programBefore.
*/
class bechmark8 {

}

class programBefore {
    interface C<X> {
        void foo(D<X> arg);
    }

    interface D<Y> {
        int getNumber();
    }

    class Client {
        void bar(C<String> cstr, D<String> dstr) {
            cstr.foo(dstr);
        }
    }
}

class programAfter {
    interface C<X> {
        void foo(D<?> arg);
    }

    interface D<Y> {
        int getNumber();
    }

    class Client {
        void bar(C<?> cstr, D<?> dstr) {
            cstr.foo(dstr);
        }
    }

}
