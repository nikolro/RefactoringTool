import java.util.List;

class bechmark2 {
    class programBefore {
        class C2<X> {
            X foo (List<? super X> csx) {  }
            void bar (D2<? extends X> dsx) {  }

        }
        class D2<Y> {
            void baz(C2<Y> cx) {  }
        }
    }

    class programAfter {
        class C2<X> {
            X foo (List<? super X> csx) {  }
            void bar (D2<?> dsx) {  }
        }
        class D2<Y> {
            void baz(C2<? extends Y> cx) {  }
        }
}
